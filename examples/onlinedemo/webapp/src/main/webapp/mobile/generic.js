cloneTemplatePage = function(pageBaseId, urlHref, dataOptions) {

  var urlHrefEncoded = urlencode(urlHref);

  var pageId = pageBaseId + "-" + urlHrefEncoded;
  removePage(pageId);

  var page = cloneAndInsertPage(pageBaseId, pageId)
  
  dataOptions.dataUrl = pageBaseId + "?url=" + urlHrefEncoded
  return page;
}

extract = function(urlHref) {
  var regex = /.*?url=(.*)/
  var matches = regex.exec(urlHref)
  var url = matches && matches[1]
  if(url) {
    return urldecode(url)
  }
  return urlHref
}

handleDomainObjectRepresentation = function(urlHref, dataOptions, json, xhr) {
  
  var page = cloneTemplatePage("genericDomainObjectView", urlHref, dataOptions);
  
  var header = page.children(":jqmData(role=header)");
  header.find("h1").html(json.title);

  // value properties
  var valueProperties = json.members.filter(function(item) {
    return item.memberType === "property" && !item.value.href;
  });
  var valuePropertiesDiv = page.children(":jqmData(role=content)").find(".valueProperties");
  var valuePropertiesTemplateDiv = page.children(".valueProperties-tmpl");
  applyTemplateDiv(valueProperties, valuePropertiesDiv, valuePropertiesTemplateDiv);

  
  // reference properties
  var referenceProperties = json.members.filter(function(item) {
    return item.memberType === "property" && item.value.href;
  });
  var referencePropertiesList = page.children(":jqmData(role=content)").find(".referenceProperties");
  var referencePropertiesTemplateDiv = page.children(".referenceProperties-tmpl");
  applyTemplateDiv(referenceProperties, referencePropertiesList, referencePropertiesTemplateDiv);

  var collections = json.members.filter(function(item) {
    return item.memberType === "collection";
  }).map(function(value, i) {
    var href = grepLink(value.links, "details").href
    return {
      "hrefUrlEncoded" : urlencode(value.links[0].href),
      "id" : value.id,
      "href" : value.links[0].href
    }
  });

  // collections
  var collectionsList = page.children(":jqmData(role=content)").find(".collections");
  var collectionsTemplateDiv = page.children(".collections-tmpl");
  applyTemplateDiv(collections, collectionsList, collectionsTemplateDiv);


  // refresh
  page.page();
  page.trigger("create");
  
  referencePropertiesList.listview("refresh");
  collectionsList.listview("refresh");
  
  return page
} 


handleListRepresentation = function(urlHref, dataOptions, json, xhr) {
  
  var page = cloneTemplatePage("genericListView", urlHref, dataOptions);
  
  var items = $.map(json, function(value, i) {
    return {
      "hrefUrlEncoded" : urlencode(value.href),
      "title" : value.title,
      "href" : value.href
    }
  });

  var header = page.children(":jqmData(role=header)");
  var content = page.children(":jqmData(role=content)");

  header.find("h1").html("Objects");

  var div = page.find("ul");
  var templateDiv = page.find(".tmpl");
  
  applyTemplateDiv(items, div, templateDiv);
  
  // no longer needed?
  //page.page();
  //div.listview("refresh");
  
  return page;
}

handleObjectCollectionRepresentation = function(urlHref, dataOptions, json, xhr) {
  
  var page = cloneTemplatePage("genericObjectCollectionView", urlHref, dataOptions);
  
  var items = $.map(json.value, function(value, i) {
    return {
      "hrefUrlEncoded" : urlencode(value.href),
      "title" : value.title,
      "href" : value.href
    }
  });

  var header = page.children(":jqmData(role=header)");
  var content = page.children(":jqmData(role=content)");

  var parentTitle = grepLink(json.links, "up").title
  
  var collectionId = json.id;
  header.find("h1").html(collectionId + " for " + parentTitle);

  var div = page.find("ul");
  var templateDiv = page.find(".tmpl");
  
  applyTemplateDiv(items, div, templateDiv);
  
  // no longer needed?
  //page.page();
  //div.listview("refresh");
  
  return page;
}


handleActionResultRepresentation = function(urlHref, dataOptions, json, xhr) {
  var resultType = json.resulttype
  if(resultType === "object") {
    return handleDomainObjectRepresentation(urlHref, dataOptions, json.result, xhr)
  }
  if(resultType === "list") {
    return handleListRepresentation(urlHref, dataOptions, json.result.value, xhr)
  }
  alert("not yet supported")
}

handlers = {
    "application/json;profile=\"urn:org.restfulobjects/domainobject\"": handleDomainObjectRepresentation,
    "application/json; profile=\"urn:org.restfulobjects/domainobject\"": handleDomainObjectRepresentation,
    "application/json;profile=\"urn:org.restfulobjects/list\"": handleListRepresentation,
    "application/json; profile=\"urn:org.restfulobjects/list\"": handleListRepresentation,
    "application/json;profile=\"urn:org.restfulobjects/objectcollection\"": handleObjectCollectionRepresentation,
    "application/json; profile=\"urn:org.restfulobjects/objectcollection\"": handleObjectCollectionRepresentation,
    "application/json;profile=\"urn:org.restfulobjects/actionresult\"": handleActionResultRepresentation,
    "application/json; profile=\"urn:org.restfulobjects/actionresult\"": handleActionResultRepresentation
}

submitAndRender = function(urlHref, dataOptions) {
  $.ajax({
    url : urlHref,
    dataType : 'json',
    success : function(json, str, xhr) {
      var contentType = xhr.getResponseHeader("Content-Type");
      var handler = handlers[contentType];
      if(!handler) {
        alert("unable to handle response")
        return;
      } 
      var page = handler(urlHref, dataOptions, json, xhr)

      $.mobile.changePage(page, dataOptions);
    }
  })
}

submitRenderAndNavigate = function(e, data) {
  if (typeof data.toPage !== "string") {
    return;
  }

  var url = $.mobile.path.parseUrl(data.toPage)
  if(url.href.search(/^.+\.html.*$/) !== -1) {
    return;
  }

  submitAndRender(url.href, data.options);
  e.preventDefault();
}

