refreshList = function(items, selector, templateSelector) {
    $(selector).empty();
    $(templateSelector)
        .tmpl(items)
        .appendTo(selector);
    $(selector).listview("refresh");
}

login = function() {
    var username = $("#username").val();
    if (!username) {
        $("#login label.error").text("username required").show()
                .fadeOut(1500);
        return false;
        //username = "sven";
    }

    var password = $("#password").val();
    if (!password) {
        $("#login label.error").text("password required").show()
                .fadeOut(1500);
        return false;
        //password = "pass";
    }

    $("#login").data("userCredentials", {
        "username" : username,
        "password" : password
    });
    return true;
}


todaysTasks = function() {
    var username = $("#login").data("userCredentials").username;
    var password = $("#login").data("userCredentials").password;
    
    //$("#todays-tasks .username").text(username)
    
    $.ajax({
        url : "http://localhost:8080/services/toDoItems/actions/toDosForToday/invoke",
        dataType : 'json',
        username : username,
        password : password,
        success : function(data) {
        	refreshList(
    			data.result.value, 
    			"#todays-tasks ul.tasks", 
    			"#todays-tasks .tasks-tmpl");
        }
    });
};

$(function() {
	$(document).bind("mobileinit", function(){
		  $.mobile.ajaxEnabled=false;
	});
	
    $(document).delegate("#login a", "click", login);
    $(document).delegate("#todays-tasks", "pagebeforeshow", todaysTasks);
});
