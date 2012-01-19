var util = namespace('org.apache.isis.viewer.json.jqmobile.util');
var generic = namespace('org.apache.isis.viewer.json.jqmobile.generic');

generic.cloneTemplatePage = function(pageBaseId, urlHref, dataOptions) {
  var urlHrefEncoded = util.urlencode(urlHref);

  var pageId = pageBaseId + "-" + urlHrefEncoded;
  util.removePage(pageId);

  var page = util.cloneAndInsertPage(pageBaseId, pageId)
  
  dataOptions.dataUrl = pageBaseId + "?url=" + urlHrefEncoded
  return page;
}

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
  // does it match: foobar.html?url=xxx; if so, then return xxx
  var matches = /.*?url=(.*)/.exec(urlHref)
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

generic.handleDomainObjectRepresentation = function(urlHref, dataOptions, json, xhr) {
  
  var page = generic.cloneTemplatePage("genericDomainObjectView", urlHref, dataOptions);
  
  var header = page.children(":jqmData(role=header)");
  header.find("h1").html(json.title);

  // value properties
  var valueProperties = json.members.filter(function(item) {
    return item.memberType === "property" && !item.value.href;
  });
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


  // refresh
  page.page();
  page.trigger("create");
  
  referencePropertiesList.listview("refresh");
  collectionsList.listview("refresh");
  
  return page
} 


generic.handleListRepresentation = function(urlHref, dataOptions, json, xhr) {
  
  var page = generic.cloneTemplatePage("genericListView", urlHref, dataOptions);

  var items = generic.itemLinks(json)

  var header = page.children(":jqmData(role=header)");
  var content = page.children(":jqmData(role=content)");

  header.find("h1").html("Objects");

  var div = page.find("ul");
  var templateDiv = page.find(".tmpl");
  
  util.applyTemplateDiv(items, div, templateDiv);
  
  return page;
}

generic.handleObjectCollectionRepresentation = function(urlHref, dataOptions, json, xhr) {
  
  var page = generic.cloneTemplatePage("genericObjectCollectionView", urlHref, dataOptions);

  var items = generic.itemLinks(json.value)

  var header = page.children(":jqmData(role=header)");
  var content = page.children(":jqmData(role=content)");

  var parentTitle = util.grepLink(json.links, "up").title
  
  var collectionId = json.id;
  header.find("h1").html(collectionId + " for " + parentTitle);

  var div = page.find("ul");
  var templateDiv = page.find(".tmpl");
  
  util.applyTemplateDiv(items, div, templateDiv);
  
  return page;
}


generic.handleActionResultRepresentation = function(urlHref, dataOptions, json, xhr) {
  var resultType = json.resulttype
  if(resultType === "object") {
    return generic.handleDomainObjectRepresentation(urlHref, dataOptions, json.result, xhr)
  }
  if(resultType === "list") {
    return generic.handleListRepresentation(urlHref, dataOptions, json.result.value, xhr)
  }
  alert("not yet supported")
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

generic.submitAndRender = function(urlHref, dataOptions) {
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
      var page = handler(urlHref, dataOptions, json, xhr)

      $.mobile.changePage(page, dataOptions);
    }
  })
}

generic.submitRenderAndNavigate = function(e, data) {
  if (typeof data.toPage !== "string") {
    return;
  }

  var url = $.mobile.path.parseUrl(data.toPage)
  var urlHref = generic.extract(url.href)
  if(!urlHref) {
    return;
  }

  generic.submitAndRender(urlHref, data.options);
  e.preventDefault();
}

