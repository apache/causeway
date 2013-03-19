/*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  
         http://www.apache.org/licenses/LICENSE-2.0
         
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/

$(document).ready(function() {
	initialiseButtonConfirmation();
	addDateDropDown();
});

function initialiseButtonConfirmation() {
	$('form.confirm input.button, a.confirm').click(function(){
		  var confirmed = confirm('Are you sure?');
		  return confirmed
	});
}

function addDateDropDown() {
	var dateFormat =  formatForLanguage();
	$( "input.Date" ).datepicker({
		format: dateFormat,
		weekStart: 1
	});
	/*
	$( "input.Date" ).addClass('input-append');
	$( "input.Date" ).after('<span class="add-on"><i class="icon-calendar"></i></span>');
	*/
	/*
	$( "input.Date" ).datepicker({
		showOn: "button",
		constrainInput: false,
		buttonImage: "/images/calendar.gif",
		buttonImageOnly: true,
		dateFormat: dateFormat,
		showButtonPanel: true
	});
	*/
}

function formatForLanguage() {
	var languageCode = $('div#main').data('language');
	if (languageCode == null) {
		languageCode = 'en-uk';
	}
	if (languageCode == 'en-us') {
		return "mm/dd/yy";
		// return 'M dd, yy';
	} else if (languageCode == 'en-uk') {
		return 'dd/m/yy';
	} else {
		return 'yy-mm-dd';
	}
}

