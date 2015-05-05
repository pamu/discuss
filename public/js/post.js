$(function() {
    $("#done").click(function() {
        var discussion = $("#discussion").val();
        if (discussion.length) {
           $("#myModal").modal('hide');

        } else {
            $("#msgCenter").html('<span class="alert alert-error">Discussion should not be empty.</span>');
        }
    });
});