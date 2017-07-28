package com.dzd.phonebook.controller;

import com.dzd.base.page.Pager;
import com.dzd.base.util.HtmlUtil;
import com.dzd.base.util.SessionUtils;
import com.dzd.phonebook.aop.MethodDescription;
import com.dzd.phonebook.entity.*;
import com.dzd.phonebook.service.*;
import com.dzd.phonebook.util.*;
import com.dzd.utils.LogUtil;
import com.github.pagehelper.Page;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色controller
 *
 * @author chenchao
 * @date 2016-6-29 11:26
 */
@Controller
@RequestMapping("/sysRole")
public class SysRoleController<T> extends AbstractController {

    @SuppressWarnings("rawtypes")
    @Autowired
    private SysRoleService sysRoleService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private SysRoleRelService sysRoleRelService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private SysMenuService sysMenuService;

    @SuppressWarnings("rawtypes")
    @Autowired
    private SysMenuBtnService sysMenuBtnService;

    @Autowired
    private SysUserService sysUserService;

    public static final LogUtil log = LogUtil.getLogger(SysUserController.class);

    @RequestMapping("/listview")
    public String menu(HttpServletRequest request, Model model) throws Exception {
        Object menuId = request.getParameter("id");
        model.addAttribute("menuId", menuId);
        return "sysRole/sysrolelist";
    }

    @RequestMapping("/addsysuser")
    public String addSysUser() throws Exception {
        return "sysUser/addsysuser";
    }

    @RequestMapping(value = "/roleListInfo")
    @ResponseBody
    public Object roleListInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        DzdResponse dzdPageResponse = new DzdResponse();
        try {
            DzdPageParam dzdPageParam = new DzdPageParam();
            //查询角色列表
            Page<SysRole> dataList = sysRoleService.queryRoleList(dzdPageParam);
            JspResponseBean jspResponseBean = new JspResponseBean();
            if (!CollectionUtils.isEmpty(dataList)) {
                jspResponseBean.setDataList(dataList.getResult());
                jspResponseBean.setTotal(dataList.getTotal());
            }
            dzdPageResponse.setData(jspResponseBean);
            dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
        } catch (Exception e) {
            log.error(null, e);
            e.printStackTrace();
        }
        return dzdPageResponse;
    }

    /**
     * 查询角色列表
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/rolelist")
    @ResponseBody
    public void list(HttpServletRequest request, SysRole role, HttpServletResponse response) throws Exception {
        try {
            Object menuId = request.getParameter("menuId");
            String offset = request.getParameter("offset");
            String pageSize = request.getParameter("limit");
            Pager pager = new Pager();
            if (offset != null && !offset.equals("")) {
                pager.setPageOffset(Integer.parseInt(offset));
            }
            if (pageSize != null && !pageSize.equals("")) {
                pager.setPageSize(Integer.parseInt(pageSize));
            }
            role.setPager(pager);

            List<SysMenuBtn> sysMenuBtns = null;
            if (menuId != null) {
                sysMenuBtns = sysMenuBtnService.queryByMenuid(Integer.parseInt(menuId.toString()));
            }
            List<SysRole> dataList = sysRoleService.queryByList(role);
            if (!CollectionUtils.isEmpty(dataList)) {
                for (SysRole sysRole : dataList) {
                    sysRole.setSysMenuBtns(sysMenuBtns);
                }
                // 设置页面数据
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("total", role.getPager().getRowCount());
                jsonObject.put("rows", dataList);
                jsonObject.put("retCode", ErrorCodeTemplate.CODE_SUCESS);
                HtmlUtil.writerJson(response, jsonObject);
            }
        } catch (Exception e) {
            log.error(null, e);
            e.printStackTrace();
        }

    }

    /**
     * 修改、增加
     *
     * @param request
     * @param data
     * @return
     */
    @RequestMapping(value = "/from/merge", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public DzdResponse merge(HttpServletRequest request, @RequestBody Map<String, Object> data) {
        DzdResponse dzdPageResponse = new DzdResponse();
        SysUser user = SessionUtils.getUser(request);
        String content = "";
        try {
            SysRole sysRole = getRequestParameters(SysRole.class, data);
            Object menuIds = data.get("menuIds");
            List<String> menuId = null;
            if (menuIds != null) {
                menuId = (List<String>) menuIds;
            }

            Object tops = data.get("tops");
            List<String> toparr = null;
            if (tops != null) {
                toparr = (List<String>) tops;
            }

            if (sysRole.getId() == null) {
                sysRole.setState(0);
                sysRoleService.saveRole(sysRole, menuId, toparr);
                content = ErrorCodeTemplate.ROLE_ADD;
            } else {
                sysRoleService.updateRole(sysRole, menuId, toparr);

                if (sysRole.getId() == 3) {  //超级管理员
                    content = ErrorCodeTemplate.ROLE_LEVEL_ADMINISTRATOR;
                } else if (sysRole.getId() == 58) { //客服
                    content = ErrorCodeTemplate.ROLE_CUSTOMER_SERVICE;
                } else if (sysRole.getId() == 48) {
                    content = ErrorCodeTemplate.ROLEIRST_LEVEL_ADMINISTRATOR;
                } else if (sysRole.getId() == 51) {
                    content = ErrorCodeTemplate.ROLE_SALES_MANAGER;
                } else if (sysRole.getId() == 52) {
                    content = ErrorCodeTemplate.ROLE_USER_ADMINISTRATOR;
                } else if (sysRole.getId() == 59) {
                    content = ErrorCodeTemplate.ROLE_SALESMAN;
                } else if (sysRole.getId() == 60) {
                    content = ErrorCodeTemplate.USER_LEVEL_AUSER;
                }
            }
            //保存操作日志
            CommonRoleServiceUtil.saveOperateLog(user, null, content, sysUserService);
            dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
        } catch (Exception e) {
            log.error(null, e);
            dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
            e.printStackTrace();
        }
        return dzdPageResponse;
    }

    /**
     * 根据编号查询
     *
     * @param request
     * @param model
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/formEdit")
    @ResponseBody
    public DzdResponse sysRoleEdit(HttpServletRequest request, Model model) {
        DzdResponse dzdPageResponse = new DzdResponse();
        try {
            Object id = request.getParameter("id");
            if (id == null) {
                dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
                return dzdPageResponse;
            }
            SysRole sysRole = (SysRole) sysRoleService.queryById(Integer.parseInt(id.toString()));
            DzdPageParam dzdPageParam = new DzdPageParam();
            Map<String, Object> condition = new HashMap<String, Object>();
            condition.put("sysRoleId", id);
            dzdPageParam.setCondition(condition);
            List<SysRoleRel> sysRoleRels = sysRoleRelService.queryMenuByRoleId(dzdPageParam);
            sysRole.setSysRoleRels(sysRoleRels);

            // 查找所有菜单用菜单按钮封装数据
            List<SysMenu> dataList = sysMenuService.queryRootSysMenuList();
            List<MenuTree> menuTreeList = null;
            if (!CollectionUtils.isEmpty(dataList)) {
                menuTreeList = new ArrayList<MenuTree>();
                // 第一级菜单
                for (SysMenu sysMenu : dataList) {
                    MenuTree menuTree = new MenuTree();
                    menuTree.setParentId("root");
                    menuTree.setId(sysMenu.getId());
                    menuTree.setText(sysMenu.getName());
                    menuTree.setType("menu");
                    menuTree.setTopsId("0");
                    menuTreeList.add(menuTree);

                    List<MenuTree> childMenuTreeList = new ArrayList<MenuTree>();
                    if (!CollectionUtils.isEmpty(sysMenu.getChild())) {
                        // 第三级菜单
                        for (SysMenu menu : sysMenu.getChild()) {
                            MenuTree childMenuTree = new MenuTree();
                            childMenuTree.setParentId(sysMenu.getId() + "");
                            childMenuTree.setId(menu.getId());
                            childMenuTree.setText(menu.getName());
                            childMenuTree.setType("menu");
                            childMenuTree.setTopsId("0");
                            childMenuTreeList.add(childMenuTree);
                            List<SysMenuBtn> menuBtns = sysMenuBtnService.queryByMenuid(menu.getId());
                            menu.setBtns(menuBtns);
                            List<MenuTree> btnList = new ArrayList<MenuTree>();
                            // 第三级按钮
                            for (SysMenuBtn menuBtn : menuBtns) {
                                MenuTree btnTree = new MenuTree();
                                btnTree.setParentId(menu.getId() + "");
                                btnTree.setId(menuBtn.getId());
                                btnTree.setText(menuBtn.getBtnName());
                                btnTree.setType("button");
                                btnTree.setTopsId("2");
                                btnList.add(btnTree);
                            }
                            childMenuTree.setNodes(btnList);
                        }
                    }
                    if (childMenuTreeList.size() > 0) {
                        menuTree.setNodes(childMenuTreeList);
                    }
                }
            }
            JspResponseBean jspResponseBean = new JspResponseBean();
            jspResponseBean.setData(sysRole);
            dzdPageResponse.setData(jspResponseBean);
            jspResponseBean.setDataList(menuTreeList);
            dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
        } catch (Exception e) {
            log.error(null, e);
            dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
            e.printStackTrace();
        }
        return dzdPageResponse;
    }

    /**
     * 删除
     *
     * @param request
     * @param model
     * @return
     */
    @MethodDescription("删除角色")
    @RequestMapping(value = "/from/delete", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DzdResponse delete(HttpServletRequest request, Model model) {
        DzdResponse response = new DzdResponse();
        try {
            String sysRoleId = request.getParameter("sysRoleId");
            sysRoleService.deleteRole(Integer.parseInt(sysRoleId));
            response.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
        } catch (Exception e) {
            response.setRetCode(ErrorCodeTemplate.CODE_FAIL);
            e.printStackTrace();
        }
        return response;
    }


    /**
     * 查询角色信息列表
     *
     * @param request
     * @param data
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/roleInfoList", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public DzdResponse roleInfoList(HttpServletRequest request, @RequestBody Map<String, Object> data)
            throws Exception {
        DzdResponse dzdPageResponse = new DzdResponse();
        try {
            DzdPageParam dzdPageParam = new DzdPageParam();
            JspResponseBean jspResponseBean = new JspResponseBean();
            Page<SysRole> dataList = sysRoleService.queryRoleList(dzdPageParam);
            if (!CollectionUtils.isEmpty(dataList)) {
                jspResponseBean.setDataList(dataList.getResult());
            }
            dzdPageResponse.setData(jspResponseBean);
            dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
        } catch (Exception e) {
            log.error(null, e);
            dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
            e.printStackTrace();
        }
        return dzdPageResponse;
    }

}
