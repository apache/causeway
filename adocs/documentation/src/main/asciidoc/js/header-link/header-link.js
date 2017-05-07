$( document ).ready(function() {

    (function() {
      $(function() {
        return $("#doc-content h2, #doc-content h3, #doc-content h4, #doc-content h5, #doc-content h6").each(function(i, el) {
          var $el, icon, id;
          $el = $(el);
          id = $el.attr('id');
          icon = '<i class="fa fa-link"></i>';
          if (id) {
            return $el.prepend($("<a />").addClass("header-link").attr("href", "#" + id).html(icon));
          }
        });
      });
    }).call(this);

});
