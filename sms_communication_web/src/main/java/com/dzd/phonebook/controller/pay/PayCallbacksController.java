package com.dzd.phonebook.controller.pay;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.dzd.utils.LogUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dzd.phonebook.entity.SmsRechargeOrder;
import com.dzd.phonebook.entity.SmsUserBlank;
import com.dzd.phonebook.service.Instruct;
import com.dzd.phonebook.service.SmsRechargeOrderService;
import com.dzd.phonebook.service.SmsUserBlankService;
import com.dzd.phonebook.service.SmsUserService;
import com.dzd.phonebook.util.InstructState;
import com.dzd.phonebook.util.RechargeVariable;
import com.dzd.phonebook.util.RedisUtil;
import com.dzd.phonebook.util.SmsUserMoneyRunning;

/**
 * 支付成功，回调 Created by CHENCHAO on 2017/5/24.
 */
@RequestMapping("/pay")
@Controller
public class PayCallbacksController {
	public static final LogUtil log = LogUtil.getLogger(PayCallbacksController.class);

	@Autowired
	private SmsRechargeOrderService smsRechargeOrderService;
	@Autowired
	private SmsUserBlankService smsUserBlankService;
	@Autowired
	private SmsUserService smsUserService;

	/**
	 * 回调地址
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping("/payCallBacks")
	public String callbacks(HttpServletRequest request) {
		String status = request.getParameter("status");
		String orderNo = request.getParameter("orderId");
		String amount = request.getParameter("amount");

		log.info("-----------------》充值回调：orderNo:" + orderNo + "," + "status:" + status + "amount:" + amount);

		try {
			// 1.根据订单号查询订单
			SmsRechargeOrder order = smsRechargeOrderService.queryRechargeOrderByOrderId(orderNo);
			if (order == null) {
				System.out.println("无此订单,订单号为:" + orderNo);
				return "notFound";
			} else {
				log.info("-------------》订单：" + order.getOrderNo());
				Integer orderStatus = order.getStatus();
				// 2.如果订单已经支付成功,则直接返回
				if (RechargeVariable.RECHARGE_ORDER_STATUS_SUCCESS == orderStatus) {
					log.info("-------------》订单已经支付成功!" + order.getOrderNo());
					return null;
				}

				// 3.修改订单状态
				order.setStatus(RechargeVariable.RECHARGE_ORDER_STATUS_SUCCESS);
				order.setUpdated(new Date());
				smsRechargeOrderService.updateSmsRechargeOrder(order);

				order = smsRechargeOrderService.queryRechargeOrderByOrderId(orderNo);


				if (order!=null && order.getStatus() .equals(RechargeVariable.RECHARGE_ORDER_STATUS_SUCCESS)) {
					// 4. 修改用户余额
					Integer uid = order.getSmsUserId();
					SmsUserBlank smsUserBlank = smsUserBlankService.queryUserBlank(uid);
					Integer surplusNum = smsUserBlank.getSurplusNum();// 充值前条数
					Integer orderSmsNum = order.getSmsNumber();// 充值条数
					Integer afterSmsNum = surplusNum + orderSmsNum; // 充值后的总条数
					smsUserBlank.setSurplusNum(afterSmsNum);
					smsUserBlank.setSumNum(orderSmsNum);
					smsUserBlankService.updateUserBlank(smsUserBlank);
					log.info("--------------》修改用户余额成功！");

					// 发送动作指令到redis
					String keys = smsUserService.querySmsUserKey(uid);
					instructSend(InstructState.USERTOPUP_SUCESS, keys, uid); // 发送动作指令到redis

					// 5. 添加到充值记录
					SmsUserMoneyRunning moneyRunning = new SmsUserMoneyRunning();
					moneyRunning.setUid(order.getUserId());// 操作人id
					moneyRunning.setType(3);// 快钱充值
					moneyRunning.setSmsUserId(uid);// 被操作人id
					moneyRunning.setComment("快钱充值");// 备注
					moneyRunning.setBeforeNum(surplusNum);// 操作前条数
					moneyRunning.setAfterNum(afterSmsNum);// 操作后条数
					moneyRunning.setOperateNum(orderSmsNum);// 操作条数
					moneyRunning.setOrderNo(orderNo);// 订单编号
					moneyRunning.setCreateTime(new Date());// 创建时间
					smsUserService.saveSmsUserMoneyRunning(moneyRunning);
					log.info("--------------》消费记录成功！");
					return "<result>1</result><redirecturl>http://www.yoursite.com/show.asp</redirecturl>";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "/error";
		}
		return "/";
	}

	private void instructSend(String keys, String smsUserKey, Integer smsUserId) {
		Instruct instruct = new Instruct();
		instruct.setKey(keys);
		instruct.setSmsUserKey(smsUserKey);
		instruct.setSmsUserId(smsUserId + "");
		ObjectMapper mapper = new ObjectMapper();
		try {
			String jsonStr = mapper.writeValueAsString(instruct);
			RedisUtil.publish(InstructState.AB, jsonStr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
