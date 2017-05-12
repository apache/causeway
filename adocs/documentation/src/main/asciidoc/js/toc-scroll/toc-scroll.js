$( document ).ready(function() {

    $("ul.nav li a").click(function (event) {
        var $target = $(event.currentTarget),
            $scrollToTarget = $($target.attr("href")),
            $header = $("nav.navbar-static-top"),
            prop = {
                scrollTop:   $scrollToTarget.offset().top - $header.outerHeight(true)
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