jQuery(document).ready(function() {
  jQuery(".jarManifestPanel .content").hide();
  jQuery(".jarManifestPanel .heading").click(function()
  {
    jQuery(this).next(".jarManifestPanel .content").slideToggle(500);
  });
});
