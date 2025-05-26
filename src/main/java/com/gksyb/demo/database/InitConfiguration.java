package com.gksyb.demo.database;

import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.basic.api.database.CodeFirstCommand;
import com.easy.query.core.basic.api.database.DatabaseCodeFirst;
import com.gksyb.demo.BcCode;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * create time 2025/5/22 19:50
 * 文件说明
 *
 * @author xuejiaming
 */
@Configuration
public class InitConfiguration {

    public InitConfiguration(EasyEntityQuery easyEntityQuery){
        DatabaseCodeFirst databaseCodeFirst = easyEntityQuery.getDatabaseCodeFirst();
        databaseCodeFirst.createDatabaseIfNotExists();
        CodeFirstCommand codeFirstCommand = databaseCodeFirst.syncTableCommand(Arrays.asList(BcCode.class));
        codeFirstCommand.executeWithTransaction(s->s.commit());
    }
}
