package com.dzd.phonebook.service;


import com.dzd.base.service.BaseService;
import com.dzd.db.mysql.MysqlOperator;
import com.dzd.phonebook.dao.UserMsmSendDao;
import com.dzd.phonebook.entity.SmsReceiveReplyPush;
import com.dzd.phonebook.entity.SysMenuBtn;
import com.dzd.phonebook.util.DzdPageParam;
import com.dzd.phonebook.util.SmsAisleGroup;
import com.dzd.phonebook.util.SmsSendLog;
import com.dzd.phonebook.util.SmsSendTask;
import com.dzd.sms.application.Define;
import com.github.pagehelper.Page;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;


/**
 * 发送消息服务类
 *
 * @author ougy
 * @date 2016-6-24
 */
@Service("userMsmSendService")
public class UserMsmSendService<T> extends BaseService<T> {


    @Autowired
    private UserMsmSendDao<T> mapper;

    public UserMsmSendDao<T> getDao() {
        return mapper;
    }


    /**
     * @Description:根据菜单按钮列显示查询发送记录
     */
    public Page<SmsSendTask> queryMsmSendList(List<SysMenuBtn> sysMenuBtns, DzdPageParam dzdPageParam) {
        DzdPageParam dzdPageParams = preQueryParameter(sysMenuBtns, dzdPageParam);
        return getDao().queryMsmSendListPage(dzdPageParams);
    }


	/**
	 * @Title: preQueryParameter
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @author:    hz-liang
	 * @param sysMenuBtns
	 * @param dzdPageParam
	 * @return  
	 * @return: DzdPageParam   
	 * @throws
	 */
	private DzdPageParam preQueryParameter(List<SysMenuBtn> sysMenuBtns, DzdPageParam dzdPageParam)
	{
		DzdPageParam dzdPageParams = dzdPageParam;
        Map<String, Object> columnMap = (Map<String, Object>) dzdPageParams.getCondition();

        if (sysMenuBtns != null) {
            for (SysMenuBtn sysMenuBtn : sysMenuBtns) {
                //  通道类型列
                if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.SENDLIST_AISLE_COLUMN_URL)) {
                    columnMap.put(Define.MENUURLACTION.AISLECOLUMN, true);
                    break;
                }


            }
        }
        dzdPageParams.setCondition(columnMap);
		return dzdPageParams;
	}
    
    /**
     * @Description:根据菜单按钮列显示查询发送记录
     */
    public Page<SmsSendTask> querySendListPage(List<SysMenuBtn> sysMenuBtns, DzdPageParam dzdPageParam) {
    	DzdPageParam dzdPageParams = preQueryParameter(sysMenuBtns, dzdPageParam);
    	return getDao().querySendListPage(dzdPageParams);
    }


    /**
     * 查询发送详情
     *
     * @param smsSendLog
     * @param sysMenuBtns
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public List<SmsSendLog> querySendDetailList(SmsSendLog smsSendLog, List<SysMenuBtn> sysMenuBtns) {
        try {

            if (sysMenuBtns != null) {
                for (SysMenuBtn sysMenuBtn : sysMenuBtns) {

                    // 状态报告列
                	if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.SENDDETAIL_REPORT_COLUMN_URL)) {
                        smsSendLog.setStatusReportColumn("true");
                    }

                    // 状态时间
                    else if (sysMenuBtn.getActionUrls().equals(Define.MENUURLACTION.SENDDETAIL_STATUSTIME_COLUMN_URL)) {
                        smsSendLog.setStatusTimeColumn("true");
                    }

                }
            }
            
            Integer rowCount =queryByCount(smsSendLog);// 总数
    		Integer pageOffset = smsSendLog.getPager().getPageOffset();// 当前页
    		Integer pageSize = smsSendLog.getPager().getPageSize();// 每页显示数据
    		
    		smsSendLog.getPager().setRowCount(rowCount);
    		smsSendLog.getPager().setPageOffset(pageOffset);
    		smsSendLog.setRows(pageSize);
            
            String selectSql = "select ssk.id, su.`name` smsUserName, su.email smsUserEmail, sstp.phone receivePhone, sstp.state, if(ssk.send_type=0,ssk.create_time,ssk.timing_time) actualSendTime, sstp.send_time sendTime, ssk.content, ssk.send_type sendType, ssk.timing_time timing, ssk.create_time createTime";
            
            final String statusReportColumn = smsSendLog.getStatusReportColumn();
            final String statusTimeColumn = smsSendLog.getStatusTimeColumn();
            if(statusReportColumn!=null){
            	selectSql += ", sstp.receive_state fkState, sstp.receive_code receiveCode";
            }
            if(statusTimeColumn!=null){
            	selectSql += ", sstp.receive_time feedbackTime, sstp.push_state receiveState";
            }

            String fromSql = " from sms_send_task_phone sstp LEFT JOIN sms_send_task ssk on sstp.sms_send_task_id = ssk.id LEFT JOIN sms_user su on su.id = ssk.sms_user_id";
            
            String whereSql = " where 1=1";
            if(!StringUtils.isEmpty(smsSendLog.getStartInput())){
            	if(!StringUtils.isEmpty(smsSendLog.getLogTime())){
            		whereSql += " and ((ssk.send_type=0 && (date_format(ssk.create_time, '%Y-%m-%d %H:%i') BETWEEN date_format('" + smsSendLog.getStartInput() +"', '%Y-%m-%d %H:%i') AND date_format('" + smsSendLog.getEndInput() +"', '%Y-%m-%d %H:%i'))) or (ssk.send_type!=0 && (date_format(ssk.timing_time, '%Y-%m-%d %H:%i') BETWEEN date_format('" + smsSendLog.getStartInput() +"', '%Y-%m-%d %H:%i') AND date_format('" + smsSendLog.getEndInput() +"', '%Y-%m-%d %H:%i'))))";
            	}else{
            		whereSql += " and date_format(ssk.create_time, '%Y-%m-%d %H:%i') >= date_format('" + smsSendLog.getStartInput() +"', '%Y-%m-%d %H:%i') and date_format(ssk.create_time, '%Y-%m-%d %H:%i') <= date_format('" + smsSendLog.getEndInput() +"', '%Y-%m-%d %H:%i')";
            	}
            }
            if(smsSendLog.getStateBs() != null && smsSendLog.getState() != null){
            	whereSql += " and sstp.state = " + smsSendLog.getState();
            }
            if(smsSendLog.getStateBs() != null){
            	whereSql += " and sstp.state <99 and sstp.state>=2";
            }
            if(!StringUtils.isEmpty(smsSendLog.getContent())){
            	whereSql += " and ssk.content like '%" + smsSendLog.getContent() + "%'";
            }
            if(smsSendLog.getId() != null){
            	whereSql += " and ssk.id = " + smsSendLog.getId();
            }
            if(!StringUtils.isEmpty(smsSendLog.getReceivePhone())){
            	whereSql += " and sstp.phone = " + smsSendLog.getReceivePhone();
            }
            if(!StringUtils.isEmpty(smsSendLog.getSmsUserEmail())){
            	whereSql += " and su.email = '" + smsSendLog.getSmsUserEmail() + "'";
            }
            if(smsSendLog.getSysUserId() != null){
            	whereSql += " and su.sys_user_id = '" + smsSendLog.getSysUserId() + "'";
            }
            if("today".equals(smsSendLog.getLogTime())){
            	if(smsSendLog.getType() != null && -2 == smsSendLog.getType()){
            		whereSql += " and (sstp.state = 0 or sstp.state = 99)";
            	}
            	if(smsSendLog.getType() != null && -1 == smsSendLog.getType()){
            		whereSql += " and sstp.state = -1";
            	}
            	if(smsSendLog.getType() != null && 0 == smsSendLog.getType()){
            		whereSql += " and sstp.state <99 and sstp.state>=2";
            	}
            	if(smsSendLog.getType() != null && 1 == smsSendLog.getType()){
            		whereSql += " and sstp.state = 100";
            	}
            }else if("history".equals(smsSendLog.getLogTime())){
            	if(smsSendLog.getType() != null && 0 == smsSendLog.getType()){
            		whereSql += " and sstp.state <99 and sstp.state>=2";
            	}
            	if(smsSendLog.getType() != null && 1 == smsSendLog.getType()){
            		whereSql += " and (sstp.state = 100 or sstp.state = 0 or sstp.state = 99)";
            	}
            }else{
            	if(smsSendLog.getType() != null && -2 == smsSendLog.getType()){
            		whereSql += " and (sstp.state = 0 or sstp.state = 99)";
            	}
            	if(smsSendLog.getType() != null && -1 == smsSendLog.getType()){
            		whereSql += " and sstp.state = -1";
            	}
            	if(smsSendLog.getType() != null && 0 == smsSendLog.getType()){
            		whereSql += " and sstp.state <99 and sstp.state>=2";
            	}
            	if(smsSendLog.getType() != null && 1 == smsSendLog.getType()){
            		whereSql += " and sstp.state = 100";
            	}
            }
            if("today".equals(smsSendLog.getLogTime())){
            	whereSql += " and ( ( ssk.send_type=0 && to_days(ssk.create_time) = to_days(now())) || (ssk.send_type=1 &&  date_format(ssk.timing_time, '%Y-%m-%d %H:%i') BETWEEN date_format(now(), '%Y-%m-%d 00:00') and date_format(now(), '%Y-%m-%d %H:%i') )  )";
            }
            if(!CollectionUtils.isEmpty(smsSendLog.getSysUserIds())){
            	StringBuffer strbf = new StringBuffer();
            	boolean flag = true;
            	for(Integer sysId : smsSendLog.getSysUserIds()){
            		if(flag){
            			strbf.append(sysId);
            			flag = false;
            		}
            		strbf.append("," + sysId);
            	}
            	whereSql += " and su.sys_user_id in(" + strbf.toString() +")";
            }
            
            whereSql += " and (ssk.send_type=0 || (ssk.send_type=1 && date_format(ssk.timing_time, '%Y-%m-%d %H:%i:%s') < date_format(now(), '%Y-%m-%d %H:%i:%s')))";
            
            String orderSql = " order by actualSendTime desc";
            
            String pageSql = "";
            if(!StringUtils.isEmpty(smsSendLog.getPager().getOrderCondition())){
            	pageSql += " " + smsSendLog.getPager().getOrderCondition();
            }
            if(!StringUtils.isEmpty(smsSendLog.getPager().getMysqlQueryCondition())){
            	pageSql += " " + smsSendLog.getPager().getMysqlQueryCondition();
            }
            
            String sql = selectSql + fromSql + whereSql + orderSql + pageSql;
            List<SmsSendLog> smsSendLogList = MysqlOperator.I.query(sql, new RowMapper()
			{
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException
				{
					SmsSendLog smsSendLog = new SmsSendLog();
					smsSendLog.setId(rs.getInt("id"));
					smsSendLog.setSmsUserName(rs.getString("smsUserName"));
					smsSendLog.setSmsUserEmail(rs.getString("smsUserEmail"));
					smsSendLog.setReceivePhone(rs.getString("receivePhone"));
					smsSendLog.setState(rs.getInt("state"));
					smsSendLog.setActualSendTime(rs.getTimestamp("actualSendTime"));
					smsSendLog.setSendTime(rs.getTimestamp("sendTime"));
					smsSendLog.setContent(rs.getString("content"));
					smsSendLog.setSendType(rs.getInt("sendType"));
					smsSendLog.setTiming(rs.getTimestamp("timing"));
					smsSendLog.setCreateTime(rs.getTimestamp("createTime"));
					
					if(statusReportColumn!=null){
						smsSendLog.setFkState(rs.getString("fkState"));
						smsSendLog.setReceiveCode(rs.getString("receiveCode"));
		            }
		            if(statusTimeColumn!=null){
		            	smsSendLog.setFeedbackTime(rs.getTimestamp("feedbackTime"));
		            	smsSendLog.setReceiveState(rs.getInt("receiveState"));
		            }
					return smsSendLog;
				}
			});
            
            return smsSendLogList;
//            return (List<SmsSendLog>) super.queryByList(smsSendLog);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询任务所有的通道组
     *
     * @param uid
     * @param bid
     * @return
     */
    public List<SmsAisleGroup> queryTaskAisleGroup(Integer uid, Integer bid, Integer superAdmin) {
        return getDao().queryTaskAisleGroup(uid, bid, superAdmin);
    }

    public List<SmsAisleGroup> queryTaskAisleType() {
        return getDao().queryTaskAisleType();
    }

    /**
     * @Description:发送消息统计数量
     * @author:oygy
     * @time:2017年4月1日 上午10:56:47
     */
    public SmsSendTask queryMsmSendCount(DzdPageParam dzdPageParam) {
        return getDao().queryMsmSendCount(dzdPageParam);
    }

    /**
     * @Description:根据消息ID查询出本批次发送的所有手机号码
     * @author:oygy
     * @time:2017年1月7日 下午4:46:17
     */
    public List<String> queryMsmSendPhoneByid(Map map) {
        return getDao().queryMsmSendPhoneByid(map);
    }

    /**
     * @Description:查询消息详情列表
     * @author:oygy
     * @time:2017年1月9日 上午11:42:42
     */
    public Page<SmsSendLog> queryMsmSendDetailsList(DzdPageParam dzdPageParam) {
        return getDao().queryMsmSendDetailsListPage(dzdPageParam);
    }

    /**
     * 根据消息ID查询消息发送状态
     *
     * @Description:
     * @author:oygy
     * @time:2017年1月13日 下午6:33:17
     */
    public Integer querySmsSendById(Integer smsTaskId) {
        return getDao().querySmsSendById(smsTaskId);
    }

    /**
     * 根据消息ID修改消息发送状态为终止发送
     *
     * @Description:
     * @author:oygy
     * @time:2017年1月13日 下午6:33:17
     */
    public void updateSmsSendById(Integer smsTaskId) {
        getDao().updateSmsSendById(smsTaskId);
    }

    /**
     * 查询用户的回复信息
     *
     * @Description:
     * @author:oygy
     * @time:2017年1月16日 上午9:59:43
     */
    public Page<SmsReceiveReplyPush> queryUserList(DzdPageParam dzdPageParam) {
        return getDao().queryUserPage(dzdPageParam);
    }

    /**
     * @Description:根据用户ID查询所拥有的通道组
     * @author:oygy
     * @time:2017年3月14日 下午4:08:40
     */
    public List<SmsAisleGroup> querySmsGroupByUserId(Integer uid) {
        return getDao().querySmsGroupByUserId(uid);
    }

}
