var util = namespace('org.apache.isis.viewer.json.jqmobile.util');

util.findPage = function(id) {
  var page = $("#pageHolder").find("#"+id);
  if(page[0]) {
    return page[0];
  }
  return null
}

util.removePage = function(id) {
  var page = util.findPage(id)
  if(page) {
    page.remove();
  }
}

util.cloneAndInsertPage = function(sourceId, targetId) {
  var page = $("#"+sourceId).clone().attr("id", targetId);
  page.appendTo("#pageHolder");
  return page
}

util.grepLink = function(links, relStr) {
  return $.grep(links, function(v) { return v.rel === relStr } )[0]
}

util.queryParamsFor = function (href) {
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
util.urlencode = function(str) {
    str = (str + '').toString();
    return encodeURIComponent(str).replace(/!/g, '%21').replace(/'/g, '%27').replace(/\(/g, '%28').replace(/\)/g, '%29').replace(/\*/g, '%2A').replace(/%20/g, '+');
};

// from http://phpjs.org/functions/urldecode:572
util.urldecode = function(str) {
	return decodeURIComponent((str + '').replace(/\+/g, '%20'));
};

util.applyTemplate = function(data, selector, templateSelector) {
    $(selector).empty();
    $(templateSelector)
        .tmpl(data)
        .appendTo(selector);
}

util.applyTemplateDiv = function(data, div, templateDiv) {
  div.empty();
  templateDiv
      .tmpl(data)
      .appendTo(div);
}

