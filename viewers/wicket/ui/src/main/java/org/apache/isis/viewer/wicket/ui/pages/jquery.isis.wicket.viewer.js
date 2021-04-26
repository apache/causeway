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
            OPEN_IN_NEW_TAB: 'openInNewTab',
            OPEN_SELECT2: 'openSelect2',
            CLOSE_SELECT2: 'closeSelect2',
            FOCUS_FIRST_PARAMETER: 'focusFirstParameter',
            FOCUS_FIRST_PROPERTY: 'focusFirstProperty'
        },
        copyModalShown: false
    };

    /**
     * taken from http://www.minimit.com/articles/solutions-tutorials/vertical-center-bootstrap-3-modals
     */
    function centerModals() {
        $('.modal').each(function() {
            var $clone = $(this).clone().css('display', 'block').appendTo('body');
            var top = Math.round(($clone.height() - $clone.find('.modal-content').height()) / 2);
            top = top > 0 ? top : 0;
            $clone.remove();
            $(this).find('.modal-content').css("margin-top", top);
        });
    }
    $(document, '.modal').on('show.bs.modal', centerModals);
    $(window).on('resize', centerModals);

    var isisVeilTimeoutId;
    
    var isisShowVeil = function() {
        if(isisVeilTimeoutId) {
            clearTimeout(isisVeilTimeoutId);
            isisVeilTimeoutId = null;
        }
        $("#veil").show();
    };

    var isisFadeInVeil = function(attributes, jqxhr, settings) {
        // use timeouts because JQuery's delay(...) cannot be stopped.
        if(isisVeilTimeoutId) {
            // already queued a fade-in
            return;
        }
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

    Wicket.Event.subscribe(Isis.Topic.OPEN_SELECT2, function(jqEvent, panelId) {
        setTimeout(function() {
            var $panel = $('#'+panelId);
             console.log($panel);
            $($panel).find('select').select2('open');
//            $($panel).find('select').filter(':visible:first').focus();
        }, 0);
    });

    Wicket.Event.subscribe(Isis.Topic.CLOSE_SELECT2, function(jqEvent, panelId) {
        setTimeout(function() {
            var $panel = $('#'+panelId);
             console.log($panel);
            $($panel).find('select').select2('close');
            $($panel).find('select').filter(':visible:first').focus();
        }, 0);
    });

    Wicket.Event.subscribe(Isis.Topic.FOCUS_FIRST_PARAMETER, function(jqEvent, elementId) {
        setTimeout(function() {
            $('#'+elementId).find('.inputFormTable.parameters').find('input,textarea,select,div.cbx').filter(':visible:first').focus();
        }, 0);
    });

    Wicket.Event.subscribe(Isis.Topic.FOCUS_FIRST_PROPERTY, function(jqEvent, elementId) {
        setTimeout(function() {
            if(elementId) {
                $("#" + elementId).find('a.scalarValueInlinePromptLink').filter(':visible:first').focus();
            } else {
                $(document).find('a.scalarValueInlinePromptLink').filter(':visible:first').focus();
            }
        }, 0);
    });

    /* for modal dialogs */
    Wicket.Event.subscribe(Wicket.Event.Topic.AJAX_CALL_BEFORE_SEND, function(jqEvent, attributes, jqXHR, settings) {
        if (attributes.c !== window && !$('#'+attributes.c).hasClass('noVeil')) {
            isisFadeInVeil(attributes, jqXHR, settings);
        }
    });

    Wicket.Event.subscribe(Wicket.Event.Topic.AJAX_CALL_COMPLETE, function(jqEvent, attributes, jqXHR, status) {
        isisHideVeil(/*attributes, jqXHR, status*/);
    });
    

    
    /* only seem to work in non-modal situation */
    $('.buttons .okButton:not(.noVeil)').click(isisFadeInVeil);
    $('.buttons .ok:not(.noVeil)').click(isisFadeInVeil);
    $('.cssSubMenuItemsPanel .cssSubMenuItem a:not(.noVeil)').click(isisFadeInVeil);

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

    $('#menu-toggle').click(function(e) {
        e.preventDefault();
        $('#wrapper').toggleClass('toggled');
    });

    $('#sidebar-wrapper').keyup(function(e) {
        if (e.keyCode == 27) {
           $('#wrapper').addClass('toggled');
        }
    });

/*
    $('.editing .editable').parent().hover(function () {
        var inputEl = $(this).find("> .editable")
        $(inputEl).css({cursor: 'pointer'})
    },function () {
        var inputEl = $(this).find("> .editable")
        $(inputEl).css({cursor: 'auto'})
    });
*/

    /*
    Adapted from https://bootstrap-menu.com/detail-basic-hover.html
    Ignoring mouseleave events if hovering over a popover that belongs to a menuitem.
    The magic number in the window width predicate corresponds to the nav-bar collaps behavior 
    as used in Footer/HeaderPanel.html templates; see Bootstrap 4 ref. ...
        navbar-expand = never collapses vertically (remains horizontal)
        navbar-expand-sm = collapses below sm widths <576px
        navbar-expand-md = collapses below md widths <768px
        navbar-expand-lg = collapses below lg widths <992px
        navbar-expand-xl = collapses below xl widths <1200px
	*/
	document.querySelectorAll('.navbar .nav-item, div.additionalLinkList').forEach(function(everyitem){	
		
		everyitem.addEventListener('mouseover', function(e){
			if(window.innerWidth<576){
				return; // when collapsed is a no-op
			}
			this.querySelectorAll('a[data-toggle], button[data-toggle]').forEach(function(el_link){
				let nextEl = el_link.nextElementSibling;
				el_link.classList.add('show');
				nextEl.classList.add('show');
			})
		});

		everyitem.addEventListener('mouseleave', function(e){
			if(window.innerWidth<576){
				return; // when collapsed is a no-op
			}
			// do not hide the dropdown if hovering over a popover (tooltip) attached to the dropdown item
			// The MouseEvent.relatedTarget read-only property is the secondary target for the mouse event, 
			// if there is one: That is, the EventTarget the pointing device entered to.
			let relatedTarget = $(e.relatedTarget);
			if(relatedTarget.hasClass('popover')
					|| relatedTarget.hasClass('popover-body') // not strictly required, just an optimization
					|| relatedTarget.parents('.popover').length>0) {
				e.preventDefault();
				return;
			}
			this.querySelectorAll('a[data-toggle], button[data-toggle]').forEach(function(el_link){
				let nextEl = el_link.nextElementSibling;
				el_link.classList.remove('show');
				nextEl.classList.remove('show');
			})
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


