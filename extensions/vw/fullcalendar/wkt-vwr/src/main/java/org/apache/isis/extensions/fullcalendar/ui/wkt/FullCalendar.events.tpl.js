function(start, end, timezone, callback) {
    Wicket.Ajax.ajax({
        "u": "${url}",
        "dt": "json",
        "wr":  false,
        "ep": {
            "start": start.valueOf(),
            "end": end.valueOf(),
            "timezone": timezone,
            "timezoneOffset": new Date().getTimezoneOffset(),
            "anticache": ""+new Date().getTime()+"."+Math.random()
        },
        "sh": [function(data, textStatus, jqXHR, attrs) { callback(jqXHR); }]
    });
}