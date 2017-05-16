$('#search-form').submit(function(ev) {
    ev.preventDefault(); // to stop the form from submitting

    var basedir = $('#basedir').text().trim();
    var docname = $('#docname').text().trim();
    var filetype = $('#filetype').text().trim();

    var searchField = $('#search-field');
    var searchText = searchField.val()
    if(!searchText) {
        $("#search-panel").removeClass("active");
    }

    $.getJSON('/elasticlunr/index.json', function (data) {

        var index = elasticlunr.Index.load(data);
        var searchResults = index.search(searchText);

        $('#search-results').empty();
        if(searchResults.length === 0) {

            if(searchText) {
                $("#search-panel").addClass("active");
                $('#search-results').append("<br/>No matches found for '" + searchText + "'<br/><br/>");
            }

        } else {
            $("#search-panel").addClass("active");
            $("#search-panel a").attr("href", docname + "." + filetype)

            for (var i = 0; i < Math.min(searchResults.length, 20); i++) {

                var searchResult = searchResults[i];
                var ref = searchResult['ref'];

                var doc = index.documentStore.docs[ref];

                var title = doc.title;
                var description = doc.description;
                var url = basedir + doc.url;
                var score = searchResult['score'];
                var percentScore = score.toFixed(2);

                if(description) {
                    var urlAfterSlash = /[^/]*$/.exec(url)[0];
                    var urlNoAnchor = urlAfterSlash.split("#")[0];
                    $('#search-results').append(
                        "<br/>" +
                        "<span class='searchLink'>" +
                            "<a href='" + url + "'>" +
                                title +
                                "&nbsp;<span class='searchUrl'>" + urlNoAnchor + "</span>" +
                                "&nbsp;<span class='searchScore'>(" + percentScore + ")</span>" +
                            "</a>" +
                        "</span>" +
                        "<p class='searchDescription'>" + description + "</p>"
                    );
                }
            };
        }
        $('#search-results-clear').attr("href", location.pathname.split('/').slice(-1))
        $('#search-results').focus();

        $('html,body').animate({
            scrollTop: $("#search-results").offset().top - 100
        });

    });

    $(searchField).val('');
});

$(document).ready(function(){
    var searchField = $('#search-field');
    $(searchField).focus();
});