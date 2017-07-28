package com.dzd.phonebook.util.send.api;

import com.dzd.phonebook.entity.VertifyCode;

/**
 * Created by CHENCHAO on 2017/6/15.
 */
public class SmsContentBean {


    /**
     * 发送短信 - 重置密码
     *
     * @param code
     * @return
     */
    public static String getSendPwdSmsContent(VertifyCode code) {
        String content ="【千讯数据】账户密码" + code.getNewPwd()
                + "（全网信通随机重置密码），请登录账户及时修改密码！";
        return content;
    }

    /**
     * 发送短信 - 咨询与反馈
     *
     * @param code
     * @return
     */
    public static String getSendVerifyCodeSmsContent(VertifyCode code) {
        String content = "【千讯数据】验证码" + code.getVertifycode() + "（全网信通），30分钟内输入有效，如非本人操作请忽略。";
        return content;
    }

    /**
     * 发送短信 - 找回密码
     *
     * @param code
     * @return
     */
    public static String getSendVerifyCodeSmsContentByResetPwd(VertifyCode code) {
        String content = "【千讯数据】验证码"+code.getVertifycode()+"（全网信通重置密码验证），30分钟内输入有效。";
        return content;
    }

    /**
     * 发送短信 - 实时群发的验证码短
     *
     * @param code
     * @return
     */
    public static String getSendVerifyCodeSms(VertifyCode code) {
        String content = "【千讯数据】验证码"+code.getVertifycode()+"（全网信通实时任务验证），30分钟内输入有效。";
        return content;
    }
}
