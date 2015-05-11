$(function() {
    $.ajax({
         url: '/discussions',
         type: 'GET',
         contentType: 'application/json; charset=utf-8',
         dataType: 'json',
         async: true,
         success: function(msg) {
                if (msg.discussions) {
                    for(var i = 0; i < msg.discussions.length; i++) {
                        $("#discussions").append('<div> Discussion: <a href="/dicussion/'+ msg.discussions[i].id +'">' + msg.discussions[i].headline + '</a></div>')
                    }
                }
                if (msg.error) {
                    $("#discussions").append('<span> Could not load data !!!, reason: ' + msg.error + '</span>');
                }
             }
         });
});