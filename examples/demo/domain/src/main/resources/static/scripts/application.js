$(document).ready(function() {

    /// adds style dynamically to enable CSS styling of
    // asciidoc text in form: link:xxx[`SomeClassName`]
    $("a:has(code)").addClass("a-has-code");

    /// prevents space bar from scrolling page down
    /// and instead, if on a link, then clicks it
    window.addEventListener('keydown', function(e) {
        if (e.keyCode === 32) {
            console.log(e.target)
            if (e.target === document.body) {
                e.preventDefault();
            } else if (e.target.tagName.toLowerCase() === 'a') {
                e.preventDefault();
                e.target.click();
            }
        }
    });

});


