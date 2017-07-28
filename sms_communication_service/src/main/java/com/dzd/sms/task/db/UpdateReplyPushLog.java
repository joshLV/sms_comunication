package com.dzd.sms.task.db;

import com.dzd.cache.redis.manager.RedisClient;
import com.dzd.cache.redis.manager.RedisManager;

import com.dzd.db.mysql.MysqlOperator;
import com.dzd.sms.application.Define;
import com.dzd.sms.service.data.SmsReplyPush;
import com.dzd.sms.task.base.BaseTask;
import com.dzd.utils.DateUtil;

import com.dzd.utils.LogUtil;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017-1-16.
 */

public class UpdateReplyPushLog   extends BaseTask {
    private static LogUtil logger = LogUtil.getLogger(UpdateReplyPushLog.class);
    String mkey = Define.KEY_SMS_AISLE_REPLY_PUSH_DB;
    RedisClient redisClient = RedisManager.I.getRedisClient();

    public UpdateReplyPushLog() {
        this.singlePackageNumber = 100;
        this.threadNumber = 3;
    }



    /**
     * 判断是否有任务可以执行
     */
    public boolean existTask() {
        return redisClient.llen(mkey) > 0;
    }
    public Long queryTaskNumber(){ return redisClient.llen(mkey); }
    @Override
    public void singleExecutor() {
        List<SmsReplyPush> smsReplyPushArrayList = new ArrayList<SmsReplyPush>();
        try {

            String sql = "select t.content from sms_send_task_phone as p, sms_send_task as t  where p.sms_send_task_id=%s and t.id=p.sms_send_task_id and p.aid=%s and p.phone='%s'  order by p.id desc  limit 1";
            long len = redisClient.llen(mkey);
            if (len > singlePackageNumber) len = singlePackageNumber;
            for (int i = 0; i < len; i++) {
                Object objectValue = redisClient.lpop(mkey);
                if (objectValue != null) {
                    SmsReplyPush smsReplyPush = (SmsReplyPush) objectValue;


                    //加一个回复内容
                    long aisleId = smsReplyPush.getAisleId();
                    long taskId = smsReplyPush.getTaskId();
                    String phone = smsReplyPush.getPhone();

                    Map<String, Object> m = MysqlOperator.I.queryForMap(String.format(sql, taskId, aisleId, phone) );
                    smsReplyPush.setSendContent( m.get("content").toString());

                    smsReplyPushArrayList.add( smsReplyPush );
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        //这里插入， 是因为在收到回复时， 并没有计算出需要回复状态的列表
        try {

            logger.info("smsReplyPushArrayList.size()="+smsReplyPushArrayList.size());
            if (smsReplyPushArrayList.size() > 0) {
                final List<SmsReplyPush> tmpSmsReplyPushList = smsReplyPushArrayList;
                String sql = "insert into  sms_receive_reply_push( sms_aisle_id, phone, content, send_content, update_time, create_time,sms_user_id,sms_send_task_id,region,state )" +
                        " values(?,?,?,?,?,?,?,?,?,? )";
                MysqlOperator.I.batchUpdate(sql, new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {

                        ps.setLong(1, tmpSmsReplyPushList.get(i).getAisleId());
                        ps.setString(2, tmpSmsReplyPushList.get(i).getPhone());
                        ps.setString(3, tmpSmsReplyPushList.get(i).getContent());
                        ps.setString(4, tmpSmsReplyPushList.get(i).getSendContent());
                        ps.setString(5, DateUtil.formatDateTime( tmpSmsReplyPushList.get(i).getUpdateTime() ) );
                        ps.setString(6, DateUtil.formatDateTime( tmpSmsReplyPushList.get(i).getCreateTime() ) );
                        ps.setLong(7, tmpSmsReplyPushList.get(i).getUserId());
                        ps.setLong(8, tmpSmsReplyPushList.get(i).getTaskId());
                        ps.setString(9, tmpSmsReplyPushList.get(i).getRegion());
                        ps.setInt(10, tmpSmsReplyPushList.get(i).getState());
                    }

                    public int getBatchSize() {
                        return tmpSmsReplyPushList.size();
                    }
                });

            }
        } catch (Exception e) {
            excep = true;
            e.printStackTrace();
            logger.error("Exception:" + e.getCause().getClass() + "," + e.getCause().getMessage());
        }


    }

}
