package com.dzd.phonebook.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dzd.utils.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.dzd.base.util.SessionUtils;
import com.dzd.base.util.StringUtil;
import com.dzd.phonebook.controller.base.WebBaseController;
import com.dzd.phonebook.entity.OperatorSectionNo;
import com.dzd.phonebook.entity.SmsFileConfig;
import com.dzd.phonebook.entity.SmsFilterNumberRecord;
import com.dzd.phonebook.entity.SysUser;
import com.dzd.phonebook.page.MobileCheckUtil;
import com.dzd.phonebook.service.CommonRoleServiceUtil;
import com.dzd.phonebook.service.SmsAccountService;
import com.dzd.phonebook.service.SmsFileConfigService;
import com.dzd.phonebook.service.SmsFilterNumberRecordService;
import com.dzd.phonebook.service.SysRoleRelService;
import com.dzd.phonebook.util.DzdPageParam;
import com.dzd.phonebook.util.DzdResponse;
import com.dzd.phonebook.util.ErrorCodeTemplate;
import com.dzd.phonebook.util.FileUploadUtil;
import com.dzd.phonebook.util.PhoneFilterUtil;
import com.dzd.phonebook.util.SmsSendUtil;
import com.dzd.phonebook.util.WebRequestParameters;
import com.dzd.sms.api.service.SmsFilterOrderBusiness;
import com.dzd.sms.application.Define;
import com.dzd.sms.application.SmsServerManager;
import com.dzd.sms.service.data.SmsUser;
import com.dzd.sms.util.DistinguishOperator;
import com.github.pagehelper.Page;

/**
 * Created by dzd-technology01 on 2017/5/17.
 */
@Controller
@RequestMapping("/smsFilter")
public class SmsFilterNumberController extends WebBaseController {

	public static final LogUtil log = LogUtil.getLogger(SmsFilterNumberController.class);

	private static final int PAGECALC = 20;

	@SuppressWarnings("rawtypes")
	@Autowired
	private SmsFilterNumberRecordService smsFilterNumberRecordService;

	@Autowired
	private SmsFileConfigService<SmsFileConfig> smsFileConfigService;

	@SuppressWarnings("rawtypes")
	@Autowired
	private SysRoleRelService sysRoleRelService;

	@SuppressWarnings("rawtypes")
	@Autowired
	private SmsAccountService smsAccountService;

	Map<String, Object> dateMap = new HashMap<String, Object>();

	public Map<String, Object> getDateMap() {
		return dateMap;
	}

	public void setDateMap(Map<String, Object> dateMap) {
		this.dateMap = dateMap;
	}

	@RequestMapping("/listView")
	public String pageView(HttpServletRequest request, Model model) {
		Object id = request.getParameter("id");
		model.addAttribute("id", id);
		return "app/userSendMassSms/filter";
	}

	@RequestMapping("/distinguishoperator")
	public String distinguishOperator(HttpServletRequest request, Model model) {
		Object id = request.getParameter("id");
		model.addAttribute("id", id);
		return "systemUser/distinguishOperator";
	}

	@RequestMapping("/operatorsectionno")
	public String operatorSectionNo(HttpServletRequest request, Model model) {
		Object id = request.getParameter("id");
		model.addAttribute("id", id);
		return "systemUser/operatorSectionNo";
	}

	@RequestMapping(value = "/filterDistinguishOperator")
	@ResponseBody
	public DzdResponse filterDistinguishOperator(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		DzdResponse dzdResponse = new DzdResponse();
		try {
			Map<String, Object> operatorMap = new HashMap<String, Object>();

			String uuid = request.getParameter(Define.REQUESTPARAMETER.FORWARDUUID);

			int mobileCurrentPage = Integer.valueOf(request.getParameter(Define.DISTINGUISHOPERATOR.MOBILECURRENTPAGE));
			int unicomCurrentPage = Integer.valueOf(request.getParameter(Define.DISTINGUISHOPERATOR.UNICOMCURRENTPAGE));
			int telecomCurrentPage = Integer
					.valueOf(request.getParameter(Define.DISTINGUISHOPERATOR.TELECOMCURRENTPAGE));
			int invalidCurrentPage = Integer
					.valueOf(request.getParameter(Define.DISTINGUISHOPERATOR.INVALIDCURRENTPAGE));

			List<SmsFileConfig> configList = smsFileConfigService.querySmsFileConfigList(uuid, null);

			if (CollectionUtils.isEmpty(configList)) {
				return dzdResponse;
			}

			List<String> mobiles = SmsSendUtil.getPhoneListByConfig(configList);

			StringBuffer fileName = new StringBuffer();
			for (SmsFileConfig smsFileConfig : configList) {
				fileName.append(smsFileConfig.getFileName() + "、");
			}
			String fName = URLDecoder.decode(fileName.substring(0, fileName.length() - 1), "UTF-8");
			operatorMap.put(Define.STATICAL.FILENAME, fName);

			Map<String, List<String>> mobileAssort = MobileCheckUtil.mobileAssort(mobiles);

			List<String> invalidList = mobileAssort.get(Define.PHONEKEY.INVALID);
			List<String> validList = mobileAssort.get(Define.PHONEKEY.VALID);
			List<String> duplicateList = mobileAssort.get(Define.PHONEKEY.DUPLICATE);

			// 构造出参数据
			operatorMap = DistinguishOperator.construcFilterRecordMap(invalidList, validList, operatorMap);

			// 总页数传递前台
			setTotalPage(request, operatorMap);

			// 过滤记录入库
			insertFilterRecord(request, duplicateList, operatorMap);

			construcPhonesByPage(operatorMap, mobileCurrentPage, unicomCurrentPage, telecomCurrentPage,
					invalidCurrentPage);

			dzdResponse.setData(operatorMap);
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
		} catch (Exception e) {
			e.printStackTrace();
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
		}
		return dzdResponse;
	}

	@SuppressWarnings("unchecked")
	private void construcPhonesByPage(Map<String, Object> operatorMap, int mobileCurrentPage, int unicomCurrentPage,
			int telecomCurrentPage, int invalidCurrentPage) {
		List<String> beformobiles = (List<String>) operatorMap.get(Define.DISTINGUISHOPERATOR.MOBILEOPERATOR);
		List<String> befortelecoms = (List<String>) operatorMap.get(Define.DISTINGUISHOPERATOR.TELECOMOPERATOR);
		List<String> beforunicoms = (List<String>) operatorMap.get(Define.DISTINGUISHOPERATOR.UNICOMOPERATOR);
		List<String> beforinvalidList = (List<String>) operatorMap.get(Define.PHONEKEY.INVALID);

		int startLength;
		int endLength;
		if (!CollectionUtils.isEmpty(beformobiles)) {
			startLength = (mobileCurrentPage - 1) * 20;
			endLength = getEndLength(beformobiles, startLength);
			operatorMap.put(Define.DISTINGUISHOPERATOR.MOBILEOPERATOR, beformobiles.subList(startLength, endLength));
		}
		if (!CollectionUtils.isEmpty(befortelecoms)) {
			startLength = (telecomCurrentPage - 1) * 20;
			endLength = getEndLength(befortelecoms, startLength);
			operatorMap.put(Define.DISTINGUISHOPERATOR.TELECOMOPERATOR, befortelecoms.subList(startLength, endLength));
		}
		if (!CollectionUtils.isEmpty(beforunicoms)) {
			startLength = (unicomCurrentPage - 1) * 20;
			endLength = getEndLength(beforunicoms, startLength);
			operatorMap.put(Define.DISTINGUISHOPERATOR.UNICOMOPERATOR, beforunicoms.subList(startLength, endLength));
		}
		if (!CollectionUtils.isEmpty(beforinvalidList)) {
			startLength = (invalidCurrentPage - 1) * 20;
			endLength = getEndLength(beforinvalidList, startLength);
			operatorMap.put(Define.PHONEKEY.INVALID, beforinvalidList.subList(startLength, endLength));
		}
	}

	private int getEndLength(List<String> beformobiles, int startLength) {
		return ((startLength + 20 - 1) > (beformobiles.size() - 1)
				|| (startLength + 20 - 1) == (beformobiles.size() - 1)) ? beformobiles.size() : startLength + 20;
	}

	@RequestMapping(value = "/saveuploadfile")
	@ResponseBody
	public DzdResponse saveUploadFileForSendMessage(@RequestParam final MultipartFile[] uploadFile,
			final HttpServletRequest request, HttpServletResponse response) throws Exception {
		DzdResponse dzdResponse = new DzdResponse();

		String uuid = request.getParameter(Define.REQUESTPARAMETER.FORWARDUUID);

		Map<String, Object> operatorMap = new HashMap<String, Object>();
		// 校验文件格式
		uploadFileCheck(uploadFile, operatorMap, dzdResponse);

		if (ErrorCodeTemplate.CODE_FAIL.equals(dzdResponse.getRetCode())) {
			return dzdResponse;
		}
		List<String> mobileList = getImputMobiles(uploadFile, operatorMap);

		if (CollectionUtils.isEmpty(mobileList)) {
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
			return dzdResponse;
		}

		StringBuffer strb = new StringBuffer();
		for (String phone : mobileList) {
			phone = FileUploadUtil.formatMobile(phone);
			strb.append(phone + "\n");
		}
		String phones = strb.toString().substring(0, strb.length() - 1);

		// 4. 上传文件到服务器
		String fileName = FileUploadUtil.saveFileInfo(uploadFile[0].getInputStream(),
				new byte[(int) uploadFile[0].getSize()], operatorMap.get(Define.STATICAL.FILENAME).toString());

		SysUser user = SessionUtils.getUser(request);
		SmsUser smsUsers = SmsServerManager.I.getUserBySysId(Long.valueOf(user.getId()));
		Integer uid = user.getId();
		if (smsUsers != null) {
			uid = smsUsers.getId().intValue();// 用户id
		}

		// 6. 保存文件信息到数据库
		String phone = phones.indexOf("\n") == -1 ? phones : phones.substring(0, phones.indexOf("\n"));
		phone = FileUploadUtil.formatMobile(phone);

		SmsFileConfig config = new SmsFileConfig();
		config.setSms_uid(uid);
		config.setUuid(uuid);
		config.setFileName(fileName);
		config.setType(1);
		config.setPhone(phone);
		config.setPhoneSize(mobileList.size());
		try {
			smsFileConfigService.add(config);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("====================》上传文件至服务器发生了异常，发送跳转失败：" + e.getMessage());
		}
		dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
		return dzdResponse;
	}

	@RequestMapping(value = "/updateoperatorsectionno")
	@ResponseBody
	public void updateOperatorSectionNo(HttpServletRequest request, HttpServletResponse response) {
		String mobile = request.getParameter(Define.DISTINGUISHOPERATOR.MOBILEOPERATOR);
		String unicom = request.getParameter(Define.DISTINGUISHOPERATOR.UNICOMOPERATOR);
		String telecom = request.getParameter(Define.DISTINGUISHOPERATOR.TELECOMOPERATOR);

		try {
			if (!StringUtils.isEmpty(mobile)) {
				String[] mobiles = mobile.split(":");
				smsFilterNumberRecordService.updateOperatorSectionNo(mobiles[0], mobiles[1]);
			}
			if (!StringUtils.isEmpty(unicom)) {
				String[] unicoms = unicom.split(":");
				smsFilterNumberRecordService.updateOperatorSectionNo(unicoms[0], unicoms[1]);
			}
			if (!StringUtils.isEmpty(telecom)) {
				String[] telecoms = telecom.split(":");
				smsFilterNumberRecordService.updateOperatorSectionNo(telecoms[0], telecoms[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("====================》号段信息更新失败：" + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/queryoperatorsectionno")
	@ResponseBody
	public List<OperatorSectionNo> queryOperatorSectionNo(HttpServletRequest request, HttpServletResponse response) {
		List<OperatorSectionNo> operatorSectionNoList = null;
		try {
			operatorSectionNoList = smsFilterNumberRecordService.queryOperatorSectionNo();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("====================》号段信息更新失败：" + e.getMessage());
		}
		return operatorSectionNoList;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/exportfilternumber")
	@ResponseBody
	public void exportFilterNumber(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> dateMap = new HashMap<String, Object>();

		// 入参数据
		String uuid = request.getParameter(Define.REQUESTPARAMETER.FORWARDUUID);
		String mobileOperator = request.getParameter(Define.DISTINGUISHOPERATOR.MOBILEOPERATOR);
		String unicomOperator = request.getParameter(Define.DISTINGUISHOPERATOR.UNICOMOPERATOR);
		String telecomOperator = request.getParameter(Define.DISTINGUISHOPERATOR.TELECOMOPERATOR);
		String invalidOperator = request.getParameter(Define.DISTINGUISHOPERATOR.INVALIDOPERATOR);

		// 获取服务器号码文件
		List<SmsFileConfig> configList = smsFileConfigService.querySmsFileConfigList(uuid, null);

		// 获取文件号码信息
		List<String> mobileList = SmsSendUtil.getPhoneListByConfig(configList);

		// 进行一次过滤，获取有效以及无效号码
		Map<String, List<String>> mobileAssort = MobileCheckUtil.mobileAssort(mobileList);

		List<String> invalidList = mobileAssort.get(Define.PHONEKEY.INVALID);
		List<String> validList = mobileAssort.get(Define.PHONEKEY.VALID);

		// 进行二次过滤，获取运营商号码信息
		dateMap = DistinguishOperator.construcFilterRecordMap(invalidList, validList, dateMap);

		// 前台选择导出号码信息
		List<String> mobiles = "1".equals(mobileOperator)
				? (List<String>) dateMap.get(Define.DISTINGUISHOPERATOR.MOBILEOPERATOR) : new ArrayList<String>();
		List<String> telecoms = "1".equals(telecomOperator)
				? (List<String>) dateMap.get(Define.DISTINGUISHOPERATOR.TELECOMOPERATOR) : new ArrayList<String>();
		List<String> unicoms = "1".equals(unicomOperator)
				? (List<String>) dateMap.get(Define.DISTINGUISHOPERATOR.UNICOMOPERATOR) : new ArrayList<String>();
		List<String> invalids = "1".equals(invalidOperator) ? invalidList : new ArrayList<String>();

		Map<String, List<String>> tmpMap = new HashMap<String, List<String>>();
		tmpMap.put(Define.DISTINGUISHOPERATOR.MOBILEOPERATOR, mobiles);
		tmpMap.put(Define.DISTINGUISHOPERATOR.UNICOMOPERATOR, unicoms);
		tmpMap.put(Define.DISTINGUISHOPERATOR.TELECOMOPERATOR, telecoms);
		tmpMap.put(Define.DISTINGUISHOPERATOR.INVALIDOPERATOR, invalids);

		SmsFilterOrderBusiness smsFilterOrderBusiness = new SmsFilterOrderBusiness();
		smsFilterOrderBusiness.orderExport(request, response, tmpMap);
	}

	private void setTotalPage(HttpServletRequest request, Map<String, Object> operatorMap) {

		int mobileLength = Integer.valueOf(operatorMap.get(Define.DISTINGUISHOPERATOR.MOBILELENGTH).toString());
		int telecomLength = Integer.valueOf(operatorMap.get(Define.DISTINGUISHOPERATOR.TELECOMLENGTH).toString());
		int unicomLength = Integer.valueOf(operatorMap.get(Define.DISTINGUISHOPERATOR.UNICOMLENGTH).toString());
		int invalidLength = Integer.valueOf(operatorMap.get(Define.DISTINGUISHOPERATOR.INVALIDLENGTH).toString());

		operatorMap.put(Define.DISTINGUISHOPERATOR.MOBILETOTALPAGE, mobileLength < PAGECALC ? 1
				: mobileLength % PAGECALC == 0 ? mobileLength / PAGECALC : mobileLength / PAGECALC + 1);
		operatorMap.put(Define.DISTINGUISHOPERATOR.UNICOMTOTALPAGE, unicomLength < PAGECALC ? 1
				: unicomLength % PAGECALC == 0 ? unicomLength / PAGECALC : unicomLength / PAGECALC + 1);
		operatorMap.put(Define.DISTINGUISHOPERATOR.TELECOMTOTALPAGE, telecomLength < PAGECALC ? 1
				: telecomLength % PAGECALC == 0 ? telecomLength / PAGECALC : telecomLength / PAGECALC + 1);
		operatorMap.put(Define.DISTINGUISHOPERATOR.INVALIDTOTALPAGE, invalidLength < PAGECALC ? 1
				: invalidLength % PAGECALC == 0 ? invalidLength / PAGECALC : invalidLength / PAGECALC + 1);
	}

	// 冒泡排序
	public void bubbleSort(int[] numbers) {
		int temp = 0;
		int size = numbers.length;
		for (int i = 0; i < size - 1; i++) {
			for (int j = 0; j < size - 1 - i; j++) {
				if (numbers[j] > numbers[j + 1]) // 交换两数位置
				{
					temp = numbers[j];
					numbers[j] = numbers[j + 1];
					numbers[j + 1] = temp;
				}
			}
		}
	}

	/**
	 * @param uploadFile
	 * @param dzdResponse
	 * @throws @Title:
	 *             uploadFileCheck
	 * @Description: 校验文件格式
	 * @author: hz-liang
	 * @return: void
	 */
	private void uploadFileCheck(final MultipartFile[] uploadFile, Map<String, Object> operatorMap,
			DzdResponse dzdResponse) {
		if (uploadFile == null) {
			dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
		} else {
			// 1. 获取文件
			String fName = uploadFile[0].getOriginalFilename();
			if (uploadFile[0].getSize() > Integer.MAX_VALUE || fName == null || fName.equals("")) {
				dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
			} else {
				// 2. 获取文件后缀
				String suffix = fName.substring(fName.lastIndexOf("."));

				// 3. 判断文件格式
				if (!(suffix.equals(".txt") || suffix.equals(".xlsx") || suffix.equals(".xls"))) {
					// 7. 文件格式不正确提示
					Map<String, String> map = new HashMap<String, String>();
					map.put("msg", "请选择txt文件或者excel文件");
					dzdResponse.setData(map);
					dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
				}

				if (suffix.equals(".txt")) {
					operatorMap.put(Define.STATICAL.FILENAMESUFFIX, Define.NUMBER.ZERO);
				} else if (suffix.equals(".xlsx")) {
					operatorMap.put(Define.STATICAL.FILENAMESUFFIX, Define.NUMBER.ONE);
				} else if (suffix.equals(".xls")) {
					operatorMap.put(Define.STATICAL.FILENAMESUFFIX, Define.NUMBER.TWO);
				}

				operatorMap.put(Define.STATICAL.FILENAME, fName);
			}

		}

	}

	/**
	 * @param request
	 * @param duplicateList
	 * @param operatorMap
	 * @throws @Title:
	 *             insertFilterRecord
	 * @Description: 过滤记录入库
	 * @author: hz-liang
	 * @return: void
	 */
	private void insertFilterRecord(final HttpServletRequest request, List<String> duplicateList,
			Map<String, Object> operatorMap) {
		SysUser user = SessionUtils.getUser(request);
		SmsUser smsUsers = SmsServerManager.I.getUserBySysId(Long.valueOf(user.getId()));
		Integer uid = user.getId();
		if (smsUsers != null) {
			uid = smsUsers.getId().intValue();// 用户id
		}

		SmsFilterNumberRecord filterNumberRecord = new SmsFilterNumberRecord();
		filterNumberRecord.setUid(uid);
		filterNumberRecord.setName(operatorMap.get(Define.STATICAL.FILENAME).toString());
		filterNumberRecord
				.setMobileNumber(Integer.valueOf(operatorMap.get(Define.DISTINGUISHOPERATOR.MOBILELENGTH).toString()));
		filterNumberRecord
				.setUnicomNumber(Integer.valueOf(operatorMap.get(Define.DISTINGUISHOPERATOR.UNICOMLENGTH).toString()));
		filterNumberRecord.setTelecomNumber(
				Integer.valueOf(operatorMap.get(Define.DISTINGUISHOPERATOR.TELECOMLENGTH).toString()));
		filterNumberRecord.setDuplicateNumber(duplicateList.size());
		filterNumberRecord
				.setWrongNumber(Integer.valueOf(operatorMap.get(Define.DISTINGUISHOPERATOR.INVALIDLENGTH).toString()));

		smsFilterNumberRecordService.saveFilterNumberRecord(filterNumberRecord);
	}

	private List<String> getImputMobiles(final MultipartFile[] uploadFile, Map<String, Object> operatorMap)
			throws Exception {
		List<String> mobileList = new ArrayList<String>();

		File tempFile = File.createTempFile("tempFile", null);
		uploadFile[0].transferTo(tempFile);

		if (Define.NUMBER.ZERO.equals(operatorMap.get(Define.STATICAL.FILENAMESUFFIX))) {
			mobileList = FileUploadUtil.readDataByTxt(tempFile);
		} else if (Define.NUMBER.ONE.equals(operatorMap.get(Define.STATICAL.FILENAMESUFFIX))) {
			mobileList = FileUploadUtil.readDataByExcelXLSX(tempFile);
		} else if (Define.NUMBER.TWO.equals(operatorMap.get(Define.STATICAL.FILENAMESUFFIX))) {
			mobileList = FileUploadUtil.readDataByExcelXLS(tempFile);
		}

		return mobileList;
	}

	/**
	 * @Description:过滤号码列表
	 * @author:wangran
	 * @time:2017年05月17日 下午2:01:34
	 */
	@SuppressWarnings({ "unchecked" })
	@RequestMapping(value = "/filterList", method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public DzdResponse filterList(HttpServletRequest request, @RequestBody Map<String, Object> data) throws Exception {
		DzdResponse dzdPageResponse = new DzdResponse();
		SysUser user = SessionUtils.getUser(request);
		try {
			WebRequestParameters parameters = getRequestParameters(WebRequestParameters.class, data);
			if (parameters == null) {
				return dzdPageResponse;
			}
			Object menuId = data.get("menuId");
			Object email = data.get("email");

			if (menuId == null) {
				return dzdPageResponse;
			}

			DzdPageParam dzdPageParam = new DzdPageParam();
			Map<String, Object> sortMap = new HashMap<String, Object>();
			if (parameters.getPagenum() != 0 && parameters.getPagesize() != 0) {
				dzdPageParam.setStart(parameters.getPagenum());
				dzdPageParam.setLimit(parameters.getPagesize());
			}
			if (email != null && !StringUtil.isEmpty(email.toString())) {
				sortMap.put("email", email.toString());
			}
			// 获取账户下面的归属账户sys_user_id
			dzdPageParam = CommonRoleServiceUtil.getSysUserId(user, dzdPageParam, sortMap, sysRoleRelService,
					smsAccountService);

			dzdPageParam.setCondition(sortMap);

			Page<SmsFilterNumberRecord> dataList = smsFilterNumberRecordService.querySmsFilterList(dzdPageParam);

			dzdPageResponse.setRows(dataList.getResult());
			dzdPageResponse.setTotal(dataList.getTotal());

		} catch (Exception e) {
			logger.error("====================》过滤号码列表查询失败：" + e.getMessage());
			e.printStackTrace();
		}
		return dzdPageResponse;
	}

	/**
	 * 跳转到发送短信页面
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/toSendView")
	public String toSendView(HttpServletRequest request) {
		try {
			Object uuid = request.getParameter("forwardUuid");
			Object operators = request.getParameter("operators");
			if (StringUtil.isEmpty(uuid.toString()) || StringUtil.isEmpty(operators.toString())) {
				return "";
			}

			// 1. 获取有效号码
			List<SmsFileConfig> configList = smsFileConfigService.querySmsFileConfigList(uuid.toString(), null);
			List<String> phoneList = SmsSendUtil.getPhoneListByConfig(configList);
			Map<String, Object> map = FileUploadUtil.getPhoneMap(phoneList, operators.toString());
			List<String> validList = (List<String>) map.get("validList");

			// 2. 上传保存新的号码文件
			SysUser user = SessionUtils.getUser(request);
			smsFileConfigService.deleteByUUID(uuid.toString(), null);
			boolean flag = PhoneFilterUtil.uploadPhoneFilter(validList, uuid.toString(), user, smsFileConfigService);
			if (flag) {
				return "redirect:/smsUser/sendView.do?forwardUuid=" + uuid.toString() + "&operators=" + operators;
			} else {
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
