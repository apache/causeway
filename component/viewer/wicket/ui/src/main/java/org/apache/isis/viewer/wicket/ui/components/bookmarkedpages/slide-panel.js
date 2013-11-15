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
$(document).ready(function(){
 
    var showBookmarks = function(){
        $('#bookmarkedPagesSlidingDiv').stop().animate(
            {width:"300px", opacity:0.95}, 100, 
            function() {
                $('.content').fadeIn('50');
            });
        $('.showPanelTab').animate({opacity: 0.5});
    };

    var hideBookmarks = function(){
        $('.content').fadeOut('0', 
            function() { 
                $('#bookmarkedPagesSlidingDiv').stop().animate({width:"0", opacity:0.1}, 0);
            });
        $('.showPanelTab').animate({opacity: 1.0});
     };

     var hideBookmarksQuickly = function(){
         $('.content').hide();
         $('#bookmarkedPagesSlidingDiv').css({width:"0"}, 0);
         $('.showPanelTab').css({opacity: 1.0});
      };

    $('.showPanelTab').mouseenter(showBookmarks);
    $('#bookmarkedPagesSlidingDiv').mouseleave(hideBookmarks);
    
    $('body').keydown(function(e) {
        
        // alt+]
        if(e.which === 221 && e.altKey) {
            if($('#bookmarkedPagesSlidingDiv .content').is(":visible")) {
                hideBookmarksQuickly();
            } else {
                showBookmarks();
            }
        }
      });
});