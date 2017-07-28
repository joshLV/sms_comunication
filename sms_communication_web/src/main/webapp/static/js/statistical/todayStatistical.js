// var ChineseDistricts_city = {};
var path = $("#path").val();
$(function () {
    refreshAisleGroup(path);// 刷新通道信息
    $.datetimepicker.setLocale('ch');//设置中文
    load_data();
    hideColumn();

    $("#search_btn").unbind("click");
    $("#search_btn").bind("click", function () {
        refresh_data();
        hideColumn();
    });

    $("#refresh_btn").unbind("click");
    $("#refresh_btn").bind("click", function () {
        refresh_data();
        hideColumn();
    });

});

function hideColumn() {
    $('#tb_data').bootstrapTable('hideColumn', 'aisleGroup');
    $('#tb_data').bootstrapTable('hideColumn', 'nickName');
}

//刷新数据
function refresh_data() {


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
    hideColumn();
}

function queryParamsVal(params) {  //配置参数
    var temp = {   //这里的键的名字和控制器的变量名必须一直，这边改动，控制器也需要改成一样的
        pagesize: params.limit,   //页面大小
        pagenum: (params.offset / params.limit) + 1,  //页码
        email: $("#user_input").val(), //账号信息
        aisleName: $("#aisle_select").val(),//状态
        nickName: $("#nickName_select").val()//状态
    };
    return temp;
}

function smsSendDetails(ids) {
    window.location.href = $("#path").val() + "/smsUser/sendListview.do?account=" + ids + "&id=" + $("#menu_id").val() + "&back=" + "back";

}

function load_data() {
    $('#tb_data').bootstrapTable({
        url: 'statisticalList.do?menuId=' + $("#menu_id").val() + "&logTime=" + $("#logTime").val(),
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
        clickToSelect: true,
        ccext: false,// 自定义样式,为true 跨行显示短信内容
        lines: 13, //跨多少行
        sidePagination: "server", //服务端处理分页
        method: "post",
        queryParams: queryParamsVal, //参数
        columns: [
            {
                title: "账号",
                field: "email",
                align: "center",
                valign: "middle",
                width: 100,
                formatter: function (value, row, index) {
                    if (index == 0) {
                        return "合计";
                    } else {
                        return row.email;
                    }
                }
            },
            {
                title: "发送总数",
                field: "billingNum",
                align: "center",
                valign: "middle",
                formatter: function (value, row, index) {
                    if (index == 0) {
                        return row.sumBillingNum;
                    } else {
                        return row.billingNum;
                    }
                }
            },
            {
                title: "发送中",
                field: "sendingNum",
                align: "center",
                valign: "middle",
                formatter: function (value, row, index) {
                	if (index == 0) {
                        return row.sumBillingNum - (row.sumSucceedNumUs + row.sumSucceedNumMs + row.sumSucceedNumTs + row.sumFailureNumUs + row.sumUnknownNumUs + row.sumFailureNumMs + row.sumUnknownNumMs + row.sumFailureNumTs + row.sumUnknownNumTs + row.sumUnknownSucceedNum + row.sumUnknownFailureNum + row.sumUnknowNnknownNum);
                    } else {
                    	return row.billingNum - (row.succeedNumUs + row.succeedNumMs + row.succeedNumTs + row.failureNumUs + row.failureNumTs + row.failureNumMs + row.unknownNumMs + row.unknownNumUs + row.unknownNumTs + row.unknownSucceedNum + row.unknownFailureNum + row.unknowNnknownNum);
                    }
                }
            },
            {
                title: "已发送",
                field: "sendedNum",
                align: "center",
                valign: "middle",
                formatter: function (value, row, index) {
                	if (index == 0) {
                        return row.sumUnknownNumUs + row.sumUnknownNumMs + row.sumUnknownNumTs + row.sumUnknowNnknownNum;
                    } else {
                    	return row.unknownNumUs + row.unknownNumMs + row.unknownNumTs + row.unknowNnknownNum;
                    }	
                }
            },
            {
                title: "发送成功",
                field: "succeedNum",
                align: "center",
                valign: "middle",
                formatter: function (value, row, index) {
                	if (index == 0) {
                        return row.sumSucceedNumUs + row.sumSucceedNumMs + row.sumSucceedNumTs + row.sumUnknownSucceedNum;
                    } else {
                    	return row.succeedNumUs + row.succeedNumMs + row.succeedNumTs + row.unknownSucceedNum;
                    }	
                }
            },
            {
                title: "发送失败",
                field: "failureNum",
                align: "center",
                valign: "middle",
                formatter: function (value, row, index) {
                	if (index == 0) {
                        return row.sumFailureNumUs + row.sumFailureNumMs + row.sumFailureNumTs + row.sumUnknownFailureNum;
                    } else {
                    	return row.failureNumUs + row.failureNumMs + row.failureNumTs + row.unknownFailureNum;
                    }	
                }
            },
            {
                title: "通道类型",
                field: "aisleGroup",
                align: "center",
                valign: "middle",
                width: 150

            },
            {
                title: "归属",
                field: "nickName",
                align: "center",
                valign: "middle",
                width: 100

            },
            {
                title: '操作',
                align: 'center',
                valign: "middle",
                width: 80,
                formatter: function (value, row, index) {
                    if (index != 0) {
                        var i = '<a class="btn btn-sm btn-white" href="javaScript:void(0);" onclick="smsSendDetails(\'' + row.smsUserId + '\');"><i class="fa fa-paste"></i>' + "发送记录" + '</a> ';
                        return i;
                    }
                }
            }

        ],

        responseHandler: function (a) {
            hideColumn();
            var btn_arr = a["data"];
            if (btn_arr != null && btn_arr.length > 0) {
                var user_path = '/todayStatistical';
                for (var k = 0; k < btn_arr.length; k++) {
                    var btn_obj = btn_arr[k];
                    if (btn_obj["actionUrls"] == user_path + '/aisleSelect.do') {
                        $("#aisleSpan").show();// 显示下拉框
                        $('#tb_data').bootstrapTable('showColumn', 'aisleGroup');
                    }
                    if (btn_obj["actionUrls"] == user_path + '/nickSelect.do') {
                        $("#nickSpan").show();// 显示下拉框
                        $('#tb_data').bootstrapTable('showColumn', 'nickName');
                    }
                }
            }
            return a;
        },

        formatNoMatches: function () {
            return '无符合条件的记录';
        }
    });

    if ($("div.pagination").is(":visible")) {
        $(".minlid").hide();
    } else {
        $(".minlid").show();
    }
}

