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
package org.apache.isis.viewer.wicket.model.util;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.hint.HintStore;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;

/**
 * Scoped by the {@link Component component's path}.
 */
public class ComponentHintKey<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static <T extends Serializable> ComponentHintKey<T> create(
            final Component path,
            final String key,
            final HintStore hintStore) {
        return new ComponentHintKey<T>(path, key, null, hintStore);
    }

    public static <T extends Serializable> ComponentHintKey<T> create(
            final String fullKey,
            final HintStore hintStore) {
        return new ComponentHintKey<T>(null, null, fullKey, hintStore);
    }

    private final Component component;
    private final String keyName;
    private final String fullKey;
    private final HintStore hintStore;

    private ComponentHintKey(
            final Component component,
            final String keyName,
            final String fullKey,
            final HintStore hintStore) {
        this.component = component;
        this.keyName = keyName;
        this.fullKey = fullKey;
        this.hintStore = hintStore;
    }

    public String getKey() {
        return fullKey != null
                    ? fullKey
                    :  keyFor(component, keyName);
    }

    protected String keyFor(final Component component, final String keyName) {
        return UiHintContainer.Util.hintPathFor(component) + "-" + keyName;
    }

    public String getKeyName() {
        return keyName;
    }

    public boolean matches(final Component component, final String keyName) {
        final String key = getKey();
        final String keyOfProvided = keyFor(component, keyName);
        return keyOfProvided.equals(key);
    }

    public void set(final Bookmark bookmark, T t) {
        if(bookmark == null) {
            return;
        }
        if(t != null) {
            getHintStore().set(bookmark, getKey(), t);
        } else {
            remove(bookmark);
        }
    }

    public T get(final Bookmark bookmark) {
        if(bookmark == null) {
            return null;
        }
        return (T) getHintStore().get(bookmark, getKey());
    }

    protected HintStore getHintStore() {
        return hintStore;
    }

    public void remove(final Bookmark bookmark) {
        if(bookmark == null) {
            return;
        }
        final String key = getKey();
        getHintStore().remove(bookmark, key);
    }

    public void hintTo(
            final Bookmark bookmark,
            final PageParameters pageParameters,
            final String prefix) {
        String value = (String) get(bookmark);
        final String prefixedKey = prefix + getKey();
        if(value == null) {
            return;
        }
        pageParameters.add(prefixedKey, value);
    }


    public static <T extends Serializable> ComponentHintKey<T> noop() {
        return new ComponentHintKey<T>(null, null, null, null) {
            @Override
            public String getKey() {
                return null;
            }

            @Override
            public void set(final Bookmark bookmark, final T serializable) {
            }

            @Override
            public T get(final Bookmark bookmark) {
                return null;
            }

            @Override
            public void remove(final Bookmark bookmark) {
            }
        };
    }




}
