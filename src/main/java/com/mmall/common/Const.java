package com.mmall.common;

public class Const {

    public  static  final String CURRENT_USER="currentUser";

    public static  final String EMAIL="email";

    public static  final String USERNAME="username";

    //通过内部的一个接口类来对常量进行分组
    public interface Role{
        int ROLE_CUSTOMER=0;//普通用户
        int ROLE_ADMIN=1; //管理员
    }
}
