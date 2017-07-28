package com.dzd.sms.api.service;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dzd.utils.LogUtil;
import org.apache.log4j.Logger;

import com.dzd.phonebook.util.NoMergeExcelUtil;
import com.dzd.phonebook.util.SmsSendLog;

/** * 
@author  作者 
E-mail: * 
@date 创建时间：2017年5月22日 上午9:50:39 * 
@version 1.0 * 
@parameter  * 
@since  * 
@return  */
public class SmsLogOrderBusiness
{
	private  static LogUtil logger = LogUtil.getLogger(SmsOrderExportBusiness.class );
	
	public void orderExport(HttpServletRequest request, HttpServletResponse response,
			List<SmsSendLog> dataList)
	{
		try
		{
			// 构造导出数据
			List<String> resultList = constructeResultList(dataList);

			NoMergeExcelUtil.reportMergeTxt(request, response, resultList);// utils类需要用到的参数
		} catch (Exception e)
		{
			e.printStackTrace();
			logger.error("导出失败");
			throw new RuntimeException("导出失败");
		}
		
	}

	private List<String> constructeResultList(List<SmsSendLog> dataList)
	{
		List<String> resultList = new ArrayList<String>();
		for ( SmsSendLog smsSendLog : dataList )
		{
			resultList.add(smsSendLog.getReceivePhone());
		}
		return resultList;
	}


}
