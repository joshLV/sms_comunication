<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html>
<head>

    <link href="${ctx}/static/css/bootstrap-table.css" rel="stylesheet">
    
    <link href="${ctx}/static/bower_components/bootstrap/dist/css/bootstrap.min.css" rel="stylesheet">
	
    <link href="${ctx}/static/bower_components/bootstrap/dist/css/jquery.bootstrap.css" rel="stylesheet">
     <link rel="stylesheet" type="text/css" href="${ctx}/static/dzd/laydate/need/laydate.css"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/static/dzd/themes/dzd.css">
	<style>
		 #add_button {
          border: none;
    border-radius: 4px;
    position: relative;
    top: -2px;
    background: #4284DA;
    color: #FFFFFF;
    width: 62px;
    height: 22px;
    font-size: 12px;
    line-height: 20px;
    padding: 0 8px;
    text-align: center;
    }
	</style>
</head>
<body>
<!-- 开通网上充值功能 -->
<c:if test="${isShow == 1}">
    <div style="text-align:center;font-size:12px">短信回复未开通</div>
</c:if>

<!-- 短信回复未开通 -->
<c:if test="${isShow == 0}">
<div>
    <input id="superAdmin" type="hidden" value="${session_user.superAdmin}">
    <div class="title-remark"></div>
    <div class="com-content">
        <div class="com-menu inp-menu" style="text-align:center;">

            <a href="#" id="add_sysuser"></a>

            <div class="inp-box ">

                <span class="inp-title ">账号：</span>
                <input class="inp-box-inp" id="email_input" type="text"/>

                <span style="margin-left: 10px" class="inp-title">手机号码：</span>
                <input class="inp-box-inp" id="phone_input" type="text"/>



                日期：<input id="start_input" type="text" class="inp-box-inp" /> 至  <input id="end_input" type="text" class="inp-box-inp" />



                <span id="aisleSpan" style="display: none;">
                    通道名称:
                    <select id="aisleNameSelect">
                        <option value="">全部</option>
                        <c:forEach var="s" items="${smsAisles}">
                            <option value="${s.id}">${s.name}</option>
                        </c:forEach>
                    </select>
                </span>

               <%-- <button class="btn btn-info" type="button" id="search_btn">
                        <span class="glyphicon glyphicon-search"></span>
                </button>
                </br>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                账号：<input id="email_input" type="text" />
                回复内容<input id="content_input" type="text"/> --%>

            </div>
            <div class="inp-box ">
                <input class="f-btn" value="查询" type="button" id="search_btn" style="margin-left: 10px"/>
            </div>


        </div>
    </div>
    <input type="hidden" id="menu_id" value="${menuId}"/>
    <div class="row">
        <table id="tb_data"></table>
        <div class="minlid">
				<a id="firstpage"  href="javaScript:void(0)">首页</a><a href="javaScript:void(0)" id="lastpage">尾页</a><input class="page-inp" id="pagenum" type="text"/><a href="javaScript:void(0)" id="turnpage">跳转</a>
			</div>
    </div>
</div>


<div class="modal fade"  id="del">
    <div class="modal-dialog awl">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"
                        aria-label="Close">
                    <span aria-hidden="true"></span>
                </button>
                <h4 class="modal-title" id="del_h4_title">确定删除</h4>
            </div>
            <p class="aw-newcontent">
                <span class="aw-new">确认是否删除？</span>
            </p>

            <div class="modal-footer">
                <input type="button" class="f-btn" id="btn_del" value="确  定"/>
                <input type="button" class="f-btn" data-dismiss="modal" value="取  消">

            </div>
        </div>
    </div>
</div>


<div class="modal fade" id="add_sysuser_div">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"
                        aria-label="Close">
                    <span aria-hidden="true"></span>
                </button>
                <h4 class="modal-title" id="h4_title">新增黑名单</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal">
                    <input type="hidden" id="id"/>
                    <div class="form-group">
                        <label for="phone" class="col-sm-2 control-label">黑名单手机号</label>
                        <div class="col-sm-5">
                            <input type="text" class="form-control" id="phone">
                        </div>
                    </div>
                    <div class="form-group" id="aisleId_div">
                        <label for="aisleId" class="col-sm-2 control-label">通道id</label>
                        <div class="col-sm-5">
                            <input type="text" class="form-control" id="aisleId">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="aisleName" class="col-sm-2 control-label">通道名称</label>
                        <div class="col-sm-5">
                            <input type="text" class="form-control" id="aisleName">
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
             <input type="button" class="f-btn" id="save_sysuser_btn" value="确  定"/>
                <input type="button" class="f-btn" data-dismiss="modal" value="取  消">            
            </div>
        </div>
    </div>
</div>

<!-- Custom Theme JavaScript -->
<input type="hidden" id="server_path" value="${ctx}"/>
<script src="${ctx}/static/js/commons/jquery-2.1.1.js"></script>
<script src="${ctx}/static/js/commons/common.js"></script>
<script src="${ctx}/static/js/bootstrap-table.js"></script>
<script src="${ctx}/static/js/bootstrap-table-zh-CN.min.js"></script>
 <script src="${ctx}/static/dzd/laydate/laydate.js" type="text/javascript" charset="utf-8"></script>
<script src="${ctx}/static/bower_components/bootstrap/dist/js/bootstrap.js"></script>
<script src="${ctx}/static/js/smsManage/smsReceiveReplyPush.js"></script>



<script type="text/javascript" src="${ctx}/static/dzd/turnpage.js"></script>
</c:if>
</body>
</html>