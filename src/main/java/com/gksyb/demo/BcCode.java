package com.gksyb.demo;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gksyb.demo.proxy.BcCodeProxy;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.UpperSnakeCaseStrategy.class)
@EntityProxy
@Table(value = "BC_CODE",comment = "字典表")
public class BcCode implements ProxyEntityAvailable<BcCode, BcCodeProxy> {

    /**
     * 主键
     */
    @Column(primaryKey = true,comment = "主键")
    private String sid;

    /**
     * 类型
     */
    @Column(comment = "类型")
    private String codeType;

    /**
     * 编码
     */
    @Column(comment = "编码")
    private String codeEn;

    /**
     * 名称
     */
    @Column(comment = "名称")
    private String codeCn;

    /**
     * 顺序
     */
    @Column(comment = "顺序")
    private Integer codeSeq;

    /**
     * 备注
     */
    @Column(comment = "备注")
    private String remark;

}
