package com.dzd.sms.task.quartz;


import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dzd.utils.LogUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.jdbc.core.RowMapper;

import com.dzd.cache.redis.manager.RedisClient;
import com.dzd.cache.redis.manager.RedisManager;
import com.dzd.db.mysql.MysqlOperator;
import com.dzd.sms.application.Define;
import com.dzd.sms.application.SmsServerManager;
import com.dzd.utils.DateUtil;
import com.dzd.utils.StringUtil;

/**
 * @Author WHL
 * @Date 2017-4-24.
 */
@DisallowConcurrentExecution
public class TaskSettlementJob implements Job {


    private  static LogUtil logger = LogUtil.getLogger(TaskSettlementJob.class );
    public final static  String classKey ="TaskSettlementJob";
    public RedisClient redisClient = RedisManager.I.getRedisClient();
    
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "-TaskSettlementJob-taskAmountReturnedFromDatabase");
       
        try{
            if(redisClient.exists(classKey)){

            }else {
                redisClient.set(classKey, "1", 10*60);//60分钟
           
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
        	redisClient.del(classKey);
		}
    }

    public static boolean taskAndPhoneReturnState(){
        int n = 100000;
        boolean flag = false;
        while (n-->0 && checkLastTaskReturnState() ){
        	flag =  true;
        	System.out.println("checkLastTaskReturnState.number="+n);
        }
        return flag;
    }
    public static boolean checkLastTaskReturnState(){
        boolean re = false;
        String startDateTime = DateUtil.formatDateTime( new Date( new Date().getTime()-3*24*3600*1000));
        String endDateTime = DateUtil.formatDate(new Date(), "yyyy-MM-dd 00:00:00");//DateUtil.formatDateTime( new Date( new Date().getTime()-75*3600*1000)); 
        String sql = null;//"select id, sms_user_id from sms_send_task where  state<101 and (send_type=0 or timing_time<'" + endDateTime + "') and send_time BETWEEN '"+startDateTime+"' and '"+endDateTime+"' limit 1000 ";
        logger.info("checkLastTaskReturnState.sql="+sql);
        try{
            Map<String,String> taskIdMapUser = new HashMap<String, String>();
            List<Map<String, String>> lists =MysqlOperator.I.query(sql, new RowMapper() {
                public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String, String> b = new HashedMap();
                    b.put( rs.getString("id"),rs.getString("sms_user_id") );
                    return b;
                }
            });

            for( Map<String, String> item:lists ){
                for( String key:item.keySet()){
                    taskIdMapUser.put( key, item.get(key));
                }
            }

            if( taskIdMapUser.size()>0 ) {
                returnMoney(taskIdMapUser);
                re = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return  re;
    }

    /**
     * 
     * @Title: updateTaskAndPhoneStates
     * @Description: 按天结算
     * @author:    hz-liang
     * @return  
     * @return: boolean   
     * @throws
     */
	public static boolean updateTaskAndPhoneStates()
	{
		String startDateTime = DateUtil
		        .formatDate(new Date(new Date().getTime() - 1 * 24 * 3600 * 1000), "yyyy-MM-dd 00:00:00");
		String endDateTime = DateUtil.formatDate(new Date(), "yyyy-MM-dd 00:00:00");
		try
		{
			List<Map<String, Object>> lists = queryForUpdate(startDateTime, endDateTime);
			// return money
			returnUserMoney(lists);
			// update
			updateState(startDateTime, endDateTime);
		} catch (Exception e)
		{
			return false;
		}
		return true;
	}

	private static void updateState(String startDateTime, String endDateTime)
	{
		// 定时任务未发送是状态为0，故而不需要判断控制
		String Tsql = "UPDATE sms_send_task_phone sstp SET sstp.state = if(sstp.state=-1,26,if(sstp.state=99,100,sstp.state)) "
		        + " where sstp.create_time between '" + startDateTime + "' and '" + endDateTime + "'";
		excutUpdate(Tsql);

		// 定时任务判断控制
		String Psql = "UPDATE sms_send_task ssk SET ssk.return_state=1, ssk.state = 101 "
		        + " where (ssk.send_type=0 or (ssk.send_type=1 && ssk.timing_time < now())) and ssk.create_time between '" + startDateTime + "' and '" + endDateTime + "'";
		excutUpdate(Psql);
	}
	
	public static void excutUpdate(String sql)
	{
		try
		{
			System.out.println(sql);
			MysqlOperator.I.update(sql);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void returnUserMoney(List<Map<String, Object>> lists)
	{
		for ( Map<String, Object> reslutMap : lists )
		{
			SmsServerManager.I.reduceUserBalance(
			        Long.valueOf(reslutMap.get(Define.REQUESTPARAMETER.SMSUSERID).toString()),
			        Integer.valueOf(reslutMap.get(Define.STATICAL.BILLING_NUM).toString()) * (-1),
			        "凌晨结算！" + null);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static List<Map<String, Object>> queryForUpdate(String startDateTime, String endDateTime)
	{
		String sql = "select ssk.sms_user_id smsUserId, sum(sstp.billing_num) billing_num"
		        + " from sms_send_task_phone sstp"
		        + " left join sms_send_task ssk on ssk.id=sstp.sms_send_task_id"
		        + " where sstp.state < 99 and ssk.state < 101"
		        + " and sstp.send_time between '" + startDateTime + "' and '" + endDateTime
		        + "' group by ssk.sms_user_id";


//        sql = "select ssk.sms_user_id smsUserId, sum(sstp.billing_num) billing_num"
//                + " from sms_send_task_phone sstp"
//                + " left join sms_send_task ssk on ssk.id=sstp.sms_send_task_id"
//                + " where ( ( ssk.send_type=0 and sstp.create_time between '" + startDateTime + "' and '"+endDateTime+"' ) or ( ssk.send_type=1 and ssk.timing_time BETWEEN '" + startDateTime + "' and '"+endDateTime+"' ) ) "
//                + " and sstp.state < 99"
//                + " and ssk.state < 101"
//                //+ " and sstp.create_time between '" + startDateTime + "' and '" + endDateTime
//                + "' group by ssk.sms_user_id ";


		logger.info(sql);
		List<Map<String, Object>> lists = MysqlOperator.I.query(sql, new RowMapper()
		{
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException
			{
				Map<String, Object> b = new HashMap<String, Object>();
				b.put(Define.REQUESTPARAMETER.SMSUSERID, rs.getString(Define.REQUESTPARAMETER.SMSUSERID));
				b.put(Define.STATICAL.BILLING_NUM, rs.getString(Define.STATICAL.BILLING_NUM));
				return b;
			}
		});
		return lists;
	}

    public static void returnMoney(Map<String,String> taskIdMapUser){

        if( taskIdMapUser.size()==0){
            return;
        }

        List<String> paramsIds= new ArrayList<String>(taskIdMapUser.keySet());
        try{
//            RLock lock =  SmsServerManager.I.redisson.getLock("job-returnMoney");
//            lock.lock();
            try {
                String sql;
                if( taskIdMapUser.size()==1 ){
                    sql =  String.format("select id from sms_send_task where id =%s and state<101  ", paramsIds.get(0) );
                }else{
                    sql =  String.format("select id from sms_send_task where id in (%s) and state<101  ", StringUtil.arrayToString(paramsIds,",") );
                }

                List<String> ids = MysqlOperator.I.query(sql, new RowMapper() {
                    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getString("id");
                    }
                });


                for(String taskIdString:paramsIds){
                    if( ids.contains( taskIdString )) {
                        logger.info("return process task id="+taskIdString);
                        long taskId = Long.valueOf(taskIdString);
                        //查询是否返还的任务
                        Integer count = 0;
                        Integer fee = 0;
                        //成功的数量
                        int successNum = 0;
                        int auditFailNum = 0;
                        //查询需要返还的数量
                        Map<Integer, Integer> re = getTaskSendResult(taskId);
                        if (re != null) {
                            //返还的数
                            int returnNum = 0;
                            for (Integer stateKey : re.keySet()) {
                            	//----------------------------------------
                                if (stateKey.intValue() >= 99) {
                                    successNum += re.get(stateKey);
                                } else{
                                    returnNum += re.get(stateKey);
                                }
                            }
                            //更新返还状态， 及返还金额
                            updateTaskReturnState(taskId, successNum, count);
                            // 更新号码状态
                            updateTaskPhoneReturnState(taskId);
                            if (returnNum > 0) {
                                //给用户返款
                                SmsServerManager.I.addUserBalance(Long.valueOf(taskIdMapUser.get(taskIdString)), returnNum, "任务ID：" + taskId,null);
                            }
                            logger.info("返还金额=" + fee + ",失败数量=" + count);

                        } else {
                            logger.info("失败-返还金额=" + fee + ",失败数量=" + count);
                        }
                    }else{
                        logger.info("return ignore task id="+taskIdString  );
                    }


                    //删除任务及发送号码缓存
                    SmsServerManager.I.delSmsTaskCache(taskIdString);

                }

            }
            finally {
//                lock.unlock();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    /**
     * 查询返还的数量和金额
     * 警告： state=25 为审核失败， 即为终止发送
     * state=99为提交成功
     * state=100为发送成功
     * 以上几种状态不返还
     */
    public static Map<Integer, Integer> getTaskSendResult(Long taskId) {
        Map<Integer, Integer> re = new HashedMap();
        try {

            String sql = "select state, sum(billing_num) as f from sms_send_task_phone where sms_send_task_id=" + taskId + " group by state ";

            //String sql = "select count(id) as c, sum(billing_num) as f from sms_send_task_phone where sms_send_task_id=" + taskId + " and (state<>25 and state<99) and state<>0 ";
            //String sql = "select count(id) as c, sum(billing_num) as f from sms_send_task_phone where sms_send_task_id=" + taskId + " and  state<99 ";

            logger.info(sql);
            List<Map<Integer, Integer>> lists =MysqlOperator.I.query(sql, new RowMapper() {
                public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<Integer, Integer> b = new HashedMap();
                    Integer state = 0;
                    Integer f = 0;

                    state = rs.getInt("state");


                    if( rs.getObject("f") !=null ) {
                        if (rs.getObject("f") instanceof Long) {
                            f =  ( (Long)rs.getLong("f")).intValue();
                        } else {
                            f = ( (BigDecimal)rs.getBigDecimal("f")).intValue();
                        }
                    }

                    b.put(state, f);
                    return b;
                }
            });

            for( int i=0;i<lists.size();i++){
                for(Integer state: lists.get(i).keySet()){
                    re.put( state, lists.get(i).get(state));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return re;
    }


    /**
     * 更新任务状态
     */
    public static void updateTaskReturnState(Long taskId, int fee, int count) {
        String sql = " update sms_send_task set return_state=1, state=101,  actual_num=" + fee + ",update_time = NOW() where id = " + taskId;
        try {
            System.out.println(sql);
            MysqlOperator.I.update(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
	 * 更新任务状态
	 */
	public static void updateTaskPhoneReturnState(Long taskId)
	{
		String sql = "UPDATE sms_send_task_phone p  SET p.state = if( p.state=-1,26,if(p.state=99,100,p.state))" +
						" WHERE p.sms_send_task_id = " + taskId + " and p.state in(-1,99)" ;
		try
		{
			System.out.println(sql);
			MysqlOperator.I.update(sql);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

//
//    //72小时
//    ///////////////////////判断是否返还///////////////////////////
//
//    try {
//        float returnHour = 72f;
//        if (smsTaskData.isUpdateSendLogDatabase()) {
//            if (smsTaskData.getReturnState() == 0 ) {
//                Date sendDate = smsTaskData.getTaskSendTime();
//                //logger.info("task id="+smsTaskData.getTaskId() + ",  send Date="+DateUtil.formatDateTime(sendDate));
//                //过72小时， 需要返还数据
//                if ( sendDate !=null ) {
//                    int difMinute = DateUtil.diffDate(DateUtil.MINUTE, sendDate, new Date());
//                    if ( difMinute > returnHour * 60) {
//                        taskIdMapUser.put( smsTaskData.getTaskId().toString(), smsTaskData.getUserId().toString());
//                        logger.info("return task id="+smsTaskData.getTaskId() + ",  send Date="+DateUtil.formatDateTime(sendDate)+" difMinute="+difMinute);
//                        //先更新返还状态
//                        //smsTaskData.setReturnState(1);
//                        //SmsServerManager.I.cacheSmsTaskDate(smsTaskData);
//
//                        //删除任务及发送号码缓存
//                        //SmsServerManager.I.delSmsTaskCache(smsTaskData);
//
////
////
////                                                //审核通过，或免审的任务需要结算
////                                                if( smsTaskData.isAuditState() || smsTaskData.isFree() ) {
////                                                    //查询是否返还的任务
////                                                    Integer count = 0;
////                                                    Integer fee = 0;
////                                                    //成功的数量
////                                                    int successNum = 0;
////
////                                                    int auditFailNum = 0;
////
////                                                    //查询需要返还的数量
////                                                    Map<Integer, Integer> re = getTaskSendResult(smsTaskData.getTaskId());
////                                                    if (re != null) {
////                                                        //返还的数
////                                                        int returnNum = 0;
////
////                                                        for(Integer stateKey: re.keySet()){
////                                                            if( stateKey.intValue()>=99 ){
////                                                                successNum+=re.get(stateKey);
////                                                            }else if( stateKey.intValue() !=9 && stateKey.intValue() !=25 ){
////                                                                returnNum+= re.get(stateKey);
////                                                            }
////                                                        }
////
////                                                        if (returnNum > 0) {
////                                                            //给用户返款
////                                                            SmsServerManager.I.addUserBalance(smsTaskData.getUserId(), returnNum, "任务ID：" + smsTaskData.getTaskId());
////                                                        }
////                                                        logger.info("返还金额=" + fee + ",失败数量=" + count);
////                                                    } else {
////                                                        logger.info("失败-返还金额=" + fee + ",失败数量=" + count);
////                                                    }
////
////
////
////                                                    //更新返还状态， 及返还金额
////                                                    updateTaskReturnState(smsTaskData.getTaskId(), successNum, count);
////                                                }////////////// return end /////////////////
//
//
//
//                    }
//                }
//            } else {
//                //SmsWarehouse.getInstance().delSmsTaskCache(smsTaskData);
//            }
//        }
//
//    } catch (Exception e) {
//        e.printStackTrace();
//    }finally {
//
//    }


}
