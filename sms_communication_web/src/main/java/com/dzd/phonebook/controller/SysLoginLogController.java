package com.dzd.phonebook.controller;

import com.dzd.base.util.SessionUtils;
import com.dzd.base.util.StringUtil;
import com.dzd.phonebook.controller.base.WebBaseController;
import com.dzd.phonebook.entity.SysLog;
import com.dzd.phonebook.entity.SysUser;
import com.dzd.phonebook.service.CommonRoleServiceUtil;
import com.dzd.phonebook.service.SmsAccountService;
import com.dzd.phonebook.service.SysLoginLogService;
import com.dzd.phonebook.service.SysRoleRelService;
import com.dzd.phonebook.util.DzdPageParam;
import com.dzd.phonebook.util.DzdResponse;
import com.dzd.phonebook.util.WebRequestParameters;
import com.dzd.utils.LogUtil;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录日志
 * Created by wangran on 2017/6/02.
 */
@Controller
@RequestMapping("/login")
public class SysLoginLogController extends WebBaseController {
    public static final LogUtil log = LogUtil.getLogger(SysLoginLogController.class);

    @Autowired
    private SysLoginLogService sysLoginLogService;

    @Autowired
    private SysRoleRelService sysRoleRelService;

    @Autowired
    private SmsAccountService smsAccountService;

    @RequestMapping("/listview")
    public String list(HttpServletRequest request, Model model) throws Exception {
        Object menuId = request.getParameter("id");
        model.addAttribute("menuId", menuId);
        return "log/loginLog";
    }

    /**
     * @Description:登录日志列表
     * @author:wangran
     * @time:2017年6月2日
     */
    @RequestMapping(value = "/loginLogList", method = RequestMethod.POST)
    @ResponseBody
    public DzdResponse loginLogList(HttpServletRequest request, @RequestBody Map<String, Object> data) throws Exception {
        DzdResponse dzdPageResponse = new DzdResponse();
        DzdPageParam dzdPageParam = new DzdPageParam();
        Map<String, Object> sortMap = new HashMap<String, Object>();
        SysUser user = SessionUtils.getUser(request);
        try {
            WebRequestParameters parameters = getRequestParameters(WebRequestParameters.class, data);
            if (parameters == null) {
                return dzdPageResponse;
            }
            Object email = data.get("email");
            Object loginState = data.get("loginState");
            Object ip = data.get("ip");
            if (email != null && !StringUtil.isEmpty(email.toString())) {
                sortMap.put("email", email);
            }
            if (loginState != null && !StringUtil.isEmpty(loginState.toString())) {
                sortMap.put("loginState", loginState);
            }
            if (ip != null && !StringUtil.isEmpty(ip.toString())) {
                sortMap.put("ip", ip);
            }
            if (!StringUtil.isEmpty(parameters.getStartInput())) {
                sortMap.put("startInput", parameters.getStartInput());
                sortMap.put("endInput", parameters.getEndInput());
            }
            if (parameters.getPagenum() != 0 && parameters.getPagesize() != 0) {
                dzdPageParam.setStart(parameters.getPagenum());
                dzdPageParam.setLimit(parameters.getPagesize());
            }


            //获取账户下面的归属账户sys_user_id
            dzdPageParam=CommonRoleServiceUtil.getSysUserId(user,dzdPageParam,sortMap,sysRoleRelService,smsAccountService);


            sortMap.put("sortVal", "order by createTime desc");
            dzdPageParam.setCondition(sortMap);

            Page<SysLog> dataList = sysLoginLogService.querySysLoginLogPage(dzdPageParam);
            if (!CollectionUtils.isEmpty(dataList)) {
                dzdPageResponse.setRows(dataList.getResult());
                dzdPageResponse.setTotal(dataList.getTotal());
            }
        } catch (Exception e) {
            logger.error("====================》登陆日志查询失败：" + e.getMessage());
            e.printStackTrace();
        }
        return dzdPageResponse;
    }
}
