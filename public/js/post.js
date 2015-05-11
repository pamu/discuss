$(function() {
    $("#done").click(function() {
        var discussion = $("#discussion").val();
        if (discussion.length) {
           $("#myModal").modal('hide');
           var item = {};
           item["name"] = discussion;
           var json = JSON.stringify(item);
           $.ajax({
                       url: '/discussion',
                       type: 'POST',
                       data: json,
                       contentType: 'application/json; charset=utf-8',
                       dataType: 'json',
                       async: true,
                       success: function(msg) {
                           if(msg.success) {
                               //$("#error_center").empty();
                               //$("#error_center").html("<p>Thank you for your interest :)</p>")
                           }
                           if (msg.failure) {
                                //$("#error_center").empty();
                                //$("#error_center").html("<p>Thank you for your interest :)</p>")
                           }
                       }
                   });
        } else {
            $("#msgCenter").html('<span class="alert alert-error">Discussion should not be empty.</span>');
        }
    });
});