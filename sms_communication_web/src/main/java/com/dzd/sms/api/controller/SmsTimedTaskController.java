package com.dzd.sms.api.controller;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dzd.utils.LogUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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
import com.dzd.cache.redis.manager.RedisClient;
import com.dzd.cache.redis.manager.RedisManager;
import com.dzd.phonebook.controller.base.WebBaseController;
import com.dzd.phonebook.entity.SysMenuBtn;
import com.dzd.phonebook.entity.SysUser;
import com.dzd.phonebook.service.ChannelService;
import com.dzd.phonebook.service.CommonRoleServiceUtil;
import com.dzd.phonebook.service.MsmSendService;
import com.dzd.phonebook.service.SmsAccountService;
import com.dzd.phonebook.service.SmsAisleGroupService;
import com.dzd.phonebook.service.SmsOrderExportService;
import com.dzd.phonebook.service.SmsSpecificSymbolService;
import com.dzd.phonebook.service.SmsWordShieldingService;
import com.dzd.phonebook.service.SysMenuBtnService;
import com.dzd.phonebook.service.SysRoleRelService;
import com.dzd.phonebook.service.UserMessageService;
import com.dzd.phonebook.service.UserSendMassSmsService;
import com.dzd.phonebook.util.DzdPageParam;
import com.dzd.phonebook.util.DzdResponse;
import com.dzd.phonebook.util.ErrorCodeTemplate;
import com.dzd.phonebook.util.ServiceBeanUtil;
import com.dzd.phonebook.util.SmsAisle;
import com.dzd.phonebook.util.SmsAisleGroup;
import com.dzd.phonebook.util.SmsSendLog;
import com.dzd.phonebook.util.SmsSendTask;
import com.dzd.phonebook.util.WebRequestParameters;
import com.dzd.sms.application.Define;
import com.dzd.sms.application.SmsServerManager;
import com.dzd.sms.service.data.SmsRequestParameter;
import com.dzd.sms.service.data.SmsTaskData;
import com.dzd.sms.service.data.SmsUser;
import com.dzd.sms.task.quartz.QuartzJobManager;
import com.github.pagehelper.Page;

import net.sf.json.JSONObject;

/**
 * @ClassName: SmsTimedTaskController
 * @Description:TODO(这里用一句话描述这个类的作用)
 * @author: hz-liang
 * @date: 2017年5月23日 下午2:25:17
 */
@Controller
@RequestMapping("/sms/task")
public class SmsTimedTaskController extends WebBaseController {
    static LogUtil logger = LogUtil.getLogger(SmsTimedTaskController.class);
    // 服务操作类

    @SuppressWarnings("rawtypes")
    @Autowired
    private MsmSendService msmSendService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private UserMessageService userMessageService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private SysMenuBtnService sysMenuBtnService;

    @SuppressWarnings("unused")
    @Autowired
    private SmsSpecificSymbolService smsSpecificSymbolService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private ChannelService channelService;

    @Autowired
    private SmsSpecificSymbolService symbolService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private SysRoleRelService sysRoleRelService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private SmsAccountService smsAccountService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private SmsWordShieldingService wordService;
    
    @SuppressWarnings("rawtypes")
	@Autowired
    private SmsAisleGroupService smsAisleGroupService;

    private static final String SMS_FLAG_BOOLEAN = "smsFlag";

    private List<List<String>> dataList;

    public List<List<String>> getDataList() {
        return dataList;
    }

    public void setDataList(List<List<String>> dataList) {
        this.dataList = dataList;
    }

    /**
     * 跳转定时任务页面
     *
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/timedtask")
    public String timedTask(HttpServletRequest request, Model model) {
        List<SmsAisle> smsAisleGroup = msmSendService.querySmsAisleGroup(null);// 查询所有的通道名称

        model.addAttribute(Define.REQUESTPARAMETER.AISLENAMES, smsAisleGroup);

        Object menuId = request.getParameter(Define.REQUESTPARAMETER.ID);
        model.addAttribute(Define.REQUESTPARAMETER.MENUID, menuId);

        return "smsManage/timedTask";
    }

    /**
     * 查询定时任务
     *
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/querytimedtask")
    @ResponseBody
    public DzdResponse queryTimedTask(HttpServletRequest request, HttpServletResponse response,
                                      @RequestBody Map<String, Object> data) {
        DzdResponse dzdPageResponse = new DzdResponse();
        try {
            WebRequestParameters parameters = getRequestParameters(WebRequestParameters.class,
                    data);
            if (parameters == null) {
                return dzdPageResponse;
            }
            // 查询入参
            DzdPageParam dzdPageParam = preParameter(data, parameters, request);

            String menuId = request.getParameter(Define.REQUESTPARAMETER.MENUID);
            // 2. 查询菜单按钮
            SysUser user = SessionUtils.getUser(request);
            List<SysMenuBtn> sysMenuBtns = null;
            if (menuId != null) {
                if (user.getSuperAdmin() == 1) {// 管理员查询所有菜单按钮
                    sysMenuBtns = sysMenuBtnService.queryByMenuid(Integer.parseInt(menuId));
                } else {// 其余角色查询配置好的按钮
                    sysMenuBtns = sysMenuBtnService.queryMenuListByRole(Integer.parseInt(menuId), user.getId());
                }
            }

            Page<SmsSendLog> dataList = msmSendService.querySmsTimedTask(sysMenuBtns, dzdPageParam);

            dzdPageResponse.setData(sysMenuBtns);
            if (!CollectionUtils.isEmpty(dataList)) {
                for (SmsSendLog smsSendLog : dataList.getResult()) {
                    smsSendLog.setSysMenuBtns(sysMenuBtns);
                }
                dzdPageResponse.setRows(dataList.getResult());
                dzdPageResponse.setTotal(dataList.getTotal());
            }
        } catch (Exception e) {
            logger.error(null, e);
            e.printStackTrace();
        }
        return dzdPageResponse;
    }

    /**
     * 更新定时任务
     *
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    @SuppressWarnings("static-access")
	@RequestMapping(value = "/updatetimedtask", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> updateTimedTask(HttpServletRequest request,
                                               HttpServletResponse response) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            String befroBillingNum = request.getParameter(Define.REQUESTPARAMETER.BEFROBILLINGNUM);
            List<SmsSendTask> smsSendTasks = preParameterForSmsSendTask(request, resultMap, befroBillingNum);

            if (!CollectionUtils.isEmpty(smsSendTasks)) {
                RedisClient redisClient = RedisManager.I.getRedisClient();
                SmsTaskData smsTaskData = null;
                String taskId = null;

                for (SmsSendTask smsSendTask : smsSendTasks) {
                    msmSendService.updateTimedTask(smsSendTask);

                    if (!StringUtils.isEmpty(befroBillingNum) && (smsSendTask.getBillingNum() != null) && (Integer.valueOf(befroBillingNum)
                            - smsSendTask.getBillingNum()) != 0) {
                        SmsServerManager.I.addUserBalance(smsSendTask.getSmsUserId().longValue(),
                                Integer.valueOf(befroBillingNum) - smsSendTask.getBillingNum(),
                                "任务ID：" + smsSendTask.getId(), "timingTag");
                    }

                    // 更新缓存数据
                    taskId = smsSendTask.getId().toString();
                    smsTaskData = (SmsTaskData) redisClient.hgetObject(Define.KEY_SMS_TASK_CACHE, taskId);
                    if (smsTaskData != null) {
                        if (smsSendTask.getSendType() != null) {
                            smsTaskData.setTaskType(smsSendTask.getSendType());
                        }
                        if (smsSendTask.getSendType() != null && 1 == smsSendTask.getSendType()) {
                            smsTaskData.setFree(true);
                            smsTaskData.setAuditState(true);
                        } else if (smsSendTask.getSendType() != null
                                && 2 == smsSendTask.getSendType()) {
                            smsTaskData.setFree(false);
                            smsTaskData.setAuditState(false);
                        }
                        if (smsSendTask.getTimingTime() != null) {
                            smsTaskData.setTiming(smsSendTask.getTimingTime());
                            QuartzJobManager.I.addTimeTask(smsTaskData.getTiming(),
                                    smsTaskData.getTaskId());
                        }
                        if (smsSendTask.getBillingNum() != null) {
                            smsTaskData.setBilling_num(smsSendTask.getBillingNum());
                        }
                        redisClient.hsetObject(Define.KEY_SMS_TASK_CACHE, new String(taskId),
                                smsTaskData);
                    }
                }

            }
        } catch (Exception e) {
            logger.error(null, e);
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * 修改定时任务
     *
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/querytimedtaskformodify")
    @ResponseBody
    public SmsSendLog queryTimedTaskForModify(HttpServletRequest request,
                                              HttpServletResponse response) {
        SmsSendLog smsSendLog = null;
        try {
            Map<String, Object> param = new HashMap<String, Object>();

            String taskId = request.getParameter(Define.REQUESTPARAMETER.TASKID);

            if (!StringUtils.isEmpty(taskId)) {
                param.put(Define.REQUESTPARAMETER.TASKID, taskId);
            }

            SysUser user = (SysUser) request.getSession().getAttribute("session_user");
			com.dzd.phonebook.util.SmsUser smsUsers = userMessageService.querySmsUserById(user.getId());
            Map<String, Object> smsAisleGroupSignMap = smsAisleGroupService.querySmsAisleGroupSign(smsUsers);
            List<String> querySmsUserSignList = (List<String>)smsAisleGroupSignMap.get("signList");

            smsSendLog = msmSendService.queryTimedTaskForModify(param);
            if (smsSendLog == null) {
                return null;
            }

            smsSendLog.setSignatureAttr(querySmsUserSignList.toArray());

            fillSmsSendLog(smsSendLog);

        } catch (Exception e) {
            logger.error(null, e);
            e.printStackTrace();
        }
        return smsSendLog;
    }

    /**
     * 删除定时任务
     *
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/deletetimedtask")
    @ResponseBody
	public void deleteTimedTask(HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			Map<String, Object> param = new HashMap<String, Object>();

			String taskId = request.getParameter(Define.REQUESTPARAMETER.TASKIDS);

			String[] taskIdStr = taskId.split(",");
			List<String> taskIds = Arrays.asList(taskIdStr);

			if ( !CollectionUtils.isEmpty(taskIds) )
			{
				param.put(Define.REQUESTPARAMETER.TASKIDS, taskIds);
			}

			RedisClient redisClient = RedisManager.I.getRedisClient();
			SmsTaskData smsTaskData = null;
			for ( String tId : taskIds )
			{
				smsTaskData = (SmsTaskData) redisClient.hgetObject(Define.KEY_SMS_TASK_CACHE, tId);
				if ( smsTaskData == null )
				{
					logger.info("无法获取缓存数据，删除数据返款失败，taskId = " + tId);
					continue;
				}
				SmsServerManager.I.addUserBalance(smsTaskData.getUserId(),
				        smsTaskData.getBilling_num(), "任务ID：" + tId, "timingTag");
			}

			msmSendService.deleteTimedTask(param);

		} catch (Exception e)
		{
			logger.error(null, e);
			e.printStackTrace();
		}
	}

    /**
     * 查询定时任务
     *
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/querytimedtaskfororder")
    @ResponseBody
    public DzdResponse queryTimedTaskForOrder(HttpServletRequest request,
                                              HttpServletResponse response) {
        DzdResponse dzdPageResponse = new DzdResponse();
        try {
            // 查询入参
            String taskIds = request.getParameter(Define.REQUESTPARAMETER.TASKIDS);
            List<String> taskIdList = Arrays.asList(taskIds.split(","));

            List<List<String>> dataList = new ArrayList<List<String>>();
            List<String> phonelist = null;
            for (String taskId : taskIdList) {
                phonelist = msmSendService.querySmsTaskPhone(taskId);
                dataList.add(phonelist);
            }

            if (CollectionUtils.isEmpty(dataList)) {
                dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
                return dzdPageResponse;
            }
            this.setDataList(dataList);
            dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
        } catch (Exception e) {
            logger.error(null, e);
            e.printStackTrace();
        }
        return dzdPageResponse;
    }

    @RequestMapping(value = "/orderExport")
    @ResponseBody
    public void orderExport(HttpServletRequest request, HttpServletResponse response) {
        List<List<String>> dataLists = this.getDataList();

        SmsOrderExportService orderExportService = new SmsOrderExportService();
        orderExportService.taskOrderExport(request, response, dataLists);
    }

    private void fillSmsSendLog(SmsSendLog smsSendLog) {
        String content = smsSendLog.getContent();
        int signEndPos = content.indexOf("】");
        smsSendLog.setSignature(content.substring(0, signEndPos + 1));
        smsSendLog.setContent(content.substring(signEndPos + 1, content.length()));
    }

    private List<SmsSendTask> preParameterForSmsSendTask(HttpServletRequest request,
                                                         Map<String, Object> resultMap, String befroBillingNum) throws ParseException {
        List<SmsSendTask> smsSendTasks = new ArrayList<SmsSendTask>();

        String taskId = request.getParameter(Define.REQUESTPARAMETER.TASKID);
        String sendType = request.getParameter(Define.REQUESTPARAMETER.SENDTYPE);
        String taskIds = request.getParameter(Define.REQUESTPARAMETER.TASKIDS);
        String sendTypes = request.getParameter(Define.REQUESTPARAMETER.SENDTYPES);
        String content = request.getParameter(Define.STATICAL.CONTENT);
        String timing = request.getParameter(Define.REQUESTPARAMETER.TIMING);
        String billingNnum = request.getParameter(Define.REQUESTPARAMETER.BILLINGNNUM);
        String detailBillingNum = request.getParameter("detailBillingNum");

        SysUser user = SessionUtils.getUser(request);
        com.dzd.phonebook.util.SmsUser smsUsers = userMessageService
                .querySmsUserById(user.getId());

        if (!StringUtils.isEmpty(content)) {
            SmsAisleGroup smsAisleGroup = channelService
                    .querySmsAisleGroupById(smsUsers.getAisleGroupId());

            SmsRequestParameter parameter = new SmsRequestParameter();
            parameter.setText(content);
            parameter.setBillingNnum(Integer.valueOf(billingNnum));
            parameter.setSurplusNum(Integer.valueOf(billingNnum) - Integer.valueOf(befroBillingNum));
            parameter.setTimeTaskCeck(true);
            parameter.setUnregTypeId(smsAisleGroup.getUnregTypeId());// 退订格式
            parameter.setTiming(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timing + ":00"));
            parameter.setSmsUserState(smsUsers.getState());
            parameter.setAisleGroupState(smsAisleGroup.getState());
            parameter.setDredgeAM(smsAisleGroup.getDredgeAM());// 开通时间段 开始时间
            parameter.setDredgePM(smsAisleGroup.getDredgePM());// 开通时间段 截止时间
            parameter.setShieldingFieldId(smsAisleGroup.getShieldingFieldId());// 通道组敏感词id
            parameter.setUid(smsUsers.getId().toString());
            parameter.setAisleLongNum(smsAisleGroup.getSmsLength());// 通道组长短信计费

            ServiceBeanUtil serviceBean = new ServiceBeanUtil();
            serviceBean.setChannelService(channelService);
            serviceBean.setSymbolervice(symbolService);
            serviceBean.setWordService(wordService);

            UserSendMassSmsService userSendMassSmsService = new UserSendMassSmsService();
            JSONObject json = userSendMassSmsService.sendBeforVerify(parameter, serviceBean);
            if (!json.getBoolean(SMS_FLAG_BOOLEAN)) {
                getResultBooleanJson(json, resultMap);
                return smsSendTasks;
            }
        }

        String[] taskIdStr = null;
        String[] sendTypeStr = null;
        String[] contentStr = null;
        Date[] timingStr = null;
        int[] billingNnumInt = null;

        if (taskIds != null && !taskIds.equals("")) {
            taskIdStr = taskIds.toString().split(",");
            sendTypeStr = sendTypes.toString().split(",");
        } else {
            taskIdStr = new String[1];
            sendTypeStr = new String[1];
            contentStr = new String[1];
            timingStr = new Date[1];
            billingNnumInt = new int[1];

            if (!StringUtils.isEmpty(taskId)) {
                taskIdStr[0] = taskId;
            }
            if (!StringUtils.isEmpty(sendType)) {
                sendTypeStr[0] = sendType;
            }
            if (!StringUtils.isEmpty(content)) {
                contentStr[0] = content;
            }
            if (!StringUtils.isEmpty(billingNnum)) {
                billingNnumInt[0] = Integer.valueOf(billingNnum);
            }
            if (!StringUtils.isEmpty(timing)) {
                timingStr[0] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timing + ":00");
            }
        }

        SmsSendTask smsSendTask = null;
        for (int i = 0; i < taskIdStr.length; i++) {
            smsSendTask = new SmsSendTask();
            if (taskIdStr != null && taskIdStr.length != 0 && taskIdStr[0] != null) {
                smsSendTask.setId(Integer.valueOf(taskIdStr[i]));
            }
            if (sendTypeStr != null && sendTypeStr.length != 0 && sendTypeStr[0] != null) {
                smsSendTask.setSendType(Integer.valueOf(sendTypeStr[i]));
            }
            if (contentStr != null && contentStr.length != 0 && contentStr[0] != null) {
                smsSendTask.setContent(contentStr[i]);
            }
            if (timingStr != null && timingStr.length != 0 && timingStr[0] != null) {
                smsSendTask.setTimingTime(timingStr[i]);
            }
            if (billingNnumInt != null && billingNnumInt.length != 0 && billingNnumInt[0] != 0) {
                smsSendTask.setBillingNum(billingNnumInt[i]);
            }
            smsSendTask.setSmsUserId(smsUsers.getId());
            smsSendTask.setDetailBillingNum(detailBillingNum);// 发送详情的计费条数
            smsSendTasks.add(smsSendTask);
        }
        return smsSendTasks;
    }

    public void getResultBooleanJson(JSONObject json, Map<String, Object> resultMap) {
        resultMap.put(SMS_FLAG_BOOLEAN, json.getBoolean(SMS_FLAG_BOOLEAN));
        resultMap.put("code", json.get("code"));
        resultMap.put("msg", json.get("msg"));
    }

    /**
     * @param data
     * @param parameters
     * @return
     * @throws
     * @Title: preParameter
     * @Description: 查询入参
     * @author: hz-liang
     * @return: DzdPageParam
     */
    @SuppressWarnings("static-access")
    private DzdPageParam preParameter(Map<String, Object> data, WebRequestParameters parameters,
                                      HttpServletRequest request) {
        Object email = null;
        Object sendType = null;
        Object aisleName = null;

        if (!CollectionUtils.isEmpty(data)) {
            email = data.get(Define.REQUESTPARAMETER.EMAIL);
            sendType = data.get(Define.REQUESTPARAMETER.SENDTYPE);
            aisleName = data.get(Define.REQUESTPARAMETER.AISLENAME);
        }

        DzdPageParam dzdPageParam = new DzdPageParam();
        Map<String, Object> sortMap = new HashMap<String, Object>();
        if (parameters.getPagenum() != 0 && parameters.getPagesize() != 0) {
            dzdPageParam.setStart(parameters.getPagenum());
            dzdPageParam.setLimit(parameters.getPagesize());
        }

        if (!StringUtil.isEmpty(parameters.getStartInput())) {
            sortMap.put(Define.REQUESTPARAMETER.STARTINPUT, parameters.getStartInput());
            sortMap.put(Define.REQUESTPARAMETER.ENDINPUT, parameters.getEndInput());
        }

        if (email != null && !StringUtil.isEmpty(email.toString())) {
            sortMap.put(Define.REQUESTPARAMETER.EMAIL, email.toString());
        }

        if (aisleName != null && !StringUtil.isEmpty(aisleName.toString())) {
            sortMap.put(Define.REQUESTPARAMETER.AISLENAME, aisleName.toString());
        }

        if (sendType != null && !StringUtil.isEmpty(sendType.toString())) {
            sortMap.put(Define.REQUESTPARAMETER.SENDTYPE, Integer.parseInt(sendType.toString()));
        }

        SysUser user = SessionUtils.getUser(request);
        if (user.getId() != 1) {
            SmsUser smsUser = SmsServerManager.I.getUserBySysId(Long.valueOf(user.getId()));
            sortMap.put(Define.STATICAL.SMSID, smsUser.getId());
            sortMap.put(Define.STATICAL.SYSID, smsUser.getSysId());
        }

        SysUser sysUser = SessionUtils.getUser(request);
        Map<String, Object> customMap = new HashMap<String, Object>();

        CommonRoleServiceUtil.getSysUserId(sysUser, dzdPageParam, customMap,
                sysRoleRelService, smsAccountService);
        sortMap.put(Define.REQUESTPARAMETER.SYSUSERIDS, customMap.get(Define.REQUESTPARAMETER.SMSUSERVAL));

        dzdPageParam.setCondition(sortMap);
        return dzdPageParam;
    }

}
