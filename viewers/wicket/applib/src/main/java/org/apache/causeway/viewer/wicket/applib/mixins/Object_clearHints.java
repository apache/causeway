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
 *
 */
package org.apache.causeway.viewer.wicket.applib.mixins;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.hint.HintStore;

import lombok.RequiredArgsConstructor;

/**
 * Provides the ability for the end-user to discard UI hints so that the
 * object is rendered in its initial state:
 * <p>
 * When a domain object is rendered the end-user can select different tabs,
 * and for collections can sort the columns, navigate to second pages, or
 * select different views of collections.
 * If the user revisits that object, the Wicket viewer (at least) will remember
 * these hints and render the domain object in the same state.
 * <p>
 * These rendering hints are also included if the user copies the URL using
 * the anchor link (to right hand of the object's title).
 * <p>
 * This mixin - contributed to <code>java.lang.Object</code> and therefore
 * to all domain objects - provides the ability for the end user to clear
 * any hints that might have been set for the domain object being rendered.
 *
 * @see HintStore
 *
 * @since 1.x {@index}
 */
@Action(
        domainEvent = Object_clearHints.ActionDomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        cssClassFa = "fa-circle",
        describedAs = "Resets the presentation of the displayed object/page to its initial form. "
                + "(table sorting, tab selection, etc.)",
        position = ActionLayout.Position.PANEL,
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        sequence = "400.1"
)
@RequiredArgsConstructor
public class Object_clearHints {

    @Autowired(required = false) HintStore hintStore;
    @Inject BookmarkService bookmarkService;

    private final Object holder;

    public static class ActionDomainEvent
        extends org.apache.causeway.applib.events.domain.ActionDomainEvent<Object> {}
    @MemberSupport public Object act() {
        if (hintStore != null) {
            bookmarkService.bookmarkFor(holder).ifPresent(bookmark -> this.hintStore.removeAll(bookmark));
        }
        return holder;
    }

    @MemberSupport public boolean hideAct() {
        return hintStore == null;
    }

    // -- HELPER

}
