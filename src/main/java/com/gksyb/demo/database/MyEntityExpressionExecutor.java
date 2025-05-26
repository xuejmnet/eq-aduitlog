package com.gksyb.demo.database;

import com.easy.query.core.basic.jdbc.executor.DefaultEntityExpressionExecutor;
import com.easy.query.core.basic.jdbc.executor.ExecutorContext;
import com.easy.query.core.expression.executor.query.ExecutionContextFactory;
import com.easy.query.core.expression.sql.builder.*;
import com.easy.query.core.expression.sql.expression.EntityPredicateSQLExpression;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MyEntityExpressionExecutor extends DefaultEntityExpressionExecutor {


    private final List<DatabaseInterceptor> interceptors;

    public MyEntityExpressionExecutor(DatabaseInterceptorCollector databaseInterceptorCollector, ExecutionContextFactory executionContextFactory) {
        super(executionContextFactory);
        this.interceptors = databaseInterceptorCollector.getDatabaseInterceptors();
    }

    @Override
    public <T> long insert(ExecutorContext executorContext, List<T> entities, EntityInsertExpressionBuilder expressionBuilder, boolean fillAutoIncrement) {
        long result = super.insert(executorContext, entities, expressionBuilder, fillAutoIncrement);
        Class<?> clazz = expressionBuilder.getQueryClass();
        for (DatabaseInterceptor interceptor : interceptors) {
            if (interceptor.apply(clazz)) {
                interceptor.insert(entities, expressionBuilder, executorContext);
            }
        }
        return result;
    }

    @Override
    public <T> long executeRows(ExecutorContext executorContext, EntityExpressionBuilder expressionBuilder, List<T> entities) {
        long result = super.executeRows(executorContext, expressionBuilder, entities);
        Class<?> clazz = expressionBuilder.getQueryClass();
        for (DatabaseInterceptor interceptor : interceptors) {
            if (!interceptor.apply(clazz)) {
                continue;
            }
            if (expressionBuilder instanceof EntityUpdateExpressionBuilder) {
                interceptor.update(entities, (EntityUpdateExpressionBuilder) expressionBuilder, executorContext);
            } else if (expressionBuilder instanceof EntityDeleteExpressionBuilder) {
                interceptor.delete(entities, (EntityDeleteExpressionBuilder) expressionBuilder, executorContext);
            }

        }
        return result;
    }

    @Override
    public long executeRows0(ExecutorContext executorContext, EntityPredicateExpressionBuilder entityPredicateExpressionBuilder, EntityPredicateSQLExpression entityPredicateSQLExpression) {
        long result = super.executeRows0(executorContext, entityPredicateExpressionBuilder, entityPredicateSQLExpression);
        Class<?> clazz = entityPredicateExpressionBuilder.getQueryClass();
        for (DatabaseInterceptor interceptor : interceptors) {
            if (interceptor.apply(clazz)) {
                interceptor.executeRows(entityPredicateSQLExpression, executorContext);
            }
        }
        return result;
    }

}
