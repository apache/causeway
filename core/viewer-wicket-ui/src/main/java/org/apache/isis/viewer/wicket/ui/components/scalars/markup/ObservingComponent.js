var sse_observe = function(targetId, observing) { 

	function updateField(newValue) {
		document.getElementById(targetId).innerHTML = newValue;  
	}
	
	function isEventSourceSupported() {
		return typeof(EventSource) !== "undefined";  
	}
	
	if(isEventSourceSupported()) {
	  var source = new EventSource(observing);
	  source.onmessage = function(event) {
		    var decodedData = window.atob(event.data);
		    updateField(decodedData);
	  };
	} else {
		updateField("Sorry, your browser does not support server-sent events.");
	}

}

sse_observe("${targetId}", "${observing}");