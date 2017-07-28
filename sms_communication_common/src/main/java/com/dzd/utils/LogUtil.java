package com.dzd.utils;

/**
 * Created by IDEA
 * Author: WHL
 * Date: 2017/7/28
 * Time: 17:21
 */
public class LogUtil {
    private final static LogUtil I = new LogUtil();
    public static LogUtil getLogger(Class cls){
        return I;
    }
    public void info(String message){

    }

    public void info(String message,String str2,String str3){

    }


    public void error(String message){

    }
    public void error(String message, Object obj){

    }

    public void debug(String message){

    }

    public void debug(String message,Object obj){

    }

    public void debug(String message,Object obj,Object obj2){

    }


    public void warn(String message,Object obj){

    }

    public void warn(String message,Object obj,Object obj2){

    }

    public void warn(String message,Object obj,Object obj2,Object obj3){

    }
}
