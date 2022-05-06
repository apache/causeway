function(info, successCallback, failureCallback) {

    Wicket.Ajax.ajax({
        "u": "${url}",
        "dt": "json",
        "wr":  false,
        "ep": {
            "start": info.start.valueOf(),
            "end": info.end.valueOf(),
        },
        "sh": [function(data, textStatus, jqXHR, attrs) { successCallback(jqXHR) }]
    });
}