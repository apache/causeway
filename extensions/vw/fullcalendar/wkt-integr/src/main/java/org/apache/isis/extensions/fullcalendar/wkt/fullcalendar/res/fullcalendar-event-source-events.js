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
/** 
	see https://fullcalendar.io/docs/events-function
	implNote: java class FullCalendarEventSourceEvents is sensitive 
		to the number of leading comment lines in this file! 
*/
function(fetchInfo, successCallback, failureCallback) { 
	Wicket.Ajax.ajax({
        "u": "${url}",
        "dt": "json",
        "wr":  false,
        "ep": {
            "startStr": fetchInfo.startStr,
            "endStr": fetchInfo.endStr,
            "timeZone": fetchInfo.timeZone
        },
        "sh": [function(data, textStatus, jqXHR, attrs) { successCallback(jqXHR); }]
    });
}
