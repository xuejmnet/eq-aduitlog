package com.gksyb.demo;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gksyb.demo.database.Description;
import com.gksyb.demo.proxy.BcCodeProxy;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.UpperSnakeCaseStrategy.class)
@Description("字典表")
@EntityProxy
@Table("BC_CODE")
public class BcCode implements ProxyEntityAvailable<BcCode, BcCodeProxy> {

    /**
     * 主键
     */
    @Column(primaryKey = true)
    @Description("主键")
    private String sid;

    /**
     * 类型
     */
    @Description("类型")
    private String codeType;

    /**
     * 编码
     */
    @Description("编码")
    private String codeEn;

    /**
     * 名称
     */
    @Description("名称")
    private String codeCn;

    /**
     * 顺序
     */
    @Description("顺序")
    private Integer codeSeq;

    /**
     * 备注
     */
    @Description("备注")
    private String remark;

}
