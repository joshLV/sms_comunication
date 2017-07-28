package com.dzd.phonebook.service;

import com.dzd.base.service.BaseService;
import com.dzd.phonebook.dao.SmsManageMentAccountDao;
import com.dzd.phonebook.entity.SysMenuBtn;
import com.dzd.phonebook.util.DzdPageParam;
import com.dzd.phonebook.util.SmsUser;
import com.dzd.sms.application.Define;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Description:账户管理列表
 * @author:wangran
 * @time:2017年5月22日 上午13:37:06
 */
@Service("smsManageMentAccountService")
public class SmsManageMentAccountService<T> extends BaseService<T> {

    @Autowired
    private SmsManageMentAccountDao<T> mapper;

    public SmsManageMentAccountDao<T> getDao() {
        return mapper;
    }

    /**
     * @Description:账户管理列表
     * @author:ougy
     * @time:2016年12月31日 下午2:18:48
     */
    public Page<SmsUser> querySmsUserManageMentListPage(DzdPageParam dzdPageParam,List<SysMenuBtn> sysMenuBtns) {

        DzdPageParam dzdPageParams = dzdPageParam;
        Map<String, Object> columnMap = (Map<String, Object>) dzdPageParams.getCondition();

        if (sysMenuBtns != null) {
            for (SysMenuBtn sysMenuBtn : sysMenuBtns) {
                //  通道类型列
                if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.SYSACCOUNT_AISLE_COLUMN_URL)) {
                    columnMap.put(Define.MENUURLACTION.AISLECOLUMN, true);
                }


                //  用户类型列
                else if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.SYSACCOUNT_ROLENAME_COLUMN_URL)) {
                    columnMap.put(Define.MENUURLACTION.ROLENAMECOLUMN, true);
                }

                //  账户归属列
                else if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.SYSACCOUNT_BELONG_COLUMN_URL)) {
                    columnMap.put(Define.MENUURLACTION.NICKNAMECOLUMN, true);
                }
            }
        }
        dzdPageParams.setCondition(columnMap);
        return getDao().querySmsUserManageMentListPage(dzdPageParam);
    }

    /**
     * 查询账户信息
     *
     * @param id
     * @return
     */
    public SmsUser querySmsManageMentUser(Object id) {
        return getDao().querySmsManageMentUser(id);
    }

    /**
     * 批量删除账户信息
     *
     * @param list
     */
    public void deleteByIds(List list) {
        getDao().deleteByIds(list);
    }

    /**
     * @Description:添加账户信息
     * @author:oygy
     * @time:2017年05月19日 下午16:11:16
     */
    public void saveSmsUserManagement(SmsUser smsUser) {
        getDao().saveSmsUserManagement(smsUser);
    }


}
