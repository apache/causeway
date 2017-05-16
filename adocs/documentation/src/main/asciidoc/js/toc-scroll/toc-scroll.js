$( document ).ready(function() {

    $("ul.nav li a").click(function (event) {
        var $target = $(event.currentTarget),
            $scrollToTarget = $($target.attr("href")),
            $header = $("nav.navbar-static-top"),
            prop = {
                // subtracting the offset results in the incorrect entry in the TOC from being highlighted.
                // since the sticky-header keeps the header out of the way, this isn't a problem for us.
                scrollTop:   $scrollToTarget.offset().top /*- $header.outerHeight(true)*/
            },
            speed = 500;


        $($header).attr("data-toc-scrolling", "true")
        $('html, body').animate(prop, speed,
            function() {
                var x = 2;
                $($header).removeAttr("data-toc-scrolling")
            });
    });

});