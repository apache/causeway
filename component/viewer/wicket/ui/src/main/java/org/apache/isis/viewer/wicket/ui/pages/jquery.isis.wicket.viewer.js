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
$(function() {

    'use strict';

    if (typeof(Isis) === 'object' && typeof(Isis.openInNewTab) === 'function') {
        return;
    }

    window.Isis = {
        Topic: {
            OPEN_IN_NEW_TAB: 'openInNewTab'
        },
        copyModalShown: false
    };

    var isisVeilTimeoutId;
    
    var isisShowVeil = function() {
        if(isisVeilTimeoutId) {
            clearTimeout(isisVeilTimeoutId);
            isisVeilTimeoutId = null;
        }
        $("#veil").stop().fadeIn(1000)/*show()*/;
    };

    var isisFadeInVeil = function(attributes, jqxhr, settings) {
        // use timeouts because JQuery's delay(...) cannot be stopped. 
        //var activeEl = attributes.currentTarget.activeElement;
        isisVeilTimeoutId = setTimeout(function() {
            $("#veil").fadeIn(750);
        }, 250);
        
    };

    var isisHideVeil = function() {
        if(isisVeilTimeoutId) {
            clearTimeout(isisVeilTimeoutId);
            isisVeilTimeoutId = null;
        }
        $("#veil").stop().hide();
    };

    Wicket.Event.subscribe(Isis.Topic.OPEN_IN_NEW_TAB, function(jqEvent, url) {
        var win=window.open(url, '_blank');
        if(win) { win.focus(); }
    });

    /* for modal dialogs */
    Wicket.Event.subscribe(Wicket.Event.Topic.AJAX_CALL_BEFORE_SEND, function(jqEvent, attributes, jqXHR, settings) {
        if (attributes.c !== window && !$('#'+attributes.c).hasClass('noVeil')) {
            isisShowVeil(attributes, jqXHR, settings);
        }
    });

    Wicket.Event.subscribe(Wicket.Event.Topic.AJAX_CALL_COMPLETE, function(jqEvent, attributes, jqXHR, status) {
        isisHideVeil(attributes, jqXHR, status);
    });
    

    
    /* only seem to work in non-modal situation */
    $('.buttons .okButton:not(.noVeil)').click(isisShowVeil);
    $('.buttons .ok:not(.noVeil)').click(isisShowVeil);
    $('.cssSubMenuItemsPanel .cssSubMenuItem a:not(.noVeil)').click(isisShowVeil);

    $('div.collectionContentsAsAjaxTablePanel > table.contents > tbody > tr.reloaded-after-concurrency-exception') 
        .livequery(function(){
            var x = $(this);
            $(this).animate({ "backgroundColor": "#FFF" }, 1000, "linear", function() {
                $(x).css('background-color','').removeClass("reloaded-after-concurrency-exception");
            }); 
        });

    /**
     * Show/hide the CopyLink modal window with alt+]
     */
    $('body').keydown(function(e) {
        if (e.which === 221 && e.altKey) {
            if (Isis.copyModalShown) {
                $('.copyModal').modal('hide');
                $('.modal-backdrop').remove();
                Isis.copyModalShown = false;
            }
            else {
                Isis.copyModalShown = true;
                $('.copyLink').click();
            }
        }
    });

    /**
     * Show/Hide the copy link only when hovering the entity icon and title
     */
    $('.entityIconTitleAndCopylink').hover(
        function() {
            $('.copyLink').css({visibility: 'visible'});
        },
        function() {
            $('.copyLink').css({visibility: 'hidden'});
        }
    );
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
