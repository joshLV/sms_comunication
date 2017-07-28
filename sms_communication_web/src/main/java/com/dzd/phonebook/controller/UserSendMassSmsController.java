
package com.dzd.phonebook.controller;

import com.dzd.base.page.Pager;
import com.dzd.base.util.DateUtil;
import com.dzd.base.util.HtmlUtil;
import com.dzd.base.util.SessionUtils;
import com.dzd.base.util.StringUtil;
import com.dzd.phonebook.controller.base.WebBaseController;
import com.dzd.phonebook.entity.SmsFileConfig;
import com.dzd.phonebook.entity.SmsReceiveReplyPush;
import com.dzd.phonebook.entity.SysMenuBtn;
import com.dzd.phonebook.entity.SysUser;
import com.dzd.phonebook.service.*;
import com.dzd.phonebook.util.*;
import com.dzd.sms.application.Define;
import com.dzd.sms.application.SmsServerManager;
import com.dzd.sms.service.data.SmsRequestParameter;
import com.dzd.utils.LogUtil;
import com.github.pagehelper.Page;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户群发短信Controller
 *
 * @Author CHENCHAO
 * @Date 2017-03-25 15:38:00
 */
@Controller
@RequestMapping("/smsUser")
public class UserSendMassSmsController extends WebBaseController {
	static LogUtil logger = LogUtil.getLogger(UserSendMassSmsController.class);
	private UserSendMassSmsService sendMassSmsService = new UserSendMassSmsService();
	@SuppressWarnings("rawtypes")
	@Autowired
	private UserMsmSendService userMsmSendService;
	@Autowired
	private SysMenuBtnService sysMenuBtnService;
	@SuppressWarnings("rawtypes")
	@Autowired
	private MsmSendService msmSendService;
	@Autowired
	private SmsFileConfigService<SmsFileConfig> smsFileConfigService;
	@Autowired
	private SmsUserBlankService smsUserBlankService;
	@SuppressWarnings("rawtypes")
	@Autowired
	private SmsUserMessageService smsUserMessageService;
	@Autowired
	private SmsUserService smsUserService;
	@SuppressWarnings("rawtypes")
	@Autowired
	private UserMessageService userMessageService;
	@Autowired
	private SmsSpecificSymbolService smsSpecificSymbolService;
	@Autowired
	private ChannelService channelService;
	@Autowired
	private SmsWordShieldingService smsWordShieldingService;
	@Autowired
	private SmsAccountService smsAccountService;
	@Autowired
	private SysRoleRelService sysRoleRelService;

	/**
	 * 发送短信页面
	 *
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping("/sendView")
	public String list(HttpServletRequest request, Model model) {
		SysUser user = SessionUtils.getUser(request);
		SmsUser smsUsers = userMessageService.querySmsUserById(user.getId());
		Integer signType = smsUsers.getSignatureType();// 0:绑定签名;1:自定义签名
		Integer surplusNum = SmsServerManager.I.getUserSurplusNum(Long.parseLong(smsUsers.getId().toString()));// redis缓存获取账号余额信息

		// 查询通道
		SmsAisleGroup smsAisleGroup = channelService.querySmsAisleGroupById(smsUsers.getAisleGroupId());
		model.addAttribute("phone", smsUsers.getPhone());
		model.addAttribute("signType", signType);
		model.addAttribute("surplusNum", surplusNum);
		model.addAttribute("forwardUuid", request.getParameter("forwardUuid"));
		model.addAttribute("operators", request.getParameter("operators"));
		model.addAttribute("smsAisleGroup", smsAisleGroup);
		return "app/userSendMassSms/send";

	}

	/**
	 * 发送记录页面
	 *
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping("/sendListview")
	public String sendlist(HttpServletRequest request, Model model) {
		Object menuId = request.getParameter("id");
		SysUser user = SessionUtils.getUser(request);
		SmsUser smsUsers = userMessageService.querySmsUserById(user.getId());
		List<SmsUser> buserList = smsUserService.querySmsBUserById(smsUsers.getId());

		if (request.getParameter("account") != null) {
			model.addAttribute("account",
					SmsServerManager.I.getUser(Long.valueOf(request.getParameter("account"))).getAccount());
			model.addAttribute("back", request.getParameter("back"));
		}
		Object data = request.getParameter("data");
		if (data != null) {
			JSONObject jsonStr = JSONObject.fromObject(data);
			Object id = jsonStr.get("id");
			Object account = jsonStr.get("account"); // 账号
			Object startInput = jsonStr.get("startInput"); // 开始时间
			Object endInput = jsonStr.get("endInput"); // 结束时间
			Object sendType = jsonStr.get("sendType"); // 定时任务
			Object groupId = jsonStr.get("groupId"); // 通道类型id
			menuId = jsonStr.get("menuId");
			model.addAttribute("id", id);
			model.addAttribute("account", account);
			model.addAttribute("startInput", startInput);
			model.addAttribute("endInput", endInput);
			model.addAttribute("sendType", sendType);
			model.addAttribute("groupId", groupId);
			model.addAttribute("buserList", buserList);
		}
		model.addAttribute("menuId", menuId);
		return "app/userSendMassSms/fstask";
	}

	/**
	 * 发送详情页面
	 *
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping("/sendDetailsView")
	public String smsSendDetails(HttpServletRequest request, Model model) {
		Object data = request.getParameter("data");
		JSONObject jsonStr = JSONObject.fromObject(data);
		Object id = jsonStr.get("id");
		Object menuId = jsonStr.get("menuId");
		Object account = jsonStr.get("account"); // 账号
		Object startInput = jsonStr.get("startInput"); // 开始时间
		Object endInput = jsonStr.get("endInput"); // 结束时间
		Object sendType = jsonStr.get("sendType"); // 定时任务
		Object groupId = jsonStr.get("groupId"); // 通道类型id

		model.addAttribute("id", id);
		model.addAttribute("menuId", menuId);
		model.addAttribute("account", account);
		model.addAttribute("startInput", startInput);
		model.addAttribute("endInput", endInput);
		model.addAttribute("sendType", sendType);
		model.addAttribute("groupId", groupId);

		return "app/userSendMassSms/fsxq";
	}

	/**
	 * 短信回复页面
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/pushSmsListview")
	public String loading() throws Exception {
		return "app/userSendMassSms/dxhf";
	}

	/**
	 * 手动添加号码,入库并过滤
	 *
	 * @return
	 */
	@RequestMapping("/handAddPhone")
	@ResponseBody
	public DzdResponse addPhone(HttpServletRequest request) {
		DzdResponse dzdResponse = new DzdResponse();
		try {
			// 1. 基本参数
			Object phone = request.getParameter("phone");
			if (phone == null) {
				dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
				return dzdResponse;
			}
			String uuid = request.getParameter("uuid");
			SysUser user = SessionUtils.getUser(request);

			// 2. 输入号码保存文件，同时入库
			JSONObject json = PhoneFilterUtil.headPhoneFilter(phone.toString(), uuid, user, smsFileConfigService);

			dzdResponse.setData(json);
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
		} catch (Exception e) {
			e.printStackTrace();
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
		}
		return dzdResponse;
	}

	/**
	 * 上传号码文件入库并过滤
	 *
	 * @param uploadFile
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/fileUpload")
	@ResponseBody
	public DzdResponse fileUpload(@RequestParam final MultipartFile[] uploadFile, final HttpServletRequest request,
			HttpServletResponse response) {
		DzdResponse dzdResponse = new DzdResponse();

		try {
			// 1. 获取文件
			String fName = uploadFile[0].getOriginalFilename();

			if (uploadFile[0].getSize() > Integer.MAX_VALUE || fName == null || fName.equals("")) {
				dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
				return dzdResponse;
			}

			// 2. 获取文件后缀
			String suffix = fName.substring(fName.lastIndexOf("."));
			final SysUser user = SessionUtils.getUser(request);
			final String uuid = request.getParameter("uuid");

			// 3. 判断文件格式
			if (suffix.equals(".txt") || suffix.equals(".xlsx") || suffix.equals(".xls")) {
				// 4. 上传文件到服务器
				String fileName = FileUploadUtil.saveFile(uploadFile);

				// 5. 读取文件中的号码,进行分类得到个数
				Map<String, Object> map = FileUploadUtil.getSmsPhoneCategoryMap(uploadFile, request);
				String phone = map.get("phoneList").toString();
				Integer validPhoneNum = (Integer) map.get("validPhoneNum");

				// 6. 保存文件信息到数据库
				SmsFileConfig config = new SmsFileConfig();
				config.setSms_uid(user.getId());
				config.setUuid(uuid);
				config.setFileName(fileName);
				config.setType(1);
				config.setPhone(phone);
				config.setPhoneSize(validPhoneNum);
				smsFileConfigService.add(config);

				JSONObject json = new JSONObject();
				json.putAll(map);

				dzdResponse.setData(json);
				dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
			} else {

				// 7. 文件格式不正确提示
				Map<String, String> map = new HashMap<String, String>();
				map.put("msg", "请选择txt文件或者excel文件");
				dzdResponse.setData(map);
				dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
			logger.error("====================》上传文件发生了异常：" + e.getMessage());
		}
		return dzdResponse;
	}

	/**
	 * 过滤号码
	 *
	 * @return
	 * @deprecated: 排错、重复、有效号码
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "phoneVerify", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public DzdResponse phoneVerify(HttpServletRequest request, HttpServletResponse response,
			@RequestBody Map<String, Object> data) {
		DzdResponse dzdResponse = new DzdResponse();
		try {
			// 1.读取服务器的文件,得到所有号码
			String uuid = data.get("uuid").toString();
			String operators = data.get("operators") == null ? "" : data.get("operators").toString();

			List<SmsFileConfig> configList = smsFileConfigService.querySmsFileConfigList(uuid, null);
			if (configList == null || configList.size() == 0) {
				dzdResponse.setRetMsg("无可发号码!");
				dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
				return dzdResponse;
			}

			List<String> phoneList = SmsSendUtil.getPhoneListByConfig(configList);

			// 2.计算需要发送的有效号码的短信条数
			Map<String, Object> map = FileUploadUtil.getPhoneMap(phoneList, operators);
			List<String> validPhoneList = (List<String>) map.get("validList");

			// 3.保存号码文件
			String fileName = uuid + ".txt";
			FileUploadUtil.writerFile(validPhoneList, fileName);

			// 4.保存到json
			JSONObject json = new JSONObject();
			json.putAll(map);
			dzdResponse.setData(json);
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
		} catch (Exception e) {
			e.printStackTrace();
			dzdResponse.setRetMsg("系统异常,请稍候再试!");
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
		}
		return dzdResponse;
	}

	/**
	 * 发送短信前的校验
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping("checkSendBeforeVerify")
	@ResponseBody
	public DzdResponse sendBeforeVerify(HttpServletRequest request, @RequestBody Map<String, Object> data) {
		DzdResponse dzdResponse = new DzdResponse();
		try {
			// 1. 参数设置
			String uuid = data.get("uuid").toString();
			List<SmsFileConfig> configList = smsFileConfigService.querySmsFileConfigList(uuid, null);
			if (configList == null || configList.size() == 0) {
				JSONObject json = new JSONObject();
				json.put("smsFlag", false);
				json.put("code", -200);
				json.put("msg", "手机号码不能为空!");
				dzdResponse.setData(json);
				dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
				return dzdResponse;
			}

			SmsRequestParameter parmeter = SendSmsRequestParameterUtil.sendSmsRequestParameter(data, request,
					userMessageService, channelService, smsFileConfigService);

			// 2. 服务类实体设置
			ServiceBeanUtil serviceBean = new ServiceBeanUtil();
			serviceBean.setChannelService(channelService);

			// 3.校验发送，并返回结果
			JSONObject json = sendMassSmsService.sendBeforVerify(parmeter, serviceBean);
			dzdResponse.setData(json);
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
		} catch (Exception e) {
			e.printStackTrace();
			JSONObject json = new JSONObject();
			json.put("msg", Define.USERSENDSMS.SMS_SEND_SYSTEM_ERROR);
			json.put("code", "-1000");
			json.put("phoneMap", "");
			dzdResponse.setData(json);
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
			e.printStackTrace();
		}
		return dzdResponse;

	}

	/**
	 * 发送短信
	 *
	 * @param request
	 * @param data
	 * @return
	 */
	@RequestMapping(value = "/send", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public DzdResponse smsGroupSend(HttpServletRequest request, @RequestBody Map<String, Object> data) {
		DzdResponse dzdResponse = new DzdResponse();
		try {
			// 1. 参数设置
			SysUser user = SessionUtils.getUser(request);
			SmsUser smsUsers = userMessageService.querySmsUserById(user.getId());
			SmsRequestParameter parmeter = SendSmsRequestParameterUtil.sendSmsRequestParameter(data, request,
					userMessageService, channelService, smsFileConfigService);

			// 2. 服务类实体设置
			ServiceBeanUtil serviceBean = new ServiceBeanUtil();
			serviceBean.setChannelService(channelService);
			serviceBean.setSymbolervice(smsSpecificSymbolService);
			serviceBean.setWordService(smsWordShieldingService);

			// 3.调用发送短信，并返回结果
			JSONObject json = sendMassSmsService.send(parmeter, serviceBean);

			// 4.查询可发短信条数、消息条数
			Integer surplusNum = SmsServerManager.I.getUserSurplusNum(Long.parseLong(smsUsers.getId().toString()));// redis缓存获取账号余额信息
			SessionUtils.setBlank(request, surplusNum);
			json.put("surplusNum", surplusNum);
			json.put("sendType", parmeter.getSendType());
			dzdResponse.setData(json);
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
		} catch (Exception e) {
			e.printStackTrace();
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
			logger.error("===============》群发短信出现异常：" + e.getMessage());
		}
		return dzdResponse;
	}

	/**
	 * 获取用户短信条数和消息数
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping("/getSmsUserMsgOrSmsNum")
	@ResponseBody
	public DzdResponse getUserMsg(HttpServletRequest request) {
		DzdResponse dzdResponse = new DzdResponse();
		try {
			SysUser user = SessionUtils.getUser(request);
			SmsUser smsUsers = userMessageService.querySmsUserById(user.getId());
			Integer msgCount = smsUserMessageService.queryMessageCount(smsUsers.getEmail(), smsUsers.getGroupTypeId());
			Integer useBalance = SmsServerManager.I.getUserSurplusNum(Long.parseLong(smsUsers.getId().toString()));// redis缓存获取账号余额信息
			SessionUtils.setBlank(request, useBalance);
			SessionUtils.setMessageCount(request, msgCount);
			MsgUtil msgUtil = new MsgUtil();
			msgUtil.setMsgCount(msgCount);
			msgUtil.setSurplusNum(useBalance);

			dzdResponse.setData(msgUtil);
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
		} catch (Exception e) {
			e.printStackTrace();
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
		}
		return dzdResponse;
	}

	/**
	 * 根据uuid查询上传文件的信息
	 *
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/queryPhoneConfigUUID")
	@ResponseBody
	public DzdResponse queryConfigPhoneByUUID(HttpServletRequest request) throws Exception {
		// 1. 接收基本参数
		DzdResponse dzdResponse = new DzdResponse();
		String uuid = request.getParameter("uuid");
		String operators = request.getParameter("operators");
		Object type = request.getParameter("loadType");
		if (uuid == null) {
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
			return dzdResponse;
		}
		try {

			// 2. 有效号码数量
			List<SmsFileConfig> configList = smsFileConfigService.querySmsFileConfigList(uuid, null);
			if (type != null && type.equals("load") && configList != null && configList.size() != 0) {
				List<String> phoneList = SmsSendUtil.getPhoneListByConfig(configList);
				Map<String, Object> map = FileUploadUtil.getPhoneMap(phoneList, operators);
				Integer validPhoneNum = (Integer) map.get("validPhoneNum");
				if (configList != null && configList.size() > 0) {
					configList.get(0).setAllPhoneNum(validPhoneNum);
				}
			}

			dzdResponse.setData(configList);
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
		} catch (Exception e) {
			e.printStackTrace();
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
		}
		return dzdResponse;
	}

	/**
	 * 删除号码文件
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("deleteFile")
	@ResponseBody
	public DzdResponse deleteFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		DzdResponse dzdResponse = new DzdResponse();
		try {
			// 1. uuid
			String uuid = request.getParameter("uuid");
			String fileName = request.getParameter("fileName");

			// 2. uuid对应的文件
			List<SmsFileConfig> configList = smsFileConfigService.querySmsFileConfigList(uuid, fileName);

			// 3. 文件不存在则直接返回
			if (configList == null || configList.size() == 0) {
				dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
				return dzdResponse;
			}

			// 4.删除服务器文件
			boolean flag = FileUploadUtil.deleteFile(configList, request);
			if (flag) {

				// 5.删除UUID对应的数据库配置
				smsFileConfigService.deleteByUUID(uuid, fileName);
				dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
			} else {
				dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
			logger.error("===============》删除文件出现异常：" + e.getMessage());
		}
		return dzdResponse;
	}

	/**
	 * 查询发送记录
	 *
	 * @param request
	 * @param response
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/psendList", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public DzdResponse psendList(HttpServletRequest request, HttpServletResponse response,
			@RequestBody Map<String, Object> data) throws Exception {
		DzdResponse dzdPageResponse = new DzdResponse();
		try {

			// 1. 参数设置
			WebRequestParameters parameters = getRequestParameters(WebRequestParameters.class, data);
			if (parameters == null) {
				return dzdPageResponse;
			}
			Object sendType = data.get("sendType");
			Object account = data.get("account");
			Object groupId = data.get("groupId");
			Object menuId = request.getParameter("menuId");
			if (menuId == null) {
				return dzdPageResponse;
			}
			SysUser user = SessionUtils.getUser(request);
			Integer sysUserId = user.getId();
			DzdPageParam dzdPageParam = new DzdPageParam();
			Map<String, Object> sortMap = new HashMap<String, Object>();
			if (parameters.getPagenum() != 0 && parameters.getPagesize() != 0) {
				dzdPageParam.setStart(parameters.getPagenum());
				dzdPageParam.setLimit(parameters.getPagesize());
			}
			sortMap.put("sysBid", user.getId());

			if (!StringUtil.isEmpty(sysUserId.toString())) {
				sortMap.put("sysUserId", sysUserId);
			}

			if (account != null && !StringUtil.isEmpty(account.toString())) {
				sortMap.put("smsUserEmail", account);
			}

			if (groupId != null && !StringUtil.isEmpty(groupId.toString())) {
				sortMap.put("groupId", groupId);
			}

			if (!StringUtil.isEmpty(parameters.getStartInput())) {
				sortMap.put("startInput", parameters.getStartInput() + " 00:00");
				sortMap.put("endInput", parameters.getEndInput() + " 23:59");

			} else {
				sortMap.put("startInput", DateUtil.todayDateStr() + " 00:00");
				sortMap.put("endInput", DateUtil.todayDateStr() + " 23:59");
			}

			if (!StringUtil.isEmpty(sendType.toString())) {
				sortMap.put("sendType", sendType);
			}
			sortMap.put("superAdmin", user.getSuperAdmin());
			// 排序
			// sortMap.put("sortVal", "order by id desc");

			// 得到登陆账户下面的子归属账户
			dzdPageParam = CommonRoleServiceUtil.getSysUserId(user, dzdPageParam, sortMap, sysRoleRelService,
					smsAccountService);

			dzdPageParam.setCondition(sortMap);

			// 2. 查询菜单按钮
			List<SysMenuBtn> sysMenuBtns = null;
			if (menuId != null) {
				if (user.getSuperAdmin() == 1) {// 管理员查询所有菜单按钮
					sysMenuBtns = sysMenuBtnService.queryByMenuid(Integer.parseInt(menuId.toString()));
				} else {// 其余角色查询配置好的按钮
					sysMenuBtns = sysMenuBtnService.queryMenuListByRole(Integer.parseInt(menuId.toString()), sysUserId);
				}
			}

			// 3. 查询集合数据
			Page<SmsSendTask> dataList = userMsmSendService.querySendListPage(sysMenuBtns, dzdPageParam);

			dzdPageResponse.setData(sysMenuBtns);
			if (!CollectionUtils.isEmpty(dataList)) {
				for (SmsSendTask smsSendTask : dataList.getResult()) {
					smsSendTask.setSysMenuBtns(sysMenuBtns);
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
	 * 查询对应任务所有的通道组
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("queryAisleGroup")
	@ResponseBody
	public DzdResponse queryTaskAisleGroup(HttpServletRequest request) throws Exception {
		DzdResponse dzdResponse = new DzdResponse();
		try {
			SysUser user = SessionUtils.getUser(request);
			SmsUser smsUsers = userMessageService.querySmsUserById(user.getId());
			Integer uid = smsUsers.getId();
			Integer bid = user.getId();
			Integer superAdmin = user.getSuperAdmin();

			// 查询用户通道组、或下属通道组
			List<SmsAisleGroup> aisleGroupList = userMsmSendService.queryTaskAisleGroup(uid, bid, superAdmin);

			dzdResponse.setData(aisleGroupList);
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
		} catch (Exception e) {
			e.printStackTrace();
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
		}
		return dzdResponse;
	}

	/**
	 * 查询发送详情列表
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/sendSmsDetailList")
	@ResponseBody
	public void list(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> data)
			throws Exception {
		try {
			Object pagenum = data.get("pagenum");
			Object pagesize = data.get("pagesize");
			SmsSendLog smsSendLog = new SmsSendLog();
			org.json.JSONObject jsonObject = new org.json.JSONObject();
			WebRequestParameters parameters = getRequestParameters(WebRequestParameters.class, data);
			if (parameters == null) {
				HtmlUtil.writerJson(response, jsonObject);
			}
			Object ids = data.get("id");// 任务id
			Object phone = data.get("phone");// 号码
			Object type = data.get("type");// 状态 0:发送失败,1:发送成功
			Object menuId = data.get("menuId");
			if (menuId == null) {
				return;
			}

			SysUser user = SessionUtils.getUser(request);
			SmsUser smsUsers = userMessageService.querySmsUserById(user.getId());

			if (ids != null && !StringUtil.isEmpty(ids.toString())) {
				smsSendLog.setId(Integer.parseInt(ids.toString()));
			}

			if (!StringUtil.isEmpty(phone.toString())) {
				smsSendLog.setReceivePhone(phone.toString());
			}
			if (!StringUtil.isEmpty(type.toString())) {
				smsSendLog.setType(Integer.parseInt(type.toString()));
			}
			if (!StringUtil.isEmpty(smsUsers.getId().toString())) {
				smsSendLog.setSmsBid(smsUsers.getId());
			}

			Pager pager = new Pager();
			if (pagenum != null && !pagenum.equals("")) {
				Integer pagenums = 0;
				if (pagesize != null && !pagesize.equals("")) {
					pagenums = (Integer.parseInt(pagenum.toString()) - 1) * Integer.parseInt(pagesize.toString());
				}
				pager.setPageOffset(pagenums);
			}
			smsSendLog.setPager(pager);
			if (pagesize != null) {
				smsSendLog.setRows(Integer.valueOf(pagesize.toString()));
			}

			List<SysMenuBtn> sysMenuBtns = null;
			if (menuId != null) {
				if (user.getSuperAdmin() == 1) {// 管理员查询所有菜单按钮
					sysMenuBtns = sysMenuBtnService.queryByMenuid(Integer.parseInt(menuId.toString()));
				} else {// 其余角色查询配置好的按钮
					sysMenuBtns = sysMenuBtnService.queryMenuListByRole(Integer.parseInt(menuId.toString()),
							user.getId());
				}
			}

			// 查询发送详情列表
			List<SmsSendLog> dataList = userMsmSendService.querySendDetailList(smsSendLog, sysMenuBtns);

			SmsSendLog smsSedNum = msmSendService.querySedNum(smsSendLog);
			if (dataList.size() > 0) {
				SmsSendLog sendLog = dataList.get(0);
				// 定时任务发送按照定时当天时间作为详情控制时间
				String createTimeStr = com.dzd.utils.DateUtil.formatDate(sendLog.getActualSendTime());
				String nowTimeStr = com.dzd.utils.DateUtil.formatDate(new Date());

				dataList.get(0).setCreateTimeStr(createTimeStr);
				dataList.get(0).setNowTimeStr(nowTimeStr);

				dataList.get(0).setBillingNnum(smsSedNum.getBillingNnum());// 提交短信条数
				dataList.get(0).setSendingNum(smsSedNum.getSendingNum());// 发送中
				dataList.get(0).setSendFinishNum(smsSedNum.getSendFinishNum());// 发送完成

				dataList.get(0).setSucceedNum(smsSedNum.getSucceedNum());// 发送成功
				dataList.get(0).setFailureNum(smsSedNum.getFailureNum());// 发送失败
			}

			if (!CollectionUtils.isEmpty(dataList)) {
				for (SmsSendLog smsSendLogs : dataList) {
					smsSendLogs.setSysMenuBtns(sysMenuBtns);
				}
				// 设置页面数据
				jsonObject.put("sysMenuBtns", sysMenuBtns);
				jsonObject.put("total", smsSendLog.getPager().getRowCount());
				jsonObject.put("rows", dataList);
				HtmlUtil.writerJson(response, jsonObject);
			}
		} catch (Exception e) {
			logger.error(null, e);
			e.printStackTrace();
		}

	}

	/**
	 * 查询用户短信回复列表
	 *
	 * @param request
	 * @param response
	 * @param data
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/pushlist", method = RequestMethod.POST)
	@ResponseBody
	public DzdResponse blacklist(HttpServletRequest request, HttpServletResponse response,
			@RequestBody Map<String, Object> data) throws Exception {
		DzdResponse dzdPageResponse = new DzdResponse();
		try {
			WebRequestParameters parameters = getRequestParameters(WebRequestParameters.class, data);
			if (parameters == null) {
				return dzdPageResponse;
			}
			Object phone = data.get("phone");

			SysUser user = SessionUtils.getUser(request);

			Integer sysUserId = user.getId();

			DzdPageParam dzdPageParam = new DzdPageParam();

			Map<String, Object> sortMap = new HashMap<String, Object>();

			if (parameters.getPagenum() != 0 && parameters.getPagesize() != 0) {
				dzdPageParam.setStart(parameters.getPagenum());
				dzdPageParam.setLimit(parameters.getPagesize());
			}
			if (!StringUtil.isEmpty(parameters.getStartInput())) {
				sortMap.put("start", parameters.getStartInput());
				sortMap.put("end", parameters.getEndInput());
			}
			if (phone != null && !StringUtil.isEmpty(phone.toString())) {
				sortMap.put("SmsBlacklistHone", phone.toString());
			}
			if (sysUserId != null && !StringUtil.isEmpty(sysUserId.toString())) {
				sortMap.put("sysUserId", sysUserId);
			}

			// 排序
			if (parameters.getSort() != null && parameters.getOrder() != null) {
				sortMap.put("sortVal", "order by " + parameters.getSort() + " " + parameters.getOrder());
			} else {
				sortMap.put("sortVal", "order by id desc");
			}

			dzdPageParam.setCondition(sortMap);
			Page<SmsReceiveReplyPush> dataList = userMsmSendService.queryUserList(dzdPageParam);

			if (!CollectionUtils.isEmpty(dataList)) {
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
	 * 下载号码txt
	 */
	@RequestMapping(value = "/export/csv", method = { RequestMethod.GET })
	@ResponseBody
	public void exportCsv(HttpServletRequest request, HttpServletResponse response) {
		Object id = request.getParameter("id");
		Object phone1 = request.getParameter("phone");
		Object type = request.getParameter("type");
		Map map = new HashMap();
		map.put("msmSendId", id);
		map.put("phone", phone1);
		map.put("type", type);
		if (id == null || "".equals(id)) {
			return;
		}

		SysUser sysUser = SessionUtils.getUser(request);
		List<String> projects = userMsmSendService.queryMsmSendPhoneByid(map);
		String fileName = sysUser.getEmail() + com.dzd.utils.DateUtil.formatNowDate(new Date());

		// 导出txt文件
		response.setContentType("text/plain;charset=utf-8");

		response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".txt");
		BufferedOutputStream buff = null;
		StringBuffer write = new StringBuffer();
		String enter = "\r\n";
		ServletOutputStream outSTr = null;
		try {
			outSTr = response.getOutputStream(); // 建立
			buff = new BufferedOutputStream(outSTr);
			// 把内容写入文件
			if (projects.size() > 0) {
				for (String phone : projects) {
					write.append(phone);
					write.append(enter);
				}
			}
			buff.write(write.toString().getBytes("UTF-8"));
			buff.flush();
			buff.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				buff.close();
				outSTr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	// 修改用户验证码状态
	@RequestMapping("/updateSmsVerifyType")
	@ResponseBody
	public void updateSmsVerifyType(HttpServletRequest request) {
		// 修改用户已经登录，在登录状态下发短信不需要输入验证码
		SysUser sysUser = SessionUtils.getUser(request);
		SmsUser smsUsers = userMessageService.querySmsUserById(sysUser.getId());
		smsUsers.setVerifyType(1);
		smsUserService.updateSmsUserVerifyType(smsUsers);
	}

}
