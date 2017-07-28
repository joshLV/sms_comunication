package com.dzd.sms.util;

import com.dzd.sms.application.Define;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author 作者
 *         E-mail: *
 * @version 1.0 *
 * @date 创建时间：2017年5月26日 下午2:19:02 *
 * @parameter *
 * @return
 * @since *
 */
public class DistinguishOperator {
    static List<String> mobilePrefix = Arrays.asList("134", "135", "136", "137", "138", "139",
            "150", "151", "157", "158", "159", "182", "183", "184", "187", "188", "147", "152",
            "178", "1703", "1705", "1706");// 移动
    static List<String> telecomPrefix = Arrays.asList("133", "153", "172", "173", "177", "180",
            "181", "189", "1700", "1701", "1702");// 电信
    static List<String> unicomPrefix = Arrays.asList("130", "131", "132", "155", "156", "185",
            "186", "144", "145", "171", "175", "176", "1709", "1707", "1708");// 联通


    /**
     * @param invalidList
     * @param validList
     * @return
     * @throws
     * @Title: construcFilterRecordMap
     * @Description: 构造出参数据
     * @author: hz-liang
     * @return: Map<String,Object>
     */
    public static Map<String, Object> construcFilterRecordMap(List<String> invalidList,
                                                              List<String> validList, Map<String, Object> operatorMap) {
        List<String> mobiles = new ArrayList<String>();// 移动
        List<String> unicoms = new ArrayList<String>();// 联通
        List<String> telecoms = new ArrayList<String>();// 电信
        List<String> unknown = new ArrayList<String>();// 未知

        // 0：联通，1：移动，2：电信
        for (String phone : validList) {
            if (0 == DistinguishOperator.getSupplier(phone)) {
                unicoms.add(phone);
            } else if (1 == DistinguishOperator.getSupplier(phone)) {
                mobiles.add(phone);
            } else if (2 == DistinguishOperator.getSupplier(phone)) {
                telecoms.add(phone);
            } else if (-1 == DistinguishOperator.getSupplier(phone)) {
                unknown.add(phone);
            }
        }
        
        operatorMap.put(Define.DISTINGUISHOPERATOR.MOBILEOPERATOR, mobiles);
        operatorMap.put(Define.DISTINGUISHOPERATOR.TELECOMOPERATOR, telecoms);
        operatorMap.put(Define.DISTINGUISHOPERATOR.UNICOMOPERATOR, unicoms);
        operatorMap.put(Define.DISTINGUISHOPERATOR.INVALIDOPERATOR, unknown);
        operatorMap.put(Define.PHONEKEY.INVALID, unknown);

        operatorMap.put(Define.DISTINGUISHOPERATOR.INVALIDLENGTH, unknown.size());// 无效号码数量
        operatorMap.put(Define.DISTINGUISHOPERATOR.MOBILELENGTH, mobiles.size());// 移动号码数量
        operatorMap.put(Define.DISTINGUISHOPERATOR.TELECOMLENGTH, telecoms.size());// 电信号码数量
        operatorMap.put(Define.DISTINGUISHOPERATOR.UNICOMLENGTH, unicoms.size());//  联通号码数量
        operatorMap.put(Define.DISTINGUISHOPERATOR.UNKNOWNLENGTH, unknown.size());// 未知号码数量
        return operatorMap;
    }


    /**
     * 参数必需是大于4位的号码前缀
     *
     * @param phone
     * @return
     */
    public static int getSupplier(String phone) {

        // 0：联通，1：移动，2：电信,-1未知
        String pre = phone.substring(0, 3);
        String phonePrefix = phone.substring(0, 4);
        if (mobilePrefix.contains(pre) || mobilePrefix.contains(phonePrefix)) {
            return 1;
        }

        if (unicomPrefix.contains(pre) || unicomPrefix.contains(phonePrefix)) {
            return 0;
        }

        if (telecomPrefix.contains(pre) || telecomPrefix.contains(phonePrefix)) {
            return 2;
        }

        return -1;
    }

}
