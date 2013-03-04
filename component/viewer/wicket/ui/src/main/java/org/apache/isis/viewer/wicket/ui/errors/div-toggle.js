jQuery(document).ready(function() {
  jQuery(".exceptionStackTracePanel .content").hide();
  jQuery(".exceptionStackTracePanel .heading").click(function()
  {
    jQuery(this).next(".exceptionStackTracePanel .content").slideToggle(500);
  });
});
