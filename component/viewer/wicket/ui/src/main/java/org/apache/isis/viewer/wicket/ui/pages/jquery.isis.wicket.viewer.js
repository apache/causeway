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
$(document).ready(function() {
    var showVeil = function() {
        $('#veil').show(); 
    };
    
    $('.buttons .okButton:not(.noVeil)').click(showVeil);
    $('.buttons .ok:not(.noVeil)').click(showVeil);
    $('.cssSubMenuItemsPanel .cssSubMenuItem a:not(.noVeil)').click(showVeil);
    
    $('.first-field input').focus();
    
    
    $('div.collectionContentsAsAjaxTablePanel > table.contents > tbody > tr.reloaded-after-concurrency-exception') 
        .livequery(function(){
            x=$(this);
            $(this).animate({ "backgroundColor": "#FFF" }, 1000, "linear", function() {
                $(x).css('background-color','').removeClass("reloaded-after-concurrency-exception");
            }); 
        }); 
    
});

/**
 * enables 'maxlength' to work as an attribute on 'textarea'
 * 
 * as per: see http://stackoverflow.com/questions/4459610/set-maxlength-in-html-textarea
 */
$(function() {  
    $("textarea[maxlength]").bind('input propertychange', function() {  
        var maxLength = $(this).attr('maxlength');  
        if ($(this).val().length > maxLength) {  
            $(this).val($(this).val().substring(0, maxLength));  
        }  
    })  
});
