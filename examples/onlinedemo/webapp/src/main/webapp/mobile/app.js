//cloneGenericView = function(pageBaseId) {
//  cloneAndInsertPage(pageBaseId, pageBaseId + "-template")
//}
//
//cloneGenericViews = function() {
//  cloneGenericView("genericDomainObjectView")
//  cloneGenericView("genericObjectCollectionView")
//  cloneGenericView("genericListView")
//}


$(function() {
  $(document).bind("mobileinit", function() {
    $.mobile.ajaxEnabled = false;
  });

  $("#settings-theme input").click(function(e) {
    
    var theme = e.currentTarget.value;
    $("div[data-role='page']").each( function() {
      $(this).attr("data-theme", e);
      // TODO: how refresh?
    });
  });

  $(document).bind("pagebeforechange", submitRenderAndNavigate);

  //cloneGenericViews();
  
  // if user manually refreshes page for domain object, then re-retrieve
  var locationHref = location.href;
  if(locationHref.indexOf("genericDomainObjectView") != -1) {
    var urlHref = extract(locationHref);
    submitAndRender(urlHref, "pop");
  } else {
    $.mobile.changePage($("#home"))
  }
  
});
