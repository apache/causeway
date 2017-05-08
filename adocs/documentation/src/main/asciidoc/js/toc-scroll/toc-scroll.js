$( document ).ready(function() {

    $("ul.nav li a").click(function (event) {
        var $target = $(event.currentTarget),
            $scrollToTarget = $($target.attr("href")),
            $header = $("nav.navbar-fixed-top"),
            prop = {
                scrollTop:   $scrollToTarget.offset().top - $header.outerHeight(true)
            },
            speed = 500;

        $('html, body').animate(prop, speed);
    });

});
