package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.math.BigDecimal;

/**
 * @author jordan
 */
@Entity
@Data
@ApiModel("账变中心")
public class AccountChange extends BaseEntity {

	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@ApiModelProperty(value = "订单号")
	private String orderNo;

	@ApiModelProperty(value = "账变类型:0.洗码领取,7.转入wm,8.一键回收,9.代理佣金领取")
	private Integer type;

	@ApiModelProperty(value = "额度变化")
	private BigDecimal amount;

	@ApiModelProperty(value = "额度变化前")
	private BigDecimal amountBefore;

	@ApiModelProperty(value = "额度变化后")
	private BigDecimal amountAfter;

	@ApiModelProperty("总代ID")
	private Long firstProxy;

	@ApiModelProperty("区域代理ID")
	private Long secondProxy;

	@ApiModelProperty("基层代理ID")
	private Long thirdProxy;
}
