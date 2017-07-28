package com.dzd.phonebook.business;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;

import com.dzd.base.page.Pager;
import com.dzd.base.util.HtmlUtil;
import com.dzd.base.util.SessionUtils;
import com.dzd.base.util.StringUtil;
import com.dzd.phonebook.entity.SysMenuBtn;
import com.dzd.phonebook.entity.SysUser;
import com.dzd.phonebook.service.CommonRoleServiceUtil;
import com.dzd.phonebook.service.MsmSendService;
import com.dzd.phonebook.service.SmsAccountService;
import com.dzd.phonebook.service.SysRoleRelService;
import com.dzd.phonebook.util.DzdPageParam;
import com.dzd.phonebook.util.SmsSendLog;
import com.dzd.phonebook.util.TempParameter;
import com.dzd.phonebook.util.WebRequestParameters;
import com.dzd.sms.application.Define;
import com.dzd.sms.application.SmsServerManager;
import com.dzd.sms.service.data.SmsUser;
import com.github.pagehelper.Page;

/**
 * *
 * 
 * @author 作者 E-mail: *
 * @date 创建时间：2017年6月15日 上午10:11:20 *
 * @version 1.0 *
 * @parameter *
 * @since *
 * @return
 */
public class MsmSendBusiness {
	/**
	 * 
	 * @Title: preTemParameter @Description: URL入参数据构造 @author: hz-liang @param
	 * request @param data @return @return: TempParameter @throws
	 */
	public TempParameter preTemParameter(HttpServletRequest request, Map<String, Object> data) {
		TempParameter tempParameter = new TempParameter();
		tempParameter.setMenuId(request.getParameter(Define.REQUESTPARAMETER.MENUID));
		tempParameter.setOffset(request.getParameter(Define.REQUESTPARAMETER.OFFSET));
		tempParameter.setLogTime(request.getParameter(Define.REQUESTPARAMETER.LOGTIME));
		tempParameter.setExport(Boolean.parseBoolean(request.getParameter(Define.REQUESTPARAMETER.EXPORT)));
		if (!CollectionUtils.isEmpty(data)) {
			tempParameter.setPagenum(data.get(Define.REQUESTPARAMETER.PAGENUM));
			tempParameter.setPagesize(data.get(Define.REQUESTPARAMETER.PAGESIZE));
			tempParameter.setContent(data.get(Define.STATICAL.CONTENT));
			tempParameter.setState(data.get(Define.STATICAL.TYPE));
			tempParameter.setIds(data.get(Define.REQUESTPARAMETER.IDS));
			tempParameter.setSmsUser(data.get(Define.REQUESTPARAMETER.SMSUSER));
			tempParameter.setPhone(data.get(Define.REQUESTPARAMETER.PHONE));
			tempParameter.setBgztSelect(data.get(Define.REQUESTPARAMETER.BGZTSELECT));
		}

		SysUser user = SessionUtils.getUser(request);
		if (user.getSuperAdmin() != 1) {
			SmsUser smsUser = SmsServerManager.I.getUserBySysId(Long.valueOf(user.getId()));
			String uid = user.getId().toString();
			tempParameter.setSid(uid);
			if (smsUser != null) {
				uid = smsUser.getId().toString();// 用户id
			}
			tempParameter.setUid(uid);
		}

		return tempParameter;
	}

	/**
	 * 
	 * @Title: preParameter @Description: 构建查询入参数据 @author: hz-liang @param
	 * request @param tempParameter @param smsSendLog @param parameters @return:
	 * void @throws
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void preParameter(HttpServletRequest request, SysRoleRelService sysRoleRelService,
			SmsAccountService smsAccountService, TempParameter tempParameter, SmsSendLog smsSendLog,
			WebRequestParameters parameters) {
		DzdPageParam dzdPageParam = new DzdPageParam();
		SysUser sysUser = SessionUtils.getUser(request);
		Map<String, Object> sortMap = new HashMap<String, Object>();

		dzdPageParam = CommonRoleServiceUtil.getSysUserId(sysUser, dzdPageParam, sortMap, sysRoleRelService,
				smsAccountService);

		if (sortMap.get("smsUserVal") != null) {
			smsSendLog.setSysUserIds((List<Integer>) sortMap.get("smsUserVal"));
		}

		if (parameters != null && !StringUtil.isEmpty(parameters.getStartInput())) {
			smsSendLog.setStartInput(parameters.getStartInput() + " 00:00");
			smsSendLog.setEndInput(parameters.getEndInput() + " 23:59");
		}

		if (tempParameter.getState() != null && !StringUtil.isEmpty(tempParameter.getState().toString())) {
			smsSendLog.setType(Integer.parseInt(tempParameter.getState().toString()));
		}

		if (tempParameter.getContent() != null && !StringUtil.isEmpty(tempParameter.getContent().toString())) {
			smsSendLog.setContent(tempParameter.getContent().toString());
		}

		if (tempParameter.getIds() != null && !StringUtil.isEmpty(tempParameter.getIds().toString())) {
			smsSendLog.setId(Integer.parseInt(tempParameter.getIds().toString()));
		}

		if (tempParameter.getSmsUser() != null && !StringUtil.isEmpty(tempParameter.getSmsUser().toString())) {
			smsSendLog.setSmsUserEmail(tempParameter.getSmsUser().toString());
		}

		if (tempParameter.getPhone() != null && !StringUtil.isEmpty(tempParameter.getPhone().toString())) {
			smsSendLog.setReceivePhone(tempParameter.getPhone().toString());
		}

		if (tempParameter.getBgztSelect() != null && !StringUtil.isEmpty(tempParameter.getBgztSelect().toString())) {
			// 0:查询转态报告已经返回的,1:查询转态报告没有返回的
			smsSendLog.setBgztSelect(Integer.parseInt(tempParameter.getBgztSelect().toString()));
		}

		if (!StringUtils.isEmpty(tempParameter.getLogTime())) {
			smsSendLog.setLogTime(tempParameter.getLogTime());
		}

		if (!StringUtils.isEmpty(tempParameter.getUid())) {
			smsSendLog.setSmsUserId(tempParameter.getUid());
		}
	}

	/**
	 * 
	 * @Title: prePageParameter @Description: 构建页码入参数据 @author: hz-liang @param
	 * tempParameter @param smsSendLog @return: void @throws
	 */
	public void prePageParameter(TempParameter tempParameter, SmsSendLog smsSendLog) {
		Pager pager = new Pager();
		if (tempParameter.getPagenum() != null && !tempParameter.getPagenum().equals("")) {
			Integer pagenums = 0;
			if (tempParameter.getPagesize() != null && !tempParameter.getPagesize().equals("")) {
				pagenums = (Integer.parseInt(tempParameter.getPagenum().toString()) - 1)
						* Integer.parseInt(tempParameter.getPagesize().toString());
			}

			pager.setPageOffset(pagenums);
		}
		if (tempParameter.getPagesize() != null && !tempParameter.getPagesize().equals("")) {
			smsSendLog.setRows(Integer.parseInt(tempParameter.getPagesize().toString()));
		}
		// role.setPager(pager);
		smsSendLog.setPager(pager);
	}

	/**
	 * 
	 * @Title: constructResult @Description: 构造出参数据 @author: hz-liang @param
	 * response @param logTime @param smsSendLog @param jsonObject @param
	 * sysMenuBtns @param dataList @return: void @throws
	 */
	@SuppressWarnings("rawtypes")
	public void constructResult(MsmSendService msmSendService, HttpServletResponse response, String logTime,
			SmsSendLog smsSendLog, JSONObject jsonObject, List<SysMenuBtn> sysMenuBtns, List<SmsSendLog> dataList) {
		// SmsSendLog smsSedNum = msmSendService.querySumSendNum(smsSendLog);

		// if (dataList.size() > 0) {
		// dataList.get(0).setBillingNnum(smsSedNum.getBillingNnum());// 提交短信条数
		// dataList.get(0).setSendingNum(smsSedNum.getSendingNum());// 发送中
		// dataList.get(0).setSendFinishNum(smsSedNum.getSendFinishNum());//
		// 发送完成
		//
		// dataList.get(0).setSucceedNum(smsSedNum.getSucceedNum());// 发送成功
		// dataList.get(0).setFailureNum(smsSedNum.getFailureNum());// 发送失败
		// }

		if (!CollectionUtils.isEmpty(dataList)) {
			for (SmsSendLog sysRole : dataList) {
				sysRole.setSysMenuBtns(sysMenuBtns);
			}
			// 设置页面数据
			jsonObject.put(Define.RESULTSTATE.TOTAL, smsSendLog.getPager().getRowCount());
			jsonObject.put(Define.RESULTSTATE.ROWS, dataList);
			HtmlUtil.writerJson(response, jsonObject);
		}
	}

	public void setTotalNumMap(Map<String, Integer> totalNumMap, com.dzd.phonebook.util.SmsUser smsUserInfo) {
		totalNumMap.put(Define.STATICAL.SENDNUM,
				setDefultValue(totalNumMap.get(Define.STATICAL.SENDNUM)) + smsUserInfo.getSendNum());
		totalNumMap.put(Define.STATICAL.SUCCEEDNUM,
				setDefultValue(totalNumMap.get(Define.STATICAL.SUCCEEDNUM)) + smsUserInfo.getSucceedNum());
		totalNumMap.put(Define.STATICAL.FAILURENUM,
				setDefultValue(totalNumMap.get(Define.STATICAL.FAILURENUM)) + smsUserInfo.getFailureNum());
		totalNumMap.put(Define.STATICAL.SUMSUCCEEDNUMUS,
				setDefultValue(totalNumMap.get(Define.STATICAL.SUMSUCCEEDNUMUS)) + smsUserInfo.getSucceedNumUs());
		totalNumMap.put(Define.STATICAL.SUMSUCCEEDNUMMS,
				setDefultValue(totalNumMap.get(Define.STATICAL.SUMSUCCEEDNUMMS)) + smsUserInfo.getSucceedNumMs());
		totalNumMap.put(Define.STATICAL.SUMSUCCEEDNUMTS,
				setDefultValue(totalNumMap.get(Define.STATICAL.SUMSUCCEEDNUMTS)) + smsUserInfo.getSucceedNumTs());
		totalNumMap.put(Define.STATICAL.UNKNOWNSUCCEEDNUM,
				setDefultValue(totalNumMap.get(Define.STATICAL.UNKNOWNSUCCEEDNUM))
						+ smsUserInfo.getUnknownSucceedNum());
		totalNumMap.put(Define.STATICAL.SUMFAILURENUMUS,
				setDefultValue(totalNumMap.get(Define.STATICAL.SUMFAILURENUMUS)) + smsUserInfo.getFailureNumUs());
		totalNumMap.put(Define.STATICAL.SUMFAILURENUMMS,
				setDefultValue(totalNumMap.get(Define.STATICAL.SUMFAILURENUMMS)) + smsUserInfo.getFailureNumMs());
		totalNumMap.put(Define.STATICAL.SUMFAILURENUMTS,
				setDefultValue(totalNumMap.get(Define.STATICAL.SUMFAILURENUMTS)) + smsUserInfo.getFailureNumTs());
		totalNumMap.put(Define.STATICAL.UNKNOWNFAILURENUM,
				setDefultValue(totalNumMap.get(Define.STATICAL.UNKNOWNFAILURENUM))
						+ smsUserInfo.getUnknownFailureNum());
	}

	private int setDefultValue(Object obj) {
		obj = obj == null ? 0 : obj;
		return Integer.parseInt(obj.toString());
	}

	public void setTotalSmsUser(Page<com.dzd.phonebook.util.SmsUser> dataList, Map<String, Integer> totalNumMap) {
		com.dzd.phonebook.util.SmsUser totalSmsUser = new com.dzd.phonebook.util.SmsUser();
		totalSmsUser.setSendNum(totalNumMap.get(Define.STATICAL.SENDNUM).intValue());
		totalSmsUser.setSucceedNum(totalNumMap.get(Define.STATICAL.SUCCEEDNUM).intValue());
		totalSmsUser.setFailureNum(totalNumMap.get(Define.STATICAL.FAILURENUM).intValue());
		totalSmsUser.setSucceedNumUs(totalNumMap.get(Define.STATICAL.SUMSUCCEEDNUMUS).intValue());
		totalSmsUser.setSucceedNumMs(totalNumMap.get(Define.STATICAL.SUMSUCCEEDNUMMS).intValue());
		totalSmsUser.setSucceedNumTs(totalNumMap.get(Define.STATICAL.SUMSUCCEEDNUMTS).intValue());
		totalSmsUser.setUnknownSucceedNum(totalNumMap.get(Define.STATICAL.UNKNOWNSUCCEEDNUM).intValue());
		totalSmsUser.setFailureNumUs(totalNumMap.get(Define.STATICAL.SUMFAILURENUMUS).intValue());
		totalSmsUser.setFailureNumMs(totalNumMap.get(Define.STATICAL.SUMFAILURENUMMS).intValue());
		totalSmsUser.setFailureNumTs(totalNumMap.get(Define.STATICAL.SUMFAILURENUMTS).intValue());
		totalSmsUser.setUnknownFailureNum(totalNumMap.get(Define.STATICAL.UNKNOWNFAILURENUM));
		totalSmsUser.setAuditTime(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

		dataList.getResult().add(0, totalSmsUser);
	}

}
