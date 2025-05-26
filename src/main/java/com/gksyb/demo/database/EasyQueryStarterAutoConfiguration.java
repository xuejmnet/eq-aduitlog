package com.gksyb.demo.database;

import com.easy.query.core.basic.jdbc.executor.EntityExpressionExecutor;
import com.easy.query.core.bootstrapper.StarterConfigurer;
import com.easy.query.core.inject.ServiceCollection;
import com.easy.query.sql.starter.config.EasyQueryProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(EasyQueryProperties.class)
public class EasyQueryStarterAutoConfiguration {


    @Bean
    @Primary
    public StarterConfigurer starterConfigurer(Map<String, DatabaseInterceptor> databaseInterceptorMap) {
        List<DatabaseInterceptor> databaseInterceptors = databaseInterceptorMap.values().stream().sorted(Comparator.comparingInt(DatabaseInterceptor::sort)).collect(Collectors.toList());
        return new EasyQueryStarterConfigurer(new DatabaseInterceptorCollector(databaseInterceptors));
    }

    static class EasyQueryStarterConfigurer implements StarterConfigurer {
        private final DatabaseInterceptorCollector databaseInterceptorCollector;

        public EasyQueryStarterConfigurer(DatabaseInterceptorCollector databaseInterceptorCollector) {
            this.databaseInterceptorCollector = databaseInterceptorCollector;
        }

        @Override
        public void configure(ServiceCollection services) {
            services.addService(databaseInterceptorCollector);
            services.addService(EntityExpressionExecutor.class, MyEntityExpressionExecutor.class);
        }

    }
}
