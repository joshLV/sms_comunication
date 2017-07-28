package com.dzd.phonebook.dao;

import com.dzd.base.dao.BaseDao;
import com.dzd.phonebook.util.DzdPageParam;
import com.dzd.phonebook.util.SmsUser;
import com.github.pagehelper.Page;

import java.util.List;

//申请账户
public interface SmsApplyAccountDao<T> extends BaseDao<T>  {

	/**
	 * 申请账户列表
	 * @param dzdPageParam
	 * @return
	 */
	public Page<SmsUser> queryApplyAccountlistPage(DzdPageParam dzdPageParam);

	/**
	 * 根据id查询申请用户信息
	 * @param object
	 * @return
	 */
	public SmsUser queryApplyAccountById(Object object);


	/**
	 * 修改申请账户信息
	 * @param smsUser
	 */
	public void updateApplyAccount(SmsUser smsUser);


	/**
	 * 删除申请账户信息
	 * @param ids
	 */
	public void deleteApplyAccount(List ids);

	/**
	 * 新增申请账户信息
	 * @param smsUser
	 */
	public void saveSmsUserApplyAccount(SmsUser smsUser);
	
}