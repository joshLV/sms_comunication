package com.dzd.base.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dzd.base.util.SessionUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.dzd.phonebook.entity.SysUser;

/**
 * 登录拦截器
 *
 * @author chenchao
 * @date 2016-6-30 14:05:00
 */
public class LoginInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Object menuObj = request.getSession().getAttribute("menuList");
        Object useObj = SessionUtils.getUser(request);

        if (useObj != null && menuObj != null) {
            return true;
        } else {
            String con = request.getContextPath();
            response.sendRedirect(con + "/loginview.do#layout");
            return false;
        }
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // TODO Auto-generated method stub

    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // TODO Auto-generated method stub

    }
}
