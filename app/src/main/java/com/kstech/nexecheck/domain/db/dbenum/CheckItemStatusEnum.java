package com.kstech.nexecheck.domain.db.dbenum;

/**
 *
 *
 **/
public enum CheckItemStatusEnum {
    UN_CHECK("0","未检"),
    UN_FINISH("1","未完成"),
    PASS("2","合格"),
    UN_PASS("3","未合格");

    CheckItemStatusEnum(String code,String name){
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
        for (CheckItemStatusEnum c : CheckItemStatusEnum.values()) {
            if (c.getCode() .equals(code) ) {
                return c.name;
            }
        }
        return null;
    }
    // 普通方法
    public static String getCode(String name) {
        for (CheckItemStatusEnum c : CheckItemStatusEnum.values()) {
            if (c.getName() .equals(name) ) {
                return c.code;
            }
        }
        return null;
    }
}
