package com.dzd.phonebook.service;


import com.dzd.base.service.BaseService;
import com.dzd.phonebook.dao.SmsAccountDao;
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
 * @Description:短信账户管理列表
 * @author:wangran
 * @time:2017年5月22日 上午13:37:06
 */
@Service("smsAccountService")
public class SmsAccountService<T> extends BaseService<T> {

    @Autowired
    private SmsAccountDao<T> mapper;

    public SmsAccountDao<T> getDao() {
        return mapper;
    }


    /**
     *
     * @Description:短信账户列表
     * @author:ougy
     * @time:2016年12月31日 下午2:18:48
     */
    public Page<SmsUser> querySmsAccountUserListPage(DzdPageParam dzdPageParam,List<SysMenuBtn> sysMenuBtns) {
        DzdPageParam dzdPageParams = dzdPageParam;
        Map<String, Object> columnMap = (Map<String, Object>) dzdPageParams.getCondition();

        if (sysMenuBtns != null) {
            for (SysMenuBtn sysMenuBtn : sysMenuBtns) {
                //  通道类型列
                if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.SMSACCOUNT_AISLE_COLUMN_URL)) {
                    columnMap.put(Define.MENUURLACTION.AISLECOLUMN, true);
                }


                //  归属列
                else if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.SMSACCOUNT_BID_COLUMN_URL)) {
                    columnMap.put(Define.MENUURLACTION.NICKNAMECOLUMN, true);
                }
            }
        }
        dzdPageParams.setCondition(columnMap);
        return getDao().querySmsAccountUserListPage(dzdPageParam);
    }

    /**
     * 根据用户sys_user_id 查询下面所有归属bId (bId 下面的子归属 多层)
     * @param list
     * @return
     */
    public List<Integer> queryBids(List list){
        return getDao().queryBids(list);
    }
}
