package com.dzd.sms.api.service;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dzd.utils.LogUtil;
import org.apache.log4j.Logger;

import com.dzd.base.util.SessionUtils;
import com.dzd.phonebook.util.BatchCompressExportUtil;

/** * 
@author  作者 
E-mail: * 
@date 创建时间：2017年5月22日 上午9:50:39 * 
@version 1.0 * 
@parameter  * 
@since  * 
@return  */
public class SmsFilterOrderBusiness
{
	private  static LogUtil logger = LogUtil.getLogger(SmsOrderExportBusiness.class );
	
	@SuppressWarnings({ "rawtypes", "static-access", "unchecked" })
	public void orderExport(HttpServletRequest request, HttpServletResponse response, Map<String, List<String>> resultList)
	{
		BatchCompressExportUtil excelUtil = new BatchCompressExportUtil();
		OutputStream out = null;
		try
		{
			out = response.getOutputStream();
			excelUtil.setTxtResponseHeader(response, SessionUtils.getSmsUser(request).getEmail() + "-"
			        + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()));

			excelUtil.exportTxt(resultList, out, request);

		} catch (Exception e)
		{
			e.printStackTrace();
			logger.error("导出失败");
			throw new RuntimeException("导出失败");
		}finally
		{
			try
			{
				out.flush();
				out.close();
				// 导出完成后删除文件夹中文件
				excelUtil.delFolder(request.getSession().getServletContext().getRealPath("/exportZipFiles"));
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}

}
