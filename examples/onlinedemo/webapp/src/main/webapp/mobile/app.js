var util    = namespace('org.apache.isis.viewer.json.jqmobile.util');
var generic = namespace('org.apache.isis.viewer.json.jqmobile.generic');

$(function() {
  $("#settings-theme input").click(function(e) {
    
    var theme = e.currentTarget.value;
    $("div[data-role='page']").each( function() {
      $(this).attr("data-theme", e);
      // TODO: how refresh?
    });
  });

  $(document).bind("pagebeforechange", generic.submitRenderAndNavigate);

  // if user manually refreshes page for domain object, then re-retrieve
  var locationHref = location.href;
  if(locationHref.indexOf("genericDomainObjectView") != -1) {
    var urlHref = generic.extract(locationHref);
    generic.submitAndRender(urlHref, "pop");
  } else {
    $.mobile.changePage($("#home"))
  }
});