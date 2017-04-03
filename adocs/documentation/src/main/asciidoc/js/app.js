$('#search-form').submit(function(ev) {
    ev.preventDefault(); // to stop the form from submitting
    /* Validations go here */

    var searchField = $('#search-field');
    var searchText = searchField.val()


    $.getJSON('/elasticlunr/index.json', function (data) {

        var index = elasticlunr.Index.load(data);
        var searchResults = index.search(searchText 
        /*,{
            fields: {
                title: {boost: 3},
                description: {boost: 2},
                body: {boost: 1}
        }}
        */
        );

        $('#search-results').empty();
        if(searchResults.length === 0) {

            if(searchText) {
                $('#search-results').append("<br/>No matches found for '" + searchText + "'<br/><br/>");
            }

        } else {
            for (var i = 0; i < Math.min(searchResults.length, 20); i++) {

                var searchResult = searchResults[i];
                var ref = searchResult['ref'];

                var doc = index.documentStore.docs[ref];

                var title = doc.title;
                var description = doc.description;
                var url = doc.url;
                var score = searchResult['score'];
                var percentScore = score.toFixed(2);

                if(description) {
                    $('#search-results').append("<br/><span class='searchLink'><a href='" + url + "'>" + title + " <span class='searchScore'>" + percentScore + "</span>" + "</a></span><p class='searchDescription'>" + description + "</p>");
                }
            };
        }
        $('#search-results').focus();
        $('html,body').animate({
            scrollTop: $("#search-results").offset().top - 80
        });

    });

    $(searchField).val('');
});

$(document).ready(function(){
    var searchField = $('#search-field');
    $(searchField).focus();
});