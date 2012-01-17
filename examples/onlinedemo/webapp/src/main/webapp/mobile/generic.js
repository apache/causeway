clonePage = function(pageTemplateId, discriminator) {

  var id = pageTemplateId + "-" + urlencode(discriminator);
  var page = $(pageTemplateId).clone().attr("id", id);
  page.appendTo("#pageHolder");

  return page;
}

handleDomainObjectRepresentation = function(json) {
  
  var page = clonePage("#domainObjectView", json.oid);

  var header = page.children(":jqmData(role=header)");
  header.find("h1").html(json.title);

  var valueProperties = json.members.filter(function(item) {
    return item.memberType === "property" && !item.value.href;
  });
  var valuePropertiesDiv = page.children(":jqmData(role=content)").find(".valueProperties");
  var valuePropertiesTemplateDiv = page.children(".valueProperties-tmpl");
  applyTemplateDiv(valueProperties, valuePropertiesDiv, valuePropertiesTemplateDiv);

  
  var referenceProperties = json.members.filter(function(item) {
    return item.memberType === "property" && item.value.href;
  });
  var referencePropertiesList = page.children(":jqmData(role=content)").find(".referenceProperties");
  var referencePropertiesTemplateDiv = page.children(".referenceProperties-tmpl");
  applyTemplateDiv(referenceProperties, referencePropertiesList, referencePropertiesTemplateDiv);

  var collections = json.members.filter(function(item) {
    return item.memberType === "collection";
  }).map(function(value, i) {
    var href = $.grep(value.links, function(v) { return v.rel === "details" } )[0].href
    return {
      "hrefUrlEncoded" : urlencode(value.links[0].href),
      "id" : value.id,
      "href" : value.links[0].href
    }
  });

  var collectionsList = page.children(":jqmData(role=content)").find(".collections");
  var collectionsTemplateDiv = page.children(".collections-tmpl");
  applyTemplateDiv(collections, collectionsList, collectionsTemplateDiv);

  page.page();
  page.trigger("create");
  
  referencePropertiesList.listview("refresh");
  collectionsList.listview("refresh");

  return page
} 


listRepresentation = 0;
handleListRepresentation = function(json) {
  
  var page = clonePage("#listView", listRepresentation++);

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
  page.page();

  div.listview("refresh");
  
  return page;
}


objectCollectionRepresentation = 0;
handleObjectCollectionRepresentation = function(json) {
  
  var page = clonePage("#objectCollectionView", objectCollectionRepresentation++);

  var items = $.map(json.value, function(value, i) {
    return {
      "hrefUrlEncoded" : urlencode(value.href),
      "title" : value.title,
      "href" : value.href
    }
  });

  var header = page.children(":jqmData(role=header)");
  var content = page.children(":jqmData(role=content)");

  header.find("h1").html(json.id);

  var div = page.find("ul");
  var templateDiv = page.find(".tmpl");
  
  applyTemplateDiv(items, div, templateDiv);
  page.page();

  div.listview("refresh");
  
  return page;
}


handleActionResultRepresentation = function(json) {
  var resultType = json.resulttype
  if(resultType === "object") {
    return handleDomainObjectRepresentation(json.result)
  }
  if(resultType === "list") {
    return handleListRepresentation(json.result.value)
  }
  alert("not yet supported")
}

handlers = {
    "application/json;profile=\"urn:org.restfulobjects/domainobject\"": handleDomainObjectRepresentation,
    "application/json;profile=\"urn:org.restfulobjects/list\"": handleListRepresentation,
    "application/json;profile=\"urn:org.restfulobjects/objectcollection\"": handleObjectCollectionRepresentation,
    "application/json;profile=\"urn:org.restfulobjects/actionresult\"": handleActionResultRepresentation
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
      if(dataOptions) {
        dataOptions.dataUrl = urlHref;
      }
      var page = handler(json)
  
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

