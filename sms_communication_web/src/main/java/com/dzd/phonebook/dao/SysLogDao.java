package com.dzd.phonebook.dao;

import com.dzd.base.dao.BaseDao;
import com.dzd.phonebook.entity.OperatorSectionNo;
import com.dzd.phonebook.entity.SmsFilterNumberRecord;
import com.dzd.phonebook.entity.SysLog;
import com.dzd.phonebook.util.DzdPageParam;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by wangran on 2017/6/02.
 */

/**
 * 操作日志
 * @param <T>
 */
public interface SysLogDao<T> extends BaseDao<T> {

    /**
     * 操作日志列表
     * @param dzdPageParam
     * @return
     */
    Page<SysLog> querySysLogPage(DzdPageParam dzdPageParam);

    /**
     * 修改操作日志账号
     * @param sysLog
     * @return
     */
    public void updateSysLog(SysLog sysLog);

    /***
     * 修改操作日志里内容中的账号
     * @param new_email  修改后的账号
     * @param old_email  修改之前的账号
     */
    public void updateSysLogContentEmail(@Param("new_email") String new_email,@Param("old_email") String old_email);

}
