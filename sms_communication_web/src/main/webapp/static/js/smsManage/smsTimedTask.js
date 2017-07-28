// var ChineseDistricts_city = {};
var path = $("#path").val();
var quuids = "";
$(function () {
    refreshAisleGroup(path);// 刷新通道信息
    var startday = {//历史
        elem: '#start_input',
        format: 'YYYY-MM-DD',
        min: laydate.now(), //设定最大日期为当前日期
        istime: false,
        istoday: true,
        issure: false,
        choose: function (datas) {
            endtday.min = datas; //开始日选好后，重置结束日的最小日期
            endtday.start = datas //将结束日的初始值设定为开始日
        }
    };
    var endtday = {
        elem: '#end_input',
        format: 'YYYY-MM-DD',
        issure: false,
        min: laydate.now(),
        istime: false,
        istoday: true,
        choose: function (datas) {

            startday.max = datas //将结束日的值设定为开始日的最大值
        }
    }
    laydate(endtday);
    var timed = {
        elem: '#time_input',
        format: 'YYYY-MM-DD hh:mm',
        min: laydate.now(), //设定最小日期为当前日期
        istime: true,
        istoday: true,
        up: true,
        notime: true,
        dayon:true
    };
    laydate.skin('yahui');
    laydate(timed);
    laydate(startday);

    load_data();
    $('#tb_data').bootstrapTable('hideColumn', 'aisleName');
    $('#tb_data').bootstrapTable('hideColumn', 'operation');
    $('#tb_data').bootstrapTable('hideColumn', 'select');


    $("#search_btn").unbind("click");
    $("#search_btn").bind("click", function () {
        refresh_data();
        $('#tb_data').bootstrapTable('hideColumn', 'aisleName');
        $('#tb_data').bootstrapTable('hideColumn', 'operation');
        $('#tb_data').bootstrapTable('hideColumn', 'select');
    });

    $("#reset").unbind("click");
    $("#reset").bind("click", function () {
        // $('#phone_input').val("");
        $('#start_input').val("");
        $('#end_input').val("");
    });

    var $table_stop = $('#tb_data'), $change_stop = $('#stop_btn');
    $change_stop.click(function () {
        typeChange($table_stop, 2);
    });

    var $table_start = $('#tb_data'), $change_start = $('#start_btn');
    $change_start.click(function () {
        typeChange($table_start, 1);
    });

    var $table_delete = $('#tb_data'), $delete = $('#delete_btn');
    $delete.click(function () {
        deleteTimeTask($table_delete);
    });


    var $table_export = $('#tb_data'), $export = $('#export_btn');
    $export.click(function () {
        orderExport($table_export);
    });

});
function saveBtn() {
    var checkTimeing = 10 * 60 * 1000;
    if (Date.parse(new Date($("#time_input").val())) < Date.parse(new Date())) {
        alert("您预定的时间无效.");
        return;
    } else if ((Date.parse(new Date($("#time_input").val())) - Date.parse(new Date())) < checkTimeing) {
        alert("预定发送时间需设置为10分钟以后!\n" + "请重新设置发送时间.");
        return;
    }
    save();
};
function rebind() {

    if( $("#save_btn").attr("clicknum") == "1"){
        return;
    }else{
        $("#save_btn").attr("clicknum","1");
    }
    $("#save_btn").bind("click", function () {
        $(this).attr("disabled", "disabled");
        Button_Click(saveBtn, 200);
    });//验证按钮防止重复点击


    var i = 0;  //判断点击次数寄存
    var closetimer = null;  //延时函数寄存
    function Button_Click(fn, time)//botton点击事件
    {
        i++;  //记录点击次数
        var action = fn;
        closetimer = window.setTimeout(function () {
            setout(action)
        }, time); //后执行事件 	提交
    }

    function setout(fn) {  //点击执行事件
        if (i > 1)   //如果点击次数超过1
        {
            alert("请勿频繁点击按钮!");
            window.clearTimeout(closetimer);  //清除延时函数
            closetimer = null;  //设置延时寄存为null
            i = 0;  //重置点击次数为0
        } else if (i == 1) {  //如果点击次数为1
            fn();// 咨询提交
            i = 0;  //重置点击次数为0
            //添加执行操作的代码
        }
    }
}
function orderExport($table) {

    var taskIds = $.map($table.bootstrapTable('getSelections'), function (row) {
        if (row.superAdmin == 1) {
            return null;
        }
        return row.id;
    });
    if (taskIds == "") {
        alert("请选择定时任务！");
        return;
    }

    if (taskIds == null) {
        taskIds = new Array();
    }

    var exportDate = {
        'taskIds': taskIds.join(","),
    };

    $.post(
        "querytimedtaskfororder.do",
        exportDate,
        function (data) {
            if (data["retCode"] == "000000") {
                window.location.href = "orderExport.do?email=" + $("#smsUserEmail").val();
            } else {
                alert("没有可以导出数据");
            }

        });
}

function deleteTimeTask($table) {

    var taskIds = $.map($table.bootstrapTable('getSelections'), function (row) {
        if (row.superAdmin == 1) {
            return null;
        }
        return row.id;
    });
    if (taskIds == "") {
        alert("请选择定时任务！");
        return;
    }

    if (taskIds == null) {
        taskIds = new Array();
    }

    if (window.confirm("您确定删除选择的定时任务吗？")) {

    } else {
        return;
    }
    $('#delete_btn').attr("disabled", "disabled");
    var deleteDate = {
        'taskIds': taskIds.join(","),
    };

    $.post("deletetimedtask.do", deleteDate, function (data) {
        refresh_data();
        $('#tb_data').bootstrapTable('hideColumn', 'aisleName');
        $('#tb_data').bootstrapTable('hideColumn', 'operation');
        $('#tb_data').bootstrapTable('hideColumn', 'select');

    });


}

function save() {
    var content = $("#content").val();// 短信内容
    content = $.trim(content);// 去除两端空格
    content = content.replace(/(^\n*)|(\n*$)/g, "");//去掉两端换行
    var signature = "";// 签名
    if ($("#signatureType").val() == 0) {
        signature = $("#signature_select").val();
    } else if ($("#signatureType").val() == 1) {
        signature = "【" + $("#input_sign").val() + "】";
    }
    if (signature == "" || signature == null) {
        alert("请填写短信签名!");
        $("#save_btn").removeAttr("disabled");
        return;
    }
    if (content == '' || content == null) {
        alert("请填写短信内容!");
        $("#save_btn").removeAttr("disabled");
        return;
    }
    content = signature + content;
    if ($("#time_input").val() == "") {
        alert("请填写发送时间!");
        $("#save_btn").removeAttr("disabled");
        return;
    }
    var taskId = $("#taskId").val();
    var timing = $("#time_input").val();
    var billingNnum = Number($(".smslength").text()) * Number($("#sendNum").val());
    var detailBillingNum = $(".smslength").text();

    var canCommit = true;
    if (!timing) {
        canCommit = false;
    }
    if (!content) {
        canCommit = false;
    }
    if (!signature) {
        canCommit = false;
    }
    if (!canCommit) {
        alert("修改信息不允许为空，请检查");
        $("#save_btn").removeAttr("disabled");
        return;
    }
    if ($("#canCommit").val() == 1) {
        alert("您的短信内容包含敏感词，请重新输入!");
        $("#save_btn").removeAttr("disabled");
        return;
    }

    new_updateTimeTask(taskId, null, null, null, content, timing, signature, billingNnum,detailBillingNum);
    $("#save_btn").removeAttr("disabled");
}

function typeChange($table, sendType) {

    var taskIds = $.map($table.bootstrapTable('getSelections'), function (row) {
        if (row.superAdmin == 1) {
            return null;
        }
        return row.id;
    });
    var sendTypes = $.map($table.bootstrapTable('getSelections'), function (row) {
        if (row.superAdmin == 1) {
            return null;
        }
        if (sendType == row.sendType && sendType == 1) {
            return "start";
        }
        if (sendType == row.sendType && sendType == 2) {
            return "stop";
        }
        if (row.sendType == 1) {
            return 2;
        }
        if (row.sendType == 2) {
            return 1;
        }
        return null;
    });

    for (var i = 0; i < sendTypes.length; i++) {
        if (sendTypes[i] == "start") {
            alert("您选择修改的定时任务包含已启动部分，请重新选择！");
            return;
        }
        if (sendTypes[i] == "stop") {
            alert("您选择修改的定时任务包含已停止部分，请重新选择！");
            return;
        }
    }

    if (taskIds == "") {
        alert("请选择定时任务！");
        return;
    }

    updatetimedtask(null, null, taskIds, sendTypes, null, null, null, null);

}
/**
 * 弹框载入设置短信条数、字数、剩余字数
 */
function initSelectSmsNumber(con, num) {

    var content = con;
    var signLength = num.length;
    var allLength = signLength + con.length;
    getSmsNumber(allLength, content, signLength);// 短信字数
    getContentLength(allLength);// 短信条数
    getSmsSurplusNumber(allLength);// 剩余输入字数

}

function editData(id) {
    $("#signature_select").html("");//清空签名
    $.ajax({
        type: "get",
        url: "querytimedtaskformodify.do?taskId=" + id,
        success: function (data) {
            $("#input_sign").val("");
            $("#taskId").val(data.id);
            $("#email").text(data.smsUserEmail);
            var content = data.content;
            $("#content").val(content);
            var dataSignature = data.signatureAttr;//定时任务ID对应签名无【】
            var nowSignature = data.signature;//定时任务签名【】

            $("#signatureType").val(data.signatureType);
            $("#i_length").text(data.content.length);
            $("#i_num").text(data.billingNnum);
            $("#sendNum").val(data.sendNum);
	    $("#befroBillingNum").val(data.billingNnum);
            $("#time_input").val(new Date(data.timing).Format("yyyy-MM-dd hh:mm"));
            initSelectSmsNumber(content, nowSignature);//弹框载入设置短信条数、字数、剩余字数
            if (data.signatureType == 0) {//签名类型为绑定
                if (dataSignature != null) {                   
                    var htl = "";
                    for (var i = 0; i < dataSignature.length; i++) {
                        var ss = dataSignature[i];
                        var nss = nowSignature.substring(1, Number(nowSignature.length) - 1);
                        if (ss == nss) {
                            htl += "<option selected=\"selected\"  value=\"【" + ss + "】\">【" + ss + "】</option>";
                        } else {
                            htl += "<option  value=\"【" + ss + "】\">【" + ss + "】</option>";
                        }
                    }

                    $("#signature_select").append(htl);
                }
                $("#signature_select").show();
                $(".pho-s-inp-box").hide();
            } else if (data.signatureType == 1) {
                $("#input_sign").val(nowSignature.substring(1, Number(nowSignature.length) - 1));
                $("#signature_select").hide();
                $(".pho-s-inp-box").show();
            }
            sensitiveWordsAndcalcLangth();//内容输入框事件绑定
            rebind();//保存按钮绑定；
        },
        complete: function () {
            $('#modify_div').modal("show");
        }

    });

}
$('#modify_div').on('hidden.bs.modal', function (e) {
    $("#signature_select,.pho-s-inp-box").hide();

})

function sensitiveWordsAndcalcLangth() {//输入字数判断
    var content = $("#content").val();
    // 输入短信内容
    $("#content").on("input", function () {
        var content = $("#content").val();
        content = $.trim(content);// 去除两端空格
        content = content.replace(/(^\n*)|(\n*$)/g, "");//去掉两端换行
        var select_sign = $("#signature_select").val();// 下拉框签名           
        var input_sign = $("#input_sign").val();// 输入签名
        var signLength = 0;// 签名长度
        if ($("#signatureType").val() == 0) {
            signLength = select_sign.length;
        } else if ($("#signatureType").val() == 1) {
            signLength = input_sign.length+2;
        }
        var allLength = content.length + signLength;
       
        allLength = getSmsNumber(allLength, content, signLength)// 短信去除换行之后的字数;
        getContentLength(allLength);// 短信条数
        getSmsSurplusNumber(allLength);// 剩余输入字数
    });
    $("#input_sign").on("input", function () {//输入签名
        var input_sign = $("#input_sign").val();
        var content = $("#content").val();
        var signLength = input_sign.length+2;
        var contentLength = content.length;
        var allLength = contentLength + signLength;
        allLength = getSmsNumber(allLength, content, signLength);// 短信字数
        getContentLength(allLength);// 短信条数
        getSmsSurplusNumber(allLength);// 剩余输入字数
    });
    if ($("#signature_select option").length == 1) {
        $("#signature_select").focus(function () {//下拉款赋值
            var content = $("#content").val();
            // 只替换签名
            var signLength = $(this).val().length;
            var length = $("#content").val().length;
            var allLength = length + signLength;
            getSmsNumber(allLength, content, signLength);// 短信字数
            getContentLength(allLength);// 短信条数
            getSmsSurplusNumber(allLength);// 剩余输入字数
        });
    } else if ($("#signature_select option").length > 1) {
        $("#signature_select").change(function () {//下拉款赋值
            var content = $("#content").val();
            // 只替换签名
            var signLength = $(this).val().length;
            var length = $("#content").val().length;
            var allLength = length + signLength;
            getSmsNumber(allLength, content, signLength);// 短信字数
            getContentLength(allLength);// 短信条数
            getSmsSurplusNumber(allLength);// 剩余输入字数
        });
    }


}


/**
 * 获取短信字数
 * @param allLength
 * @param content
 * @param signLength
 */
function getSmsNumber(allLength, content, signLength) {
    // 内容长度超过300，则截取前300位
    if (allLength > 300) {
    	alert("短信内容字数不能超过300字！")
        content = content.substring(0, 300 - signLength);
        content = content.replace(/(^\n*)|(\n*$)/g, "");//去掉两端换行
        $("#content").val(content);

    }
    allLength = content.length + signLength;
    $(".dxlength").text(allLength);// 短信字数
    return allLength
}

/**
 * 获取短信条数
 * @param allLength
 */
function getContentLength(allLength) {
    // 短信条数
    var sigleSmsLength = 1;
    if (allLength > 70) {
        var smsTextLen = allLength - 70;
        sigleSmsLength = parseInt((smsTextLen / 66) + 1);
        if (smsTextLen % 66 > 0) {
            sigleSmsLength += 1;
        }
    } else {
        sigleSmsLength = 1;
    }
    $(".smslength").html(sigleSmsLength);
}

/**
 * 剩余输入字数
 * @param allLength
 */
function getSmsSurplusNumber(allLength) {
    var surplusNum;
    if (allLength > 300) {
        surplusNum = 0;
    } else {
        surplusNum = 300 - allLength;
    }
    $(".s-dxlength").html(surplusNum);
}


// 刷新数据
function refresh_data() {
    $('#tb_data').bootstrapTable('hideColumn', 'aisleName');
    $('#tb_data').bootstrapTable('hideColumn', 'operation');
    $('#tb_data').bootstrapTable('hideColumn', 'select');

    var start_input = $('#start_input').val();
    var end_input = $('#end_input').val();
    //开始时间不为空
    if (start_input != "" && end_input == "") {
        if (!end_input) {
            alert("请选择结束时间！");
            return;
        }
    }
    $('#tb_data').bootstrapTable('destroy');
    load_data();
}

function queryParamsVal(params) {  //配置参数
    var temp = {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
        pagesize: params.limit,   //页面大小
        pagenum: (params.offset / params.limit) + 1,  //页码
        email: $("#user_input").val(),
        sendType: $("#sendType_select").val(),
        aisleName: $("#aisle_select").val(),
        startInput: $("#start_input").val(),//开始时间
        endInput: $("#end_input").val()//结束时间
    };
    return temp;
}


function new_updateTimeTask(taskId, sendType, taskIds, sendTypes, content, timing, signature, billingNnum,detailBillingNum){
    if (taskIds == null || sendTypes == null) {
        taskIds = new Array();
        sendTypes = new Array();
    }
    var updateDate = {
        'taskId': taskId,
        'sendType': sendType,
        'taskIds': taskIds.join(","),
        'sendTypes': sendTypes.join(","),
        'content': content,
        'timing': timing,
        "signature": signature,
        "billingNnum": billingNnum,
        "detailBillingNum":detailBillingNum,
        "befroBillingNum" : $("#befroBillingNum").val()
    };

    $.post("updatetimedtask.do", updateDate, function (data) {
        if (!!content && !!data["msg"]) {
            alert(data["msg"]);
            return false;
        }
        $('#modify_div').modal("hide")

        if (sendType == 2 || (!!sendTypes && sendTypes[0] == 2)) {
            alert("您选择的定时任务已停止！");
        } else if (sendType == 1 || (!!sendTypes && sendTypes[0] == 1)) {
            alert("您选择的定时任务已启动！");
        }

        refresh_data();
        $('#tb_data').bootstrapTable('hideColumn', 'aisleName');
        $('#tb_data').bootstrapTable('hideColumn', 'operation');
        $('#tb_data').bootstrapTable('hideColumn', 'select');
        console.log("数据更新成功");
    });
}




function updatetimedtask(taskId, sendType, taskIds, sendTypes, content, timing, signature, billingNnum) {

	if (sendType == 1 && timing<new Date().Format("yyyy-MM-dd hh:mm")) {
		alert("此定时任务已过期，如需发送请操作\"修改\"!");
    	return;
    }

    if (taskIds == null || sendTypes == null) {
        taskIds = new Array();
        sendTypes = new Array();
    }

    var updateDate = {
        'taskId': taskId,
        'sendType': sendType,
        'taskIds': taskIds.join(","),
        'sendTypes': sendTypes.join(","),
        'content': content,
        'timing': timing,
        "signature": signature,
        "billingNnum": billingNnum,
        "befroBillingNum" : $("#befroBillingNum").val()
    };

    $.post("updatetimedtask.do", updateDate, function (data) {
    	 if (!!content && !!data["msg"]) {
             alert(data["msg"]);
             return false;
         }
        $('#modify_div').modal("hide")

        if (sendType == 2 || (!!sendTypes && sendTypes[0] == 2)) {
            alert("您选择的定时任务已停止！");
        } else if (sendType == 1 || (!!sendTypes && sendTypes[0] == 1)) {
            alert("您选择的定时任务已启动！");
        }

       

        refresh_data();
        $('#tb_data').bootstrapTable('hideColumn', 'aisleName');
        $('#tb_data').bootstrapTable('hideColumn', 'operation');
        $('#tb_data').bootstrapTable('hideColumn', 'select');
        console.log("数据更新成功");
    });

}


function load_data() {

    var sendType = $("#sendType_select").val();
    $('#tb_data').bootstrapTable({
        url: 'querytimedtask.do?menuId=' + $("#menu_id").val(),
        dataType: "json",
        cache: false,
        striped: true,
        pageSize: 50,
        pageList: [50],
        pagination: true,
        paginationPreText: "上一页",
        paginationNextText: "下一页",
        paginationHAlign: "left",
        formatShowingRows: function (pageFrom, pageTo, totalRows) {
            if (totalRows > 50) {
                $(".minlid").show();
            } else {
                $(".minlid").hide();

            }
            return '总共 ' + totalRows + ' 条记录';
        },
        ccext: false,// 自定义样式,为true 跨行显示短信内容
        lines: 13, //跨多少行
        clickToSelect: true,
        sidePagination: "server", //服务端处理分页
        method: "post",
        queryParams: queryParamsVal, //参数
        columns: [
            {
                title: "全选",
                field: "select",
                align: "center",
                valign: "middle",
                width: 30,
                checkbox: sendType == "" ? true : false
            },
            {
                title: "账号",
                field: "smsUserEmail",
                align: "center",
                width: 100,
                valign: "middle",
                formatter: function (value, row, index) {
                    $("#smsUserEmail").val(row.smsUserEmail);
                    return row.smsUserEmail;
                }
            },
            {
                title: "短信数量",
                field: "billingNnum",
                align: "center",
                valign: "middle",
                width: 80
            },
            {
                title: "预定发送时间",
                field: "timing",
                align: "center",
                valign: "middle",
                width: 115,
                formatter: function (value, row, index) {
                    if (!row.timing) {
                        return "";
                    }
                    return new Date(row.timing).Format("yyyy-MM-dd hh:mm");
                }
            },
            {
                title: "短信内容",
                field: "content",
                align: "left",
                valign: "middle"

            },
            {
                title: "状态",
                field: "sendType",
                align: "center",
                valign: "middle",
                width: 45,
               
                formatter: function (value, row, index) {
                    if (row.sendType == 1) {
                        return "启动";
                    } else if (row.sendType == 2) {
                        return "<span style='color:#fd0000'>停止</span>";
                    }
                }
            },
            {
                title: "通道类型",
                field: "aisleName",
                align: "center",
                valign: "middle",
                width: 150
            },
            {
                title: '操作',
                field: "operation",
                align: 'center',
                valign: "middle",
                width: 80,
                formatter: function (value, row, index) {
                    var j = '';
                    if (row.sendType == 1) {
                        j = '<a class="btn btn-sm btn-white" href="javaScript:void(0);" onclick="updatetimedtask(\'' + row.id + '\' , \'' + 2 + '\',null,null,null,null,null,null);"><i class="fa fa-paste"></i>' + "停止" + '</a> ';
                    } else if (row.sendType == 2) {
                        j = '<a class="btn btn-sm btn-white" href="javaScript:void(0);" onclick="updatetimedtask(\'' + row.id + '\' , \'' + 1 + '\',null,null,null,\'' + new Date(row.timing).Format("yyyy-MM-dd hh:mm") + '\',null,null);"><i class="fa fa-paste"></i>' + "启动" + '</a> ';
                    }

                    var i = '<a class="btn btn-sm btn-white" href="javaScript:void(0);" onclick="editData(\'' + row.id + '\');"><i class="fa fa-paste"></i>' + "修改" + '</a> ';
                    return j + "&nbsp;" + i;
                }
            }
        ],

        responseHandler: function (a) {
            $('#tb_data').bootstrapTable('hideColumn', 'aisleName');
            $('#tb_data').bootstrapTable('hideColumn', 'operation');
            $('#tb_data').bootstrapTable('hideColumn', 'select');
            var btn_arr = a["data"];
            if (btn_arr != null && btn_arr.length > 0) {
                var user_path = '/smsTimedTask';
                for (var k = 0; k < btn_arr.length; k++) {
                    var btn_obj = btn_arr[k];
                    if (btn_obj["actionUrls"] == user_path + '/aisleSelect.do') {
                        $("#aisleSpan").show();//
                        $('#tb_data').bootstrapTable('showColumn', 'aisleName');
                    }
                    if (btn_obj["actionUrls"] == user_path + '/stopSpan.do') {
                        $("#stopSpan").show();//
                    }
                    if (btn_obj["actionUrls"] == user_path + '/startSpan.do') {
                        $("#startSpan").show();//
                    }
                    if (btn_obj["actionUrls"] == user_path + '/deleteSpan.do') {
                        $("#deleteSpan").show();//
                    }
                    if (btn_obj["actionUrls"] == user_path + '/exportSpan.do') {
                        $("#exportSpan").show();//
                    }
                    if (btn_obj["actionUrls"] == user_path + '/operation.do') {
                        $('#tb_data').bootstrapTable('showColumn', 'operation');
                    }
                    if (btn_obj["actionUrls"] == user_path + '/select.do') {
                        $('#tb_data').bootstrapTable('showColumn', 'select');
                    }
                }
            }
            return a;
        },

        formatNoMatches: function () {
            return '无符合条件的记录';
        }
    });
}

