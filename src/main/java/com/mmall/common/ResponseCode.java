package com.mmall.common;

/**
 * 通过一个枚举来对常量进行分组
 */
public enum  ResponseCode {

    SUCCESS (0,"SUCCESS"),
    ERROR (1,"ERROR"),
    NEED_LOGIN (10,"NEED_LOGIN"),
    ILLEGALL_ARGUMENT(2,"ILLEGALL_ARGUMENT");

    private final int code;
    private final String desc;

    ResponseCode(int code,String desc){
        this.code=code;
        this.desc=desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
