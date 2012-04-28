// licensed under ALv2; see full statement at end of file.
function add_links(ul, linkable) {
    var el = $("<a>", {
         href: linkable.link.url,
         text: linkable.title
    });
	ul.append(
            $("<li/>").append(el)
        );
    $.data(el, "isis", linkable);
}

function bootstrap_app() {
    $.getJSON('/', 
        undefined,
		function load_services (data) {
	        $.getJSON(data.services.url,
	            undefined,
	            function(data) {
	                for(var i=0; i<data.length; i++) {
	                    add_links($("#services > ul"), data[i]);
	                    //var x = $.data(el, "isis");
	                    //var jsonString= JSON.stringify(x, null, "  ");
	                    //alert(jsonString);
	                }
	            });
    });
}   

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
