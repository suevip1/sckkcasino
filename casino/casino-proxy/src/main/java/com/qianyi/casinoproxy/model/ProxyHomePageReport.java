package com.qianyi.casinoproxy.model;

import com.qianyi.casinocore.model.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Data
@ApiModel("代理首页报表")
@Table(indexes = {@Index(columnList = "proxyUserId"),@Index(columnList = "staticsTimes")})
public class ProxyHomePageReport extends BaseEntity {

    @ApiModelProperty(value = "代理id")
    private Long proxyUserId;

    @ApiModelProperty(value = "新增基层代理")
    private Integer newThirdProxys;

    @ApiModelProperty(value = "新增玩区域代理")
    private Integer newSecondProxys;

    @ApiModelProperty(value = "新增玩家数")
    private Integer newUsers;

    @ApiModelProperty(value = "活跃玩家数")
    private Integer activeUsers;

    @ApiModelProperty(value = "统计时间段")
    private String staticsTimes;

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

    @ApiModelProperty(value = "有效下注金额")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal validbetAmount;

    @ApiModelProperty(value = "输赢金额")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal winLossAmount;
}