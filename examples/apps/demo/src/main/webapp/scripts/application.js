$(document).ready(function() {
	/// here...
});

// enable syntax highlighting

function includeJs(jsFilePath) {
    var js = document.createElement("script");

    js.type = "text/javascript";
    js.src = jsFilePath;

    document.body.appendChild(js);
}

includeJs("/scripts/prism1.14.js");

