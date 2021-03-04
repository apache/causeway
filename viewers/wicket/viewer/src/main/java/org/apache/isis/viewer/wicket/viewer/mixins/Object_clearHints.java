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
package org.apache.isis.viewer.wicket.viewer.mixins;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.hint.HintStore;
import org.apache.isis.viewer.wicket.viewer.services.HintStoreUsingWicketSession;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Provides the ability for the end-user to discard these UI hints so that the
 * object is rendered in its initial state:
 *
 * <p>
 * When a domain object is rendered the end-user can select different tabs,
 * and for collections can sort the columns, navigate to second pages, or
 * select different views of collections.
 * If the user revisits that object, the Wicket viewer (at least) will remember
 * these hints and render the domain object in the same state.
 * </p>
 *
 * <p>
 * These rendering hints are also included if the user copies the URL using
 * the anchor link (to right hand of the object's title).
 * </p>
 *
 * <p>
 *     This mixin - contributed to <code>java.lang.Object</code> and therefore
 *     to allo domain objects - provides the ability for the end user to clear
 *     any hints that might have been set for the domain object being rendered.
 * </p>
 *
 * @see HintStore {@index}
 */
@Action(
        domainEvent = Object_clearHints.ActionDomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT,
        commandPublishing = Publishing.DISABLED
)
@ActionLayout(
        cssClassFa = "far fa-circle",
        position = ActionLayout.Position.PANEL_DROPDOWN
)
@RequiredArgsConstructor
public class Object_clearHints {

    public static class ActionDomainEvent
    extends org.apache.isis.applib.events.domain.ActionDomainEvent<Object> {}

    private final Object holder;

    @MemberOrder(name = "datanucleusIdLong", sequence = "400.1")
    public Object act() {
        if (getHintStoreUsingWicketSession() != null) {
            val bookmark = bookmarkService.bookmarkForElseThrow(holder);
            val hintStore = getHintStoreUsingWicketSession();
            if(hintStore!=null) { // just in case
                hintStore.removeAll(bookmark);
            }
        }
        return holder;
    }

    public boolean hideAct() {
        return getHintStoreUsingWicketSession() == null;
    }

    private HintStoreUsingWicketSession getHintStoreUsingWicketSession() {
        return hintStore instanceof HintStoreUsingWicketSession
                ? (HintStoreUsingWicketSession) hintStore
                        : null;
    }

    @Inject HintStore hintStore;
    @Inject BookmarkService bookmarkService;

}
