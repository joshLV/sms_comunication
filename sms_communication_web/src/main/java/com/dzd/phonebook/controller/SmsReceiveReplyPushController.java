package com.dzd.phonebook.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dzd.phonebook.service.*;
import com.dzd.phonebook.util.*;
import com.dzd.utils.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dzd.base.util.SessionUtils;
import com.dzd.base.util.StringUtil;
import com.dzd.phonebook.controller.base.WebBaseController;
import com.dzd.phonebook.entity.SmsReceiveReplyPush;
import com.dzd.phonebook.entity.SysMenuBtn;
import com.dzd.phonebook.entity.SysUser;
import com.github.pagehelper.Page;

/**
 * 信息回复
 *
 * @Description:Controller
 * @author:lq
 * @time:2017年1月3日
 */
@Controller
@RequestMapping("/smsReceiveReplyPush")
public class SmsReceiveReplyPushController<T> extends WebBaseController {
    public static final LogUtil log = LogUtil.getLogger(SmsReceiveReplyPushController.class);
    @Autowired
    private SmsReceiveReplyPushService<SmsReceiveReplyPush> smsReceiveReplyPushService;
    @Autowired
    private SysMenuBtnService sysMenuBtnService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private SysRoleRelService sysRoleRelService;
    @Autowired
    private SmsAccountService smsAccountService;
    @Autowired
    private UserMessageService userMessageService;


    @RequestMapping("/listview")
    public String loading(HttpServletRequest request, Model model) throws Exception {
        Object menuId = request.getParameter("id");
        List<SmsAisle> smsAisles = channelService.querySmsAisleList();
        SysUser sysUser = SessionUtils.getUser(request);
        model.addAttribute("smsAisles", smsAisles);
        model.addAttribute("menuId", menuId);
        SmsUser smsUser = userMessageService.querySmsUserById(sysUser.getId());
        model.addAttribute("isShow", smsUser.getNetworkChargingState());
        return "smsManage/smsReceiveReplyPush";
    }

    /**
     * 查询短信回复
     *
     * @param request
     * @param response
     * @param data
     * @throws Exception
     */
    @RequestMapping(value = "/blacklist", method = RequestMethod.POST)
    @ResponseBody
    public DzdResponse blacklist(HttpServletRequest request, HttpServletResponse response,
                                 @RequestBody Map<String, Object> data) throws Exception {
        DzdResponse dzdPageResponse = new DzdResponse();
        try {
            // 1. 基本参数设置
            WebRequestParameters parameters = getRequestParameters(WebRequestParameters.class, data);
            if (parameters == null) {
                return dzdPageResponse;
            }
            Object menuId = request.getParameter("menuId");
            Object emailInput = data.get("emailInput");
            Object phone = data.get("phone");
            Object smsAisleId = data.get("smsAisleId");   //通道

            if (menuId == null) {
                return dzdPageResponse;
            }
            DzdPageParam dzdPageParam = new DzdPageParam();
            SysUser user = SessionUtils.getUser(request);
            Map<String, Object> sortMap = new HashMap<String, Object>();

            if (parameters.getPagenum() != 0 && parameters.getPagesize() != 0) {
                dzdPageParam.setStart(parameters.getPagenum());
                dzdPageParam.setLimit(parameters.getPagesize());
            }
            if (!StringUtil.isEmpty(parameters.getStartInput())) {
                sortMap.put("start", parameters.getStartInput());
                sortMap.put("end", parameters.getEndInput());
            }
            if (emailInput != null && !StringUtil.isEmpty(emailInput.toString())) {
                sortMap.put("emailInput", emailInput.toString());
            }
            if (phone != null && !StringUtil.isEmpty(phone.toString())) {
                sortMap.put("phone", phone.toString());
            }

            if(smsAisleId!=null && !StringUtil.isEmpty(smsAisleId.toString())){
                sortMap.put("smsAisleId",smsAisleId.toString());
            }

            //获取登陆账户下面的归属账户sys_user_id
            dzdPageParam=CommonRoleServiceUtil.getSysUserId(user,dzdPageParam,sortMap,sysRoleRelService,smsAccountService);

            // 排序
            if (parameters.getSort() != null && parameters.getOrder() != null) {
                sortMap.put("sortVal", "order by " + parameters.getSort() + " " + parameters.getOrder());
            } else {
                sortMap.put("sortVal", "order by createTime desc");
            }

            dzdPageParam.setCondition(sortMap);


            List<SysMenuBtn> sysMenuBtns = null;
            if (menuId != null) {
                if (user.getSuperAdmin() == 1) {// 管理员查询所有菜单按钮
                    sysMenuBtns = sysMenuBtnService.queryByMenuid(Integer.parseInt(menuId.toString()));
                } else {// 其余角色查询配置好的按钮
                    sysMenuBtns = sysMenuBtnService.queryMenuListByRole(Integer.parseInt(menuId.toString()), user.getId());
                }
            }

            Page<SmsReceiveReplyPush> dataList = smsReceiveReplyPushService.queryUserList(dzdPageParam,sysMenuBtns);
            dzdPageResponse.setData(sysMenuBtns);
            if (!CollectionUtils.isEmpty(dataList)) {
                for (SmsReceiveReplyPush smsBlacklist : dataList.getResult()) {
                    smsBlacklist.setSysMenuBtns(sysMenuBtns);
                }
                dzdPageResponse.setRows(dataList.getResult());
                dzdPageResponse.setTotal(dataList.getTotal());
            }

        } catch (Exception e) {
            log.error(null, e);
            e.printStackTrace();
        }
        return dzdPageResponse;
    }

}
