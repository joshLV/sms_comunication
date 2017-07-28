package com.dzd.phonebook.interfaces.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.dzd.utils.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONException;
import com.dzd.phonebook.interfaces.InterfaceBaseController;
import com.dzd.phonebook.interfaces.MemberStatisticsService;
import com.dzd.phonebook.page.MD5;
import com.dzd.phonebook.service.SmsUserService;
import com.dzd.phonebook.util.SmsUser;
import com.dzd.sms.task.quartz.TaskSettlementJob;

import net.sf.json.JSONObject;

/**
 * 
 * @Description:统计定时器
 * @author:oygy
 * @time:2016年12月21日 上午10:02:25
 */
@Path(value = "/V1/memberService")
public class MemberStatisticsServiceImpl extends InterfaceBaseController 
implements MemberStatisticsService{
	
	 public static final LogUtil log = LogUtil.getLogger(MemberStatisticsServiceImpl.class);
	
    
	 
	 	@Context	
	 	private HttpServletRequest request;
	
	 	@Context
	 	private HttpServletResponse response;
	 
		@SuppressWarnings("rawtypes")
		@Autowired
		private SmsUserService smsUserService;
		

	    @SuppressWarnings("unchecked")
		@GET
	    @Path("/updateMemberStatistics")
//	    @Consumes(MediaType.APPLICATION_JSON)
		public String regedit(String json) throws IOException {
	    	Map<String,Object> productMap = new HashMap<String,Object>();  
	    	Object pa = request.getParameter("pa"); 
	 		Object sign = request.getParameter("sign");
	 		Object date = request.getParameter("date");
	 		MD5 getMD5 = new MD5();
	 		String stoken =getMD5.GetMD5Code("frigid543" + pa);
	 		if(pa==null || sign==null || date==null || !stoken.equals(sign)){
	 			productMap.put("status", "000002");
				productMap.put("message", "更新失败，身份验证错误！");
				JSONObject jsonObject = JSONObject.fromObject(productMap);  
				return jsonObject.toString();
	 		}
	    
			System.out.println("----------------------开始设备激活定时器-------------------------");
			 log.info("-----------------The timer(start:taCalculateTime)--------------------");
			try{
				//Claim claim = new Claim();
				log.info("taCalculateTime---->>stSmsUserPuserBillList()");
			   
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("date", date);
				
				  List<SmsUser> dataList = smsUserService.stSmsUserPuserBillList(param); //统计出近4天的数据
				  /*List<SmsUser> dataList2 =smsUserService.querySmsUserListTj();//查询所有用户信息
						  
				  List<SmsUser> dataList3 = new ArrayList<SmsUser>();
				  //查询所有用户列表
				  Date dNow = new Date();   //当前时间
				  String[] datear =new String[]{getSpecifiedDayBefore(dNow,0),
						  						getSpecifiedDayBefore(dNow,-1),
						  						getSpecifiedDayBefore(dNow,-2),
						  						getSpecifiedDayBefore(dNow,-3)};
					
				  
				  for (SmsUser smsusers : dataList2) {
					for (int i = 0; i < datear.length; i++) {
						SmsUser smUsers = new SmsUser();
						smUsers.setId(smsusers.getId());
						smUsers.setName(smsusers.getName());
						smUsers.setEmail(smsusers.getEmail());
						smUsers.setAuditTime(datear[i]);
						smUsers.setSendNum(0);
						smUsers.setBillingNum(0);
						smUsers.setActualNum(0);;
						dataList3.add(smUsers);
					}
				  }
				  
				List<SmsUser> dataList5=new ArrayList<SmsUser>();
				for (SmsUser smsusers : dataList3) {
					for (SmsUser smsuserlist : dataList) {
						if(smsusers.getId() == smsuserlist.getId() && smsusers.getAuditTime().trim().equals(smsuserlist.getAuditTime().trim()))
						{
							dataList5.add(smsusers);
							
						}
					}
				}
				dataList3.removeAll(dataList5);
				dataList3.addAll(dataList);*/

			    log.info("taCalculateTime---->>saeveStSmsUserPuserBill()");
			   for (SmsUser smsUser : dataList) {
				   smsUserService.saeveStSmsUserPuserBill(smsUser);
			   }
				productMap.put("status", "000000");
				productMap.put("message", "更新成功！");
				
			}catch(Exception e){
				 log.error(null, e);
	            e.printStackTrace();
	            productMap.put("status", "000001");
				productMap.put("message", "更新失败！");
			}
			log.info("-----------------The timer(end:taCalculateTime)--------------------");
			JSONObject jsonObject = JSONObject.fromObject(productMap);  
			return jsonObject.toString();
		} 
	    
	    @SuppressWarnings("unchecked")
		@GET
	    @Path("/updateAisleStatistics")
//	    @Consumes(MediaType.APPLICATION_JSON)
		public String staticAisleInfo(String json) throws IOException {
	    	Map<String,Object> productMap = new HashMap<String,Object>();  
	    	Object pa = request.getParameter("pa"); 
	 		Object sign = request.getParameter("sign");
	 		Object date = request.getParameter("date");
	 		MD5 getMD5 = new MD5();
	 		String stoken =getMD5.GetMD5Code("frigid543" + pa);
	 		if(pa==null || sign==null || date==null || !stoken.equals(sign)){
	 			productMap.put("status", "000002");
				productMap.put("message", "更新失败，身份验证错误！");
				JSONObject jsonObject = JSONObject.fromObject(productMap);  
				return jsonObject.toString();
	 		}
	    
			System.out.println("----------------------开始设备激活定时器-------------------------");
			 log.info("-----------------The timer(start:taCalculateTime)--------------------");
			try{
				//Claim claim = new Claim();
				log.info("taCalculateTime---->>stSmsUserAsileList()");
			   
				Map<String,Object> param = new HashMap<String, Object>();
				param.put("date", date);
				
				  List<SmsUser> dataList = smsUserService.stSmsUserAsileList(param); //统计出近4天的数据

			    log.info("taCalculateTime---->>saeveStSmsUserAsile()");
			   for (SmsUser smsUser : dataList) {
				   smsUserService.saeveStSmsUserAsile(smsUser);
			   }
				productMap.put("status", "000000");
				productMap.put("message", "更新成功！");
				
			}catch(Exception e){
				 log.error(null, e);
	            e.printStackTrace();
	            productMap.put("status", "000001");
				productMap.put("message", "更新失败！");
			}
			log.info("-----------------The timer(end:taCalculateTime)--------------------");
			JSONObject jsonObject = JSONObject.fromObject(productMap);  
			return jsonObject.toString();
		} 
	
	public static String getSpecifiedDayBefore(Date dNow,Integer num){ 
		Date dBefore = new Date();
		Calendar calendar = Calendar.getInstance(); //得到日历
		calendar.setTime(dNow);//把当前时间赋给日历
		calendar.add(Calendar.DAY_OF_MONTH, num);  //设置为前一天
		dBefore = calendar.getTime();   //得到前一天的时间

		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd"); //设置时间格式
		String defaultStartDate = sdf.format(dBefore);    //格式化前一天
		return defaultStartDate;
	}

	/**
     * json转换成Object
     * @Description:
     * @author:oyyg
     * @time:2016年9月2日 下午3:34:50
     */
   private JSONObject jsonObject(String json){
	   String jsonMessage = json;
	   JSONObject myJsonObject = new JSONObject();
	   try
	   {
	    //将字符串转换成jsonObject对象
	    myJsonObject = JSONObject.fromObject(jsonMessage);
	   }catch (JSONException e){
		   e.getMessage();
	   }
	   return myJsonObject;
   } 
	
   public static void main(String[] args) {
		MD5 getMD5 = new MD5();
		
 		String stoken =getMD5.GetMD5Code("frigid543"+123456);
 		System.out.println(stoken);//6bbf72f0b7e597fc89936ee80aa6a27c
   }
   
   /**
    * 
    * @Title: taskStateUpdateJob
    * @Description: 刷新任务号码相关状态记录并进行款项结算
    * @author:    hz-liang
    * @return  
    * @return: boolean   
    * @throws
    */
	@GET
	@Path("/taskStateUpdateJob")
	public String taskStateUpdateJob()
	{
		boolean updateTaskAndPhoneStates = TaskSettlementJob.updateTaskAndPhoneStates();

		Map<String, Object> productMap = new HashMap<String, Object>();
		productMap.put("status", updateTaskAndPhoneStates ? 000000 : 000001);
		productMap.put("message", updateTaskAndPhoneStates ? "更新成功！" : "更新失败！");

		JSONObject jsonObject = JSONObject.fromObject(productMap);
		return jsonObject.toString();
	}
}
