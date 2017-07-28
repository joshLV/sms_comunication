package com.dzd.phonebook.controller;

import com.dzd.base.util.SessionUtils;
import com.dzd.phonebook.entity.SysUser;
import com.dzd.phonebook.entity.VertifyCode;
import com.dzd.phonebook.service.SmsUserService;
import com.dzd.phonebook.service.UserMessageService;
import com.dzd.phonebook.service.VertifyCodeService;
import com.dzd.phonebook.util.DzdResponse;
import com.dzd.phonebook.util.ErrorCodeTemplate;
import com.dzd.phonebook.util.SmsUser;
import com.dzd.phonebook.util.send.api.SendSmsUtil;
import com.dzd.phonebook.util.send.api.SmsContentBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * 验证码Controller
 * Created by Administrator on 2017/6/12.
 */
@Controller
@RequestMapping("/smsVertifyCode")
public class VerifyCodeController {
    @Autowired
    private VertifyCodeService vertifyCodeService;
    @Autowired
    private UserMessageService userMessageService;
    @Autowired
    private SmsUserService smsUserService;

    /**
     * 获取验证码
     *
     * @param request
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping("/getSmsVertifyCode")
    @ResponseBody
    public Object getVertifyCode(HttpServletRequest request) throws Exception {
        DzdResponse response = new DzdResponse();
        try {
            Object phone = request.getParameter("phone");
            Object type = request.getParameter("type");
            Object imgVerifyCode = request.getParameter("imgcode");;
            if(imgVerifyCode == null || imgVerifyCode.equals("")){
                return null;
            }

            
            if (phone == null || "".equals(phone)) {
                response.setRetCode(ErrorCodeTemplate.CODE_FAIL);
                return response;
            }

            // 2. 发送短信验证码
            VertifyCode code = new VertifyCode();
            code.setPhone(phone.toString());
            code.setCreate_time(new Date());
            code.setVertifycode(SendSmsUtil.createVertifyCode());

            
            if (type != null && !type.equals("")) {
                HttpSession session = request.getSession();
                //session中图片验证码
                Object randomString = session.getAttribute("randomString");
                //前台输入的图片验证码
                String imgCode = imgVerifyCode.toString() ;
                if (imgCode != null && !"".equals(imgCode) && randomString != null
                        && !"".equals(randomString.toString())
                        && imgCode.trim().toUpperCase().equals(randomString.toString().trim().toUpperCase())) {

                } else {
                    response.setRetCode(ErrorCodeTemplate.CODE_FAIL);
                    response.setData("图形验证码错误!");
                    return response;
                }
                //咨询与反馈获取短信验证码
                Integer phoneIsHad=smsUserService.queryPhone(phone.toString()); //查询手机号码是否注册过
                if(phoneIsHad==0){
                    response.setRetCode(ErrorCodeTemplate.CODE_FAIL);
                    response.setData(ErrorCodeTemplate.PHONE_NO_HAVE);
                    return response;
                }
                // 1.查询短信验证码次数,不能超过1次 返回提示
                List<VertifyCode> todayCodeList = vertifyCodeService.getCodeCountByPhoneAndType(phone.toString(),Integer.parseInt(type.toString()));
                if (todayCodeList != null && todayCodeList.size() >= 1) {
                    response.setData(ErrorCodeTemplate.MSG_VERTIFYCODE_INPUT_ONE);
                    response.setRetCode(ErrorCodeTemplate.CODE_FAIL);
                    return response;
                }
                code.setContent(SmsContentBean.getSendVerifyCodeSmsContent(code));
            }else{
                //发送短信页面获取验证码
                code.setContent(SmsContentBean.getSendVerifyCodeSms(code));
            }

            if(type != null && !type.equals("")) {
                code.setType(1);
            }else{
                code.setType(0);
            }
            // 发送验证码短信
            SendSmsUtil.sendSMS(code);
            vertifyCodeService.add(code);
            response.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.setData(ErrorCodeTemplate.MSG_SYSTEM_ERROR_MSG);
            response.setRetCode(ErrorCodeTemplate.CODE_FAIL);
        }
        return response;
    }


    /**
     * 获取发送短信验证码
     *
     * @param request
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping("/getVertifyCodeBySend")
    @ResponseBody
    public Object getVertifyCodeBySend(HttpServletRequest request) throws Exception {
        DzdResponse dzdResponse = new DzdResponse();
        try {
            // 1. 接收参数
            Object phone = request.getParameter("phone");
            if (phone == null || "".equals(phone)) {
                dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
                return dzdResponse;
            }

            // 2. 发送短信验证码
            VertifyCode code = new VertifyCode();
            code.setPhone(phone.toString());
            code.setCreate_time(new Date());
            code.setType(0);
            code.setVertifycode(SendSmsUtil.createVertifyCode());
            code.setContent(SmsContentBean.getSendVerifyCodeSms(code));

            // 3. 发送验证码短信 获取验证码
            SendSmsUtil.sendSMS(code);
            vertifyCodeService.add(code);
            dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
            return dzdResponse;
        } catch (Exception e) {
            e.printStackTrace();
            dzdResponse.setData(ErrorCodeTemplate.MSG_SYSTEM_ERROR_MSG);
            dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
        }
        return dzdResponse;
    }


    /**
     * 校验验证码是否正确
     *
     * @param request
     * @return
     */
    @RequestMapping("/checkVerifyCode")
    @ResponseBody
    public DzdResponse checkVerify(HttpServletRequest request) {
        DzdResponse dzdResponse = new DzdResponse();
        try {
            Object phone = request.getParameter("phone");
            Object verifyCode = request.getParameter("verifyCode");

            if (phone == null) {
                dzdResponse.setRetMsg(ErrorCodeTemplate.MSG_SMS_PHONE_IS_EMPTY);
                dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
                return dzdResponse;
            }

            if (verifyCode == null) {
                dzdResponse.setRetMsg(ErrorCodeTemplate.MSG_SMS_VERIFY_IS_EMPTY);
                dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
                return dzdResponse;
            }


            // 查询验证码
            VertifyCode vertify = vertifyCodeService.queryCodeByPhoneAndCode(phone.toString(), verifyCode.toString());
            if (vertify == null) {
                dzdResponse.setRetMsg(ErrorCodeTemplate.MSG_VERIFYCODE_ERROR);
                dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
            } else {
                dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            dzdResponse.setRetMsg(ErrorCodeTemplate.MSG_SYSTEM_ERROR_MSG);
            dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
        }
        return dzdResponse;

    }


    /**
     * 校验验证码是否正确 - 发送短信专用
     *
     * @param request
     * @return
     */
    @RequestMapping("/checkVerifyCodeBySendSms")
    @ResponseBody
    public DzdResponse checkVerifyBySendSms(HttpServletRequest request) {
        DzdResponse dzdResponse = new DzdResponse();
        try {
            Object phone = request.getParameter("phone");
            Object verifyCode = request.getParameter("verifyCode");

            if (phone == null) {
                dzdResponse.setRetMsg(ErrorCodeTemplate.MSG_SMS_PHONE_IS_EMPTY);
                dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
                return dzdResponse;
            }

            if (verifyCode == null) {
                dzdResponse.setRetMsg(ErrorCodeTemplate.MSG_SMS_VERIFY_IS_EMPTY);
                dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
                return dzdResponse;
            }


            // 查询验证码
            VertifyCode vertify = vertifyCodeService.queryCodeByPhoneAndCode(phone.toString(), verifyCode.toString());
            if (vertify == null) {
                dzdResponse.setRetMsg(ErrorCodeTemplate.MSG_VERIFYCODE_ERROR);
                dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
            } else {
                SysUser sysUser = SessionUtils.getUser(request);
                SmsUser smsUsers = userMessageService.querySmsUserById(sysUser.getId());
                smsUsers.setVerifyType(1);
                smsUserService.updateSmsUserVerifyType(smsUsers);
                dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            dzdResponse.setRetMsg(ErrorCodeTemplate.MSG_SYSTEM_ERROR_MSG);
            dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
        }
        return dzdResponse;

    }
}
