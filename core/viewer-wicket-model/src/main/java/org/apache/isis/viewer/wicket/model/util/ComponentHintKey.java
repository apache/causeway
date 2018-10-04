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

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.hint.HintStore;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Provider;
import java.io.Serializable;

/**
 * Scoped by the {@link Component component's path}.
 */
public class ComponentHintKey implements Serializable {

    private static final long serialVersionUID = 1L;

    public static ComponentHintKey create(final Provider<Component> pathProvider, final String key) {
        return new ComponentHintKey(pathProvider, null, key, null);
    }

    public static ComponentHintKey create(final Component path, final String key) {
        return new ComponentHintKey(null, path, key, null);
    }

    public static ComponentHintKey create(
            final String fullKey) {
        return new ComponentHintKey(null, null, null, fullKey);
    }

    private Provider<Component> componentProvider;
    private Component component;
    private final String keyName;
    private final String fullKey;

    private ComponentHintKey(
            final Provider<Component> componentProvider,
            final Component component,
            final String keyName,
            final String fullKey) {
        this.componentProvider = componentProvider;
        this.component = component;
        this.keyName = keyName;
        this.fullKey = fullKey;
    }

    public String getKey() {
        return fullKey != null
                ? fullKey
                        :  keyFor(component != null? component : componentProvider.get(), keyName);
    }

    protected String keyFor(final Component component, final String keyName) {
        return UiHintContainer.Util.hintPathFor(component) + "-" + keyName;
    }

    public boolean matches(final Component component, final String keyName) {
        final String key = getKey();
        final String keyOfProvided = keyFor(component, keyName);
        return keyOfProvided.equals(key);
    }

    public void set(final Bookmark bookmark, String value) {
        if(bookmark == null) {
            return;
        }
        if(value != null) {
            getHintStore().set(bookmark, getKey(), value);
        } else {
            remove(bookmark);
        }
    }

    public String get(final Bookmark bookmark) {
        if(bookmark == null) {
            return null;
        }
        return getHintStore().get(bookmark, getKey());
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
        Serializable value = get(bookmark);
        if(value == null) {
            return;
        }
        final String prefixedKey = prefix + getKey();
        pageParameters.add(prefixedKey, value);
    }


    public static ComponentHintKey noop() {
        return new ComponentHintKey(null, null, null, null) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getKey() {
                return null;
            }

            @Override
            public void set(final Bookmark bookmark, final String value) {
            }

            @Override
            public String get(final Bookmark bookmark) {
                return null;
            }

            @Override
            public void remove(final Bookmark bookmark) {
            }
        };
    }


    HintStore getHintStore() {
        return getIsisSessionFactory().getServicesInjector().lookupService(HintStore.class).orElse(null);
    }

    IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

}
