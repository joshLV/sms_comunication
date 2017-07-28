package com.dzd.phonebook.controller;

import com.dzd.base.util.SessionUtils;
import com.dzd.phonebook.entity.SmsHomePage;
import com.dzd.phonebook.entity.SysUser;
import com.dzd.phonebook.service.CommonRoleServiceUtil;
import com.dzd.phonebook.service.SmsHomePageService;
import com.dzd.phonebook.service.SysUserService;
import com.dzd.phonebook.util.DzdResponse;
import com.dzd.phonebook.util.ErrorCodeTemplate;
import com.dzd.phonebook.util.FileUploadUtil;
import com.dzd.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 首页编辑CONTROLLER
 * Created by CHENCHAO on 2017/5/31.
 */
@Controller
@RequestMapping("/smsHomePage")
public class HomePageController {
    public static final LogUtil log = LogUtil.getLogger(HomePageController.class);
    private static final String IMG_PATH = "/img/homePageImg/";
    private static final String DEFAULT_SUB_FOLDER_FORMAT_AUTO = "yyyyMMddHHmmss";

    @Autowired
    private SmsHomePageService smsHomePageService;

    @Autowired
    private SysUserService sysUserService;

    @RequestMapping("/index")
    public String index() {
        return "/homePage/index";
    }

    @RequestMapping("/homePage")
    public String list() {
        return "/homePage/home_page";
    }


    /**
     * 查询首页样式
     *
     * @param request
     * @return
     */
    @RequestMapping("/homePageList")
    @ResponseBody
    public DzdResponse queryHomePageList(HttpServletRequest request) {
        DzdResponse dzdResponse = new DzdResponse();
        try {
            SmsHomePage smsHomePage = smsHomePageService.querySmsHome();
            dzdResponse.setData(smsHomePage);
            dzdResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
        } catch (Exception e) {
            e.printStackTrace();
            dzdResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
            dzdResponse.setRetMsg("系统异常!");
        }
        return dzdResponse;
    }


    /**
     * 新增、修改
     *
     * @param request
     * @param data
     * @return
     */
    @RequestMapping(value = "/from/merge", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public DzdResponse merge(HttpServletRequest request, @RequestBody Map<String, Object> data) {
        DzdResponse dzdPageResponse = new DzdResponse();
        String contents;
        try {
            Object content = data.get("content");
            SmsHomePage smsHomePage = smsHomePageService.querySmsHome();
            SmsHomePage homePage;
            if (smsHomePage == null) {// 新增
                homePage = new SmsHomePage();
                homePage.setContent(content.toString());
                smsHomePageService.add(homePage);
                contents=ErrorCodeTemplate.HOME_PAGE_ADD;
            } else {// 修改
                homePage = smsHomePage;
                homePage.setContent(content.toString());
                smsHomePageService.update(homePage);
                contents=ErrorCodeTemplate.HOME_PAGE_REGIST;
            }
            //保存操作日志
            SysUser user= SessionUtils.getUser(request);
            CommonRoleServiceUtil.saveOperateLog(user, null, contents, sysUserService);
            dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_SUCESS);
            dzdPageResponse.setData(homePage);
        } catch (Exception e) {
            log.error(null, e);
            dzdPageResponse.setRetCode(ErrorCodeTemplate.CODE_FAIL);
            e.printStackTrace();
        }
        return dzdPageResponse;
    }


    /**
     * 上传图片
     *
     * @param file
     * @param request
     * @param response
     */
    @RequestMapping(value = "/uploadImg")
    public void uplodaImg(@RequestParam("upload") MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        try {

            String domain = getRequestDomain(request);
            // 1. 获取tomcat路径
            String proPath = request.getSession().getServletContext().getRealPath("/");
            String path = proPath +"../../"+ IMG_PATH;
            FileUploadUtil.createDir(path);//  不存在则创建文件夹
            System.out.println("上传图片地址:" + path);


            PrintWriter out = response.getWriter();
            String CKEditorFuncNum = request.getParameter("CKEditorFuncNum");
            String fileName = file.getOriginalFilename();
            String uploadContentType = file.getContentType();
            String expandedName = "";
            if (uploadContentType.equals("image/pjpeg") || uploadContentType.equals("image/jpeg")) {
                // IE6上传jpg图片的headimageContentType是image/pjpeg，而IE9以及火狐上传的jpg图片是image/jpeg
                expandedName = ".jpg";
            } else if (uploadContentType.equals("image/png") || uploadContentType.equals("image/x-png")) {
                // IE6上传的png图片的headimageContentType是"image/x-png"
                expandedName = ".png";
            } else if (uploadContentType.equals("image/gif")) {
                expandedName = ".gif";
            } else if (uploadContentType.equals("image/bmp")) {
                expandedName = ".bmp";
            } else {
                out.println("<script type=\"text/javascript\">");
                out.println("window.parent.CKEDITOR.tools.callFunction(" + CKEditorFuncNum + ",'',"
                        + "'文件格式不正确（必须为.jpg/.gif/.bmp/.png文件）');");
                out.println("</script>");
                return;
            }
            if (file.getSize() > 1024 * 1024 * 2) {
                out.println("<script type=\"text/javascript\">");
                out.println("window.parent.CKEDITOR.tools.callFunction(" + CKEditorFuncNum + ",''," + "'文件大小不得大于2M');");
                out.println("</script>");
                return;
            }
            System.out.println("保存图片的地址：" + path + "/" + fileName);

            DateFormat df = new SimpleDateFormat(DEFAULT_SUB_FOLDER_FORMAT_AUTO);
            fileName = df.format(new Date()) + expandedName;
            file.transferTo(new File(path + "/" + fileName));
            // 返回"图像"选项卡并显示图片 request.getContextPath()为web项目名
            String returnUrl = domain + "/img_path/homePageImg/"  + fileName;
            if(returnUrl.indexOf("http://")!=-1){
            }else{
                returnUrl = "http://" + returnUrl;
            }

            out.println("<script type=\"text/javascript\">");
            out.println("window.parent.CKEDITOR.tools.callFunction(" + CKEditorFuncNum + ",'" + returnUrl + "','')");

            out.println("</script>");
            return;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取请求的域名
     *
     * @param request
     * @return
     */
    private String getRequestDomain(HttpServletRequest request) {
        try {
            StringBuffer reqUrl = request.getRequestURL();
            URL url = new URL(reqUrl.toString());
            String domain = url.getHost();
            return domain;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
