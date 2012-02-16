var util = namespace('org.apache.isis.viewer.json.jqmobile.util');
var generic = namespace('org.apache.isis.viewer.json.jqmobile.generic');

generic.itemLinks = function(jsonItems) { 
  var items = $.map(jsonItems, function(value, i) {
    return {
      "hrefUrlEncoded" : util.urlencode(value.href),
      "title" : value.title,
      "href" : value.href
    }
  })
  return items
}

generic.extract = function(urlHref) {
  // does it match: foobar.html?dataUrl=xxx; if so, then return xxx
  var matches = /.*?dataUrl=(.*)/.exec(urlHref)
  var url = matches && matches[1]
  if(url) {
    return util.urldecode(url)
  }
  // does it simply match foobar.html; if so, then return null
  if ( /.*\.html$/.test(urlHref)) {
    return null
  }
  // simply return the URL, assuming it is the data url we need to get.
  return urlHref
}

generic.returnTypeFor = function(memberItem) {
  var detailsJson = util.grepAndFollowLink(memberItem.links, "details")
  if (!detailsJson) {
    return null;
  }
  var describedByJson = util.grepAndFollowLink(detailsJson.links, "describedby")
  if (!describedByJson) {
    return null;
  }
  var returnTypeLink = util.grepLink(describedByJson.links, "returntype")
  return returnTypeLink? returnTypeLink.href : null;
}

generic.dataTypeFor = function(memberItem) {
  var returnType = generic.returnTypeFor(memberItem);
  if(returnType.endsWith("boolean")) return "boolean"
  return "string"
}

generic.pageAndOptions = function(page, view, dataUrl, transition) {
  var pageAndOptions = {
      "page": page,
      "options": { 
         "dataUrl": "#" + view + "?dataUrl=" + util.urlencode(dataUrl),
         "allowSamePageTransition": true,
         "transition": transition
       }
    }
  return pageAndOptions
}

generic.handleDomainObjectRepresentation = function(urlHref, pageChangeData, json, xhr) {
  
  var page = $("#genericDomainObjectView");
  var header = page.children(":jqmData(role=header)");
  var content = page.children(":jqmData(role=content)");
  
  header.find("h1").html(json.title);

  // value properties
  var valueProperties = json.members.filter(function(item) {
    return item.memberType === "property" && !item.value.href;
  });
  
  valueProperties = $.map( valueProperties, function(value, i) {
    var dataType = generic.dataTypeFor(value)
    return {
      "id": value.id,
      "value": value.value,
      "dataTypeIsString": dataType === "string",
      "dataTypeIsBoolean": dataType === "boolean"
    }
  } );

  var valuePropertiesDiv = page.children(":jqmData(role=content)").find(".valueProperties");
  var valuePropertiesTemplateDiv = page.children(".valueProperties-tmpl");
  util.applyTemplateDiv(valueProperties, valuePropertiesDiv, valuePropertiesTemplateDiv);

  
  // reference properties
  var referenceProperties = json.members.filter(function(item) {
    return item.memberType === "property" && item.value.href;
  });
  var referencePropertiesList = page.children(":jqmData(role=content)").find(".referenceProperties");
  var referencePropertiesTemplateDiv = page.children(".referenceProperties-tmpl");
  util.applyTemplateDiv(referenceProperties, referencePropertiesList, referencePropertiesTemplateDiv);

  var collections = json.members.filter(function(item) {
    return item.memberType === "collection";
  }).map(function(value, i) {
    var href = util.grepLink(value.links, "details").href
    return {
      "hrefUrlEncoded" : util.urlencode(value.links[0].href),
      "id" : value.id,
      "href" : value.links[0].href
    }
  });

  // collections
  var collectionsList = page.children(":jqmData(role=content)").find(".collections");
  var collectionsTemplateDiv = page.children(".collections-tmpl");
  util.applyTemplateDiv(collections, collectionsList, collectionsTemplateDiv);

  page.page();
  content.find( ":jqmData(role=listview)" ).listview("refresh");
  page.trigger("create");

  return generic.pageAndOptions(page, "genericDomainObjectView", urlHref)
} 


generic.handleListRepresentation = function(urlHref, pageChangeData, json, xhr) {
  
  var page = $("#genericListView");
  var header = page.children(":jqmData(role=header)");
  var content = page.children(":jqmData(role=content)");

  var items = generic.itemLinks(json.value)

  header.find("h1").html("Objects");

  var div = page.find("ul");
  var templateDiv = page.find(".tmpl");
  
  util.applyTemplateDiv(items, div, templateDiv);

  page.page();
  content.find( ":jqmData(role=listview)" ).listview("refresh");
  page.trigger("create");

  return generic.pageAndOptions(page, "genericListView", urlHref)
}

generic.handleObjectCollectionRepresentation = function(urlHref, pageChangeData, json, xhr) {
  
  var page = $("#genericObjectCollectionView");
  var header = page.children(":jqmData(role=header)");
  var content = page.children(":jqmData(role=content)");

  var items = generic.itemLinks(json.value)

  var parentTitle = util.grepLink(json.links, "up").title
  
  var collectionId = json.id;
  header.find("h1").html(collectionId + " for " + parentTitle);

  var div = page.find("ul");
  var templateDiv = page.find(".tmpl");
  util.applyTemplateDiv(items, div, templateDiv);

  page.page();
  content.find( ":jqmData(role=listview)" ).listview("refresh");
  page.trigger("create");

  return generic.pageAndOptions(page, "genericObjectCollectionView", urlHref, "slideup")
}


generic.actionResultHandlers = {
    "object": generic.handleDomainObjectRepresentation,
    "list": generic.handleListRepresentation
}

generic.handleActionResultRepresentation = function(urlHref, pageChangeData, json, xhr) {
  var resultType = json.resulttype
  var handler = generic.actionResultHandlers[resultType];
  if(!handler) {
    alert("unable to handle result type")
    return;
  } 
  return handler(urlHref, pageChangeData, json.result, xhr)
}

generic.handlers = {
    "application/json;profile=\"urn:org.restfulobjects/domainobject\"": generic.handleDomainObjectRepresentation,
    "application/json; profile=\"urn:org.restfulobjects/domainobject\"": generic.handleDomainObjectRepresentation,
    "application/json;profile=\"urn:org.restfulobjects/list\"": generic.handleListRepresentation,
    "application/json; profile=\"urn:org.restfulobjects/list\"": generic.handleListRepresentation,
    "application/json;profile=\"urn:org.restfulobjects/objectcollection\"": generic.handleObjectCollectionRepresentation,
    "application/json; profile=\"urn:org.restfulobjects/objectcollection\"": generic.handleObjectCollectionRepresentation,
    "application/json;profile=\"urn:org.restfulobjects/actionresult\"": generic.handleActionResultRepresentation,
    "application/json; profile=\"urn:org.restfulobjects/actionresult\"": generic.handleActionResultRepresentation
}

generic.submitAndRender = function(urlHref, pageChangeData) {
  $.ajax({
    url : urlHref,
    dataType : 'json',
    success : function(json, str, xhr) {
      var contentType = xhr.getResponseHeader("Content-Type");
      var handler = generic.handlers[contentType];
      if(!handler) {
        alert("unable to handle response")
        return;
      } 
      var pageAndOptions = handler(urlHref, pageChangeData, json, xhr)

      $.mobile.changePage(pageAndOptions.page, pageAndOptions.options);
    }
  })
}

generic.submitRenderAndNavigate = function(e, pageChangeData) {
  if (typeof pageChangeData.toPage !== "string") {
    return;
  }

  var url = $.mobile.path.parseUrl(pageChangeData.toPage)
  var urlHref = generic.extract(url.href)
  if(!urlHref) {
    return;
  }

  generic.submitAndRender(urlHref, pageChangeData);
  e.preventDefault();
}

