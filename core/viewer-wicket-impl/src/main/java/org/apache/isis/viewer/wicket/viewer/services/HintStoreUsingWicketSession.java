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

import java.io.Serializable;

import org.apache.wicket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.hint.HintStore;

@DomainService(nature = NatureOfService.DOMAIN)
public class HintStoreUsingWicketSession implements HintStore {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(HintStoreUsingWicketSession.class);

    @Override
    public Serializable get(final Bookmark bookmark, final String key) {
        final String hintKey = getOidAndKey(bookmark, key);
        final Serializable value = Session.get().getAttribute(hintKey);

        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format("GET %s returns %s", hintKey, value));
        }

        return value;
    }

    @Override
    public void set(final Bookmark bookmark, final String key, final Serializable value) {
        final String hintKey = getOidAndKey(bookmark, key);

        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format("SET %s to %s", hintKey, value));
        }

        Session.get().setAttribute(hintKey, value);

    }

    @Override
    public void remove(final Bookmark bookmark, final String key) {
        final String hintKey = getOidAndKey(bookmark, key);

        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format("REMOVE %s", hintKey));
        }

        Session.get().removeAttribute(hintKey);
    }

    private String getOidAndKey(final Bookmark bookmark, final String key) {

        return bookmark.toString() + ":" + key;
    }

}
