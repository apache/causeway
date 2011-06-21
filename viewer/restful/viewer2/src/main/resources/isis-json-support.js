
function bootstrapApp() {
    $.getJSON('/index', 
        undefined,
		function(data) {
        // load services
        $('#app').html("<div id='services'><p>Services</p><ul/></div>");
        $.getJSON(data.services.url,
            undefined,
            function(data) {
                for(var i=0; i<data.length; i++) {
                    var service = data[i]._self;
                    var el = $("<a>", {
                         href: service.link.url,
                         text: service.title
                    });
                    $("#services ul").append(
                            $("<li/>").append(el)
                        );
                    $.data(el, "isis", service);
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
