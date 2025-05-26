package com.gksyb.demo.database;

import com.easy.query.core.basic.jdbc.executor.ExecutorContext;
import com.easy.query.core.expression.executor.parser.PredicatePrepareParseResult;
import com.easy.query.core.expression.sql.builder.EntityDeleteExpressionBuilder;
import com.easy.query.core.expression.sql.builder.EntityInsertExpressionBuilder;
import com.easy.query.core.expression.sql.builder.EntityUpdateExpressionBuilder;
import com.easy.query.core.expression.sql.expression.EntityPredicateSQLExpression;

import java.util.List;

public interface DatabaseInterceptor {

    /**
     * 拦截器先后顺序
     * @return
     */
    int sort();
    /**
     * 是否接受当前拦截器
     */
    boolean apply(Class<?> entityClass);

    /**
     * 插入成功后拦截
     */
    <T> void insert(List<T> entities, EntityInsertExpressionBuilder expressionBuilder, ExecutorContext executorContext);

    /**
     * 更新成功后拦截
     */
    <T> void update(List<T> entities, EntityUpdateExpressionBuilder expressionBuilder, ExecutorContext executorContext);

    /**
     * 删除成功后拦截
     */
    <T> void delete(List<T> entities, EntityDeleteExpressionBuilder expressionBuilder, ExecutorContext executorContext);

    /**
     * 表达式更新或删除执行成功后拦截
     */
    void executeRows(EntityPredicateSQLExpression entityPredicateSQLExpression, ExecutorContext executorContext);

}
