package com.dzd.phonebook.controller;

import com.dzd.base.util.SessionUtils;
import com.dzd.base.util.StringUtil;
import com.dzd.phonebook.controller.base.WebBaseController;
import com.dzd.phonebook.entity.SmsRechargeOrder;
import com.dzd.phonebook.entity.SysMenuBtn;
import com.dzd.phonebook.entity.SysRoleRel;
import com.dzd.phonebook.entity.SysUser;
import com.dzd.phonebook.service.*;
import com.dzd.phonebook.util.*;
import com.dzd.sms.application.SmsServerManager;
import com.dzd.utils.LogUtil;
import com.github.pagehelper.Page;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 账户中心---短信账户 Created by wangran on 2017/5/18.
 */
@Controller
@RequestMapping("/account")
public class AccountController extends WebBaseController {
    public static final LogUtil log = LogUtil.getLogger(AccountController.class);

    @Autowired
    private SysMenuBtnService sysMenuBtnService;

    @Autowired
    private SmsUserService smsUserService;

    @Autowired
    private SmsAccountService smsAccountService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private SysRoleRelService sysRoleRelService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SmsRechargeOrderService smsRechargeOrderService;

    @Autowired
    private MsmSendService msmSendService;

    @RequestMapping("/listview")
    public String list(HttpServletRequest request, Model model) throws Exception {
        Object menuId = request.getParameter("id");
        model.addAttribute("menuId", menuId);

        SysUser user = SessionUtils.getUser(request);
        // 查询归属
        List<SysUser> sysUserList = CommonRoleServiceUtil.getSysUserBUserList(request, smsUserService,
                sysRoleRelService, smsAccountService);
        List<SmsAisleGroupType> list = channelService.querySmsAisleGroupType();

        if (user.getSuperAdmin() != 1) {
            List<SysRoleRel> sysRoleRels = CommonRoleServiceUtil.queryRoleByUserId(user.getId(), sysRoleRelService);
            model.addAttribute("roleType", sysRoleRels.get(0).getRoleId());
        }

        List<SmsAisle> smsAisleGroup = msmSendService.querySmsAisleGroup(null);// 查询所有的通道名称

        model.addAttribute("sysUser", user);
        model.addAttribute("sysUserList", sysUserList);
        model.addAttribute("smsAisleGroup", smsAisleGroup);
        model.addAttribute("typeList", list);
        return "account/account";
    }

    /**
     * @Description:短信账户列表
     * @author:oygy
     * @time:2016年12月31日 下午2:01:34
     */
    @RequestMapping(value = "/accountList", method = RequestMethod.POST)
    @ResponseBody
    public DzdResponse accountList(HttpServletRequest request, @RequestBody Map<String, Object> data) throws Exception {
        DzdResponse dzdPageResponse = new DzdResponse();
        try {
            WebRequestParameters parameters = getRequestParameters(WebRequestParameters.class, data);
            if (parameters == null) {
                return dzdPageResponse;
            }
            Object menuId = request.getParameter("menuId"); // 菜单id
            Object state = data.get("state"); // 用户状态0：启用 1：禁用
            Object aisleType = data.get("aisleType"); // 通道类型
            Object email = data.get("email"); // 账号
            Object sysUserId = data.get("sysUserId"); // 归属

            SysUser user = SessionUtils.getUser(request);
            if (user == null || menuId == null) {
                return dzdPageResponse;
            }

            List<SysRoleRel> sysRoleRels = CommonRoleServiceUtil.queryRoleByUserId(user.getId(), sysRoleRelService);
            if (sysRoleRels == null) {
                return dzdPageResponse;
            }


            DzdPageParam dzdPageParam = new DzdPageParam();
            Map<String, Object> sortMap = new HashMap<String, Object>();
            if (parameters.getPagenum() != 0 && parameters.getPagesize() != 0) {
                dzdPageParam.setStart(parameters.getPagenum());
                dzdPageParam.setLimit(parameters.getPagesize());
            }
            if (state != null && !StringUtil.isEmpty(state.toString())) {
                sortMap.put("state", state.toString());
            }
            if (aisleType != null && !StringUtil.isEmpty(aisleType.toString())) {
                sortMap.put("aisleType", aisleType.toString());
            }
            if (email != null && !StringUtil.isEmpty(email.toString())) {
                sortMap.put("email", email.toString());
            }
            if (sysUserId != null && !StringUtil.isEmpty(sysUserId.toString())) {
                sortMap.put("sysUserId", sysUserId.toString());
            }
            // 获取账户下面的归属账户sys_user_id
            dzdPageParam = CommonRoleServiceUtil.getSysUserId(user, dzdPageParam, sortMap, sysRoleRelService,
                    smsAccountService);

            // 排序
            if (parameters.getSort() != null && parameters.getOrder() != null) {
                sortMap.put("sortVal", "order by " + parameters.getSort() + " " + parameters.getOrder());
            } else {
                sortMap.put("sortVal", "order by surplusNum asc,usedNum asc");
            }

            dzdPageParam.setCondition(sortMap);
            List<SysMenuBtn> sysMenuBtns = null;

            if (menuId != null) {
                if (user.getSuperAdmin() == 1) {// 管理员查询所有菜单按钮
                    sysMenuBtns = sysMenuBtnService.queryByMenuid(Integer.parseInt(menuId.toString()));
                } else {// 其余角色查询配置好的按钮
                    sysMenuBtns = sysMenuBtnService.queryMenuListByRole(Integer.parseInt(menuId.toString()),
                            user.getId());
                }
            }

            Page<SmsUser> dataList = smsAccountService.querySmsAccountUserListPage(dzdPageParam, sysMenuBtns);

            dzdPageResponse.setData(sysMenuBtns);
            if (!CollectionUtils.isEmpty(dataList)) {
                for (SmsUser instruct : dataList.getResult()) {
                    instruct.setSysMenuBtns(sysMenuBtns);
                }
                dzdPageResponse.setRows(dataList.getResult());
                dzdPageResponse.setTotal(dataList.getTotal());
            }
        } catch (Exception e) {
            logger.error("====================》短信账户列表查询失败：" + e.getMessage());
            e.printStackTrace();
        }
        return dzdPageResponse;
    }

    /**
     * 用户添加短信条数
     *
     * @param request
     * @param
     * @return
     */
    @RequestMapping(value = "/from/moneyMerge", method = RequestMethod.POST)
    @ResponseBody
    public DzdResponse moneyMerge(HttpServletRequest request) {
        DzdResponse dzdResponse = new DzdResponse();
        try {
            Object id = request.getParameter("czid"); // 获取充值账号ID
            Object czmoney = request.getParameter("czmoney"); // 充值短信条数
            Object type = request.getParameter("type"); // 充值类型
            Object money = request.getParameter("money"); // 充值金额
            Object sysUserId = request.getParameter("sysUserId"); // 被操作用户id


            // 充值记录
            SmsRechargeOrder smsRechargeOrder = new SmsRechargeOrder();

            if (money != null && !StringUtil.isEmpty(money.toString())) {
                smsRechargeOrder.setMoney(Float.valueOf(money.toString()));
            }
            SmsUser smsUser = new SmsUser();
            Integer useBalance = SmsServerManager.I.getUserSurplusNum(Long.parseLong(id.toString()));// redis缓存获取账号余额信息
            Integer czmoneyValue = Integer.parseInt(czmoney.toString());
            Date createTime = new Date();
            smsUser.setId(Integer.parseInt(id.toString()));
            if (type.equals("5") || type.equals("8") || type.equals("7")) {
                // 5:转出 8:核减 原账户剩余短信中扣减  7:退款
                smsUser.setSurplusNum(czmoneyValue);
                smsUser.setSumNum(czmoneyValue);
            } else {
                // 0:人工充值 4：赠送 6：转入
                smsUser.setSurplusNum(czmoneyValue);
                // 充值短信，增加总数量
                smsUser.setSumNum(Integer.parseInt(czmoney.toString()));
            }

            smsUserService.updateSmsUserBlankMoney(smsUser);

            log.info("-----------------》用户添加短信条数");
            SysUser user = (SysUser) request.getSession().getAttribute("session_user");
            SmsUserMoneyRunning smr = new SmsUserMoneyRunning();
            smr.setSmsUserId(Integer.parseInt(id.toString()));
            smr.setUid(user.getId());
            smr.setBeforeNum(useBalance); // 操作前条数
            smr.setType(Integer.parseInt(type.toString()));


            if (type.equals("5") || type.equals("8") || type.equals("7")) {
                // 5:转出 8:核减 原账户剩余短信中扣减 7：退款
                smr.setAfterNum(useBalance + czmoneyValue); // 操作后条数
                smr.setOperateNum(czmoneyValue); // 操作条数
            } else {
                // 0:人工充值 4：赠送 6：转入
                smr.setAfterNum(useBalance + czmoneyValue); // 操作后条数
                smr.setOperateNum(czmoneyValue); // 操作条数
            }


            smr.setCreateTime(createTime); // 操作时间


            // 订单号
            com.dzd.sms.service.data.SmsUser smsUser1 = SmsServerManager.I.getUser(Long.parseLong(id.toString()));
            String order = RechargeVariable.getOrderNumber(smsUser1.getAccount());
            smr.setOrderNo(order);
            smr.setComment(ErrorCodeTemplate.MSG_MANUAL_ADDITION);

            smsUserService.saveSmsUserMoneyRunning(smr);

            // 添加到充值流水表
            smsRechargeOrder.setUserId(user.getId());
            smsRechargeOrder.setOrderNo(order);
            smsRechargeOrder.setSmsUserId(Integer.parseInt(sysUserId.toString()));
            smsRechargeOrder.setSmsNumber(Integer.parseInt(czmoney.toString()));
            smsRechargeOrder.setStatus(1);
            smsRechargeOrderService.insertSmsRechargeOrder(smsRechargeOrder);

            String keys = smsUserService.querySmsUserKey(smsUser.getId());
            // 发送动作指令到redis
            instructSend(InstructState.USERTOPUP_SUCESS, keys, smsUser.getId());

            dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
            dzdResponse.setRetMsg(ErrorCodeTemplate.MSG_SUCCESS);
        } catch (Exception e) {
            logger.error("====================》用户添加短信失败：" + e.getMessage());
            dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
            dzdResponse.setRetMsg(ErrorCodeTemplate.MSG_FAIL);
            e.printStackTrace();
        }
        return dzdResponse;
    }

    /**
     * @Description:关闭开通账户
     * @author:wangran
     * @time:2017年5月18日 上午16:38:29
     */
    @RequestMapping(value = "/editState", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public DzdResponse merge(HttpServletRequest request, @RequestBody Map<String, Object> data) {
        DzdResponse dzdPageResponse = new DzdResponse();
        SysUser user = SessionUtils.getUser(request);
        try {
            Object id = data.get("id");
            Object state = data.get("state");
            SmsUser smsUser = new SmsUser();
            smsUser.setId(Integer.parseInt(id.toString()));
            smsUser.setState(Integer.parseInt(state.toString()));
            // 修改用户状态
            smsUserService.updateUser(smsUser);
            // 保存操作日志
            smsUser = (SmsUser) smsUserService.queryById(Integer.parseInt(id.toString()));
            CommonRoleServiceUtil.saveOperateLog(user, smsUser.getEmail(), ErrorCodeTemplate.USER_STATE,
                    sysUserService);
            dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);

        } catch (Exception e) {
            logger.error("====================》用户状态修改失败：" + e.getMessage());
            dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
            e.printStackTrace();
        }
        return dzdPageResponse;
    }

    /**
     * @Description:代理数据处理动作发送
     * @author:oygy
     * @time:2017年1月11日 下午2:45:22
     */
    private void instructSend(String keys, String smsUserKey, Integer smsUserId) {
        Instruct instruct = new Instruct();
        instruct.setKey(keys);
        instruct.setSmsUserKey(smsUserKey);
        instruct.setSmsUserId(smsUserId + "");
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonStr = mapper.writeValueAsString(instruct);
            RedisUtil.publish(InstructState.AB, jsonStr);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
