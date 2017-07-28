package com.dzd.sms.task.quartz;

import java.util.Date;

import com.dzd.utils.LogUtil;
import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

import com.dzd.quartz.QuartzManager;

/**
 * @Author WHL
 * @Date 2017-4-24.
 */
public class QuartzJobManager {

    private static LogUtil logger = LogUtil.getLogger(QuartzJobManager.class);

    public static QuartzJobManager I = new QuartzJobManager();
    Scheduler sche = null;
    
    private static SchedulerFactory schedulerFactory = new StdSchedulerFactory();

    public QuartzJobManager() {

        try {
            sche = new StdSchedulerFactory().getScheduler();
            //每分钟更新一次
            QuartzManager.addJob(sche, "更新任务状态", TaskStateUpdateJob.class, "*/10 * * * * ?");
            //每分钟更新一次
            QuartzManager.addJob(sche, "获取状态报告", TaskQueryReportJob.class, "*/30 * * * * ?");

            //每分钟更新一次
            QuartzManager.addJob(sche, "获取上行", TaskQueryReplyJob.class, "0 */1 * * * ?");

            //"0 */10 * * * ?   //间隔10分执行
            QuartzManager.addJob(sche, "任务结算并返还", TaskSettlementJob.class, "0 */1 * * * ?");

            QuartzManager.addJob(sche, "数据库保存异常", DbExceptionProcessJob.class, "0 */1 * * * ?");

           // QuartzManager.addJob(sche, "定时刷新任务状态为成功", TaskStateUpdateToSucessJob.class, "0 10 11 ? * *");// 0 0 5 ? * *

            // 每天执行
            QuartzManager.addJob(sche, "定时删除服务器号码文件", DeletePhoneFileJob.class, "59 59 23 * * ?");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 创建定时发送任务的定时器
     *
     * @Description:
     * @author:oygy
     * @time:2017年4月28日 上午10:25:48
     */
    public static void addTimeTask(Date excuteTime, long taskId) {
        try {
            String jobId = taskId + "";
            logger.info("startTask 设定执行时间：" + excuteTime);
            
            Scheduler sched = schedulerFactory.getScheduler();  
            TriggerKey triggerKey = TriggerKey.triggerKey("TimeTask" + taskId, "TimeTask");
            if(triggerKey != null){
            	sched.pauseTrigger(triggerKey);// 停止触发器  
            	sched.unscheduleJob(triggerKey);// 移除触发器  
            	sched.deleteJob(JobKey.jobKey(jobId, "TimeTask"));// 删除任务
            }
            
            JobDetail jobDetail = JobBuilder.newJob(MtTaskJob.class).withIdentity(jobId, "TimeTask").build(); 
            
            Trigger trigger =  TriggerBuilder.newTrigger()//创建一个新的TriggerBuilder来规范一个触发器  
                    			.withIdentity("TimeTask" + taskId, "TimeTask")//给触发器一个名字和组名  
//                  			.startNow()//立即执行  
                    			.startAt(excuteTime)//设置触发开始的时间  
//                    			.withSchedule  
//                    			(  
//                    					SimpleScheduleBuilder.simpleSchedule()  
//                    					.withIntervalInSeconds(2)//时间间隔  
//                    					.withRepeatCount(5)//重复次数（将执行6次）  
//                    			)  
                    			.build();//产生触发器  

            sched.scheduleJob(jobDetail, trigger);
            sched.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
    
    
	/*public static void main(String[] args) {

		
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//小写的mm表示的是分钟
		String dstr = "2017-07-07 19:12:00";
		Long s = 101L;
		java.util.Date date;
		try {
			date = sdf.parse(dstr);
			addTimeTask(date,s);
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/


    public void start() {

    }
}
