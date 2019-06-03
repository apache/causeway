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
package org.apache.isis.viewer.wicket.viewer.services;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.hint.HintStore;
import org.apache.wicket.Session;

import com.google.common.collect.Maps;

import lombok.extern.log4j.Log4j2;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        ) 
@Log4j2
public class HintStoreUsingWicketSession implements HintStore {

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
                (LinkedHashMap<String, String>) Session.get().getAttribute(sessionAttribute);
        if(hints == null) {
            hints = Maps.newLinkedHashMap();
            Session.get().setAttribute(sessionAttribute, hints);
        }
        return hints;
    }

    protected String sessionAttributeFor(final Bookmark bookmark) {
        return "hint-" + (
                bookmark instanceof BookmarkWithHintId
                ? ((BookmarkWithHintId) bookmark).toStringUsingHintId()
                        : bookmark.toString()
                );
    }

}
