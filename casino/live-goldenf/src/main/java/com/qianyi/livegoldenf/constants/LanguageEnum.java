package com.qianyi.livegoldenf.constants;

import lombok.Data;
import org.springframework.util.ObjectUtils;

import java.util.Locale;

public enum LanguageEnum {

    en_US("en-US", "English (英文)", "en_US"),
    zh_CN("zh-CN", "Chinese Simplified(简中)", "zh_CN"),
    ID("ID", "Indonesian (印尼语)"),
    TH("TH", "Thai (泰语)"),
    VI("VI", "Vietnamese (越南文)"),
    JA("JA", "Japanese (日文)"),
    KO("KO", "Korean (韩文)"),
    ES("ES", "Spanish（西班牙文"),
    MY("MY", "Malaysia(馬來西亞文)"),
    TR("TR", "Turkish (土耳其语)"),
    ;
    private String code;
    private String name;
    private String systemCode;

    LanguageEnum(String code, String name, String systemCode) {
        this.code = code;
        this.name = name;
        this.systemCode = systemCode;
    }

    LanguageEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public static String getLanguageCode(String lanuage) {
        for (LanguageEnum languageEnum : LanguageEnum.values()) {
            if (!ObjectUtils.isEmpty(lanuage) && lanuage.equals(languageEnum.getSystemCode())) {
                return languageEnum.getCode();
            }
        }
        return en_US.code;
    }
}