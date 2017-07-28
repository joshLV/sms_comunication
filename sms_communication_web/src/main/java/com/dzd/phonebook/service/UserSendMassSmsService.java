package com.dzd.phonebook.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dzd.utils.LogUtil;
import org.apache.log4j.Logger;

import com.dzd.base.util.DateUtil;
import com.dzd.phonebook.util.ServiceBeanUtil;
import com.dzd.phonebook.util.SmsAisleGroupHasSmsAisle;
import com.dzd.sms.api.service.SmsServiceV4;
import com.dzd.sms.application.Define;
import com.dzd.sms.application.SmsServerManager;
import com.dzd.sms.service.data.CustomParameter;
import com.dzd.sms.service.data.ShieldWord;
import com.dzd.sms.service.data.SmsRequestParameter;
import com.dzd.sms.service.data.SmsUser;
import com.dzd.sms.service.data.SpecificSymbol;
import com.dzd.sms.util.DistinguishOperator;

import net.sf.json.JSONObject;

/**
 * 发送短信服务类
 *
 * @author CHENCHAO
 * @date 2017-03-28 09:33:00
 */
public class UserSendMassSmsService {
    static LogUtil logger = LogUtil.getLogger(UserSendMassSmsService.class);
    private SmsServiceV4 smsService = new SmsServiceV4();
    private ChannelService channelService;
    private static final String SMS_FLAG_BOOLEAN = "smsFlag";

    /**
     * 发送短信前的校验
     *
     * @param parameter
     * @return
     */
    public JSONObject sendBeforVerify(SmsRequestParameter parameter, ServiceBeanUtil serviceBean) {
        this.channelService = serviceBean.getChannelService();
        JSONObject json = new JSONObject();
        // 1. 校验内容、号码不为空
        JSONObject baseJson = smsContentAndMobileIsNullValid(parameter);
        if (!jsonReturnBoolean(baseJson)) {
            return baseJson;
        }

        // 1. 验证签名格式
        JSONObject signJson = smsSignatureIsNullValid(parameter.getText());
        if (!jsonReturnBoolean(signJson)) {
            return signJson;
        }

        // 2. 验证是否包含通道组敏感词
        JSONObject wordJson = smsShieldWordsValid(parameter);
        if (!jsonReturnBoolean(wordJson)) {
            return wordJson;
        }

        // 3. 验证是否包含特殊符号
        JSONObject symboJson = specialSymbolValid(parameter);
        if (!jsonReturnBoolean(symboJson)) {
            return symboJson;
        }

        // 4. 验证通道组内容是否需要加退订回T
        JSONObject unsubJson = unsubscribeValid(parameter);
        if (!jsonReturnBoolean(unsubJson)) {
            return unsubJson;
        }

        // 6.验证定时时间
        JSONObject timeVerifyJson = timingTaskVerify(parameter);
        if (!timeVerifyJson.getBoolean(SMS_FLAG_BOOLEAN)) {
            return timeVerifyJson;
        }

        // 8. 验证用户是否开通
        if (parameter.getSmsUserState() != 0) {
            return getResultBooleanJson(false, -100, Define.USERSENDSMS.SMS_SEND_USER_CLOSE);
        }

        // 9. 验证通道组是否开启
        if (parameter.getAisleGroupState() != 1) {
            return getResultBooleanJson(false, -101, Define.USERSENDSMS.SMS_SEND_AISLE_GROUP_CLOSE);
        }

        // 10. 验证通道组的开通时段
        String dredgeAM = parameter.getDredgeAM();// 开通时间段 开始时间
        String dredgePM = parameter.getDredgePM();// 开通时间段 截止时间
        if (dredgeAM != null && !dredgeAM.equals("") && dredgePM != null && !dredgePM.equals("")) {
            if (!smsAisleDredgeValid(parameter)) {
                return getResultBooleanJson(false, -102, Define.USERSENDSMS.SMS_SEND_AISLE_UNOPEN);
            }
        }

        // 11. 验证通道起发量
        if (!parameter.isTimeTaskCeck()) {
            JSONObject verifyResult = new JSONObject();
            JSONObject startCountJson = sendPhoneBySmsAisleVerify(parameter, verifyResult);
            if (!jsonReturnBoolean(startCountJson)) {
                return startCountJson;
            }
        }

        // 12. 验证账户余额
        if (!accountBalanceVerify(parameter)) {
            return getResultBooleanJson(false, -107, Define.USERSENDSMS.SMS_SEND_INSUFFICIENT_BALANCE);
        }

        // 13. 校验是否需要输入验证码
        if (!parameter.isTimeTaskCeck()) {
            if (!checkVerifyCode(parameter)) {
                JSONObject verifyJson = getResultBooleanJson(false, -300, Define.USERSENDSMS.SMS_SEND_VERIFYCODE_MSG);
                verifyJson.put("phoneMap", parameter.getValidPhoneMap());
                return verifyJson;
            }
        }

        json.put(SMS_FLAG_BOOLEAN, true);
        json.put("code", "0");
        json.put("msg", "");
        json.put("phoneMap", parameter.getValidPhoneMap());
        return json;
    }

    /**
     * 发送短信
     *
     * @param parameter
     * @param serviceBean
     * @return
     */
    public JSONObject send(SmsRequestParameter parameter, ServiceBeanUtil serviceBean) {
        JSONObject verifyResult = new JSONObject();
        this.channelService = serviceBean.getChannelService();
        try {

            // 1. 校验基本内容
            JSONObject baseJson = sendBeforVerify(parameter, serviceBean);
            if (!baseJson.getBoolean(SMS_FLAG_BOOLEAN)) {
                return baseJson;
            }

            // 4.调用服务接口发送
            return smsService.send(verifyResult, parameter, new CustomParameter(), serviceBean);
        } catch (Exception e) {
            e.printStackTrace();
            return getResultJson(-500, Define.USERSENDSMS.SMS_SEND_SYSTEM_ERROR);
        }
    }

    /**
     * 校验号码和内容不为空
     *
     * @param parameter
     * @return
     */
    private JSONObject smsContentAndMobileIsNullValid(SmsRequestParameter parameter) {
        boolean baseFlag = true;
        int code = 0;
        String msg = "";
        if ((parameter.getMobile() == null || parameter.getMobile().size() == 0) && !parameter.isTimeTaskCeck()) {// 号码为空
            baseFlag = false;
            code = -108;
            msg = Define.USERSENDSMS.SMS_SEND_MOBILE_IS_NULL;
        } else if (parameter.getText() == null || parameter.getText().equals("")) {// 内容为空
            baseFlag = false;
            code = -107;
            msg = Define.USERSENDSMS.SMS_SEND_CONTENT_IS_NULL;

        }
        return getResultBooleanJson(baseFlag, code, msg);
    }

    /**
     * 校验通道开启时间
     *
     * @param parameter
     * @return
     */
    private static boolean smsAisleDredgeValid(SmsRequestParameter parameter) {
        Date drageDateAM = DateUtil.drageDate(parameter.getDredgeAM());// 通道开始时间
        // yyyy-MM-dd
        // HH:mm
        Date drageDatePM = DateUtil.drageDate(parameter.getDredgePM());// 通道截止时间
        // yyyy-MM-dd
        // HH:mm

        // 定时时间为空,则判断当日的 HH:mm
        if (parameter.getTiming() == null) {
            Date nowDate = DateUtil.nowDrageDate();// 当前时间 yyyy-MM-dd HH:mm

            if (nowDate.getTime() < drageDateAM.getTime()) {// 当前时间小于开始时间
                logger.info("=================>>> 通道未到开启时间!");
                return false;
            } else if (nowDate.getTime() > drageDatePM.getTime()) {// 当前时间大于截止时间
                logger.info("=================>>> 通道时间段已经过了!");
                return false;
            }

        } else {// 定时时间和通道时间对比
            Date dateTiming = DateUtil.yyyymmssDate(parameter.getTiming());// yyyy-MM-dd
            // HH:mm
            Date yyyymmddTiming = DateUtil.YYYYMMDDDATE(parameter.getTiming());// yyyy-MM-dd
            Date yyyymmddNow = DateUtil.YYYYMMDDDATE(new Date());// yyyy-MM-dd

            if (dateTiming.getTime() < drageDateAM.getTime()) {// 定时任务时间小于通道开始时间
                logger.info("=================>>> 定时时间,通道未到开启时间!");
                return false;

            } else if (yyyymmddTiming.getTime() == yyyymmddNow.getTime()) {// 当前时间大于截止时间
                if (dateTiming.getTime() > drageDatePM.getTime()) {
                    logger.info("=================>>> 定时时间,通道时间段已经过了!");
                    return false;
                }
            }

        }

        return true;
    }

    /**
     * 检验签名
     *
     * @param content
     * @return
     */
    private JSONObject smsSignatureIsNullValid(String content) {
        boolean signFlag = true;
        int code = 0;
        String msg = "";

        // 注意判断签名在前，或在后，中间的不算
        String smsContent = content;
        int signStartPos = smsContent.indexOf("【");
        int signEndPos = smsContent.indexOf("】");

        // 判断是否包含【】符号
        if (signStartPos == 0 && signEndPos > 0) {
            String sign = smsContent.substring(signStartPos + 1, signEndPos);
            // 判断签名字数3~8个字
            if (sign.length() >= 3 && sign.length() <= 8) {
                // 判断是否存在双签名
                String twoContent = smsContent.substring(signEndPos + 1, smsContent.length());
                int twoStartPos = twoContent.indexOf("【");
                int twoEndPos = twoContent.indexOf("】");
                if (twoStartPos != -1 || twoEndPos != -1) {// 存在返回双签名提示
                    signFlag = false;
                    code = -107;
                    msg = Define.USERSENDSMS.SMS_SEND_SIGNTRUE_DOUBLE_MSG;
                } else {
                    signFlag = true;
                }
            } else {// 返回字数错误提示
                signFlag = false;
                code = -107;
                msg = Define.USERSENDSMS.SMS_SEND_SIGNTURE_LENGTH;
            }
        } else {// 返回没有【】签名符号
            signFlag = false;
            code = -107;
            msg = Define.USERSENDSMS.SMS_SEND_SIGNATURE;
        }
        return getResultBooleanJson(signFlag, code, msg);
    }

    /**
     * 验证是否包含屏蔽词
     *
     * @param smsRequestParameter
     * @return
     */
    private JSONObject smsShieldWordsValid(SmsRequestParameter smsRequestParameter) {
        String regEx = "[^(a-zA-Z0-9\\u4e00-\\u9fa5)]";// 过滤中英文、数字的正则
        String content = smsRequestParameter.getText();// 短信内容
        String regContent = content.replaceAll(regEx, "").trim();// 正则提取中英文、数字之后的短信内容,判断一级屏蔽词


        StringBuilder sb = new StringBuilder();
        boolean shieldWordsFlag = true;
        int code = 0;
        String msg = "";
        String[] shieldStr = smsRequestParameter.getShieldingFieldId().split(",");

        List<ShieldWord> smsShieldWordList = SmsServerManager.I.getShieldObjectList();// redis缓存中获取屏蔽词相关信息.
        for (String shieldType : shieldStr) {
            for (ShieldWord shieldWord : smsShieldWordList) {
                String wordName = shieldWord.getWordName();
                int level = shieldWord.getLevel();// 等级
                String shieldWordType = "" + shieldWord.getType();// 屏蔽词类型

                if (shieldType.equals(shieldWordType)) {
                    // 等级为1级,过滤所有符号,添加任何符号都不能过
                    if (level == 1) {
                        // 判断是否包含屏蔽词
                        if (regContent.indexOf(wordName) != -1) {
                            shieldWordsFlag = false;
                            sb.append(wordName + ",");
                            break;
                        }

                    } else if (level == 2) { // 等级为2级，
                        if (content.indexOf(wordName) != -1) {
                            shieldWordsFlag = false;
                            sb.append(wordName + ",");
                            break;
                        }
                    }
                }

            }
        }

        if (!shieldWordsFlag) {
            String wordStr = sb.toString();
            if (wordStr != null && !wordStr.equals("")) {// 移除最后一个逗号
                wordStr = wordStr.substring(0, wordStr.length() - 1);
            }
            msg = Define.USERSENDSMS.SMS_SEND_SHIELDWORD + " " + wordStr + " " + ",请返回修改。";
            code = -100;
        }
        sb = null;
        return getResultBooleanJson(shieldWordsFlag, code, msg);
    }

    /**
     * 校验是否包含特殊符号
     *
     * @param parameter
     * @return
     */
    private JSONObject specialSymbolValid(SmsRequestParameter parameter) {
        StringBuilder sb = new StringBuilder();
        boolean sysmolFlag = true;
        int code = 0;
        String msg = "";

        // redis缓存中获取特殊符号相关信息.
        List<SpecificSymbol> smsSpecificSymbolList = SmsServerManager.I.getSpecificSymbolList();
        for (SpecificSymbol smsSpecificSymbol : smsSpecificSymbolList) {
            String sysmol = smsSpecificSymbol.getName();// 特殊符号
            if (parameter.getText().indexOf(sysmol) != -1) {
                sysmolFlag = false;
                sb.append(sysmol + ",");
            }
        }
        if (!sysmolFlag) {
            String sysmol = sb.toString();
            if (sysmol != null && !sysmol.equals("")) {// 移除最后一个逗号
                sysmol = sysmol.substring(0, sysmol.length() - 1);
            }
            msg = Define.USERSENDSMS.SMS_SEND_SPECIALSYMBOLMSG + " " + sysmol + " " + ",请返回修改。";
            code = -100;
        }

        return getResultBooleanJson(sysmolFlag, code, msg);
    }

    /**
     * 验证内容是否有加退订回T
     *
     * @param parameter
     * @return
     */
    private JSONObject unsubscribeValid(SmsRequestParameter parameter) {
        boolean unsubFlag = false;
        int code = -1;
        String msg = "";

        Integer unregTypeId = parameter.getUnregTypeId(); // 退订格式 0-关，1-开
        if (unregTypeId == 1) {
            // 判断结尾是否存在退订回X、回X退订
            String content = parameter.getText();
            String content2 = content.split("】")[1];
            if (content2.indexOf("回") == -1 || content2.indexOf("退订") == -1) {
                unsubFlag = false;
                code = -1;
            } else {
                int posi = content.lastIndexOf("回");
                int pos = content.lastIndexOf("退订");
                String newcont = content.substring(posi);
                String crecont = content.substring(pos);
                if (!regEx(newcont) || !regEx(crecont)) {
                    unsubFlag = false;
                } else {
                    if (newcont.length() > 4 && newcont.endsWith("退订")) { // 回复D退订，回TD退订
                        newcont = newcont.substring(0, newcont.length() - 2);
                        if (newcont.startsWith("回复")) {// 回复
                            newcont = newcont.substring(2);
                            if (newcont.length() <= 4) {
                                unsubFlag = true;
                            }
                        } else {// 回TGG退订
                            newcont = newcont.substring(1);
                            if (newcont.length() <= 4) {
                                code = 1;
                                unsubFlag = true;
                            }
                        }
                    } else if (newcont.length() == 4 && newcont.endsWith("退订")) {// 回T退订
                        // 回复退订
                        if (!newcont.startsWith("回复")) {
                            code = 1;
                            unsubFlag = true;
                        }
                    } else if (newcont.length() == 4 && newcont.startsWith("回退订")) {// 回退订T
                        code = 1;
                        unsubFlag = true;
                    } else if (newcont.length() > 4 && newcont.startsWith("回退订")) {
                        if (newcont.length() <= 7) {
                            code = 1;
                            unsubFlag = true;
                        }
                    } else if (newcont.length() > 4 && newcont.startsWith("回复退订")) {
                        if (newcont.length() <= 8) {
                            code = 1;
                            unsubFlag = true;
                        }
                    } else if (crecont.contains("回")) {// 退订
                        crecont = crecont.substring(2);
                        if (crecont.startsWith("回")) {// 退订回
                            if (crecont.startsWith("回复")) {// 退订回复
                                if (3 <= crecont.length() && crecont.length() <= 6) {
                                    code = 1;
                                    unsubFlag = true;
                                }
                            } else {
                                if (2 <= crecont.length() && crecont.length() <= 5) {
                                    code = 1;
                                    unsubFlag = true;
                                }
                            }
                        } else {// 退订 44 回,退订44回复
                            if (crecont.endsWith("回")) {
                                if (2 <= crecont.length() && crecont.length() <= 5) {
                                    code = 1;
                                    unsubFlag = true;
                                }
                            } else if (crecont.endsWith("回复")) {
                                if (3 <= crecont.length() && crecont.length() <= 6) {
                                    code = 1;
                                    unsubFlag = true;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            unsubFlag = true;
        }
        if (!unsubFlag) {
            msg = Define.USERSENDSMS.SMS_SEND_AISLE_HUIFU;
        }
        return getResultBooleanJson(unsubFlag, code, msg);

    }

    /**
     * 判断特殊符号
     *
     * @param str
     * @return
     */
    public boolean regEx(String str) {
        String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$";
        boolean flag = str.matches(regex);
        return flag;
    }

    /**
     * 退订格式判断结果
     *
     * @param str
     * @return
     */
    public boolean checkResult(String str) {
        boolean flag = false;
        if (str.length() > 4 || str.equals("")) {
            flag = false;
        } else if (!regEx(str)) {
            flag = false;
        } else {
            flag = true;
        }
        return flag;
    }

    /**
     * 验证通道的开启状态、配置的起发量
     *
     * @param smsRequestparameter
     * @param verifyResult
     * @return
     */
    private JSONObject sendPhoneBySmsAisleVerify(SmsRequestParameter smsRequestparameter, JSONObject verifyResult) {
        Long groupId = smsRequestparameter.getGroupTypeId();// 通道组id
        Map<String, Object> mobileMap = DistinguishOperator.construcFilterRecordMap(new ArrayList<String>(),
                smsRequestparameter.getMobile(), new HashMap<String, Object>());
        Integer mobileOperator = (Integer) mobileMap.get(Define.DISTINGUISHOPERATOR.MOBILELENGTH);// 移动号码数量
        Integer unicomOperator = (Integer) mobileMap.get(Define.DISTINGUISHOPERATOR.UNICOMLENGTH);// 联通号码数量
        Integer telecomOperator = (Integer) mobileMap.get(Define.DISTINGUISHOPERATOR.TELECOMLENGTH);// 电信号码数量
        Integer invalidOperator = (Integer) mobileMap.get(Define.DISTINGUISHOPERATOR.UNKNOWNLENGTH);// 未知号码数量

        // 验证是否符合通道配置的起发量
        if (unicomOperator > 0) {// 验证联通 0
            SmsAisleGroupHasSmsAisle smsAisleInfo = channelService.querySmsAisleHasSmsAisleGroupByOperatorId(groupId,
                    0);
            if (smsAisleInfo != null) {
                commonVerifyOperator(smsAisleInfo, verifyResult, smsRequestparameter,
                        Define.DISTINGUISHOPERATOR.UNICOMCHINA_NAME, unicomOperator);
                if (!verifyResult.getBoolean(SMS_FLAG_BOOLEAN)) {
                    return verifyResult;
                }
            }
        }

        // 验证移动号码 1
        if (mobileOperator > 0) {
            SmsAisleGroupHasSmsAisle smsAisleInfo = channelService.querySmsAisleHasSmsAisleGroupByOperatorId(groupId,
                    1);
            if (smsAisleInfo != null) {
                commonVerifyOperator(smsAisleInfo, verifyResult, smsRequestparameter,
                        Define.DISTINGUISHOPERATOR.MOBILECHINA_NAME, mobileOperator);
                if (!verifyResult.getBoolean(SMS_FLAG_BOOLEAN)) {
                    return verifyResult;
                }
            }
        }

        // 验证电信号码 2
        if (telecomOperator > 0) {
            SmsAisleGroupHasSmsAisle smsAisleInfo = channelService.querySmsAisleHasSmsAisleGroupByOperatorId(groupId,
                    2);
            if (smsAisleInfo != null) {
                commonVerifyOperator(smsAisleInfo, verifyResult, smsRequestparameter,
                        Define.DISTINGUISHOPERATOR.TELECOMCHINA_NAME, telecomOperator);
                if (!verifyResult.getBoolean(SMS_FLAG_BOOLEAN)) {
                    return verifyResult;
                }
            }
        }

        // 验证未知号码 -1
        if (invalidOperator > 0) {
            SmsAisleGroupHasSmsAisle smsAisleInfo = channelService.querySmsAisleHasSmsAisleGroupByOperatorId(groupId,
                    -1);
            if (smsAisleInfo != null) {
                commonVerifyOperator(smsAisleInfo, verifyResult, smsRequestparameter,
                        Define.DISTINGUISHOPERATOR.INVALIDCHINA_NAME, invalidOperator);
                if (!verifyResult.getBoolean(SMS_FLAG_BOOLEAN)) {
                    return verifyResult;
                }
            }
        } else {
            verifyResult.put(SMS_FLAG_BOOLEAN, true);
        }
        return verifyResult;
    }

    /**
     * 三网通道校验公共方法
     *
     * @param smsAisleInfo
     * @param verifyResult
     * @param smsRequestparameter
     */
    private void commonVerifyOperator(SmsAisleGroupHasSmsAisle smsAisleInfo, JSONObject verifyResult,
                                      SmsRequestParameter smsRequestparameter, String operatorName, Integer commonPhoneSize) {
        smsRequestparameter.setStartCount(smsAisleInfo.getStartCount());
        // 验证起发数量
        if (!calcSingleSmsLength(smsRequestparameter, commonPhoneSize)) {// 发送短信数小于通道起发数量
            verifyResult.put(SMS_FLAG_BOOLEAN, false);
            verifyResult.put("code", -107);
            verifyResult.put("msg",
                    "运营商要求" + operatorName + "号码" + smsRequestparameter.getStartCount() + "个起发,请返回补充号码数量。");
        } else {
            verifyResult.put(SMS_FLAG_BOOLEAN, true);
        }
    }

    /**
     * 验证定时时间
     *
     * @param parameter
     * @return
     */
    private JSONObject timingTaskVerify(SmsRequestParameter parameter) {
        boolean timeFlag = true;
        int code = 0;
        String msg = "";

        Date timingDate = parameter.getTiming();
        long time = 10 * 60 * 1000;
        Long nowLong = new Date().getTime();
        if (timingDate != null) {
            Long timingLong = timingDate.getTime();
            if (timingLong < nowLong) {// 定时时间小于当前时间
                timeFlag = false;
                msg = Define.USERSENDSMS.SMS_SEND_TIMING_INVALID;
                code = -105;
            } else if (timingLong - nowLong <= time) { // 定时时间在十分钟之内
                timeFlag = false;
                code = -106;
                msg = Define.USERSENDSMS.SMS_SEND_TIMING_BEFORE;
            }
        }
        return getResultBooleanJson(timeFlag, code, msg);
    }

    /**
     * 验证发送量时候少于通道最低发送量
     *
     * @param parameter
     */
    private static boolean calcSingleSmsLength(SmsRequestParameter parameter, Integer commonPhoneSize) {
        Integer minSendNum = 0;
        if(parameter.getStartCount()!=null){
            minSendNum = parameter.getStartCount();
        }
        Integer smsLength = commonPhoneSize;
        if (smsLength < minSendNum) {// 发送短信数小于通道起发数量
            return false;
        }
        return true;
    }

    /**
     * 校验账户余额
     *
     * @param parameter
     * @return
     */
    private boolean accountBalanceVerify(SmsRequestParameter parameter) {
        Long uid = Long.valueOf(parameter.getUid());
        SmsUser user = SmsServerManager.I.getUser(uid);
        int surplus_num = parameter.isTimeTaskCeck() ? parameter.getSurplusNum() : getSmsLength(parameter);// 发送短信条数

        // 判断帐户余额
        Integer useBalance = SmsServerManager.I.getUserSurplusNum(user.getId());// redis缓存获取账号余额信息
        if (useBalance < surplus_num) {// 账户余额不足
            return false;
        }
        return true;
    }

    /**
     * 验证是否需要输入验证码
     *
     * @param parameter
     * @return
     */
    private boolean checkVerifyCode(SmsRequestParameter parameter) {
        Integer smsLength = UserSendMassSmsService.getSmsLength(parameter);// 计费条数
        if (smsLength > 49) {
            if (parameter.getVerifyType() == 0 && parameter.getSendType() == 0) {// 首次登陆并且不是定时任务,需要验证码
                return false;
            }
        }

        return true;
    }

    /**
     * 校验短信条数高于49以上,则需要输入验证码
     *
     * @param parameter
     * @return
     */
    public static boolean securityCodeVerify(SmsRequestParameter parameter) {
        int surplus_num = getSmsLength(parameter);// 发送短信条数
        if (surplus_num > 49) {
            return true;
        }
        return false;
    }

    /**
     * 计算短信条数
     *
     * @param parameter
     * @return
     */
    public static Integer getSmsLength(SmsRequestParameter parameter) {
        Integer smsLength = 0;
        if (parameter.getText().length() > 70) {// 计算短信条数
            Integer singleSmsLength;
            int smsTextLen = parameter.getText().length() - 70;
            singleSmsLength = (smsTextLen / parameter.getAisleLongNum()) + 1;
            // 尾数加1条
            if (smsTextLen % parameter.getAisleLongNum() > 0) {
                singleSmsLength += 1;
            }
            smsLength = parameter.getMobile().size() * singleSmsLength;
        } else {
            smsLength = parameter.getMobile().size();
        }
        return smsLength;
    }

    /**
     * 返回的公共json
     *
     * @param code
     * @param msg
     * @return
     */
    public JSONObject getResultJson(Integer code, String msg) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        return json;
    }

    public JSONObject getResultBooleanJson(boolean flag, Integer code, String msg) {
        JSONObject json = new JSONObject();
        json.put(SMS_FLAG_BOOLEAN, flag);
        json.put("code", code);
        json.put("msg", msg);
        return json;
    }

    private boolean jsonReturnBoolean(JSONObject json) {
        if (!json.getBoolean(SMS_FLAG_BOOLEAN)) {
            return false;
        }
        return true;
    }


}
