package com.dzd.phonebook.util;

/**
 * 错误代码
 *
 * @author chenchao
 * @date 2016-6-27 10:08:00
 */
public class ErrorCodeTemplate {
    /* 成功 */
    public static final String CODE_SUCESS = "000000";
    /* 失败 */
    public static final String CODE_FAIL = "000001";
    /* 参数异常 */
    public static final String CODE_PARAMETER_ERROR = "000999";

    public static final String CODE_OTHER = "000002";

    public static final String MSG_USER_ISNULL = "账户名或密码错误!";
    public static final String MSG_STATE_ERROR = "登录异常,请稍候再试!";
    public static final String MSG_SYSUSER_EMPTY = "用户不存在!";
    public static final String MSG_SYSUSER_PWD_IS_MISS = "密码错误!";
    public static final String MSG_VERTIFYCODE_EMPTY = "请输入验证码!";
    public static final String MSG_REGISTER_MSG = "用户已经存在!";

    public static final String MSG_SUCCESS_MSG = "登录成功!";
    public static final String MSG_SYSTEM_ERROR_MSG = "服务器异常!";

    public static final String MSG_SMS_PHONE_IS_EMPTY = "请输入手机号码!";
    public static final String MSG_SMS_VERIFY_IS_EMPTY = "请输入验证码!";
    public static final String MSG_VERIFYCODE_ERROR = "验证码错误或失效!";

    public static final String MSG_VERTIFYCODE_INPUT = "获取次数频繁（一天三次）,请稍候重试!";

    /**
     * 咨询与反馈短信验证码返回信息
     */
    public static final String MSG_VERTIFYCODE_INPUT_ONE = "您的号码今日已提交咨询，客服将会安排处理。\n" +
            "感谢您的支持！\n";

    /**
     * 找回密码短信验证码返回信息
     */
    public static final String MSG_VERTIFYCODE_INPUT_TWO = "获取次数频繁（一天二次）,请稍候重试!";

    public static final String MSG_SYSUSER_EMPTY_1 = "账户名无效！";

    public static final String MSG_SUCCESS = "操作成功！";

    public static final String MSG_FAIL = "操作失败！";

    public static final String MSG_MANUAL_ADDITION = "手动添加短信数量！";

    public static final String MSG_EMAIL_PHONE_ERROR = "账号和手机号码不匹配！";

    public static final String PHONE_NO_HAVE = "手机号码不存在，请重新输入!";

    /**
     * 找回密码时手机验证码验证 次数“一天2次” 类型
     */
    public static final Integer VERTIFYCODE_TYPE = 2;

    public static final String USER_STATE = "关闭/开通账户";
    public static final String USER_ADD = "新建账户";
    public static final String USER_UPATE = "账户设置";
    public static final String USER_REGIST = "注册账户";
    public static final String CHANNEL_ADD = "新增通道";
    public static final String CHANNEL_REGIST = "通道设置";
    public static final String CHANNEL_GROUP_ADD = "新增通道组";
    public static final String CHANNEL_GROUP_REGIST = "通道组设置";
    public static final String SHIELDWORD_ADD = "新增敏感词";
    public static final String SHIELDWORD_REGIST = "编辑敏感词";
    public static final String ROLE_ADD = "新增角色";
    public static final String ROLE_LEVEL_ADMINISTRATOR = "超级管理员权限编辑";
    public static final String ROLE_CUSTOMER_SERVICE = "客服权限编辑";
    public static final String ROLEIRST_LEVEL_ADMINISTRATOR = "一级管理员权限编辑";
    public static final String ROLE_SALES_MANAGER = "销售经理权限编辑";
    public static final String ROLE_USER_ADMINISTRATOR = "用户管理员权限编辑";
    public static final String ROLE_SALESMAN = "业务员权限编辑";
    public static final String USER_LEVEL_AUSER = "用户权限编辑";
    public static final String HOME_PAGE_ADD = "首页新增";
    public static final String HOME_PAGE_REGIST = "首页编辑";
    public static final String MENU_ADD = "新增菜单";
    public static final String MENU_REGIST = "编辑菜单";
    public static final String USER_SQ_ADD = "申请账户";
    public static final String USER_SQ_UPDATE = "修改申请账户";

}

