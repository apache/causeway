renderTasks = function(list, title) {
  var items = $.map(list, function(value, i) {
    return {
      "hrefUrlEncoded" : urlencode(value.href),
      "title" : value.title,
      "href" : value.href
    }
  });

  var page = $("#home");

  var header = page.children(":jqmData(role=header)");
  var content = page.children(":jqmData(role=content)");

  header.find("h1").html(title);

  var div = page.find("ul.tasks");
  var templateDiv = page.find(".tmpl");
  
  applyTemplateDiv(items, div, templateDiv);
  page.page();

  div.listview("refresh");
  
  return page;
}

todaysTasksPage = function() {
  $.ajax({
    url : "/services/toDoItems/actions/toDosForToday/invoke",
    dataType : 'json',
    success : function(json, str, xhr) {
      renderTasks(json.result.value, "Today's Tasks")
    }
  });
};


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
});
