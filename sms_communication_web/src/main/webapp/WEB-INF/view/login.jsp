<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>

<title>登录</title>

<link href="${ctx}/static/bower_components/bootstrap/dist/css/bootstrap.min.css" rel="stylesheet">

<link rel="stylesheet" type="text/css" href="${ctx}/static/dzd/themes/admin.css">
<!--[if IE]>
<link rel="stylesheet" type="text/css" href="${ctx}/static/dzd/themes/dzdie8.css"/>
<script src="${ctx}/static/js/app/html5shiv.js"></script>
<script src="${ctx}/static/js/app/selectivizr.js"></script>
<style>
	
	
</style>

<![endif]-->

    <!--[if lt IE 7]> <style>
    #loginUsers table tr td{
    	display:none;
    }
    .nouse-ti{
    	padding-top:85px;
    }
    .nouse-ti,.nouse{
    	display:block!important;
    }
    </style>
     <![endif]-->
    <!--[if IE 8]> <style>
    #loginUsers table tr td{
    	display:none;
    }
    .nouse-ti{
    	padding-top:85px;
    }
    .nouse-ti,.nouse{
    	display:block!important;
    }
    </style>
    <![endif]-->
	<!--[if IE 9]> <style>
    #loginUsers table tr td{
    	display:none;
    }
    .nouse-ti{
    	padding-top:85px;
    }
    .nouse-ti,.nouse{
    	display:block!important;
    }
    </style><![endif]-->
<style>
	#captcha:focus,#captcha{
	width:96px;
}
.nouse{
display:none;
}
</style>
</head>
	<body>
		<input type="hidden" value="${ctx}" id="server_path" />
		<input type="hidden" value="${ctx}" id="path" />

		</head>

		<body class="new_index_bg">
			<input type="hidden" value="${ctx}" id="server_path" />
		<input type="hidden" value="${ctx}" id="path" />

		</head>

		<body class="new_index_bg">
			<table height="100%" cellspacing="0" cellpadding="0" width="100%" border="0" style="position:absolute;">
				<tbody>
					<tr>
						<td align="middle">
							<table cellspacing="0" cellpadding="0" width="468" border="0">
								<tbody>
									<tr>
										<td>

										</td>
									</tr>
									<tr>
										<td>

										</td>
									</tr>
								</tbody>
							</table>
							<table cellspacing="0" cellpadding="0" width="445" height="450" border="0">
								<tbody>
									<tr>
										<td width="16">

										</td>
										<td align="middle">
											<form id="loginUsers" name="loginUsers" action="http://sms.dzd.com/loginUsers.action" method="post">
												<table cellspacing="0" cellpadding="0" width="440" height="363" id="logo-table" style="background-position:2px 0;"border="0">

													
														<tr height="40" align="center" >
															<td class="nouse-ti">
																	<span class="nouse">您使用的浏览器不支持全网信通！<br>
																		建议使用以下浏览器：<br>
																		IE9以上版本浏览器<br>
																		360浏览器（极速模式）<br>
																		火狐浏览器<br>
																		谷歌浏览器<br>
																		QQ浏览器</span>
															</td>
															<td style="vertical-align: middle;text-align: right;"><br>
																<a href="${ctx}/announcement.do#consult" target="_blank" class="awbtn" data-tar="zx" id="zxfk"><u>咨询与反馈</u></a>&nbsp;&nbsp;&nbsp;&nbsp;</td>
														</tr>
														<tr height="28" align="center">
															<td style="width:140px;">
																&nbsp;
															</td>
															<td align="right">
																<br>
																<!--<font style="font-size: 13px">全网短信发送平台</font> 
											-->
															</td>

														</tr>
														
														<tr height="36" align="left">
															<td style="text-align:right">
																	账户名 
															</td>
															<td style="padding-left:6px">								
																
																<input type="text" id="account" name="account" style="BORDER-RIGHT: #000000 1px solid; BORDER-TOP: #000000 1px solid; BORDER-LEFT: #000000 1px solid; BORDER-BOTTOM: #000000 1px solid; width:165px;">
															</td>
														</tr>
														<tr height="36" align="left">
															<td style="text-align:right;">
																密<span style="margin-left:12px">码</span>
															</td>
															<td style="padding-left:6px">
																
																<input type="password" name="password" id="password" style="BORDER-RIGHT: #000000 1px solid; BORDER-TOP: #000000 1px solid; BORDER-LEFT: #000000 1px solid; BORDER-BOTTOM: #000000 1px solid; width:165px;">
																
															</td>
														</tr>
														<tr height="36" align="left">
															<td style="text-align:right">
																验证码
															</td>
															<td style="padding-left:6px">
																
																<input type="text" id="captcha" name="captcha" size="11" maxlength="4" value="">

																<img id="captchaImage" src="captcha.do" style="vertical-align:top;height:24px;" title="点击图片刷新验证码">
															
																<a href="${ctx}/announcement.do#forgetpsw" target="_blank" class="awbtn psad" data-tar="mm"> <u>找回密码</u></a>
															</td>
														</tr>

														<tr height="36">
														<td></td>
															<td colspan="2" align="left" style="padding-left:6px">
																
																<input type="button" id="loginbtn" value="登  录" style="    height: 27px;background-size: 100% 100%; width:164px;"> &nbsp;&nbsp;&nbsp;
																<!--<input type="reset" value="&#37325;&#32622;" style="height:22px;width:70px"/>

												&nbsp;&nbsp;<a href="openApplyUsers">订户注册 </a>
											--></td>
														</tr>
														<tr height="36" align="left">
															<td style="text-align:right;">
																<input type="checkbox" style="top:0px" id="cbLisence" checked="">
															</td>
															<td style="text-align:left;padding-left:6px">																
																
																<a class="aQues" href="${ctx}/announcement.do#notice" target="_blank" data-tar="xy">同意并遵守全网信通平台使用规定</a>
															</td>
														</tr>
														<tr>
															<td colspan="2">
																&nbsp;
															</td>
														</tr>

													</tbody>
												</table>
											</form>

										</td>
										<td width="16">

										</td>
									</tr>
								</tbody>
							</table>
							<table cellspacing="0" cellpadding="0" width="468" border="0">
								<tbody>
									<tr>
										<td>

										</td>
									</tr>
								</tbody>
							</table>
						</td>
					</tr>
				</tbody>
			</table>

			<!--  -->

			

	
			<script src="${ctx}/static/js/commons/jquery-2.1.1.js"></script>
	<script src="${ctx}/static/js/commons/bootstrap.min.js"></script>
	<script src="${ctx}/static/js/commons/common.js"></script>
	<script src="${ctx}/static/js/checkinput.js"></script>
	
	<script type="text/javascript">
	  	$('#captchaImage').click(function() {
                 $('#captchaImage').attr("src", "captcha.do?timestamp=" + (new Date()).valueOf());
        }); 
	  	
	</script>
	<script type="text/javascript" src="${ctx}/static/js/login/login.js"></script>
	 <script type="text/javascript" src="${ctx}/static/dzd/dzdcom.js"></script>
		</body>

</html>