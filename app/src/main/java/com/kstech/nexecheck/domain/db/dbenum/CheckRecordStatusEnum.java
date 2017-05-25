package com.kstech.nexecheck.domain.db.dbenum;

/**
 *
 *
 **/
public enum CheckRecordStatusEnum {
    UN_FINISH("0","未完成"),
    PASS("1","合格"),
    UN_PASS("2","未合格"),
    FORCE_PASS("3","强制合格");

    CheckRecordStatusEnum(String code,String name){
        this.name = name;
        this.code = code;
    }

    private String code;
    private String name;

    public String getCode(){
        return this.code;
    }

    public String getName(){
        return this.name;
    }
    // 普通方法
    public static String getName(String code) {
        for (CheckRecordStatusEnum c : CheckRecordStatusEnum.values()) {
            if (c.getCode() .equals(code) ) {
                return c.name;
            }
        }
        return null;
    }
    // 普通方法
    public static String getCode(String name) {
        for (CheckRecordStatusEnum c : CheckRecordStatusEnum.values()) {
            if (c.getName() .equals(name) ) {
                return c.code;
            }
        }
        return null;
    }
}
