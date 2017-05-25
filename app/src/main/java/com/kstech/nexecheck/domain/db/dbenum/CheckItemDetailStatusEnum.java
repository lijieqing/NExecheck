package com.kstech.nexecheck.domain.db.dbenum;

/**
 * 检验状态：0：通讯超时1：测试超时2：人工中止3：通道断路或对地短路或对电短路或信号超限 4：合格5：未合格
 */
public enum CheckItemDetailStatusEnum {
    CONNECT_TIMEOUT("0","通讯超时"),
    TEST_TIMEOUT("1","测试超时"),
    MANUAL_END("2","人工中止"),
    OTHER("3","测量失败"),
    PASS("4","合格"),
    UN_PASS("5","未合格");

    CheckItemDetailStatusEnum(String code,String name){
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
        for (CheckItemDetailStatusEnum c : CheckItemDetailStatusEnum.values()) {
            if (c.getCode() .equals(code) ) {
                return c.name;
            }
        }
        return null;
    }
    // 普通方法
    public static String getCode(String name) {
        for (CheckItemDetailStatusEnum c : CheckItemDetailStatusEnum.values()) {
            if (c.getName() .equals(name) ) {
                return c.code;
            }
        }
        return null;
    }
}
