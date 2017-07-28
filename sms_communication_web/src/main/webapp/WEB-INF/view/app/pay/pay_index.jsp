<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,user-scalable=no">
    <title>网上充值</title>
    <link rel="stylesheet" type="text/css" href="${ctx}/static/dzd/themes/dzd.css">
    <style type="text/css">
        .inp-box > span {
            padding-left: 10px;
        }

    </style>
</head>
<body>
<input id="path" type="hidden" value="${ctx}">
<!-- 开通网上充值功能 -->
<c:if test="${isShow == 1}">
  <div style="text-align:center;font-size:12px">您的账户未开通网上充值。</div>
</c:if>


<!-- 开通网上充值功能 -->
<c:if test="${isShow == 0}">
    <form id="payForm" action="${ctx}/smsUserPay/pay.do" method="post">
        <c:if test="${session_user.superAdmin == 1 || roleId == 48}">
            <div style="text-align:center;">
                <div class="inp-box" style="">
                    账号：<input type="text" class="inp-box-inp" id="rechargeAccount"/>
                    &nbsp;
                    <input class="f-btn" type="button" value="确 定" onclick="btnChangeUser();"/>
                </div>
            </div>
        </c:if>

        <div class="inp-box">
            <span>充值账号：</span><span style="color:#fd0000;padding-left: 5px;" id="pay_account">${session_user.email}</span>
            <input id="input_account" name="account" type="hidden"/>
        </div>
        <br/>

        <div class="inp-box">
		<span id="sp_money_section">
			<span>短信价格：</span>
			<span id="oneStart"></span><span>—</span><span id="oneEnd"></span><span>条 单价 </span><span style="color:#fd0000" id="ontPrice"></span><span> 元 </span>；
			<span id="twoStart"></span><span>—</span><span id="twoEnd"></span><span>条 单价 </span><span style="color:#fd0000" id="twoPrice"></span><span> 元 </span>；
			<span id="threeStart"></span><span>条及以上 单价 </span><span style="color:#fd0000" id="threePrice"></span><span> 元 </span>
		</span>
		<span id="sp_money_none" style="display: none;">
			无效账户
		</span>


        </div>
        <br/>
        <div class="inp-box" id="smsNumber_div">
            <span>充值数量：</span>
            <select id="recharge_users" name="smsNumber">
                <option value="0">—请选择—</option>
                <option value="10000">10000</option>
                <option value="30000">30000</option>
                <option value="60000">60000</option>
                <option value="100000">100000</option>
                <option value="150000">150000</option>
                <option value="200000">200000</option>
                <option value="300000">300000</option>
                <option value="500000">500000</option>
                <option value="1000000">1000000</option>
            </select><span style="padding:5px">条</span>
            <span>金额：</span>
            <input type="text" name="money" id="money" value="0" readonly="readonly" style="width:50px;border:none;">元

        </div>
        <br/>
        <div class="inp-box">
            <span style="display:inline-block">充值方式：快钱（网银支付）</span>

            <ul class="bank-box bank_option">
                <c:forEach var="bank" items="${bankArray}">
                    <li>
                        <input id="check_bankId" type="radio" name="bankId" value='${bank.code}'>
                        <label><img src="${ctx}/static/dzd/img/${bank.icon}"/></label>
                    </li>
                </c:forEach>

            </ul>

        </div>
        <br/>

        <p style="text-align: center;"><input type="button" class="f-btn" style="width:68px;font-size:12px;"
                                              onclick="saveOrder()" value="提交充值"/></p>
    </form>
    <ul class="hint-box">
        <li class="account-title">网上充值提示：</li>
        <br/>

        <li>1、网上充值付款成功后，短信数量由系统自动充值即时到账。</li>
        <li>2、网上充值积分政策：每1万条短信积10分，1分可兑换1条短信，积分当年累计有效。不同账户间积分不能合计、转让。</li>
        <li>3、短信账户绑定的手机用户需于次年1月10日前在网上提交积分兑换申请，逾期未申请兑换的积分作废。</li>
        <li>4、积分100分起兑换，兑换的短信数量于次年1月15日前充值到账。</li>


    </ul>
     <br/>
    <ul class="account-box">
        <li class="account-title">人工充值收款账户：</li>
        <br/>
        <li>
            <ul class="public-account-box pd0">
                <li>对公账户</li>
                <li>账 号：<span>1021 0151 2010 0052 70</span></li>
                <li>户 名：<span>深圳市千讯数据股份有限公司</span></li>
                <li>开户行：<span>广发银行深圳龙华支行</span></li>
            </ul>
            <ul class=" bank-account-box">
                <li>银行账户</li>
                <li>账 号：<span>6230 5820 0003 4691 488</span></li>
                <li>户 名：<span>苏艳艳</span></li>
                <li>开户行：<span>平安银行深圳分行</span></li>
            </ul>
            <ul class=" zfb-account-box">
                <li>支付宝账户</li>
                <li>账 号：<span>zhifubao@dzd.com </span></li>
                <li>户 名：<span>苏艳艳</span></li>
                <li>&nbsp;</li>
            </ul>

        </li>
    </ul>
     <br/>
    <ul class="hint-box">
        <li class="account-title">人工充值提示：</li>
        <br/>

        <li>1、转账付款充值以全网信通收款账户查收到账为准；</li>
        <li>2、转账付款时请备注短信账户名称，以便准确查账、及时充值；</li>
        <li>3、人工充值时间：工作日早9点至下午18点。人工充值需操作流程时间，如急需使用短信，请使用网上自助充值（快捷、安全）。</li>
        <li>4、人工充值无积分政策。</li>


    </ul>
</c:if>

<div class="modal"></div>



<script src="${ctx}/static/js/commons/jquery-2.1.1.js"></script>
<script src="${ctx}/static/bower_components/bootstrap/dist/js/bootstrap.js"></script>
<script src="${ctx}/static/js/app/pay/pay.js"></script>
<script src="${ctx}/static/dzd/turnpage.js"></script>
</body>
</html>
