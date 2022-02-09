package com.qianyi.casinocore.enums;

/**
 * 账变类型配置
 */
public enum AccountChangeEnum {

    WASH_CODE(0, "XM","洗码领取"),
    TOPUP_CODE(1, "CZ","充值"),
    ADD_CODE(2, "RGZJ","人工增加"),
//    WITHDRAW_CODE(3, "TX","提现"),
    WITHDRAWDEFEATED_CODE(4, "TXSB","提现失败"),
    SUB_CODE(5, "RGKJ","人工扣减"),
    WITHDRAW_APPLY(6, "QY","提现申请"),
    WM_IN(7, "WMIN","转入WM"),
    RECOVERY(8, "RECOVERY","一键回收"),
    SHARE_PROFIT(9, "SP","代理佣金领取"),
    PG_CQ9_IN(10, "PGCQ9IN","转入PC/CQ9"),
    PG_CQ9_OUT(11, "PGCQ9OUT","转出PC/CQ9"),
    ;

    private Integer type;

    private String code;

    private String name;

    AccountChangeEnum(Integer type, String code, String name) {
        this.type = type;
        this.code = code;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
