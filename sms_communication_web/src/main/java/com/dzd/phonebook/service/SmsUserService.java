package com.dzd.phonebook.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dzd.phonebook.entity.SysMenuBtn;
import com.dzd.sms.application.Define;
import com.dzd.utils.LogUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.dzd.base.service.BaseService;
import com.dzd.phonebook.dao.SmsUserDao;
import com.dzd.phonebook.entity.SmsUserMessage;
import com.dzd.phonebook.util.DzdPageParam;
import com.dzd.phonebook.util.SmsAisleGroup;
import com.dzd.phonebook.util.SmsAisleGroupHasSmsUser;
import com.dzd.phonebook.util.SmsAisleGroupType;
import com.dzd.phonebook.util.SmsRechargeUser;
import com.dzd.phonebook.util.SmsUser;
import com.dzd.phonebook.util.SmsUserBlank;
import com.dzd.phonebook.util.SmsUserMoneyRunning;
import com.github.pagehelper.Page;


/**
 * @Description:会员服务类
 * @author:oygy
 * @time:2017年1月10日 上午11:08:06
 */
@Service("smsUserService")
public class SmsUserService<T> extends BaseService<T> {

    private final static LogUtil log = LogUtil.getLogger(SmsUserService.class);

    @Autowired
    private SmsUserDao<T> mapper;

    public SmsUserDao<T> getDao() {
        return mapper;
    }

    /**
     * @Description:查询代理列表
     * @author:ougy
     * @time:2016年12月31日 下午2:18:48
     */
    public Page<SmsUser> querySmsUserList(DzdPageParam dzdPageParam) {
        return getDao().querySmsUserListPage(dzdPageParam);
    }


    /**
     * @Description:统计短信条数
     * @author:oygy
     * @time:2017年3月29日 下午4:43:35
     */
    public Integer querySmsStatistical(DzdPageParam dzdPageParam) {
        return getDao().querySmsStatistical(dzdPageParam);
    }

    /**
     * @Description:添加代理钱包
     * @author:oygy
     * @time:2016年12月31日 下午5:23:24
     */
    public void addSmsUserBlank(SmsUserBlank smsUserBlank) {
        getDao().addSmsUserBlank(smsUserBlank);
    }

    /**
     * @Description:添加代理信息
     * @author:oygy
     * @time:2016年12月31日 下午5:29:16
     */
    public void saveSmsUser(SmsUser smsUser) {
        getDao().saveSmsUser(smsUser);
    }

    /**
     * @Description:修改代理信息
     * @author:oygy
     * @time:2016年12月31日 下午5:29:16
     */
    public void updateSmsUser(SmsUser smsUser) {
        getDao().updateSmsUser(smsUser);
    }

    /**
     * @Description:修改代理钱包
     * @author:oygy
     * @time:2016年12月31日 下午5:23:24
     */
    public void updateSmsUserBlank(SmsUserBlank smsUserBlank) {
        getDao().updateSmsUserBlank(smsUserBlank);
    }

    /**
     * @Description:根据用户ID删除通道组与用户关系表中的所有数据
     * @author:oygy
     * @time:2017年1月6日 下午3:01:14
     */
    public void deleteSmsAislegroupHasUser(Integer smsUid) {
        getDao().deleteSmsAislegroupHasUser(smsUid);
    }

    /**
     * @Description:
     * @author:oygy
     * @time:2017年1月6日 下午3:28:30
     */
    public List<SmsAisleGroupHasSmsUser> queryUserHasGroup(Integer smsUid) {
        return getDao().queryUserHasGroup(smsUid);
    }

    /**
     * @Description:根据用户级别查询出用户所有符合该级别的vip通道组
     * @author:oygy
     * @time:2017年3月2日 上午10:25:49
     */
    public List<SmsAisleGroupHasSmsUser> queryVipUserHasGroup(Integer userLevel) {
        return getDao().queryVipUserHasGroup(userLevel);
    }



    /**
     * @Description:根据用户ID给用户钱包充值
     * @author:oygy
     * @time:2017年1月11日 下午2:03:29
     */
    public void updateSmsUserBlankMoney(SmsUser smsUser) {
        getDao().updateSmsUserBlankMoney(smsUser);
    }


    /**
     * 添加金额操作明细
     *
     * @Description:
     * @author:oygy
     * @time:2017年1月14日 下午5:07:50
     */
    public void saveSmsUserMoneyRunning(SmsUserMoneyRunning smr) {
        getDao().saveSmsUserMoneyRunning(smr);
    }

    /**
     * @Description:根据用户ID查询出用户的KEY
     * @author:oygy
     * @time:2017年1月11日 下午3:54:31
     */
    public String querySmsUserKey(Integer smsUserId) {
        return getDao().querySmsUserKey(smsUserId);
    }

    /**
     * @Description:查询代理统计列表
     * @author:ougy
     * @time:2016年12月31日 下午2:18:48
     */
    public Page<SmsUser> querySmsUserStatisticalList(DzdPageParam dzdPageParam) {
        return getDao().querySmsUserStatisticalListPage(dzdPageParam);
    }

    public Page<SmsUser> querySmsUserAliseStatisticalList(DzdPageParam dzdPageParam) {
        return getDao().querySmsUserAliseStatisticalListPage(dzdPageParam);
    }


    /**
     * 查询今日统计列表和历史统计列表
     * @param sysMenuBtns
     * @param dzdPageParam
     * @return
     */
    public Page<SmsUser> querySmsUserLogStatisticalListPage(List<SysMenuBtn> sysMenuBtns, DzdPageParam dzdPageParam) {
        DzdPageParam dzdPageParams = dzdPageParam;
        Map<String, Object> columnMap = (Map<String, Object>) dzdPageParams.getCondition();

        if (sysMenuBtns != null) {
            for (SysMenuBtn sysMenuBtn : sysMenuBtns) {
                //  通道类型列
                if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.TODAYSTATISTICAL_AISLE_COLUMN_URL)) {
                    columnMap.put(Define.MENUURLACTION.AISLECOLUMN, true);
                }

                //  归属列
                else if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.TODAYSTATISTICAL_BELONG_COLUMN_URL)) {
                    columnMap.put(Define.MENUURLACTION.NICKCOLUMN, true);
                }
            }
        }
        dzdPageParams.setCondition(columnMap);
        return getDao().querySmsUserLogStatisticalListPage(dzdPageParam);
    }

    /**
     * @Description:统计所有代理发送信息
     * @author:ougy
     * @time:2016年12月31日 下午2:18:48
     */
    public SmsUser querySmsUserStatisticalZong(DzdPageParam dzdPageParam) {
        return getDao().querySmsUserStatisticalZong(dzdPageParam);
    }

    public SmsUser querySmsUserAliseStatisticalZong(DzdPageParam dzdPageParam) {
        return getDao().querySmsUserAliseStatisticalZong(dzdPageParam);
    }

    public SmsUser querySmsUserLogStatisticalZong(DzdPageParam dzdPageParam) {
        return getDao().querySmsUserLogStatisticalZong(dzdPageParam);
    }

    /**
     * 查询今日消费
     *
     * @param smsUserId
     * @return
     */
    public String querySmsUserStatisticalByUid(Integer smsUserId) {
        Long count = getDao().querySmsUserStatisticalByUid(smsUserId);
        if (count == 0) {
            return "0";
        }
        return count.toString();
    }

    /**
     * @Description:根据用户查询代理每天统计列表
     * @author:ougy
     * @time:2016年12月31日 下午2:18:48
     */
    public Page<SmsUser> querySmsUserStatistical(DzdPageParam dzdPageParam) {
        return getDao().querySmsUserStatisticalPage(dzdPageParam);
    }

    public Page<SmsUser> querySmsUserDLPage(DzdPageParam dzdPageParam) {
        return getDao().querySmsUserDLPage(dzdPageParam);
    }

    /**
     * 查询充值记录
     */
    public Page<SmsUserMoneyRunning> querySmsUserPuserBillList(DzdPageParam dzdPageParam, List<SysMenuBtn> sysMenuBtns) {
        DzdPageParam dzdPageParams = dzdPageParam;
        Map<String, Object> columnMap = (Map<String, Object>) dzdPageParams.getCondition();

        if (sysMenuBtns != null) {
            for (SysMenuBtn sysMenuBtn : sysMenuBtns) {
                //  操作者列
                if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.RECHARGE_ACCOUNT_COLUMN_URL)) {
                    columnMap.put(Define.MENUURLACTION.UACCOUNTCOLUMN, true);
                }


                //  金额列
                else if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.RECHARGE_MONEY_COLUMN_URL)) {
                    columnMap.put(Define.MENUURLACTION.MONEYCOLUMN, true);
                }

                //  账户归属列
                else if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.RECHARGE_BELONG_COLUMN_URL)) {
                    columnMap.put(Define.MENUURLACTION.NICKNAMECOLUMN, true);
                }
            }
        }
        dzdPageParams.setCondition(columnMap);
        return getDao().querySmsUserPuserBillListPage(dzdPageParam);
    }

    /**
     * 导出充值记录
     *
     * @return
     */
    public List<SmsUserMoneyRunning> querySmsUserRechargeBillList(SmsUserMoneyRunning running) {
        return getDao().querySmsUserRechargeBillList(running);
    }

    /**
     * 消费记录
     *
     * @param dzdPageParam
     * @return
     */
    public Page<SmsUserMoneyRunning> querySmsUserPuserConsumeBillListPage(DzdPageParam dzdPageParam, List<SysMenuBtn> sysMenuBtns) {
        DzdPageParam dzdPageParams = dzdPageParam;
        Map<String, Object> columnMap = (Map<String, Object>) dzdPageParams.getCondition();

        if (sysMenuBtns != null) {
            for (SysMenuBtn sysMenuBtn : sysMenuBtns) {
                //  账户归属列
                if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.CONSUME_BELONG_COLUMN_URL)) {
                    columnMap.put(Define.MENUURLACTION.NICKNAMECOLUMN, true);
                    break;
                }
            }
        }
        dzdPageParams.setCondition(columnMap);


        return getDao().querySmsUserPuserConsumeBillListPage(dzdPageParam);
    }

    /**
     * @Description:统计所有代理账单流水信息
     * @author:ougy
     * @time:2016年12月31日 下午2:18:48
     */
    public SmsUserMoneyRunning querySmsUserPuserBillZong(DzdPageParam dzdPageParam) {
        return getDao().querySmsUserPuserBillZong(dzdPageParam);
    }

    /**
     * 定时统计
     *
     * @Description:统计前一天代理数据
     * @author:oygy
     * @time:2017年2月16日 下午2:34:15
     */
    public List<SmsUser> stSmsUserPuserBillList(Map<String, Object> param) {
        return getDao().stSmsUserPuserBillList(param);
    }

    public List<SmsUser> stSmsUserAsileList(Map<String, Object> param) {
        return getDao().stSmsUserAsileList(param);
    }

    /**
     * @Description:添加钱一天代理统计数据
     * @author:oygy
     * @time:2017年2月16日 下午4:04:19
     */
    public void saeveStSmsUserPuserBill(SmsUser smsUser) {
        getDao().saeveStSmsUserPuserBill(smsUser);
    }

    public void saeveStSmsUserAsile(SmsUser smsUser) {
        getDao().saeveStSmsUserAsile(smsUser);
    }

    /**
     * @Description:查询出所有用户信息
     * @author:oygy
     * @time:2017年2月17日 下午4:59:06
     */
    public List<SmsUser> querySmsUserListTj() {
        return getDao().querySmsUserListTj();
    }

    /**
     * 账单流水统计
     *
     * @param userId
     * @return
     */
    public SmsUserMoneyRunning querySmsUserMoneyRunningStatistical(Integer userId) {
        return getDao().querySmsUserMoneyRunningStatistical(userId);
    }

    /**
     * 发送统计
     *
     * @param email
     * @return
     */
    public SmsUser querySendListStatistical(String email) {
        return getDao().querySendListStatistical(email);
    }

    /**
     * @Description:根据通道组类型查询通道组
     * @author:oygy
     * @time:2017年3月31日 下午2:28:10
     */
    public List<SmsAisleGroup> querySmsGroupById(Integer gtype) {
        return getDao().querySmsGroupById(gtype);
    }

    /**
     * @Description:根据系统用户ID查询代理账户信息
     * @author:oygy
     * @time:2017年4月6日 下午2:57:24
     */
    public SmsUser querySmsUserBySysUid(Integer sysId) {
        return getDao().querySmsUserBySysUid(sysId);
    }

    /**
     * 新增
     *
     * @param smsUser
     */
    public void addSmsUser(SmsUser smsUser) {
        getDao().addSmsUser(smsUser);
        ;
    }

    public void updateSmsUserInfo(SmsUser smsUser) {
        getDao().updateSmsUserInfo(smsUser);
    }

    public Map<String, String> fillSmsUserStatisticalListPage(DzdPageParam filldzdPageParam) {
        Map<String, String> fillSmsUserMap = new HashMap<String, String>();
        List<SmsUser> fillSmsUsers = getDao().fillSmsUserStatisticalListPage(filldzdPageParam);
        if (CollectionUtils.isEmpty(fillSmsUsers)) {
            return fillSmsUserMap;
        }
        for (SmsUser smsUser : fillSmsUsers) {
            fillSmsUserMap.put(smsUser.getEmail(), smsUser.getNickName());
        }
        return fillSmsUserMap;
    }

    /**
     * @Description:统计新增自主注册代理数量
     * @author:oygy
     * @time:2017年4月13日 下午5:23:35
     */
    public Integer queryCountNewSmsUser() {
        return getDao().queryCountNewSmsUser();
    }

    /**
     * @Description:统计用户剩余条数
     * @author:oygy
     * @time:2017年4月13日 下午5:31:22
     */
    public Integer queryCountUserSmsNum(Map<String, Object> sortMap) {
        return getDao().queryCountUserSmsNum(sortMap);
    }

    /**
     * @Description:统计所有用户每天发送情况
     * @author:oygy
     * @time:2017年4月13日 下午6:05:18
     */
    public List<SmsUser> queryCountSmsAgentStatistics(Map<String, Object> sortMap) {
        return getDao().queryCountSmsAgentStatistics(sortMap);
    }

    /**
     * @Description:统计今日购买数量
     * @author:oygy
     * @time:2017年4月14日 上午11:38:27
     */
    public Integer queryCountTodaySmsNum(Map<String, Object> sortMap) {
        return getDao().queryCountTodaySmsNum(sortMap);
    }

    /**
     * @Description:查询最后查询时间
     * @author:oygy
     * @time:2017年4月14日 下午4:16:48
     */
    public Date queryLastSmsUserTime() {
        return getDao().queryLastSmsUserTime();
    }

    public void updateLastSmsUserTime(Date lastTime) {
        getDao().updateLastSmsUserTime(lastTime);
    }

    public void addLastSmsUserTime(Date lastTime) {
        getDao().addLastSmsUserTime(lastTime);
    }

    /**
     * @Description:查询消息推送列表
     * @author:ougy
     * @time:2016年12月31日 下午2:18:48
     */
    public Page<SmsUserMessage> pushManageList(DzdPageParam dzdPageParam) {
        return getDao().pushManagePage(dzdPageParam);
    }

    /**
     * @Description:新增推送
     * @author:oygy
     * @time:2017年4月18日 下午2:29:34
     */
    public void addPushManage(SmsUserMessage smsUserMessage) {
        getDao().addPushManage(smsUserMessage);
    }

    /**
     * 查询用户的类型
     *
     * @param groupId
     * @return
     */
    public SmsAisleGroupType queryGroupTypeBySmsUserId(Integer groupId) {
        return getDao().queryGroupTypeBySmsUserId(groupId);
    }

    public Page<SmsRechargeUser> rechargeRecordList(DzdPageParam dzdPageParam) {
        return getDao().rechargeRecordListPage(dzdPageParam);
    }


    /**
     * 关于充值请调用这个方法 - 新增
     *
     * @param moneyRunning
     */
    public void saveUserMoneyRunning(SmsUserMoneyRunning moneyRunning) {
        getDao().saveUserMoneyRunning(moneyRunning);
    }


    /**
     * 修改用户信息
     *
     * @param smsUser
     */
    public void updateUser(SmsUser smsUser) {
        getDao().updateUser(smsUser);
    }

    /**
     * 查询登录的用户下是否有
     *
     * @param smsUserId
     * @return
     */
    public List<SmsUser> querySmsBUserById(Integer smsUserId) {
        List<SmsUser> bsmsUserList = getDao().querySmsBUserById(smsUserId);
        if (bsmsUserList == null || bsmsUserList.size() == 0) {
            return null;
        }
        return bsmsUserList;
    }

    /**
     * 根据用户等级查询用户信息
     *
     * @param list
     * @return
     */
    public List<SmsUser> queryByUserLevel(List list) {
        return mapper.queryByUserLevel(list);
    }

    /**
     * 修改用户等级
     *
     * @param smsUser
     */
    public void updateUserLevel(SmsUser smsUser) {
        mapper.updateUserLevel(smsUser);
    }

    /**
     * 查询超级管理员和一级管理员用户
     *
     * @param list 角色id
     * @return
     */
    public List<SmsUser> queryRoleLevel(List list) {
        return mapper.queryRoleLevel(list);
    }

    /**
     * 查询业务员或者销售经理下面所有业务员（归属）
     *
     * @param list
     * @return
     */
    public List<SmsUser> queryBySysUserId(List list) {
        return mapper.queryBySysUserId(list);
    }

    /**
     * 更改发送短信是否需要验证
     *
     * @param smsUser
     */
    public void updateSmsUserVerifyType(SmsUser smsUser) {
        mapper.updateSmsUserVerifyType(smsUser);
    }


    /**
     * 查询电话号码是否存在
     *
     * @param phone
     * @param
     * @return
     */
    public Integer queryPhone(String phone) {
        return mapper.queryPhone(phone);
    }

    /**
     * 根据账号和电话号码查询用户
     *
     * @param
     * @return
     */
    public SmsUser queryByEmailAndPhone(String email, String phone) {
        return mapper.queryByEmailAndPhone(email, phone);
    }
}
