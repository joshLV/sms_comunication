$(function () {
    page();
})
function page() {
    $("#pagenum").bind("keydown", onlyNum);
    $("#firstpage").bind("click", function () {
        $('#tb_data,#com-table,#fs-table').bootstrapTable('selectPage', "first");
    });
    $("#lastpage").bind("click", function () {
        $('#tb_data,#com-table,#fs-table').bootstrapTable('selectPage', "last");
    });
    $("#turnpage").bind("click", function () {
        var num = $("#pagenum").val();
        num = parseInt(num);
        $('#tb_data,#com-table,#fs-table').bootstrapTable('selectPage', num);
    });


}


/**
 * 刷新通道信息
 */
function refreshAisleGroup(path) {
    $.post(path + "/channel/getChannelByUserId.do", function (data) {
        if(data["data"]!=null){
            var notice = data["data"].notice;
            parent.$("#top_notice").html(notice);
        }
    });
}


function onlyNum() {// 输入数字类型
    if (!((event.keyCode >= 48 && event.keyCode <= 57)
        || (event.keyCode >= 96 && event.keyCode <= 105) || (event.keyCode == 8)) && event.keyCode != 13) {
        event.returnValue = false;

    }
    if (event.keyCode == 13) {

        $("#turnpage").click();
    }


}
$('.modal').modal({backdrop: 'static', keyboard: false, show: false});