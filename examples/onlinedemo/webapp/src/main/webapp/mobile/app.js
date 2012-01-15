queryParamsFor = function (href) {
    var vars = [], hash;
    var hashes = href.slice(href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = urldecode(hash[1]);
    }
    return vars;
}

// from http://phpjs.org/functions/urlencode:573
urlencode = function(str) {
    str = (str + '').toString();
    return encodeURIComponent(str).replace(/!/g, '%21').replace(/'/g, '%27').replace(/\(/g, '%28').replace(/\)/g, '%29').replace(/\*/g, '%2A').replace(/%20/g, '+');
};

// from http://phpjs.org/functions/urldecode:572
urldecode = function(str) {
	return decodeURIComponent((str + '').replace(/\+/g, '%20'));
};


applyTemplate = function(data, selector, templateSelector) {
    $(selector).empty();
    $(templateSelector)
        .tmpl(data)
        .appendTo(selector);
}

refreshList = function(data, selector, templateSelector) {
	applyTemplate(data, selector, templateSelector);
    $(selector).listview("refresh");
}


login = function() {
    var username = $("#username").val();
    var password = $("#password").val();
    
    username="sven";
    password="pass";

//    if (!username) {
//        $("#login label.error").text("username required").show()
//                .fadeOut(1500);
//        return false;
//    }
//
//    if (!password) {
//        $("#login label.error").text("password required").show()
//                .fadeOut(1500);
//        password = "pass";
//        return false;
//    }

    $("#login").data("userCredentials", {
        "username" : username,
        "password" : password
    });
    return true;
}

pageForTodaysTasks = function() {
    var username = $("#login").data("userCredentials").username;
    var password = $("#login").data("userCredentials").password;
    
    //$("#todays-tasks .username").text(username)
    
    $.ajax({
        url : "/services/toDoItems/actions/toDosForToday/invoke",
        dataType : 'json',
        username : username,
        password : password,
        success : function(data) {
        	var items = $.map(data.result.value, function(value, i){
        		return {"href": urlencode(value.href), "title":value.title}
        	});
        	refreshList(
    			items, 
    			"#todays-tasks ul.tasks", 
    			"#todays-tasks .tasks-tmpl");
        }
    });
};

pageForTask = function(e,f,g) {
    var username = $("#login").data("userCredentials").username;
    var password = $("#login").data("userCredentials").password;
    
    var queryParams = queryParamsFor(window.location.href);
    var href = queryParams.href;

    $.ajax({
        url : href,
        dataType : 'json',
        username : username,
        password : password,
        success : function(data) {
        	var items = data.members;
        	applyTemplate(
    			items, 
    			"#task div.task", 
    			"#task .task-tmpl");
        	$("#task").page("refresh");
        }
    });
};

$(function() {
	$(document).bind("mobileinit", function(){
		  $.mobile.ajaxEnabled=false;
	});
	
    $(document).delegate("#login a", "click", login);
    $(document).delegate("#todays-tasks", "pagebeforeshow", pageForTodaysTasks);
    $(document).delegate("#task", "pagebeforeshow", pageForTask);
});
