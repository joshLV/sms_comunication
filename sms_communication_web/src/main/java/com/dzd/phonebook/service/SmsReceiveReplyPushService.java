package com.dzd.phonebook.service;


import com.dzd.phonebook.entity.SysMenuBtn;
import com.dzd.sms.application.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dzd.base.service.BaseService;
import com.dzd.phonebook.dao.SmsReceiveReplyPushDao;
import com.dzd.phonebook.entity.SmsReceiveReplyPush;
import com.dzd.phonebook.util.DzdPageParam;
import com.github.pagehelper.Page;

import java.util.List;
import java.util.Map;

/**
 * @Description:短信回复
 * @author:liuquan
 * @time:2016年12月30日 下午2:28:03
 */
@Service
public class SmsReceiveReplyPushService<T> extends BaseService<T> {
    @Autowired
    private SmsReceiveReplyPushDao<T> mapper;

    @Override
    public SmsReceiveReplyPushDao<T> getDao() {
        // TODO Auto-generated method stub
        return mapper;
    }

	/*
     * public List<T> queryByAll(){ return mapper.queryByAll();
	 * 
	 * 
	 * }
	 */

    public Page<SmsReceiveReplyPush> queryUserList(DzdPageParam dzdPageParam, List<SysMenuBtn> sysMenuBtns) {

        DzdPageParam dzdPageParams = dzdPageParam;
        Map<String, Object> columnMap = (Map<String, Object>) dzdPageParams.getCondition();

        if (sysMenuBtns != null) {
            for (SysMenuBtn sysMenuBtn : sysMenuBtns) {
                //  通道类型列
                if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.SMSRECEIVEREPLY_AISLE_COLUMN_URL)) {
                    columnMap.put(Define.MENUURLACTION.AISLECOLUMN, true);
                    break;
                }
            }
        }
        dzdPageParams.setCondition(columnMap);
        return getDao().queryUserPage(dzdPageParam);

    }

    /**
     * 保存用户
     *
     * @param
     * @param
     * @return
     */
	/*
	 * public int saveUser(SmsReceiveReplyLog smsReceiveReplyLog) { return
	 * mapper.addlist(smsReceiveReplyLog);
	 * 
	 * 
	 * }
	 */

    /**
     * 删除
     *
     * @param
     * @param
     * @return
     */


    public void deleteSmsReceiveReplyPush(Integer smsReceiveReplyPushId) {
        mapper.deleteSmsReceive(smsReceiveReplyPushId);

    }


}
