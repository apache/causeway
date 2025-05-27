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
package org.apache.causeway.viewer.wicket.viewer.services;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Named;

import org.apache.causeway.viewer.wicket.viewer.CausewayModuleViewerWicketViewer;

import org.apache.wicket.Session;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.hint.HintStore;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._Maps;

import lombok.extern.slf4j.Slf4j;

/**
 * Wicket-specific implementation of {@link HintStore}.
 *
 * @since 2.0 {@index}
 */
@Slf4j
public class HintStoreUsingWicketSession implements HintStore {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleViewerWicketViewer.NAMESPACE + ".HintStoreUsingWicketSession";

    @Configuration
    public static class AutoConfiguration {
        @Bean
        @ConditionalOnMissingBean(HintStore.class)
        @Named(LOGICAL_TYPE_NAME)
        @Order(PriorityPrecedence.EARLY)
        @Qualifier("Wicket")
        public HintStoreUsingWicketSession hintStoreUsingWicketSession() {
            return new HintStoreUsingWicketSession();
        }
    }

    @Override
    public String get(final Bookmark bookmark, final String key) {
        final Map<String, String> hintsForBookmark = hintsFor(bookmark);
        final String value = hintsForBookmark.get(key);

        if(log.isDebugEnabled()) {
            log.debug(String.format("GET %s / %s returns %s", bookmark.toString(), key, value));
        }

        return value;
    }

    @Override
    public void set(final Bookmark bookmark, final String key, final String value) {
        final Map<String, String> hintsForBookmark = hintsFor(bookmark);

        if(log.isDebugEnabled()) {
            log.debug(String.format("SET %s / %s to %s", bookmark.toString(), key, value));
        }

        hintsForBookmark.put(key, value);
    }

    @Override
    public void remove(final Bookmark bookmark, final String key) {
        final Map<String, String> hintsForBookmark = hintsFor(bookmark);

        if(log.isDebugEnabled()) {
            log.debug(String.format("REMOVE %s / %s", bookmark.toString(), key));
        }

        hintsForBookmark.remove(key);
    }

    @Override
    public Set<String> findHintKeys(final Bookmark bookmark) {
        final Map<String, String> hintsForBookmark = hintsFor(bookmark);
        return hintsForBookmark.keySet();
    }

    @Override
    public void removeAll(final Bookmark bookmark) {
        final String sessionAttribute = sessionAttributeFor(bookmark);
        Session.get().removeAttribute(sessionAttribute);
    }

    protected Map<String, String> hintsFor(final Bookmark bookmark) {
        final String sessionAttribute = sessionAttributeFor(bookmark);
        LinkedHashMap<String, String> hints =
                _Casts.uncheckedCast(Session.get().getAttribute(sessionAttribute));
        if(hints == null) {
            hints = _Maps.newLinkedHashMap();
            Session.get().setAttribute(sessionAttribute, hints);
        }
        return hints;
    }

    protected String sessionAttributeFor(final Bookmark bookmark) {
        return "hint-" + bookmark.stringifyHonoringHintIfAny();
    }

}
