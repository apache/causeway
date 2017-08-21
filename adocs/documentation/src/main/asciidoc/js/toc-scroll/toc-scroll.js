$( document ).ready(function() {

    $("ul.nav li a").click(function (event) {
        var $target = $(event.currentTarget),
            targetHref = $target.attr("href");

        if(targetHref && targetHref.startsWith("#")) {
            var $scrollToTarget = $(targetHref),
                $header = $("nav.navbar-static-top");

            if($scrollToTarget && $scrollToTarget.offset() && $scrollToTarget.offset().top) {
                var prop = {
                        // subtracting the offset results in the incorrect entry in the TOC from being highlighted.
                        // since the sticky-header keeps the header out of the way, this isn't a problem for us.
                        scrollTop:   $scrollToTarget.offset().top
                    },
                    speed = 500;


                $($header).attr("data-toc-scrolling", "true")
                $('html, body').animate(prop, speed,
                    function() {
                        var x = 2;
                        $($header).removeAttr("data-toc-scrolling")
                    });
            }
        }
    });

});