package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@Data
@ApiModel("首页报表")
public class HomePageReport extends BaseEntity{

    @ApiModelProperty(value = "汇款金额")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal chargeAmount;

    @ApiModelProperty(value = "汇款笔数")
    private Integer chargeNums;

    @ApiModelProperty(value = "提款金额")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal withdrawMoney;

    @ApiModelProperty(value = "提款笔数")
    private Integer withdrawNums;

    @ApiModelProperty(value = "下注金额")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal betAmount;

    @ApiModelProperty(value = "输赢金额")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal winLossAmount;

    @ApiModelProperty(value = "洗码金额")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal washCodeAmount;

    @ApiModelProperty(value = "结算人人代佣金")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal  shareAmount;

    @ApiModelProperty(value = "结算代理佣金")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal  proxyAmount;

    @ApiModelProperty(value = "发放红利")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal bonusAmount;

    @ApiModelProperty(value = "充提手续费")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal serviceCharge = BigDecimal.ZERO;

    @ApiModelProperty(value = "活跃玩家数")
    private Integer activeUsers;

    @ApiModelProperty(value = "新增玩家数")
    private Integer newUsers;

    @ApiModelProperty(value = "统计时间段")
    private String staticsTimes;
}
