package com.dzd.phonebook.controller;

import com.dzd.base.annotation.Auth;
import com.dzd.base.util.Constant;
import com.dzd.base.util.MethodUtil;
import com.dzd.base.util.SessionUtils;
import com.dzd.phonebook.entity.SysLoginLog;
import com.dzd.phonebook.entity.SysMenu;
import com.dzd.phonebook.entity.SysUser;
import com.dzd.phonebook.service.*;
import com.dzd.phonebook.util.*;
import com.dzd.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录controller
 *
 * @author chenchao
 * @date 2016-6-24 16:11:00
 */
@Controller
@RequestMapping("/loginChange/change")
public class LoginChangeController {
    public static final LogUtil log = LogUtil.getLogger(LoginChangeController.class);
    @Autowired
    private SysUserService<SysUser> sysUserService;

    @Autowired
    private SysMenuService<SysMenu> sysMenuService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private SmsUserService smsUserService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private UserMessageService userMessageService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private ChannelService channelService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private SysLoginLogService sysLoginLogService;

    /**
     * 登录
     *
     * @param request
     * @param data
     * @return
     */
    @Auth(verifyLogin = false, verifyURL = false)
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Map<String, Object> login(HttpServletRequest request, @RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<String, Object>();
        String account = data.get("account").toString();
        String password = data.get("password").toString();
        String type = data.get("type").toString();
        // 登录用户ip
        String ip = CusAccessObjectUtil.getIpAddress(request);
        SysUser sysUser = sysUserService.queryUserExist(account);
        try {
            HttpSession session = request.getSession();
            // 1.账户名密码登录
            if (type != null && !"".equals(type) && type.equals("login")) {
                // 2. 用户不存在
                if (sysUser == null) {
                    response.put("errorNum", 3);// 登录错误次数
                    response.put("msg", ErrorCodeTemplate.MSG_SYSUSER_EMPTY);
                    response.put("retCode", ErrorCodeTemplate.CODE_FAIL);
                    return response;
                } else {
                    SysUser u = sysUserService.queryLogin(account, MethodUtil.MD5(password));
                    if (u == null) {
                        response.put("errorNum", 3);// 登录错误次数
                        response.put("msg", ErrorCodeTemplate.MSG_SYSUSER_PWD_IS_MISS);
                        response.put("retCode", ErrorCodeTemplate.CODE_FAIL);
                        // 保存登录日志
                        saveLoginLog(ErrorCodeTemplate.MSG_SYSUSER_PWD_IS_MISS, ip, account, sysUser.getId());
                        return response;
                    }
                }

                // 3. 登录
                response = userLogin(account, password, request, session, ip);// 调用登录方法
            }

			/*
             * // 4. 注册后直接登录,无需校验验证码 else if (type != null && !"".equals(type)
			 * && type.equals("register")) { response = userLogin(account,
			 * password, request, session);// 调用登录方法 }
			 */
            else {
                response.put("msg", ErrorCodeTemplate.MSG_STATE_ERROR);
                response.put("retCode", ErrorCodeTemplate.CODE_FAIL);
            }
        } catch (Exception e) {
            log.error(null, e);
            response.put("msg", ErrorCodeTemplate.MSG_STATE_ERROR);
            response.put("retCode", ErrorCodeTemplate.CODE_FAIL);
        }
        return response;
    }

    /**
     * 保存用户登录日志
     *
     * @param ip
     * @param msg
     */
    public void saveLoginLog(String msg, String ip, String email, int userid) {
        try {
            SysLoginLog sysLoginLog = new SysLoginLog();
            sysLoginLog.setEmail(email);
            sysLoginLog.setLoginState(msg);
            sysLoginLog.setIp(ip);
            sysLoginLog.setSysUserId(userid);

            sysLoginLogService.saveLoginLog(sysLoginLog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 登录
     *
     * @param account
     * @param password
     * @param request
     * @param session
     */
    public Map<String, Object> userLogin(String account, String password, HttpServletRequest request,
                                         HttpSession session, String ip) {
        Map<String, Object> response = new HashMap<String, Object>();

        SysUser u = sysUserService.queryLogin(account, MethodUtil.MD5(password));
        if (u == null) {
            Integer errorNum = sysUserService.queryErrorNumByEmail(account);
            if (errorNum == null) {
                errorNum = 0;
            }
            errorNum++;
            SysUser sysUser = new SysUser();
            sysUser.setEmail(account);
            sysUser.setErrorNum(errorNum);
            sysUserService.updateLoginErrorNumByEmail(sysUser);

            response.put("errorNum", errorNum);// 登录错误次数
            response.put("msg", ErrorCodeTemplate.MSG_SYSUSER_EMPTY);
            response.put("retCode", ErrorCodeTemplate.CODE_FAIL);
        } else {
            SysUser sysUser = new SysUser();
            sysUser.setEmail(account);
            sysUser.setErrorNum(0);
            sysUserService.updateLoginErrorNumByEmail(sysUser);

            // 设置User到Session
            SessionUtils.setUser(request, u);
            SysUser user = SessionUtils.getUser(request);

            List<SysMenu> rootMenus = null;
            // List<SysMenuBtn> childBtns = null;
            // 超级管理员
            if (user != null && Constant.SuperAdmin.YES.key == user.getSuperAdmin()) {
                rootMenus = sysMenuService.queryRootSysMenuList();
            } else {
                // 根据用户查询角色再查询菜单
                DzdParameters dzdParameters = new DzdParameters();
                dzdParameters.setUserId(user.getId());
                rootMenus = sysMenuService.queryMenusByUserId(dzdParameters);
                // 根据用户id查找菜单 子父同一层级
                List<String> allUrls = sysMenuService.getActionUrls(dzdParameters);
                // 将用户对应的菜单放入session中
                SessionUtils.setAccessUrl(request, allUrls);// 设置可访问的URL
            }
            // 设置代理商User到Session
            SmsUser smsUsers = userMessageService.querySmsUserById(user.getId());
            SessionUtils.setSmsUser(request, smsUsers);

            // 查询通道开通的时段
            SmsAisleGroup smsAisleGroup = channelService.querySmsAisleGroupById(smsUsers.getAisleGroupId());
            SessionUtils.setSmsAisleGroup(request, smsAisleGroup);

            // 更改用户首次登陆发送短信要输入验证码
            smsUsers.setVerifyType(0);
            smsUserService.updateSmsUserVerifyType(smsUsers);

            session.setAttribute("user", u);
            session.setAttribute("menuList", rootMenus);
            session.setMaxInactiveInterval(60 * 60);// 以秒为单位 60分钟失效

            response.put("msg", ErrorCodeTemplate.MSG_SUCCESS_MSG);
            response.put("retCode", ErrorCodeTemplate.CODE_SUCESS);

            // 保存登录日志
            saveLoginLog(ErrorCodeTemplate.MSG_SUCCESS_MSG, ip, account, u.getId());
        }
        return response;
    }

}
