package com.dzd.phonebook.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dzd.utils.LogUtil;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
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

import com.dzd.base.util.HtmlUtil;
import com.dzd.base.util.SessionUtils;
import com.dzd.base.util.StringUtil;
import com.dzd.phonebook.business.MsmSendBusiness;
import com.dzd.phonebook.controller.base.WebBaseController;
import com.dzd.phonebook.entity.SysMenuBtn;
import com.dzd.phonebook.entity.SysUser;
import com.dzd.phonebook.service.CommonRoleServiceUtil;
import com.dzd.phonebook.service.MsmSendService;
import com.dzd.phonebook.service.SmsAccountService;
import com.dzd.phonebook.service.SmsOrderExportService;
import com.dzd.phonebook.service.SmsUserService;
import com.dzd.phonebook.service.SysMenuBtnService;
import com.dzd.phonebook.service.SysRoleRelService;
import com.dzd.phonebook.service.UserMsmSendService;
import com.dzd.phonebook.util.ComparatorSmsUser;
import com.dzd.phonebook.util.DzdPageParam;
import com.dzd.phonebook.util.DzdResponse;
import com.dzd.phonebook.util.ErrorCodeTemplate;
import com.dzd.phonebook.util.SmsSendLog;
import com.dzd.phonebook.util.TempParameter;
import com.dzd.phonebook.util.WebRequestParameters;
import com.dzd.sms.application.Define;
import com.dzd.sms.application.SmsServerManager;
import com.dzd.sms.service.data.SmsUser;
import com.github.pagehelper.Page;

/**
 * @author lianghaunzheng E-mail: *
 * @version 1.0 *
 * @date 创建时间：2017年5月16日 上午11:10:10 *
 * @parameter *
 * @return
 * @since *
 */
@Controller
@RequestMapping("/statisticalandlog")
public class SmsUserStatisticalAndLogController extends WebBaseController {
	public static final LogUtil log = LogUtil.getLogger(SmsUserStatisticalAndLogController.class);

	@SuppressWarnings("rawtypes")
	@Autowired
	private MsmSendService msmSendService;

	@SuppressWarnings("rawtypes")
	@Autowired
	private UserMsmSendService userMsmSendService;

	@SuppressWarnings("rawtypes")
	@Autowired
	private SysMenuBtnService sysMenuBtnService;

	@SuppressWarnings("rawtypes")
	@Autowired
	private SmsUserService smsUserService;

	@SuppressWarnings("rawtypes")
	@Autowired
	private SysRoleRelService sysRoleRelService;

	@SuppressWarnings("rawtypes")
	@Autowired
	private SmsAccountService smsAccountService;

	private MsmSendBusiness msmSendBusiness = new MsmSendBusiness();

	private Page<com.dzd.phonebook.util.SmsUser> dataList = new Page<com.dzd.phonebook.util.SmsUser>();

	public Page<com.dzd.phonebook.util.SmsUser> getDataList() {
		return dataList;
	}

	public void setDataList(Page<com.dzd.phonebook.util.SmsUser> dataList) {
		this.dataList = dataList;
	}

	private List<SmsSendLog> smsSendLogDataList;

	public List<SmsSendLog> getSmsSendLogDataList() {
		return smsSendLogDataList;
	}

	public void setSmsSendLogDataList(List<SmsSendLog> smsSendLogDataList) {
		this.smsSendLogDataList = smsSendLogDataList;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/statisticalday")
	public String queryStatisticalDayInfo(HttpServletRequest request, Model model) throws Exception {
		SysUser sysUser = SessionUtils.getUser(request);
		Integer smsId = null;
		if (sysUser.getSuperAdmin() != 1) {
			com.dzd.phonebook.util.SmsUser smsUser = SessionUtils.getSmsUser(request);
			smsId = smsUser.getId();
		}

		List<String> aisleNames = msmSendService.queryAisleNames(smsId);// 查询所有的通道名称
		// 查询归属
		List<SysUser> sysUserList = CommonRoleServiceUtil.getSysUserBUserList(request, smsUserService,
				sysRoleRelService, smsAccountService);// 查询所有客户所属
		model.addAttribute(Define.REQUESTPARAMETER.AISLENAMES, aisleNames);
		model.addAttribute(Define.REQUESTPARAMETER.NICKNAMES, sysUserList);

		Object menuId = request.getParameter(Define.REQUESTPARAMETER.ID);
		model.addAttribute(Define.REQUESTPARAMETER.MENUID, menuId);

		return "statistical/todayStatistical"; // 跳转JSP页面
	}

	@RequestMapping("/statisticalHistory")
	public String queryStatisticalHistoryInfo(HttpServletRequest request, Model model) {
		Object menuId = request.getParameter(Define.REQUESTPARAMETER.ID);
		model.addAttribute(Define.REQUESTPARAMETER.MENUID, menuId);
		return "statistical/historyStatistical";// 跳转JSP页面
	}

	@RequestMapping("/daylog")
	public String queryDayLog(HttpServletRequest request, Model model) {
		Object menuId = request.getParameter(Define.REQUESTPARAMETER.ID);
		model.addAttribute(Define.REQUESTPARAMETER.MENUID, menuId);
		return "statistical/dayLog";// 跳转JSP页面
	}

	@RequestMapping("/historylog")
	public String queryHistoryLog(HttpServletRequest request, Model model) {
		Object menuId = request.getParameter(Define.REQUESTPARAMETER.ID);
		model.addAttribute(Define.REQUESTPARAMETER.MENUID, menuId);
		return "statistical/historyLog";// 跳转JSP页面
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/statisticaldayinfo")
	@ResponseBody
	public DzdResponse queryStatisticalDayInfo(HttpServletRequest request, HttpServletResponse response,
			@RequestBody Map<String, Object> data) {
		DzdResponse dzdResponse = new DzdResponse();
		try {
			DzdPageParam dzdPageParam = new DzdPageParam();

			// 查询数据入参param
			Map<String, Object> param = preParameter(data, request, dzdPageParam);

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

			// 查询发送信息：账号、数量、通道、归属等信息
			Page<SmsSendLog> statisticalUserInfos = msmSendService.queryStatisticalUserInfo(dzdPageParam);

			if (CollectionUtils.isEmpty(statisticalUserInfos)) {
				return dzdResponse;
			}

			// 补全每个账号发送数量的发送总量、已发送、发送成功、发送失败信息
			for (SmsSendLog SmsSendLog : statisticalUserInfos.getResult()) {
				param.put(Define.REQUESTPARAMETER.SMSUSERID, SmsSendLog.getSmsUserId());
				SmsSendLog.setSendNum(msmSendService.queryStatisticalSendNum(param));
			}
			Map<String, SmsSendLog> statisticalSucceedNum = msmSendService.queryStatisticalSucceedNum(param);
			Map<String, SmsSendLog> statisticalFailureNum = msmSendService.queryStatisticalFailureNum(param);

			// 补全成功失败总和数据信息
			SmsSendLog totalSmsSendLog = fillTotalNum(statisticalUserInfos, statisticalSucceedNum,
					statisticalFailureNum);

			statisticalUserInfos.getResult().add(0, totalSmsSendLog);

			dzdResponse.setData(sysMenuBtns);
			if (!CollectionUtils.isEmpty(statisticalUserInfos)) {
				for (SmsSendLog smsSendLog : statisticalUserInfos.getResult()) {
					smsSendLog.setSysMenuBtns(sysMenuBtns);
				}
				dzdResponse.setRows(statisticalUserInfos.getResult());
				dzdResponse.setTotal(statisticalUserInfos.getTotal());
			}
		} catch (Exception e) {
			logger.error(null, e);
			e.printStackTrace();
		}
		return dzdResponse;
	}

	/**
	 * @param statisticalUserInfos
	 * @param statisticalSucceedNum
	 * @param statisticalFailureNum
	 * @return
	 * @throws @Title:
	 *             fillTotalNum
	 * @Description: 补全成功失败总和数据信息
	 * @author: hz-liang
	 * @return: SmsSendLog
	 */
	private SmsSendLog fillTotalNum(Page<SmsSendLog> statisticalUserInfos,
			Map<String, SmsSendLog> statisticalSucceedNum, Map<String, SmsSendLog> statisticalFailureNum) {
		SmsSendLog smsSendLogNum = null;
		int totalSendNum = 0;
		int totalSucceedNum = 0;
		int totalFailureNum = 0;
		for (SmsSendLog smsSendLog : statisticalUserInfos) {
			smsSendLogNum = statisticalSucceedNum.get(smsSendLog.getSmsUserId());
			smsSendLog.setSucceedNum(
					smsSendLogNum != null && smsSendLog.getSmsUserId().equals(smsSendLogNum.getSmsUserId())
							? smsSendLogNum.getSucceedNum() : 0);

			smsSendLogNum = statisticalFailureNum.get(smsSendLog.getSmsUserId());
			smsSendLog.setFailureNum(
					smsSendLogNum != null && smsSendLog.getSmsUserId().equals(smsSendLogNum.getSmsUserId())
							? smsSendLogNum.getFailureNum() : 0);

			totalSendNum += smsSendLog.getSendNum();
			totalSucceedNum += smsSendLog.getSucceedNum();
			totalFailureNum += smsSendLog.getFailureNum();
		}

		SmsSendLog totalSmsSendLog = new SmsSendLog();
		totalSmsSendLog.setSendNum(totalSendNum);
		totalSmsSendLog.setSucceedNum(totalSucceedNum);
		totalSmsSendLog.setFailureNum(totalFailureNum);
		return totalSmsSendLog;
	}

	/**
	 * @param data
	 * @param dzdPageParam
	 * @return
	 * @throws @Title:
	 *             preParameter
	 * @Description: 查询数据入参param
	 * @author: hz-liang
	 * @return: Map<String,Object>
	 */
	private Map<String, Object> preParameter(Map<String, Object> data, HttpServletRequest request,
			DzdPageParam dzdPageParam) {
		Map<String, Object> param = new HashMap<String, Object>();

		Object smsUserEmail = data.get(Define.REQUESTPARAMETER.EMAIL);
		Object aisleName = data.get(Define.REQUESTPARAMETER.AISLENAME);
		Object nickName = data.get(Define.REQUESTPARAMETER.NICKNAME);

		SysUser user = SessionUtils.getUser(request);
		if (user.getId() != 1) {
			SmsUser smsUser = SmsServerManager.I.getUserBySysId(Long.valueOf(user.getId()));
			String uid = user.getId().toString();
			if (smsUser != null) {
				uid = smsUser.getId().toString();// 用户id
			}
			if (!StringUtil.isEmpty(uid.toString())) {
				param.put(Define.REQUESTPARAMETER.UID, uid);
			}
		}

		if (smsUserEmail != null && !StringUtil.isEmpty(smsUserEmail.toString())) {
			param.put(Define.REQUESTPARAMETER.SMSUSEREMAIL, smsUserEmail);
		}
		if (aisleName != null && !StringUtil.isEmpty(aisleName.toString())) {
			param.put(Define.REQUESTPARAMETER.AISLENAME, aisleName);
		}
		if (nickName != null && !StringUtil.isEmpty(nickName.toString())) {
			param.put(Define.REQUESTPARAMETER.NICKNAME, nickName);
		}

		dzdPageParam.setCondition(param);
		return param;
	}

	/**
	 * copy from MsmSendController make by oygy
	 *
	 * @Description:统计中心--日志统计
	 * @author:oygy
	 * @time:2016年12月31日 下午2:01:34
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	@RequestMapping(value = "/log")
	@ResponseBody
	public void log(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> data)
			throws Exception {
		try {
			SmsSendLog smsSendLog = new SmsSendLog();
			JSONObject jsonObject = new JSONObject();

			WebRequestParameters parameters = getRequestParameters(WebRequestParameters.class, data);
			if (parameters == null) {
				HtmlUtil.writerJson(response, jsonObject);
			}

			// URL入参数据构造
			TempParameter tempParameter = msmSendBusiness.preTemParameter(request, data);

			// 凌晨00:00-05:00不展示前一天数据
			Date date = new Date();// 取时间
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.add(calendar.DATE, -1);
			String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());

			if (Define.REQUESTPARAMETER.HISTORY.equals(tempParameter.getLogTime())
					&& yesterday.equals(parameters.getStartInput())
					&& isInTime("00:00/05:00", new SimpleDateFormat("HH:mm").format(new Date()), 0)) {
				return;
			}

			if (tempParameter.getMenuId() == null) {
				HtmlUtil.writerJson(response, jsonObject);
			}

			// 构建查询入参数据
			msmSendBusiness.preParameter(request, sysRoleRelService, smsAccountService, tempParameter, smsSendLog,
					parameters);

			// 构建页码入参数据
			msmSendBusiness.prePageParameter(tempParameter, smsSendLog);

			// 相关按钮数据
			SysUser user = SessionUtils.getUser(request);
			List<SysMenuBtn> sysMenuBtns = null;
			String menuId = tempParameter.getMenuId().toString();
			if (menuId != null) {
				if (user.getSuperAdmin() == 1) {// 管理员查询所有菜单按钮
					sysMenuBtns = sysMenuBtnService.queryByMenuid(Integer.parseInt(menuId));
				} else {// 其余角色查询配置好的按钮
					sysMenuBtns = sysMenuBtnService.queryMenuListByRole(Integer.parseInt(menuId), user.getId());
				}
			}

			// 查询发送日志信息
			List<SmsSendLog> dataList = userMsmSendService.querySendDetailList(smsSendLog, sysMenuBtns);

			jsonObject.put(Define.RESULTSTATE.BTN, sysMenuBtns);

			if (CollectionUtils.isEmpty(dataList)) {
				HtmlUtil.writerJson(response, jsonObject);
			}

			// 构造出参数据
			msmSendBusiness.constructResult(msmSendService, response, tempParameter.getLogTime(), smsSendLog,
					jsonObject, sysMenuBtns, dataList);

		} catch (Exception e) {
			log.error(null, e);
			e.printStackTrace();
		}

	}

	/**
	 * 今日统计和历史统计查询列表(补注释：CHENCHAO)
	 * <p>
	 * copy from MsmSendController make by oygy
	 *
	 * @Description:
	 * @author:oygy
	 * @time:2016年12月31日 下午2:01:34
	 */
	@SuppressWarnings({ "unchecked" })
	@RequestMapping(value = "/statisticalList", method = RequestMethod.POST)
	@ResponseBody
	public DzdResponse puserStatisticalList(HttpServletRequest request, HttpServletResponse response,
			@RequestBody Map<String, Object> data) throws Exception {
		DzdResponse dzdPageResponse = new DzdResponse();
		try {
			WebRequestParameters parameters = getRequestParameters(WebRequestParameters.class, data);
			if (parameters == null) {
				return dzdPageResponse;
			}

			DzdPageParam dzdPageParam = new DzdPageParam();

			Object menuId = request.getParameter(Define.REQUESTPARAMETER.MENUID);
			Object logTime = request.getParameter(Define.REQUESTPARAMETER.LOGTIME);
			Object email = data.get(Define.REQUESTPARAMETER.EMAIL);
			Object aisleName = data.get(Define.REQUESTPARAMETER.AISLENAME);
			Object date_export = data.get(Define.REQUESTPARAMETER.DATE_EXPORT);
			Object nickName = data.get(Define.REQUESTPARAMETER.NICKNAME);

			if (menuId == null) {
				return dzdPageResponse;
			}

			Map<String, Object> sortMap = new HashMap<String, Object>();
			if (parameters.getPagenum() != 0 && parameters.getPagesize() != 0) {
				dzdPageParam.setStart(parameters.getPagenum());
				dzdPageParam.setLimit(parameters.getPagesize());
			}

			if (!StringUtil.isEmpty(parameters.getStartInput())) {
				setInputDate(parameters, logTime, sortMap);
			}

			if (email != null && !StringUtil.isEmpty(email.toString())) {
				sortMap.put(Define.REQUESTPARAMETER.EMAIL, email.toString());
			}

			if (logTime != null && !StringUtil.isEmpty(logTime.toString())) {
				sortMap.put(Define.REQUESTPARAMETER.LOGTIME, logTime.toString());
			}

			if (aisleName != null && !StringUtil.isEmpty(aisleName.toString())) {
				sortMap.put(Define.MANAGEMENTUSER.AISLEGROUP, aisleName.toString());
			}
			if (nickName != null && !StringUtil.isEmpty(nickName.toString())) {
				sortMap.put(Define.REQUESTPARAMETER.NICKNAME, nickName.toString());
			}

			SysUser sysUser = SessionUtils.getUser(request);

			dzdPageParam = CommonRoleServiceUtil.getSysUserId(sysUser, dzdPageParam, sortMap, sysRoleRelService,
					smsAccountService);

			sortMap.put(Define.REQUESTPARAMETER.SYSUSERIDS, sortMap.get(Define.REQUESTPARAMETER.SMSUSERVAL));

			SysUser user = SessionUtils.getUser (request);//SysUser) request.getSession().getAttribute(Define.REQUESTPARAMETER.SESSION_USER);
			if (user.getSuperAdmin() != 1) {
				com.dzd.sms.service.data.SmsUser smsUserDate = SmsServerManager.I
						.getUserBySysId(Long.valueOf(user.getId()));
				String uid = user.getId().toString();
				sortMap.put(Define.STATICAL.SID, uid);
				if (smsUserDate != null) {
					uid = smsUserDate.getId().toString();// 用户id
				}
				if (!StringUtil.isEmpty(uid)) {
					sortMap.put(Define.REQUESTPARAMETER.UID, uid);
				}
			}

			/*// 排序
			if (parameters.getSort() != null && parameters.getOrder() != null) {
				sortMap.put(Define.REQUESTPARAMETER.SORTVAL,
						"order by " + parameters.getSort() + " " + parameters.getOrder());
			}*/

			dzdPageParam.setCondition(sortMap);

			List<SysMenuBtn> sysMenuBtns = null;
			if (menuId != null) {
				if (user.getSuperAdmin() == 1) {// 管理员查询所有菜单按钮
					sysMenuBtns = sysMenuBtnService.queryByMenuid(Integer.parseInt(menuId.toString()));
				} else {// 其余角色查询配置好的按钮
					sysMenuBtns = sysMenuBtnService.queryMenuListByRole(Integer.parseInt(menuId.toString()), user.getId());
				}
			}
			dzdPageResponse.setData(sysMenuBtns);

			// 查询代理统计信息列表
			Page<com.dzd.phonebook.util.SmsUser> dataList = smsUserService.querySmsUserLogStatisticalListPage(sysMenuBtns, dzdPageParam);

			if (CollectionUtils.isEmpty(dataList)) {
				return dzdPageResponse;
			}

			// 查询列表头信息
			com.dzd.phonebook.util.SmsUser smsUser = smsUserService.querySmsUserLogStatisticalZong(dzdPageParam);

			/*ComparatorSmsUser.comparatorListBySucceedNum(dataList);
			ComparatorSmsUser.comparatorListByAuditTime(dataList);*/

			if (dataList.size() > 0) {
				dataList.add(0, smsUser);
			}

			if (!CollectionUtils.isEmpty(dataList)) {
				for (com.dzd.phonebook.util.SmsUser instruct : dataList.getResult()) {
					instruct.setSysMenuBtns(sysMenuBtns);
				}
				dzdPageResponse.setRows(dataList.getResult());
				dzdPageResponse.setTotal(dataList.getTotal());

				if (Define.REQUESTPARAMETER.DATE_EXPORT.equals(date_export)) {
					this.setDataList(dataList);
				}
			}
		} catch (Exception e) {
			log.error(null, e);
			e.printStackTrace();
		}
		return dzdPageResponse;
	}

	@SuppressWarnings("static-access")
	private void setInputDate(WebRequestParameters parameters, Object logTime, Map<String, Object> sortMap)
			throws ParseException {
		parameters.setStartInput(parameters.getStartInput().replace(" ", ""));
		parameters.setEndInput(parameters.getEndInput().replace(" ", ""));
		// 凌晨00:00-05:00不展示前一天数据
		Date date = new Date();// 取时间
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(calendar.DATE, -1);
		String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());

		StringBuffer strbf = new StringBuffer();
		strbf.append(parameters.getStartInput()).append("/").append(parameters.getEndInput());
		if (Define.REQUESTPARAMETER.HISTORY.equals(logTime.toString()) && isInTime(strbf.toString(), yesterday, 1)
				&& isInTime("00:00/05:00", new SimpleDateFormat("HH:mm").format(new Date()), 0)) {
			if (yesterday.compareTo(parameters.getEndInput()) == 0
					&& yesterday.compareTo(parameters.getStartInput()) == 0) {
				Calendar eqCalendar = new GregorianCalendar();
				eqCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(parameters.getEndInput()));
				eqCalendar.add(calendar.MONTH, 1);
				sortMap.put(Define.REQUESTPARAMETER.ENDINPUT,
						new SimpleDateFormat("yyyy-MM-dd").format(eqCalendar.getTime()));
				sortMap.put(Define.REQUESTPARAMETER.STARTINPUT,
						new SimpleDateFormat("yyyy-MM-dd").format(eqCalendar.getTime()));
			} else if (yesterday.compareTo(parameters.getEndInput()) <= 0) {
				Calendar endCalendar = new GregorianCalendar();
				endCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(parameters.getEndInput()));
				endCalendar.add(calendar.DATE, -1);
				sortMap.put(Define.REQUESTPARAMETER.ENDINPUT,
						new SimpleDateFormat("yyyy-MM-dd").format(endCalendar.getTime()));
				sortMap.put(Define.REQUESTPARAMETER.STARTINPUT, parameters.getStartInput());
			}
		} else {
			sortMap.put(Define.REQUESTPARAMETER.STARTINPUT, parameters.getStartInput());
			sortMap.put(Define.REQUESTPARAMETER.ENDINPUT, parameters.getEndInput());
		}
	}

	/**
	 * 
	 * @Title: isInTime @Description: 比较日期时间大小 时间格式：sourceTime = "00:00/05:00"
	 * curTime = "00:20" 日期格式：sourceTime = "2017-07-11/2017-07-12" curTime =
	 * "2017-07-11" @author: hz-liang @param sourceTime @param curTime @param
	 * timeType @return @return: boolean @throws
	 */
	public static boolean isInTime(String sourceTime, String curTime, int timeType) {
		try {
			SimpleDateFormat sdf = null;
			String[] args = sourceTime.split("/");

			if (0 == timeType) {
				sdf = new SimpleDateFormat("HH:mm");
				if (args[1].equals("00:00")) {
					args[1] = "24:00";
				}
			}
			if (1 == timeType) {
				sdf = new SimpleDateFormat("yyyy-MM-dd");
			}

			if (sdf == null) {
				log.info("timeType is unCheck ： timeType = " + timeType);
				return false;
			}

			long now = sdf.parse(curTime).getTime();
			long start = sdf.parse(args[0]).getTime();
			long end = sdf.parse(args[1]).getTime();
			if (end < start) {
				if (now >= end && now <= start) {
					return false;
				} else {
					return true;
				}
			} else {
				if (now >= start && now <= end) {
					return true;
				} else {
					return false;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Illegal Argument arg:" + sourceTime);
		}
	}

	/**
	 * @param request
	 * @param response
	 * @throws Exception
	 * @throws @Title:
	 *             orderExport
	 * @Description: 查询导出数据
	 * @author: hz-liang
	 * @return: void
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/queryOrderExport")
	@ResponseBody
	public DzdResponse queryOrderExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
		DzdResponse dzdPageResponse = new DzdResponse();
		try {
			SmsSendLog smsSendLog = preParameterForSmsSendLog(request);

			// 查询发送日志信息
			List<SmsSendLog> dataList = msmSendService.queryByList(smsSendLog);

			if (CollectionUtils.isEmpty(dataList)) {
				dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
				return dzdPageResponse;
			}

			this.setSmsSendLogDataList(dataList);

			dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);

		} catch (Exception e) {
			log.error(null, e);
			e.printStackTrace();
		}
		return dzdPageResponse;
	}

	private SmsSendLog preParameterForSmsSendLog(HttpServletRequest request) {
		SmsSendLog smsSendLog = new SmsSendLog();

		smsSendLog.setNeedPage("true");
		if (!StringUtils.isEmpty(request.getParameter(Define.REQUESTPARAMETER.STARTINPUT))) {
			smsSendLog.setStartInput(request.getParameter(Define.REQUESTPARAMETER.STARTINPUT) + " 00:00");
		}
		if (!StringUtils.isEmpty(request.getParameter(Define.REQUESTPARAMETER.ENDINPUT))) {
			smsSendLog.setEndInput(request.getParameter(Define.REQUESTPARAMETER.ENDINPUT) + " 23:59");
		}
		if (!StringUtils.isEmpty(request.getParameter(Define.STATICAL.STATE))) {
			if ("2".equals(request.getParameter(Define.STATICAL.STATE))) { // 如果发送状态为发送失败则区间查询
				smsSendLog.setStateBs(1);
			} else if ("100".equals(request.getParameter(Define.STATICAL.STATE))) {
				smsSendLog.setType(1);
			} else {
				smsSendLog.setState(Integer.parseInt(request.getParameter(Define.STATICAL.STATE)));
			}
		}
		if (!StringUtils.isEmpty(request.getParameter(Define.STATICAL.CONTENT))) {
			smsSendLog.setContent(request.getParameter(Define.STATICAL.CONTENT));
		}
		if (!StringUtils.isEmpty(request.getParameter(Define.REQUESTPARAMETER.SMSUSER))) {
			smsSendLog.setSmsUserName(request.getParameter(Define.REQUESTPARAMETER.SMSUSER));
		}
		if (!StringUtils.isEmpty(request.getParameter(Define.REQUESTPARAMETER.PHONE))) {
			smsSendLog.setReceivePhone(request.getParameter(Define.REQUESTPARAMETER.PHONE));
		}
		if (Define.REQUESTPARAMETER.TODAY.equals(request.getParameter(Define.REQUESTPARAMETER.LOGTIME))) {
			smsSendLog.setLogTime(request.getParameter(Define.REQUESTPARAMETER.LOGTIME));
		}
		return smsSendLog;
	}

	@RequestMapping(value = "/orderExport")
	@ResponseBody
	public void orderExport(HttpServletRequest request, HttpServletResponse response) {
		List<SmsSendLog> dataList = this.getSmsSendLogDataList();

		SmsOrderExportService orderExportService = new SmsOrderExportService();
		orderExportService.logrOrderExport(request, response, dataList);
	}

}
