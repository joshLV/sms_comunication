package com.dzd.phonebook.service;

import com.dzd.base.util.SessionUtils;
import com.dzd.phonebook.entity.SysLog;
import com.dzd.phonebook.entity.SysRoleRel;
import com.dzd.phonebook.entity.SysUser;
import com.dzd.phonebook.util.DzdPageParam;
import com.dzd.phonebook.util.SmsUser;
import com.dzd.sms.application.Define;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dzd-technology01 on 2017/6/14.
 */
public class CommonRoleServiceUtil {

    public static List<SysUser> getSysUserBUserList(HttpServletRequest request, SmsUserService userService, SysRoleRelService rolService, SmsAccountService accountService) {
        SysRoleRelService sysRoleRelService = rolService;
        SmsAccountService smsAccountService = accountService;
        SysUser user = SessionUtils.getUser(request);
        List<SysRoleRel> sysRoleRels = queryRoleByUserId(user.getId(), sysRoleRelService);
        List<SysUser> sysUserList = new ArrayList<SysUser>();  //用户 "归属"

        List<Integer> list = new ArrayList<Integer>();
        //查询所有“一、二级”用户
        list.add(Define.ROLEID.LEVEL_1);
        list.add(Define.ROLEID.LEVEL_2);

        //查询登陆用户下面所有子归属（下拉列表“归属”）
        for (SysRoleRel sysRoleRel : sysRoleRels) {
            //如果当前登录用户为“客服”查询所有账户
            if (sysRoleRel.getRoleId() == Integer.parseInt(Define.ROLEID.ROLE_CUSTOMER_SERVICE)) {
                sysUserList = userService.queryByUserLevel(list);
            }
            //如果当前登录用户为 “一级管理员”， "业务员" ,"销售经理"，"用户管理员" 查询归属账户
            if (sysRoleRel.getRoleId() == Integer.parseInt(Define.ROLEID.ROLE_FIRST_LEVEL_ADMINISTRATOR)
                    ||sysRoleRel.getRoleId() == Integer.parseInt(Define.ROLEID.ROLE_SALES_MANAGER)
                    || sysRoleRel.getRoleId() == Integer.parseInt(Define.ROLEID.ROLE_SALESMAN)
                    || sysRoleRel.getRoleId() == Integer.parseInt(Define.ROLEID.ROLE_USER_ADMINISTRATOR)) {
                List list2 = new ArrayList();
                //当前登陆用户id
                list2.add(user.getId());
                list2.addAll(getIds(list2,smsAccountService));
                sysUserList = userService.queryBySysUserId(list2);
            }
        }
        if (user.getSuperAdmin() == 1) {
            //超级管理员
            sysUserList = userService.queryByUserLevel(list);
        }
        return sysUserList;
    }

    /**
     * 获取不同账户下面的子归属sys_user_id
     * @param sysUser
     * @param dzdPageParam
     * @param sortMap
     * @return
     */
    public static DzdPageParam getSysUserId(SysUser sysUser,DzdPageParam dzdPageParam,Map<String, Object> sortMap,SysRoleRelService sysRoleRelService,SmsAccountService smsAccountService){
        List<Integer> list = new ArrayList<Integer>();
        //登陆用户不是超级管理员("超级管理员"，"客服" 查看所有账户)
        if (sysUser != null && sysUser.getSuperAdmin() != 1) {
        	list.add(sysUser.getId());
            List<SysRoleRel> sysRoleRels = queryRoleByUserId(sysUser.getId(), sysRoleRelService);
            for (SysRoleRel sysRoleRel : sysRoleRels) {
                //如果当前登录为 "一级管理员"，"销售经理" ,"用户管理","业务员" 可以查看归属账户
                if (sysRoleRel.getRoleId() == Integer.parseInt(Define.ROLEID.ROLE_FIRST_LEVEL_ADMINISTRATOR)
                        || sysRoleRel.getRoleId() == Integer.parseInt(Define.ROLEID.ROLE_SALES_MANAGER)
                        || sysRoleRel.getRoleId() == Integer.parseInt(Define.ROLEID.ROLE_USER_ADMINISTRATOR)
                        || sysRoleRel.getRoleId() == Integer.parseInt(Define.ROLEID.ROLE_SALESMAN)) {
                    list.addAll(getIds(list, smsAccountService));
                    sortMap.put("smsUserVal", list);
                }else if(sysRoleRel.getRoleId() == Integer.parseInt(Define.ROLEID.ROLE_USER)){
                    //登录账户为 "用户"，查看自己
                    sortMap.put("smsUserVal", list);
                }
            }
        }
        return dzdPageParam;
    }
    /**
     * @Description:根据用户ID查询拥有角色
     * @author:oygy
     * @time:2016年12月31日 上午11:12:53
     */
    public  static List<SysRoleRel> queryRoleByUserId(Integer uid,SysRoleRelService sysRoleRelService) {
        DzdPageParam dzdPageParam = new DzdPageParam();
        Map<String, Object> condition = new HashMap<String, Object>();

        condition.put("sysUserId", uid);
        dzdPageParam.setCondition(condition);
        List<SysRoleRel> sysRoleRels = sysRoleRelService.queryRoleByUserId(dzdPageParam);

        return sysRoleRels;
    }

    /**
     * 根据登陆用户id查询下面所有子归属
     *
     * @param list
     */
    public static List getIds(List list,SmsAccountService smsAccountService) {
        List<Integer> result = smsAccountService.queryBids(list);
         //防止自己归属自己 查询出现死循环
        for(int i=0;i<list.size();i++){
            if(result.contains(list.get(i))){
                result.remove(list.get(i));
            }
        }

        if (result.size() > 0) {
            List<Integer> list1 = smsAccountService.queryBids(result);
            if (list1.size() > 0) {
                result.addAll(list1);
            }
            while (list1.size() > 0) {
                list1 = smsAccountService.queryBids(list1);
                if (list1.size() > 0) {
                    result.addAll(list1);
                }
            }
        }
        return result;
    }

    /**
     * 操作日志
     * @param user
     * @param content
     * @param sysUserService
     */
    public static void saveOperateLog(SysUser user,String email,String content,SysUserService sysUserService){
        try{
            String contents=content;
            if(email!=null){
                contents = content+":"+email;  //设置账号
            }
            SysLog syslog = new SysLog();
            syslog.setContent(contents);
            syslog.setUserName(user.getEmail());
            syslog.setSysUserId(user.getId());
            sysUserService.addSysLog(syslog);
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }
}
