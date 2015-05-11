$(function() {
    $("#enter").click(function() {
        var comment = $("#comment").val();
        if (comment.length) {
            $("#comment").val('')
            $("#comments").append('<div class="well alert alert-info">' + comment + '</div>');
        } else {
            $("#msg_center").append('<div class="well">Cannot Comment</div>')
        }
    });
});