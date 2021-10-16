package com.qianyi.modulecommon;

/**
 * 正则表达式汇总
 */
public enum RegexEnum {

    NAME("^[\\u0391-\\uFFE5a-zA-Z·&\\\\s]{1,20}+$","姓名","长度限制1~20位,并且只能输入中英文"),
    PASSWORD("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,15}$","密码","长度限制6~15位,并且必须是数字和字母的组合"),
    PHONE("^[0-9 ()+-]{6,15}+$","手机号","长度限制6~15位,并且由+、-、()、数字组成"),
    ACCOUNT("^[\\w]{6,15}$","用户名","长度限制6~15位,并且由字母、数字、下划线组成"),
    ;

    private String regex;

    private String name;

    private String desc;

    RegexEnum(String regex, String name, String desc) {
        this.regex = regex;
        this.name = name;
        this.desc = desc;
    }

    public String getRegex() {
        return regex;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
