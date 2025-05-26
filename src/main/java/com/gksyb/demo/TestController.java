package com.gksyb.demo;

import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.basic.extension.track.TrackManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class TestController {

    @Resource
    private EasyEntityQuery easyEntityQuery;

    @GetMapping("test")
    public String test() {
        String key ="888999";
        BcCode code = new BcCode();
        code.setCodeCn("测试");
        code.setCodeEn("test");
        code.setCodeType("test");
        code.setCodeSeq(1);
        code.setRemark("测试");
        code.setSid(key);
        easyEntityQuery.insertable(code).executeRows();

        TrackManager trackManager = easyEntityQuery.getRuntimeContext().getTrackManager();
        try {
            trackManager.begin();
            code = easyEntityQuery.queryable(BcCode.class)
                    .whereById(key).asTracking().singleNotNull();
            code.setCodeCn("测试2222");;
            easyEntityQuery.updatable(code).executeRows();
        } finally {
            trackManager.release();
        }
        easyEntityQuery.deletable(code).allowDeleteStatement(true)
                .executeRows();
        return  "success";
    }
}
