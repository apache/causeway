jQuery(document).ready(function() {
  jQuery(".errorPage .content").hide();
  jQuery(".errorPage .heading").click(function()
  {
    jQuery(this).next(".errorPage .content").slideToggle(500);
  });
});
