package com.qianyi.casinocore.vo;

import com.qianyi.casinocore.util.CommonConst;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

@Data
public class CompanyProxyReportVo implements Serializable {
    private static final long serialVersionUID = -6875647823650305179L;
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "上级代理id")
    private Long parentId;
    @ApiModelProperty(value = "账号")
    private String userName;
    @ApiModelProperty(value = "昵称")
    private String nickName;
    @ApiModelProperty(value = "代理角色 1：总代理 2：区域代理 3：基层代理")
    private Integer proxyRole;
    @ApiModelProperty(value = "上级代理账号")
    private String superiorProxyAccount;
    @ApiModelProperty(value = "新增下级代理")
    private Integer groupNewProxyUsers = CommonConst.NUMBER_0;
    @ApiModelProperty(value = "团队新增玩家数")
    private Integer groupNewUsers = CommonConst.NUMBER_0;
    @ApiModelProperty(value = "活跃玩家数")
    private Integer activeUsers = CommonConst.NUMBER_0;
    @ApiModelProperty(value = "团队充值")
    private BigDecimal chargeAmount = BigDecimal.ZERO;
    @ApiModelProperty(value = "团队提款")
    private BigDecimal withdrawMoney = BigDecimal.ZERO;
    @ApiModelProperty("团队业绩(流水)")
    private BigDecimal groupPerformance = BigDecimal.ZERO;
    @ApiModelProperty("统计日期")
    private String staticsTimes;

    private Set<Long> userIdSet;
}
