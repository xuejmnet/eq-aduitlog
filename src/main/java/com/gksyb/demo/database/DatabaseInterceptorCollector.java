package com.gksyb.demo.database;

import java.util.List;

/**
 * create time 2025/5/22 19:54
 * 文件说明
 *
 * @author xuejiaming
 */
public class DatabaseInterceptorCollector {
    private final List<DatabaseInterceptor> databaseInterceptors;

    public DatabaseInterceptorCollector(List<DatabaseInterceptor> databaseInterceptors){
        this.databaseInterceptors = databaseInterceptors;
    }

    public List<DatabaseInterceptor> getDatabaseInterceptors() {
        return databaseInterceptors;
    }
}
