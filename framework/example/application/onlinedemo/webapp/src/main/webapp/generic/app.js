// [START] Code specific to this app
$(function() {
	$('#Find').submit(findObject)
});

$(function() {
	$('#serviceMenu').click(getServices)
});

getServices = function() {
	var div = $('#objects').empty();
	var server = $('#Find input#server').val();
	var url = 'http://' + server + '/services/';

	var username = $('#Find input#username').val();
	var password = $('#Find input#password').val();

	// TODO: Factor out function below
	$.ajax({
		url : url,
		dataType : 'json',
		username : username,
		password : password,
		success : function(data) {
			var html = listRepAsHtml(data);
			div.append(html);
		}
	});

	return false; // To stop event bubbling up
}

findObject = function() {
	var server = $('#Find input#server').val();
	var oid = $('#Find input#oid').val();
	getObject('http://' + server + '/objects/' + oid);
	return false; // To stop event bubbling up
}
// [END] Code specific to this app



// Generic library methods below
bindLinks = function() {
	$(".property a").click(linkToObject);
	$(".collection a").click(linkToCollection);
	$(".action a").click(linkToAction);
};


createLink = function(href, label, onClick) {
	var link = $('<a href="' + href + '">' + label + '</a>');
	link.click(onClick);
	return link;
}

linkToObject = function() {
	getObject($(this)[0].href);
	return false; // To stop event bubbling up
}

linkToCollection = function() {
	getCollection($(this)[0].href);
	return false; // To stop event bubbling up
}

linkToAction = function() {
	getAction($(this)[0].href);
	return false; // To stop event bubbling up
}

// TODO: Factor out common logic between this and linkToActionInvokePost
// Ideally, we could store the method (GET/POST/PUT) as an Html 5 attribute on
// the
// <a> link and use that.
linkToActionInvokeGet = function() {
	// TODO: See comment within linkToActionInvokePost (below)

	var arguments = $('#objects form').serialize(); // TODO: Use $.param()
													// instead of serialize?
	var button = $('#objects form button');
	invokeActionWithGet(button[0].href, '?' + arguments);
	return false; // To stop event bubbling up
}

linkToActionInvokePost = function() {
	// TODO: Not the intended design! We really want to pass in the actual
	// form that was submitted, and extract the button/href from there, but
	// I couldn't get that to work. This (naive) approach assumes there is only
	// one
	// form & one button in the objects div.
	var button = $('#objects form button');
	var input = $('#objects form input');
	var arguments = {};
	if (input.length > 0) {
		for ( var j = 0; j < input.length; j++) {
			var value = input.val();
			arguments[input[j].name] = value;
		}
	}
	invokeActionWithPost(button[0].href, arguments);
	return false; // To stop event bubbling up
}

getObject = function(url) {

	var username = $('#Find input#username').val();
	var password = $('#Find input#password').val();

	$.ajax({
		url : url,
		dataType : 'json',
		username : username,
		password : password,
		success : function(data) {
			renderObject(data)
		}
	});

}

renderObject = function(objectRep) {
	var objects = $("#objects").empty();
	objects.append(objectRepAsHtml(objectRep, true, true));
	bindLinks();
}

getCollection = function(url) {
	var username = $('#Find input#username').val();
	var password = $('#Find input#password').val();

	$.ajax({
		url : url,
		dataType : 'json',
		username : username,
		password : password,
		success : function(data) {
			var objects = $("#objects").empty();
			objects.append(collectionRepAsHtml(data));
		}
	});
}

getAction = function(url) {

	var username = $('#Find input#username').val();
	var password = $('#Find input#password').val();

	$
			.ajax({
				url : url,
				username : username,
				password : password,
				success : function(data) {
					if (data.parameters.length == 0) { // Zero-parameter
														// actions invoked
														// immediately
						var invokeLink = getLinkRep(data, 'invoke');
						var url = invokeLink.href;
						switch (invokeLink.method) {
						case 'GET':
							invokeActionWithGet(url);
							break;
						case 'POST':
							invokeActionWithPost(url);
							break;
						case 'PUT':
							alert('PUT method not currently supported in this context');
							break;
						}
					} else { // Actions with params rendered as a dialog
						var objects = $("#objects").empty();
						objects.append(actionRepAsHtml(data));
					}
				}
			});

}

// TODO: This and next method could be merged, with the type (method) passed in
// as a
// param (values defined on an Enum). Would be better to always treat arguments
// as a
// map, and url-encode it for the GET case.
// See also comment on linkToActionInvokeGet.
invokeActionWithGet = function(url, arguments) {

	var username = $('#Find input#username').val();
	var password = $('#Find input#password').val();

	$.ajax({
		type : 'GET',
		url : url + arguments,
		username : username,
		password : password,
		success : function(data) {
			var objects = $("#objects").empty();
			objects.append(actionResultRepAsHtml(data));
		},
		dataType : 'json'
	});
}

invokeActionWithPost = function(url, arguments) {
	if (arguments != null) {
		alert("POST actions with parameters not yet supported");
	}

	var username = $('#Find input#username').val();
	var password = $('#Find input#password').val();

	$.ajax({
		type : 'POST',
		url : url,
		data : arguments,
		username : username,
		password : password,
		success : function(data) {
			var objects = $("#objects").empty();
			objects.append(actionResultRepAsHtml(data));
		},
		dataType : 'json'
	});
}

actionResultRepAsHtml = function(data) {
	var html = $('<div>');
	// TODO: Make more use of the other information in links
	switch (data.resulttype) {
	case 'list':
		html.append(listRepAsHtml(data.result));
		break;
	case 'object':
		html.append(objectRepAsHtml(data.result, true, true));
		break;
	default:
		alert(data.resulttype + ' result type not supported at present');
		break;
	}
	html.append('</div>');
	return html;
}

listRepAsHtml = function(data) {
	var html = $('<div>');
	var items = data.value;
	for ( var j = 0; j < items.length; j++) {
		html.append(createLink(items[j].href, items[j].title, linkToObject));
		html.append("<br>");
	}
	html.append('</div>');
	return html;
}

objectRepAsHtml = function(data, includeProperties, includeActions) {

	var result = $('<div>');
	var objectRep = {
		title : data.title,
		members : data.members,
		includeProperties : includeProperties,
		includeActions : includeActions
	};

	$("#objectRepAsHtml-tmpl").tmpl(objectRep).appendTo(result);

	return result;
}

collectionRepAsHtml = function(data) {
	var result = $('<div>');
	// TODO: Factor out next two lines into createParentLink - used in next
	// method also
	var parentLink = getLinkRep(data, 'up');
	if (!parentLink) {
		parentLink = getLinkRep(data, 'parent'); // HACK: remove this.
	}
	result.append(createLink(parentLink.href, 'Back To Parent', linkToObject));
	result.append('<br><h3>' + data.id + '</h3>');
	result.append(listRepAsHtml(data));
	result.append('</div>');
	return result;
}

actionRepAsHtml = function(data) {
	var form = $('<form id="' + data.id + '"></form>'); // TODO: Form id needs
														// to be improved
	var parentLink = getLinkRep(data, 'up');
	if (!parentLink) {
		parentLink = getLinkRep(data, 'parent'); // HACK: remove this.
	}
	form.append(createLink(parentLink.href, 'Back To Parent', linkToObject));
	form.append('<br><h3>' + data.id + '</h3>');
	var params = data.parameters;
	for ( var j = 0; j < params.length; j++) {
		form.append(params[j].name + ' ');
		form.append('<input type="text" name="' + params[j].name + '">');
		form.append('<br>');
	}
	var invokeLink = getLinkRep(data, 'invoke');
	var button = $('<button href="' + invokeLink.href + '">OK</button>');
	form.append(button);
	switch (invokeLink.method) {
	// TODO: Refactor so that method is held as an Html attribute on the <a>
	// link
	case 'GET':
		form.submit(linkToActionInvokeGet);
		break;
	case 'POST':
		form.submit(linkToActionInvokePost);
		break;
	default:
		alert(invokeLink.method
				+ ' method not currently supported by this viewer');
		break;
	}
	return form;
}

actionMemberAsHtml = function(member) {
	var result = $('<dt class="action">');
	result.append(member.id);
	result.append('<\dt>');

	result.append('<dd class="action">')
	result.append(createLink(member.links[0].href, member.id, linkToAction));
	return result.append('<\dd>');
}

getLinkRep = function(objectWithLinks, rel) {
	var result;
	var links = objectWithLinks.links;
	for ( var j = 0; j < links.length; j++) {
		if (links[j].rel == rel) {
			result = links[j];
		}
	}
	return result;
}
