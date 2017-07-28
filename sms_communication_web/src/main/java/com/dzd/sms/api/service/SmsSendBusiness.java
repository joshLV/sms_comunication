package com.dzd.sms.api.service;

import static com.dzd.sms.application.Define.INTERFACE_STATE_BALANCE_NONE;
import static com.dzd.sms.application.Define.INTERFACE_STATE_DEDUCTION_FAIL;
import static com.dzd.sms.application.Define.INTERFACE_STATE_EMPTY_PHONE;
import static com.dzd.sms.application.Define.INTERFACE_STATE_INSUFFICIENT_BALANCE;
import static com.dzd.sms.application.Define.INTERFACE_STATE_KEY_INVALID;
import static com.dzd.sms.application.Define.INTERFACE_STATE_KEY_UNENABLE;
import static com.dzd.sms.application.Define.INTERFACE_STATE_NONE_AISLE_GROUP;
import static com.dzd.sms.application.Define.INTERFACE_STATE_SHIELD;
import static com.dzd.sms.application.Define.INTERFACE_STATE_SIGN_LONG;
import static com.dzd.sms.application.Define.INTERFACE_STATE_SIGN_NOEXSITS;
import static com.dzd.sms.application.Define.INTERFACE_STATE_SIGN_NONE;
import static com.dzd.sms.application.Define.INTERFACE_STATE_SUCCESS;
import static com.dzd.sms.application.Define.INTERFACE_STATE_SIGN_ERROR;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dzd.sms.application.Define;
import com.dzd.sms.service.data.*;
import com.dzd.sms.util.DistinguishOperator;
import com.dzd.utils.LogUtil;
import org.apache.log4j.Logger;

import com.dzd.cache.redis.manager.RedisClient;
import com.dzd.sms.application.SmsServerManager;
import com.dzd.utils.StringUtil;

import net.sf.json.JSONObject;

/**
 * @ClassName: SmsSendBusiness
 * @Description: 短信发送的相关业务操作
 * @author: hz-liang
 * @date: 2017年3月28日 下午6:04:31
 */
public class SmsSendBusiness {
    static LogUtil logger = LogUtil.getLogger(SmsSendBusiness.class);

    /**
     * @param requestParameter
     * @param parameter
     * @param uid
     * @param user
     * @throws
     * @Title: checkApiKey
     * @Description: 校验apikey
     * @author: hz-liang
     * @return: void
     */
    public static void checkApiKey(SmsRequestParameter requestParameter, CustomParameter parameter,
                                   Long uid, SmsUser user) {
        if (user != null) {
            if (StringUtil.string2MD5(uid + user.getKey() + requestParameter.getTimestamp())
                    .equals(requestParameter.getSign())) {
                if (user.getState() == 0) {
                    parameter.setValid(true);
                    parameter.setUserId(user.getId());
                } else {
                    parameter.setValid(false);
                    parameter.setSmsState(INTERFACE_STATE_KEY_UNENABLE);
                }
            } else {
                logger.error(" 签名错误。");
                parameter.setValid(false);
                parameter.setSmsState(INTERFACE_STATE_SIGN_ERROR);
            }
        } else {
            logger.error(" uid=" + uid + " 没有找到用户。");
            parameter.setValid(false);
            parameter.setSmsState(INTERFACE_STATE_BALANCE_NONE);
        }
    }



    /**
     * @param requestParameter
     * @param parameter
     * @param user
     * @return
     * @throws
     * @Title: singNatureCheckAndGroupUserRelation
     * @Description: 签名验证（或者添加签名）、验证通道配置信息、计算内容长度
     * @author: hz-liang
     * @return: SmsAisleGroupUserRelation
     */
    public static void singNatureCheckAndGroupUserRelation(
            SmsRequestParameter requestParameter, CustomParameter parameter, SmsUser user) {
        if (!parameter.isValid()) {

            return;
        }


        setSmsAuditContentWithSingNature(parameter, user);

        // 验证通道配置信息
        /*smsAisleGroupUserRelation = checkGroupUserRelation(requestParameter, parameter,
                smsAisleGroupUserRelation);*/

        // 计算内容长度
        calcSingleSmsLength(parameter);

    }

    private static void calcSingleSmsLength(CustomParameter parameter) {
        Integer aisleSmsLength = parameter.getAisleSmsLength();
        if (parameter.getSmsContent().length() > 70) {
            Integer singleSmsLength;
            int smsTextLen = parameter.getSmsContent().length() - 70;
            singleSmsLength = (Integer) (smsTextLen / aisleSmsLength) + 1;
            // 尾数加1条
            if (smsTextLen % aisleSmsLength > 0) {
                singleSmsLength += 1;
            }
            parameter.setSingleSmsLength(singleSmsLength);
        }
    }



    private static void setSmsAuditContentWithSingNature(CustomParameter parameter, SmsUser user) {
        // 注意判断签名在前，或在后，中间的不算
        String smsContent = parameter.getSmsContent();
        int signStartPos = smsContent.indexOf("【");
        int signEndPos = smsContent.indexOf("】");

        String sign = null;

        if (signStartPos == 0 && signEndPos > 0) {
            sign = smsContent.substring(signStartPos + 1, signEndPos);
        }

        if (sign != null) {
            parameter.setSmsAuditContent(smsContent.replaceAll("【" + sign + "】", ""));
        }

        if (sign != null && user.getSignature().indexOf(sign) != -1) {
            parameter.setAudit(false);
        }

        parameter.setSmsContent(smsContent);
    }


    /**
     * @param requestParameter
     * @param result
     * @param smsMessageList
     * @param validMobileList
     * @param parameter
     * @throws
     * @Title: filterPhoneNumber
     * @Description: 过滤手机号码、设置相关发送详情信息：区域、价格、短信内容长度等
     * @author: hz-liang
     * @return: void
     */
    public static void filterPhoneNumber(SmsRequestParameter requestParameter, JSONObject result,
                                         List<SmsMessage> smsMessageList, List<String> validMobileList,
                                         CustomParameter parameter) {
        if (!parameter.isValid()) {
            return;
        }
        List<String> mobiles = requestParameter.getMobile();

        validMobileList.addAll(mobiles);
        parameter.setVailPhoneNumber(validMobileList.size());

        // redis缓存获取任务ID
        parameter.setTaskId(SmsServerManager.I.getTaskId());
        result.put("sid", parameter.getTaskId()); // 任务ID

        // 设置相关发送详情信息：区域、价格、短信内容长度等
        setSendInfo(smsMessageList, validMobileList, parameter);

        logger.info("接收号码数=" + parameter.getAllPhoneNumber() + ",错误的号码数=" + parameter.getErrorPhoneNumber() + ",有效的号码数="
                + parameter.getVailPhoneNumber());
        // 发送的手机号码有效
        if (smsMessageList.size() == 0) {
            parameter.setValid(false);
            parameter.setSmsState(INTERFACE_STATE_EMPTY_PHONE);
        }
    }

    /**
     * @param smsMessageList
     * @param validMobileList
     * @param parameter
     * @throws
     * @Title: setSendInfo
     * @Description: 设置相关发送详情信息：区域、价格、短信内容长度等
     * @author: hz-liang
     * @return: void
     */
    private static void setSendInfo(List<SmsMessage> smsMessageList, List<String> validMobileList,
                                    CustomParameter parameter) {
        String prefix = null;
        String[] phoneInfoStr = null;
        String phnoeStr = null;
        Map<String, String> phoneInfoMap = SmsServerManager.I.getPhoneInfoMap2();

        // 遍历不重复手机号，设置相关发送详情信息：区域、价格、短信内容长度等
        for (String m : validMobileList) {
            prefix = m.substring(0, 7);
//			phoneInfo = redisManager.getPhoneInfo(jedis, prefix);// redis缓存获取号码营运商信息
            phnoeStr = phoneInfoMap.get(prefix);
            phoneInfoStr = phnoeStr == null ? null : phnoeStr.split(",");
            if (phoneInfoStr != null) {
                // 存在手机号运营商详情信息时设置具体发送详情
                setSendInfoWithPhoneinfo(smsMessageList, parameter, phoneInfoStr, m);
            } else {
                // 不存在手机号运营商详情信息时设置全国发送
                setSendInfoWithNoPhoneinfo(smsMessageList, parameter, m);
            }
        }
//		jedis.close();
        parameter.setSurplus_num(validMobileList.size() * parameter.getSingleSmsLength());
    }

    /**
     * @param smsMessageList
     * @param parameter
     * @param m
     * @throws
     * @Title: setSendInfoWithNoPhoneinfo
     * @Description: 不存在手机号运营商详情信息时设置全国发送
     * @author: hz-liang
     * @return: void
     */
    private static void setSendInfoWithNoPhoneinfo(List<SmsMessage> smsMessageList,
                                                   CustomParameter parameter, String m) {

        int supplier = DistinguishOperator.getSupplier( m );


        SmsMessage smsMessage;
        // 号码运营商信息为空则该短信为发全国
        smsMessage = new SmsMessage(m, parameter.getTaskId().toString(), 0.035);
        smsMessage.setProvince("全国");
        smsMessage.setRegionId(0l);
        smsMessage.setSupplier(supplier);
        smsMessage.setNum(parameter.getSingleSmsLength());

        smsMessageList.add(smsMessage);
    }

    /**
     * @param smsMessageList
     * @param parameter
     * @param m
     * @throws
     * @Title: setSendInfoWithPhoneinfo
     * @Description: 存在手机号运营商详情信息时设置具体发送详情
     * @author: hz-liang
     * @return: void
     */
    private static void setSendInfoWithPhoneinfo(List<SmsMessage> smsMessageList,
                                                 CustomParameter parameter, String[] phoneInfoStr, String m) {
        SmsMessage smsMessage;
        smsMessage = new SmsMessage(m, parameter.getTaskId().toString(), 0.035);
        smsMessage.setProvince(phoneInfoStr[1]);
        smsMessage.setRegionId(Long.valueOf(phoneInfoStr[2]));
        smsMessage.setSupplier(Integer.valueOf(phoneInfoStr[3]));
        smsMessage.setNum(parameter.getSingleSmsLength());

        smsMessageList.add(smsMessage);
    }

    /**
     * @param parameter
     * @throws
     * @Title: checkAccountBalance
     * @Description: 验证帐户余额
     * @author: hz-liang
     * @return: void
     */
    public static void checkAccountBalance(CustomParameter parameter) {
        if (!parameter.isValid()) {
            return;
        }
        int surplus_num = parameter.getSurplus_num();


        // 判断帐户余额
        Integer useBalance = SmsServerManager.I.getUserSurplusNum(parameter.getUserId());// redis缓存获取账号余额信息

        logger.info(parameter.getTaskId() + "  surplus_num=" + surplus_num + ",singleSmsLength="
                + parameter.getSingleSmsLength() + " text length="
                + parameter.getSmsContent().length() + " useBalance=" + useBalance);


        if (useBalance < surplus_num) {
            parameter.setValid(false);
            parameter.setSmsState(INTERFACE_STATE_INSUFFICIENT_BALANCE);// 帐户余额不足
        }
    }

    public static void debitFromAccount(CustomParameter parameter) {
        if (parameter.isValid()) {
            if (!SmsServerManager.I.reduceUserBalance(parameter.getUserId(),
                    parameter.getSurplus_num(), "任务ID：" + parameter.getTaskId())) {
                parameter.setValid(false);
                parameter.setSmsState(INTERFACE_STATE_DEDUCTION_FAIL);// APIKEY无效
            }
        }
    }

    /**
     * @param requestParameter
     * @param result
     * @param smsMessageList
     * @param validMobileList
     * @param parameter
     * @param user
     * @throws
     * @Title: submitSendInfo
     * @Description: 发送提交
     * @author: hz-liang
     * @return: void
     */
    public static void submitSendInfo(SmsRequestParameter requestParameter, JSONObject result,
                                      List<SmsMessage> smsMessageList, List<String> validMobileList,
                                      CustomParameter parameter, Long groupTypeId,
                                      SmsUser user) {
        if (!parameter.isValid()) {
            return;
        }
        parameter.setTiming(requestParameter.getTiming());
        // 任务添加到队列
        SmsTaskData smsTaskData = addSmsTask(smsMessageList, parameter, groupTypeId,
                user);

        // 设置发送方式
        smsTaskData.setText(requestParameter.getText());

        // 不需要审核， 加上发送时间
        if (!parameter.isAudit()) {
			smsTaskData.setTaskSendTime(requestParameter.getTiming() == null ? new Date()
			        : requestParameter.getTiming());
            smsTaskData.setFree(true);
            smsTaskData.setAuditState(true);
            smsTaskData.setAuditTime(new Date());
        }

        // 先缓存任务
        SmsServerManager.I.cacheSmsTaskDate(smsTaskData);

        // 必需先加号码!!!!!!!!!!!!!!!!!
        String key = SmsServerManager.I.getSmsMessageQueueKey(parameter.getTaskId());// 构建缓存队列key值

        RedisClient redisClient = SmsServerManager.I.redisClient;
        //redisClient.openResource();
        // 预设手机号码任务队列，以及入库队列
        SmsServerManager.I.putSmsTaskMessageList(key.getBytes(), smsMessageList);
        //redisClient.closeResource();

        // 添加短信任务
        // SmsWarehouse.getInstance().putSmsTask(smsTaskData);
        SmsServerManager.I.putSmsTaskPending(smsTaskData);// 待审核任务
        System.out.println("SmsServiceV2.send... mobile count=" + validMobileList.size());
        result.put("count", validMobileList.size());
        result.put("billing_num", parameter.getSurplus_num());
    }

    /**
     * @param smsMessageList
     * @param parameter
     * @param user
     * @return
     * @throws
     * @Title: addSmsTask
     * @Description: 任务添加到队列
     * @author: hz-liang
     * @return: SmsTaskData
     */
    private static SmsTaskData addSmsTask(List<SmsMessage> smsMessageList,
                                          CustomParameter parameter, Long groupTypeId,
                                          SmsUser user) {
        int vailPhoneNumber = parameter.getVailPhoneNumber();
        SmsTaskData smsTaskData = new SmsTaskData(parameter.getTaskId(), vailPhoneNumber,
                parameter.getUserId());
        smsTaskData.setError_phone_num(parameter.getErrorPhoneNumber());// 错误号码数量
        smsTaskData.setBlacklist_phone_num(0);// 黑名单数量
        smsTaskData.setExpect_deduction(0.0);
        smsTaskData.setActual_deduction(0.0); // 实际扣费
        smsTaskData.setSend_num(vailPhoneNumber);// 发送数量
        if (parameter.getTiming() != null) {
            smsTaskData.setTiming(parameter.getTiming());
            smsTaskData.setTaskType(1);
        }
        // smsTaskData.setAisleName( smsAisleGroupUserRelation.get);
        smsTaskData.setAisleGroupId(groupTypeId);
        smsTaskData.setBilling_num(vailPhoneNumber * parameter.getSingleSmsLength());// 计费数量

        smsTaskData.setCallbackUrl(user.getReportUrl());

        System.out.println("smsMessageList.size=" + smsMessageList.size() + ",singleSmsLength= "
                + parameter.getSingleSmsLength() + " !isAudit=" + (!parameter.isAudit()));
        return smsTaskData;
    }

}
