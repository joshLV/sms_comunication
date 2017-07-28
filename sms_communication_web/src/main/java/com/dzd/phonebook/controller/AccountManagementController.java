package com.dzd.phonebook.controller;

import com.dzd.base.util.MethodUtil;
import com.dzd.base.util.SessionUtils;
import com.dzd.base.util.StringUtil;
import com.dzd.phonebook.aop.MethodDescription;
import com.dzd.phonebook.controller.base.WebBaseController;
import com.dzd.phonebook.entity.*;
import com.dzd.phonebook.page.MD5;
import com.dzd.phonebook.service.*;
import com.dzd.phonebook.util.*;
import com.dzd.phonebook.util.SmsUserBlank;
import com.dzd.sms.application.Define;
import com.dzd.utils.LogUtil;
import com.dzd.utils.StringUtils;
import com.github.pagehelper.Page;
import net.sf.json.JSONObject;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 账户中心---账户管理 Created by wangran on 2017/5/18.
 */
@Controller
@RequestMapping("/management")
public class AccountManagementController extends WebBaseController {
	public static final LogUtil log = LogUtil.getLogger(AccountManagementController.class);

	@Autowired
	private SysMenuBtnService sysMenuBtnService;

	@Autowired
	private SysMenuService sysMenuService;

	@Autowired
	private SysUserService<SysUser> sysUserService;

	@Autowired
	private SmsUserService smsUserService;

	@Autowired
	private ChannelService channelService;

	@Autowired
	private SmsAccountService smsAccountService;

	@Autowired
	private SmsManageMentAccountService smsManagementAccountService;

	@Autowired
	private SysRoleRelService sysRoleRelService;

	@Autowired
	private SysRoleService sysRoleService;

	@Autowired
	private SmsUserBlankService smsUserBlankService;

	@Autowired
	private UserMessageService userMessageService;

	@Autowired
	private MsmSendService msmSendService;

	@Autowired
	private SysLoginLogService sysLoginLogService;

	@Autowired
	private SysLogService sysLogService;

	private SmsOrderExportService orderExportService = new SmsOrderExportService();

	private Page<SmsUser> dataList;

	public Page<SmsUser> getDataList() {
		return dataList;
	}

	public void setDataList(Page<SmsUser> dataList) {
		this.dataList = dataList;
	}

	@RequestMapping(value = "/export", method = { RequestMethod.GET })
	@ResponseBody
	public void export(HttpServletRequest request, HttpServletResponse response) {
		if (CollectionUtils.isEmpty(dataList)) {
			return;
		}
		orderExportService.managementUserExport(request, response, dataList);
	}

	@RequestMapping("/listview")
	public String list(HttpServletRequest request, Model model) throws Exception {
		Object menuId = request.getParameter("id");
		model.addAttribute("menuId", menuId);
		SysUser user = (SysUser) request.getSession().getAttribute("session_user");
		// 用户类型
		List<SysRole> roleList = sysRoleService.queryAllList();
		// 通道类型
		List<SmsAisleGroupType> list = channelService.querySmsAisleGroupType();

		// 查询归属
		List<SysUser> sysUserList = CommonRoleServiceUtil.getSysUserBUserList(request, smsUserService,
				sysRoleRelService, smsAccountService);

		model.addAttribute("roleList", roleList);
		model.addAttribute("sysUserList", sysUserList);
		model.addAttribute("sysUser", user);
		model.addAttribute("typeList", list);
		return "account/manageMent";
	}

	/**
	 * @Description:账户管理列表
	 * @author:wangran
	 * @time:2017年5月18日 下午2:01:34
	 */
	@RequestMapping(value = "/managementList", method = RequestMethod.POST)
	@ResponseBody
	public DzdResponse managementList(HttpServletRequest request, @RequestBody Map<String, Object> data)
			throws Exception {
		DzdResponse dzdPageResponse = new DzdResponse();
		try {
			WebRequestParameters parameters = getRequestParameters(WebRequestParameters.class, data);
			if (parameters == null) {
				return dzdPageResponse;
			}
			Object menuId = request.getParameter("menuId");
			Object email = data.get("email");// 账号信息
			Object name = data.get("name"); // 用户名
			Object contact = data.get("contact"); // 联系人
			Object phone = data.get("phone"); // 手机号
			Object aisleType = data.get("aisleType"); // 通道类型
			Object networkChargingState = data.get("networkChargingState"); // 网上充值状态
			Object userType = data.get("userType"); // 用户类型
			Object sysUserId = data.get("sysUserId"); // 归属

			SysUser user = SessionUtils.getUser(request);
			if (menuId == null || user == null) {
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

			if (name != null && !StringUtil.isEmpty(name.toString())) {
				sortMap.put("name", name.toString());
			}

			if (email != null && !StringUtil.isEmpty(email.toString())) {
				sortMap.put("email", email.toString());
			}

			if (sysUserId != null && !StringUtil.isEmpty(sysUserId.toString())) {
				sortMap.put("sysUserId", sysUserId.toString());
			}

			if (contact != null && !StringUtil.isEmpty(contact.toString())) {
				sortMap.put("contact", contact.toString());
			}

			if (phone != null && !StringUtil.isEmpty(phone.toString())) {
				sortMap.put("phone", phone.toString());
			}
			if (aisleType != null && !StringUtil.isEmpty(aisleType.toString())) {
				sortMap.put("aisleType", Integer.parseInt(aisleType.toString()));
			}
			if (networkChargingState != null && !StringUtil.isEmpty(networkChargingState.toString())) {
				sortMap.put("networkChargingState", Integer.parseInt(networkChargingState.toString()));
			}
			if (userType != null && !StringUtil.isEmpty(userType.toString())) {
				sortMap.put("userType", Integer.parseInt(userType.toString()));
			}
			// 获取账户下面的归属账户sys_user_id
			dzdPageParam = CommonRoleServiceUtil.getSysUserId(user, dzdPageParam, sortMap, sysRoleRelService,
					smsAccountService);

			sortMap.put("sortVal", "order by createTime desc");

			// "导出" 时存放选择数据id
			List ids = new ArrayList();
			sortMap.put("ids", ids);

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

			Page<SmsUser> dataList = smsManagementAccountService.querySmsUserManageMentListPage(dzdPageParam,
					sysMenuBtns);

			dzdPageResponse.setData(sysMenuBtns);

			if (!CollectionUtils.isEmpty(dataList)) {
				for (SmsUser instruct : dataList.getResult()) {
					instruct.setSysMenuBtns(sysMenuBtns);
				}
				dzdPageResponse.setRows(dataList.getResult());
				dzdPageResponse.setTotal(dataList.getTotal());
			}
		} catch (Exception e) {
			logger.error("====================》账户管理查询失败：" + e.getMessage());
			e.printStackTrace();
		}
		return dzdPageResponse;
	}

	/**
	 * 账户管理新增或修改
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/from/merge", method = RequestMethod.POST)
	public String merge(HttpServletRequest request, SmsUser smsUser) {
		Object menuId = sysMenuService.queryMenusByName(); // 根据菜单名字查询菜单id
		SysUser user = SessionUtils.getUser(request);
		try {
			SmsUserBlank smsUserBlank = new SmsUserBlank(); // 代理账户的“钱包”
			SysUser sysUser = new SysUser(); // 分配给代理账户的系统账号
			SysRoleRel sysRoleRel = new SysRoleRel(); // 给用户分配角色
			Object id = request.getParameter("id");
			Object btnType = request.getParameter("btnType"); // 按钮类型"新增，另建，设置，转移"
			Object sysUserId = request.getParameter("sysUserId"); // 修改，注册时要修改的用户id
			Object smsUserBlankId = request.getParameter("smsUserBlankId"); // 代理账户钱包ID

			// 用户类型--角色名称

			sysRoleRel.setRoleId(Integer.parseInt(smsUser.getRoleName()));
			sysRoleRel.setRelType(1);
			log.info("bid:" + smsUser.getBid());

			if (id != null && !id.toString().isEmpty() && btnType.equals("update") || btnType.equals("register")) {
				smsUser.setId(Integer.parseInt(id.toString()));
			}

			if (btnType.equals("copy")) {
				smsUser.setId(null);
			}

			if (smsUserBlankId != null && !smsUserBlankId.toString().isEmpty()) {
				smsUserBlank.setId(Integer.parseInt(smsUserBlankId.toString())); // 钱包ID
			}
			if (sysUserId != null && !sysUserId.toString().isEmpty()) {
				sysUser.setId(Integer.parseInt(sysUserId.toString())); // 分配系统用户
																		// 系统用户ID
				sysRoleRel.setObjId(Integer.parseInt(sysUserId.toString()));
			}
			sysUser.setNickName(smsUser.getName()); // 分配系统用户 记录昵称
			sysUser.setSuperiorId(smsUser.getBid()); // 上级ID 归属
			sysUser.setEmail(smsUser.getEmail()); // 分配系统用户 记录账号

			if (!StringUtil.isEmpty(smsUser.getPwd())) {
				String pwd = MethodUtil.MD5(smsUser.getPwd().toString()); // 分配系统用户
																			// 记录密码
				sysUser.setPwd(pwd);
			}

			// 设置用户等级
			if (smsUser.getRoleName().equals(Define.ROLEID.ROLE_ADMINISTRATOR)
					|| smsUser.getRoleName().equals(Define.ROLEID.ROLE_FIRST_LEVEL_ADMINISTRATOR)
					|| smsUser.getRoleName().equals(Define.ROLEID.ROLE_CUSTOMER_SERVICE)) {
				smsUser.setUserLevel(Define.ROLEID.LEVEL_1);
			} else if (smsUser.getRoleName().equals(Define.ROLEID.ROLE_SALES_MANAGER)
					|| smsUser.getRoleName().equals(Define.ROLEID.ROLE_USER_ADMINISTRATOR)
					|| smsUser.getRoleName().equals(Define.ROLEID.ROLE_SALESMAN)) {
				smsUser.setUserLevel(Define.ROLEID.LEVEL_2);
			} else {
				smsUser.setUserLevel(Define.ROLEID.LEVEL_3);
			}

			// 签名
			if (smsUser.getSignatureType() == 1) {
				smsUser.setSignature(null);
			}
			smsUser.setState(0);
			String content = "";
			// 新建或者另建账户(复制账户)
			if (smsUser.getId() == null || btnType.equals("copy")) {
				log.info("-----------------》新增账户信息");
				// 1.分配系统账号
				sysUser.setState(1);
				sysUser.setDeleted(0);
				sysUser.setUserType(2);
				sysUser.setSuperAdmin(0);
				sysUserService.saveUser(sysUser, null);

				// 用户类型--角色类型（用户类型）
				sysRoleRel.setObjId(sysUser.getId());
				sysRoleRelService.add(sysRoleRel);

				// 2.添加账户信息
				smsUser.setSysUserId(sysUser.getId());
				smsUser.setCheckState(1); // 注册成功，“已注册”
				smsUser.setKey(queryKey());
				smsManagementAccountService.saveSmsUserManagement(smsUser);

				// 3.账户钱包
				smsUserBlank.setUserId(smsUser.getId());
				smsUserService.addSmsUserBlank(smsUserBlank);
				instructSend(InstructState.ADDSMSUSER_SUCESS, smsUser.getKey(), smsUser.getId()); // 发送动作指令到redis

				// 保存操作日志
				content = ErrorCodeTemplate.USER_ADD;
			} else {
				log.info("-----------------》修改查账户信息");
				if (btnType.equals("register")) {
					// "申请账户"菜单列表 ”注册账户“按钮 调用

					// 1.修改系统账号信息
					sysUser.setId(Integer.parseInt(sysUserId.toString()));
					sysUserService.updateBySelective(sysUser);

					// 2.添加账户钱包
					smsUserBlank.setUserId(smsUser.getId());
					smsUserService.addSmsUserBlank(smsUserBlank);
					// 3.分配用户类型--角色类型（用户类型）
					sysRoleRelService.add(sysRoleRel);
					// 4.修改账户信息
					// 注册成功，修改为“已注册”
					smsUser.setCheckState(1);
					smsUser.setKey(queryKey());
					smsUser.setCreateTime(new Date());
					smsUserService.updateSmsUser(smsUser);

					// 保存操作日志
					content = ErrorCodeTemplate.USER_REGIST;
				} else {
					SysUser su = sysUserService.queryById(sysUserId);
					// ”账号管理“ 模块 修改 调用
					// 1.修改系统账号信息
					sysUserService.updateBySelective(sysUser);
					// 修改账户信息
					smsUserService.updateSmsUser(smsUser);
					// 修改用户类型--角色类型（用户类型）
					sysRoleRelService.updateByObjId(sysRoleRel);

					// 保存操作日志
					content = ErrorCodeTemplate.USER_UPATE;

					// 如果账号修改
					if (su != null && !su.getEmail().equals(smsUser.getEmail())) {
						// 修改登陆日志中的账号
						SysLoginLog sysLoginLog = new SysLoginLog();
						sysLoginLog.setEmail(smsUser.getEmail());
						sysLoginLog.setSysUserId(smsUser.getSysUserId());
						sysLoginLogService.updateLoginLog(sysLoginLog);

						// 修改操作日志中的账号
						SysLog sysLog = new SysLog();
						sysLog.setUserName(smsUser.getEmail());
						sysLog.setSysUserId(smsUser.getSysUserId());
						sysLogService.updateSysLog(sysLog);

						// 修改操作日志内容中的账号
						sysLogService.updateSysLogContentEmail(":" + smsUser.getEmail(), su.getEmail());
					}

				}
				String keys = smsUserService.querySmsUserKey(smsUser.getId());
				instructSend(InstructState.UPDATESMSUSER_SUCESS, keys, smsUser.getId()); // 发送动作指令到redis
			}
			// 保存操作日志
			CommonRoleServiceUtil.saveOperateLog(user, smsUser.getEmail(), content, sysUserService);
		} catch (Exception e) {
			logger.error("====================》账户管理新增或者修改失败：" + e.getMessage());
			e.printStackTrace();
		}
		return "redirect:/management/listview.do?id=" + menuId + "#submit";
	}

	/**
	 * @Description:根据新增账户信息查询该账号是否已分配和手机号是否已经注册
	 * @author:wangran
	 * @time:2017年5月19日 上午11:56:29
	 */
	@RequestMapping(value = "/querySysUserbyMsmuser", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public DzdResponse merge(HttpServletRequest request, @RequestBody Map<String, Object> data) {
		DzdResponse dzdPageResponse = new DzdResponse();
		try {
			Object id = request.getParameter("id");
			Object email = data.get("email"); // 获取分配的账户名，
			if (email == null || "".equals(email)) {
				dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
				return dzdPageResponse;
			}
			SmsUser smsUser = new SmsUser();
			smsUser.setEmail(email.toString());
			if (id != null && !"".equals(id)) {
				smsUser.setId(Integer.parseInt(id.toString()));
			}
			// 判断 系统账号是否存在
			Integer num = sysUserService.querySysUserByuserEmal(smsUser);
			if (num > 0) {
				dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
				return dzdPageResponse;
			} else {
				dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
			}
		} catch (Exception e) {
			logger.error("====================》根据新增账户信息查询该账号是否已分配失败：" + e.getMessage());
			dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
			e.printStackTrace();
		}
		return dzdPageResponse;
	}

	/**
	 * @Description:根据ID查询账户信息
	 * @author:wangran
	 * @time:2017年5月20日 上午11:55:53
	 */
	@RequestMapping(value = "/formEdit")
	@ResponseBody
	public DzdResponse edit(HttpServletRequest request) {
		DzdResponse dzdPageResponse = new DzdResponse();
		try {
			Object id = request.getParameter("id");
			if (id != null && !StringUtil.isEmpty(id.toString())) {
				SmsUser smsUser = smsManagementAccountService.querySmsManageMentUser(Integer.parseInt(id.toString()));
				JspResponseBean jspResponseBean = new JspResponseBean();
				if (smsUser != null) {
					jspResponseBean.setData(smsUser);
					dzdPageResponse.setData(jspResponseBean);
					dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
				} else {
					dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
				}
			} else {
				dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
				return dzdPageResponse;
			}
		} catch (Exception e) {
			logger.error("====================》根据ID查询账户信息失败：" + e.getMessage());
			dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
			e.printStackTrace();
		}
		return dzdPageResponse;
	}

	/**
	 * @Description:账户转移修改
	 * @author:wangran
	 * @time:2017年5月20日 12:55:53
	 */
	@MethodDescription("转移归属")
	@RequestMapping(value = "/apply_edit/formEdit", method = RequestMethod.POST)
	@ResponseBody
	public DzdResponse apply_edit(HttpServletRequest request, @RequestParam("ids[]") List<Integer> ids) {
		DzdResponse dzdPageResponse = new DzdResponse();
		try {
			Object bid = request.getParameter("bid");
			SmsUser smsUser = new SmsUser();
			for (int i = 0; i < ids.size(); i++) {
				smsUser.setId(Integer.parseInt(ids.get(i).toString()));
				smsUser.setBid(Integer.parseInt(bid.toString()));
				smsUserService.updateUser(smsUser);
			}
			dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
		} catch (Exception e) {
			logger.error("====================》用户归属转移失败：" + e.getMessage());
			dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
			e.printStackTrace();
		}
		return dzdPageResponse;
	}

	/**
	 * @Description:账户删除
	 * @author:wangran
	 * @time:2017年5月20日 上午12:55:53
	 */
	@MethodDescription("删除账户")
	@RequestMapping(value = "/apply/delete", method = RequestMethod.POST)
	@ResponseBody
	public DzdResponse apply_delete(HttpServletRequest request, @RequestParam("ids[]") List<Integer> ids,
			@RequestParam("sysUserIds[]") List<Integer> sysUserIds) {
		DzdResponse dzdPageResponse = new DzdResponse();
		try {
			// 删除账户sms_user
			smsManagementAccountService.deleteByIds(ids);
			// 删除账户信息表
			smsUserBlankService.deleteSmsUserBlank(ids);
			// 删除用户sys_user
			sysUserService.deleteUsers(sysUserIds);

			dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
		} catch (Exception e) {
			logger.error("====================》用户删除失败：" + e.getMessage());
			dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
			e.printStackTrace();
		}
		return dzdPageResponse;
	}

	/**
	 * 新增、另建、设置 跳转页面
	 *
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/operate", method = RequestMethod.GET)
	public String operate(HttpServletRequest request, Model model) {
		String id = request.getParameter("id");
		String menuId = request.getParameter("menuId");
		String sysUserId = request.getParameter("sysUserId");
		String btnType = request.getParameter("btnType");
		String roleId = request.getParameter("roleId");
		SysUser user = (SysUser) request.getSession().getAttribute("session_user");
		List<SysRoleRel> sysRoleRels = CommonRoleServiceUtil.queryRoleByUserId(user.getId(), sysRoleRelService);
		List<SysUser> sysUserList = new ArrayList<SysUser>();

		List<Integer> list = new ArrayList<Integer>();
		list.add(Define.ROLEID.LEVEL_1);
		list.add(Define.ROLEID.LEVEL_2);

		// 查询一二级用户
		for (SysRoleRel sysRoleRel : sysRoleRels) {
			if (sysRoleRel.getRoleId() == Integer.parseInt(Define.ROLEID.ROLE_FIRST_LEVEL_ADMINISTRATOR)) { // 如果当前登录用户为
																											// 一级管理员
				sysUserList = smsUserService.queryByUserLevel(list);
			}
		}
		if (user.getSuperAdmin() == 1) {
			// 超级管理员
			sysUserList = smsUserService.queryByUserLevel(list);
		}
		// 角色类型列表
		List<SysRole> roleList = sysRoleService.queryAllList();

		model.addAttribute("btnType", btnType);
		model.addAttribute("sysUserId", sysUserId);
		model.addAttribute("roleId", roleId);
		model.addAttribute("menuId", menuId);
		model.addAttribute("sysUser", user); // 登陆用户信息
		model.addAttribute("sysUserList", sysUserList);

		model.addAttribute("roleList", roleList);

		if (StringUtils.isNotBlank(btnType)
				&& (btnType.equals("update") || btnType.equals("register") || btnType.equals("copy"))) {
			model.addAttribute("id", id);
		}

		return "account/operate";
	}

	/**
	 * 导出
	 *
	 * @param request
	 * @return
	 */
	@MethodDescription("导出账户")
	@RequestMapping(value = "/querySmsManagementData", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public DzdResponse querySmsData(HttpServletRequest request) {
		String datas = request.getParameter("datas");
		JSONObject json = JSONObject.fromObject(datas);
		DzdResponse response = new DzdResponse();
		Object menuId = json.get("menuId");
		Object email = json.get("email"); // 账号
		Object name = json.get("name"); // 用户名
		Object contact = json.get("contact"); // 联系人
		Object phone = json.get("phone"); // 手机号
		Object aisleType = json.get("aisleType"); // 通道类型
		Object networkChargingState = json.get("networkChargingState"); // 网上充值状态
		Object userType = json.get("userType"); // 账号类型
		Object sysUserId = json.get("sysUserId"); // 归属
		Object ids = json.get("ids"); // 账户id

		DzdPageParam dzdPageParam = new DzdPageParam();
		Map<String, Object> sortMap = new HashMap<String, Object>();
		if (email != null && !StringUtil.isEmpty(email.toString())) {
			sortMap.put("email", email);
		}
		if (contact != null && !StringUtil.isEmpty(contact.toString())) {
			sortMap.put("contact", contact);
		}
		if (name != null && !StringUtil.isEmpty(name.toString())) {
			sortMap.put("name", name);
		}
		if (phone != null && !StringUtil.isEmpty(phone.toString())) {
			sortMap.put("phone", phone);
		}
		if (aisleType != null && !StringUtil.isEmpty(aisleType.toString())) {
			sortMap.put("aisleType", aisleType);
		}
		if (networkChargingState != null && !StringUtil.isEmpty(networkChargingState.toString())) {
			sortMap.put("networkChargingState", networkChargingState);
		}
		if (userType != null && !StringUtil.isEmpty(userType.toString())) {
			sortMap.put("userType", userType);
		}
		if (sysUserId != null && !StringUtil.isEmpty(sysUserId.toString())) {
			sortMap.put("sysUserId", sysUserId);
		}
		if (ids != null && !StringUtil.isEmpty(ids.toString())) {
			sortMap.put("ids", ids);
		}
		sortMap.put("sortVal", "order by createTime desc");
		dzdPageParam.setCondition(sortMap);

		SysUser user = (SysUser) request.getSession().getAttribute("session_user");
		List<SysMenuBtn> sysMenuBtns = null;
		if (menuId != null) {
			if (user.getSuperAdmin() == 1) {// 管理员查询所有菜单按钮
				sysMenuBtns = sysMenuBtnService.queryByMenuid(Integer.parseInt(menuId.toString()));
			} else {// 其余角色查询配置好的按钮
				sysMenuBtns = sysMenuBtnService.queryMenuListByRole(Integer.parseInt(menuId.toString()), user.getId());
			}
		}

		Page<SmsUser> dataList = smsManagementAccountService.querySmsUserManageMentListPage(dzdPageParam, sysMenuBtns);

		if (dataList == null || CollectionUtils.isEmpty(dataList.getResult())) {
			response.setRetCode(ErrorCodeTemplate.CODE_PARAMETER_ERROR);
			logger.error(" no hava data");
			return response;
		} else {
			this.setDataList(dataList);
			response.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
		}
		return response;
	}

	/**
	 * 根据用户级别查询归属
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "queryUserByLevel", method = RequestMethod.POST)
	@ResponseBody
	public DzdResponse queryUserByLevel(HttpServletRequest request) throws Exception {
		DzdResponse dzdResponse = new DzdResponse();
		try {
			String level = request.getParameter("level");// 用户角色
			List<SmsUser> smsUsers = new ArrayList<SmsUser>();
			List<Integer> list = new ArrayList<Integer>();
			if (level.equals(Define.ROLEID.ROLE_ADMINISTRATOR) || level.equals(Define.ROLEID.ROLE_CUSTOMER_SERVICE)) {
				// 如果选择的是“超级管理员”和“客服” 归属查询 "超级管理员"
				list.add(Integer.parseInt(Define.ROLEID.ROLE_ADMINISTRATOR)); // 超级管理员
																				// 角色ID
				smsUsers = smsUserService.queryRoleLevel(list);
			} else if (level.equals(Define.ROLEID.ROLE_FIRST_LEVEL_ADMINISTRATOR)) {
				// 如果选择的是"一级管理员" 查询“超级管理员”和“客服”
				list.add(Integer.parseInt(Define.ROLEID.ROLE_ADMINISTRATOR));// 超级管理员
																				// 角色ID
				list.add(Integer.parseInt(Define.ROLEID.ROLE_CUSTOMER_SERVICE));// 客服
																				// 角色ID
				smsUsers = smsUserService.queryRoleLevel(list);
			} else if (level.equals(Define.ROLEID.ROLE_SALES_MANAGER) || level.equals(Define.ROLEID.ROLE_SALESMAN)
					|| level.equals(Define.ROLEID.ROLE_USER_ADMINISTRATOR) || level.equals(Define.ROLEID.ROLE_USER)
					|| level.equals("12")) {
				// 2级（销售经理、业务员、代理商）或者是 3级（用户 level=60）
				list.add(Define.ROLEID.LEVEL_1);// 一级用户
				list.add(Define.ROLEID.LEVEL_2);// 二级用户
				smsUsers = smsUserService.queryByUserLevel(list);
			} else {
			}
			dzdResponse.setData(smsUsers);
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
		} catch (Exception e) {
			logger.error("====================》根据用户级别查询归属失败：" + e.getMessage());
			e.printStackTrace();
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
		}
		return dzdResponse;
	}

	/**
	 * @Description:代理数据处理动作发送
	 * @author
	 * @time:2017年5月19日 下午2:45:22
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

	/**
	 * @Description:MD5加密生成Key
	 * @author:wangran
	 * @time:2017年5月19日 下午3:12:52
	 */
	public String queryKey() {
		/* 得到当前时间戳 */
		Date dt = new Date();
		Long currentTime = dt.getTime();

		Random rand = new Random();
		int randNum = rand.nextInt(10000) + 1;

		String md5Token = MD5.GetMD5Code(randNum + "" + currentTime);// 得到MD5加密后的MD5串
		return md5Token;
	}

	/**
	 * 查询所有的通道组
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("queryAisleGroup")
	@ResponseBody
	public DzdResponse queryAisleGroup(HttpServletRequest request) throws Exception {
		DzdResponse dzdResponse = new DzdResponse();
		try {
			SysUser user = SessionUtils.getUser(request);
			SmsUser smsUsers = userMessageService.querySmsUserById(user.getId());
			Integer uid = smsUsers.getId();
			Integer bid = user.getId();
			Integer superAdmin = user.getSuperAdmin();

			// 查询用户通道组、或下属通道组
			List<SmsAisle> smsAisleGroup = msmSendService.querySmsAisleGroup(null);

			dzdResponse.setData(smsAisleGroup);
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
		} catch (Exception e) {
			e.printStackTrace();
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
		}
		return dzdResponse;
	}
}
