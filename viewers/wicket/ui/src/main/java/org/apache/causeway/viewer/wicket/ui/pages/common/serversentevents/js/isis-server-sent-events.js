/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
function isis_sse_observe(targetId, observing) { 

	function updateField(newValue) {
		document.getElementById(targetId).innerHTML = newValue;  
	}

	function isEventSourceSupported() {
		return typeof(EventSource) !== "undefined";  
	}

	function onError() {
		console.log("failed event-stream subscription " + observing);
		updateField("Sorry, it seems the SSE Servlet cannot be reached.<p><small>"+observing+"</small></p>");  
	}
	
	if(isEventSourceSupported()) {

		try {
			
			var source = new EventSource(observing);
			source.onmessage = function(event) {
				var decodedData = window.atob(event.data);
				updateField(decodedData);
			};
			
			// not sure how to distinguish 'connect error' from stream 'is closed' yet, so not used
			// source.onerror = onError; 
			
		} catch(err) {
			onError();
		}
		
	} else {
		updateField("Sorry, your browser does not support server-sent events.");
	}

}