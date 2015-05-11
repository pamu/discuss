$(function() {
    $("#enter").click(function() {
        var comment = $("#comment").val();
        if (comment.length) {
            $("#comment").val('')
            $("#comments").append('<div class="well">' + comment + '</div>');
        } else {
            $("#msg_center").append('<div class="well">Cannot Comment</div>')
        }
    });
});