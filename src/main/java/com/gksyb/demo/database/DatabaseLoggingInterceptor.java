package com.gksyb.demo.database;

import com.easy.query.core.basic.extension.track.EntityState;
import com.easy.query.core.basic.extension.track.EntityTrackProperty;
import com.easy.query.core.basic.extension.track.TrackContext;
import com.easy.query.core.basic.jdbc.executor.ExecutorContext;
import com.easy.query.core.basic.jdbc.parameter.DefaultToSQLContext;
import com.easy.query.core.basic.jdbc.parameter.SQLParameter;
import com.easy.query.core.basic.jdbc.parameter.ToSQLContext;
import com.easy.query.core.context.QueryRuntimeContext;
import com.easy.query.core.expression.executor.parser.PredicatePrepareParseResult;
import com.easy.query.core.expression.lambda.SQLActionExpression;
import com.easy.query.core.expression.sql.TableContext;
import com.easy.query.core.expression.sql.builder.EntityDeleteExpressionBuilder;
import com.easy.query.core.expression.sql.builder.EntityExpressionBuilder;
import com.easy.query.core.expression.sql.builder.EntityInsertExpressionBuilder;
import com.easy.query.core.expression.sql.builder.EntityUpdateExpressionBuilder;
import com.easy.query.core.expression.sql.expression.EntityPredicateSQLExpression;
import com.easy.query.core.expression.sql.expression.EntityUpdateSQLExpression;
import com.easy.query.core.metadata.ColumnMetadata;
import com.easy.query.core.metadata.EntityMetadata;
import com.easy.query.core.metadata.EntityMetadataManager;
import com.easy.query.core.util.EasyBeanUtil;
import com.easy.query.core.util.EasyStringUtil;
import com.easy.query.core.util.EasyTrackUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DatabaseLoggingInterceptor implements DatabaseInterceptor {

    @Override
    public int sort() {
        return 0;
    }

    @Override
    public boolean apply(Class<?> clazz) {
        //自行控制比如class必须满足某个接口或者满足某个注解
        return true;
    }

    @Override
    public <T> void insert(List<T> entities, EntityInsertExpressionBuilder expressionBuilder, ExecutorContext executorContext) {
        logTimeListen(()->{
            addLog(entities, expressionBuilder, Oper.Add);
        },"对象插入拦截表达式");
    }

    @Override
    public <T> void update(List<T> entities, EntityUpdateExpressionBuilder expressionBuilder, ExecutorContext executorContext) {
        logTimeListen(()->{
            addUpdateLog(entities, expressionBuilder);
        },"对象更新拦截表达式");
    }

    @Override
    public <T> void delete(List<T> entities, EntityDeleteExpressionBuilder expressionBuilder, ExecutorContext executorContext) {
        logTimeListen(()->{
            addLog(entities, expressionBuilder, Oper.Delete);
        },"对象删除拦截表达式");
    }

    @Override
    public void executeRows(EntityPredicateSQLExpression entityPredicateSQLExpression, ExecutorContext executorContext) {
        Oper oper = Oper.Modify;
        switch (executorContext.getExecuteMethod()) {
            case DELETE:
                oper = Oper.Delete;
                break;
            case INSERT:
                oper = Oper.Add;
                break;
        }
        String type = getTableName(entityPredicateSQLExpression.getTable(0).getEntityMetadata()) + " " + oper;
        logTimeListen(()->{
            log(type, getKey(entityPredicateSQLExpression), getLogDetail(entityPredicateSQLExpression));
        },"修改删除拦截表达式");
    }

    /**
     * 可选监听警告
     * @param actionExpression
     * @param key
     */
    private void logTimeListen(SQLActionExpression actionExpression,String key){
        long start = System.currentTimeMillis();
        actionExpression.apply();
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        if(elapsed>1000){
            log.error("拦截器方法{},执行时间过长,耗时:{}ms",key,elapsed);
        }
    }

    private <T> void addUpdateLog(List<T> entities, EntityExpressionBuilder expressionBuilder) {
        QueryRuntimeContext runtimeContext = expressionBuilder.getRuntimeContext();
        EntityMetadata entityMetadata = runtimeContext.getEntityMetadataManager().getEntityMetadata(expressionBuilder.getQueryClass());
        String type = getTableName(entityMetadata) + " " + Oper.Modify;
        for (T entity : entities) {
            String detail = getLogDetail(entity, entityMetadata, runtimeContext);
            log(type, getKey(entity, entityMetadata), detail);
        }
    }

    private <T> void addLog(List<T> entities, EntityExpressionBuilder expressionBuilder, Oper oper) {
        EntityMetadata entityMetadata = expressionBuilder.getRuntimeContext().getEntityMetadataManager().getEntityMetadata(expressionBuilder.getQueryClass());
        String type = getTableName(entityMetadata) + " " + oper.name();
        for (T entity : entities) {
            String detail = getLogDetail(entity, entityMetadata);
            log(type, getKey(entity, entityMetadata), detail);
        }
    }

    private static String getLogDetail(Object entity, EntityMetadata entityMetadata) {
        Collection<ColumnMetadata> columns = entityMetadata.getColumns();
        StringBuilder sb = new StringBuilder();
        for (ColumnMetadata column : columns) {
            Object value = EasyBeanUtil.getCurrentPropertyValue(entity, column);
            if (value == null) {
                continue;
            }

            String comment = column.getComment();
            String display = getOrDefault(comment,column.getName());
            sb.append(display).append(":").append(value).append(LINE);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
    private static String getOrDefault(String comment,String def){
        if(EasyStringUtil.isNotBlank(comment)){
            return comment;
        }
        return def;
    }

    private static String getLogDetail(Object entity, EntityMetadata entityMetadata, QueryRuntimeContext runtimeContext) {
        EntityMetadataManager entityMetadataManager = runtimeContext.getEntityMetadataManager();
        TrackContext currentTrackContext = runtimeContext.getTrackManager().getCurrentTrackContext();
        if(currentTrackContext==null){
            return getLogDetail(entity, entityMetadata);
        }
        EntityState entityState = currentTrackContext.getTrackEntityState(entity);
        if (entityState == null) {
            return getLogDetail(entity, entityMetadata);
        }
        StringBuilder sb = new StringBuilder();
        EntityTrackProperty entityTrackProperty = EasyTrackUtil.getTrackDiffProperty(entityMetadataManager, entityState);
        entityTrackProperty.getDiffProperties().forEach((name, state) -> {
            String comment = state.getColumnMetadata().getComment();
            String display = getOrDefault(comment,name);
            sb.append(display).append(":").append(state.getOriginal()).append("→").append(state.getCurrent()).append(LINE);
        });
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private void log(String type, String title, String detail) {
        log.info("{},{}:{}", type, title, detail);
    }

    private static String getTableName(EntityMetadata entityMetadata) {
        String comment = entityMetadata.getComment();
        return EasyStringUtil.isBlank(comment) ? entityMetadata.getTableName() : comment;
    }

    private static String getKey(Object entity, EntityMetadata entityMetadata) {
        Object key = EasyQueryUtils.getKey(entity, entityMetadata);
        if (key instanceof String) {
            return (String) key;
        } else {
            return Objects.isNull(key) ? "null" : key.toString();
        }
    }

    private static String getKey(EntityPredicateSQLExpression expression) {
        if (expression.getWhere().isEmpty()) {
            return "null";
        }
        ToSQLContext whereContext = getToSQLContext(expression);
        String where = expression.getWhere().toSQL(whereContext);
        if (whereContext.getParameters().isEmpty()) {
            String last = where.split("=")[1].trim();
            return last.isEmpty() ? where.substring(Math.max(0, where.length() - 30)) : last;
        }
        List<Object> values = whereContext.getParameters().stream().map(SQLParameter::getValue).collect(Collectors.toList());
        if (values.size() == 1) {
            Object key = values.get(0);
            if (key instanceof String) {
                return (String) key;
            } else {
                return Objects.isNull(key) ? "null" : key.toString();
            }
        }
        return values.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    private static String getLogDetail(EntityPredicateSQLExpression expression) {
        if (expression instanceof EntityUpdateSQLExpression) {
            EntityUpdateSQLExpression updateExpression = (EntityUpdateSQLExpression) expression;
            if (updateExpression.getSetColumns().isNotEmpty()) {
                return getUpdateExpressionDetail(updateExpression);
            }
        }
        return getExpressionDetail(expression);
    }

    private static String getUpdateExpressionDetail(EntityUpdateSQLExpression expression) {
        if (expression.getSetColumns().isEmpty()) {
            return getExpressionDetail(expression);
        }
        EntityMetadata entityMetadata = expression.getTable(0).getEntityMetadata();
        ToSQLContext sqlContext = getToSQLContext(expression);
        String sql = expression.getSetColumns().toSQL(sqlContext);
        if (sqlContext.getParameters().isEmpty() || !StringUtils.hasText(sqlContext.getParameters().get(0).getPropertyNameOrNull())) {
            Object[] parameters = sqlContext.getParameters().stream().map(SQLParameter::getValue).toArray();
            return formatWith(sql, parameters);
        }
        StringBuilder sb = new StringBuilder();
        sqlContext.getParameters().forEach(parameter -> {
            String name = parameter.getPropertyNameOrNull();
            ColumnMetadata columnOrNull = entityMetadata.getColumnOrNull(name);
            String display = name;
            if(columnOrNull!=null){
                String comment = columnOrNull.getComment();
                display = getOrDefault(comment, name);
            }
            sb.append(display).append(":").append(parameter.getValue()).append(LINE);
        });
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private static String getExpressionDetail(EntityPredicateSQLExpression expression) {
        ToSQLContext sqlContext = getToSQLContext(expression);
        String sql = expression.toSQL(sqlContext);
        Object[] parameters = sqlContext.getParameters().stream().map(SQLParameter::getValue).toArray();
        return formatWith(sql, parameters);
    }

    private static ToSQLContext getToSQLContext(EntityPredicateSQLExpression expression) {
        TableContext tableContext = expression.getExpressionMetadata().getTableContext();
        return DefaultToSQLContext.defaultToSQLContext(tableContext);
    }

    private static String formatWith(String strPattern, Object... parameters) {
        if (parameters.length == 0) {
            return strPattern;
        }
        String formattedSql = strPattern;
        for (Object parameter : parameters) {
            formattedSql = formattedSql.replaceFirst("\\?", Matcher.quoteReplacement(String.valueOf(parameter)));
        }
        return formattedSql;

    }

    private static final String LINE = System.lineSeparator();

    enum Oper {
        Add,
        Modify,
        Delete
    }

}
