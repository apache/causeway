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
package org.apache.causeway.viewer.wicket.model.util;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.danekja.java.util.function.serializable.SerializableSupplier;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.hint.HintStore;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Scoped by the {@link Component component's path}.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ComponentHintKey implements Serializable {

    private static final long serialVersionUID = 1L;

    public static ComponentHintKey create(
            final MetaModelContext commonContext,
            final SerializableSupplier<Component> componentProvider,
            final String key) {
        return new ComponentHintKey(
                commonContext.lookupServiceElseFail(HintStore.class),
                componentProvider, null, key, null);
    }

    public static ComponentHintKey create(
            final MetaModelContext commonContext,
            final Component path,
            final String key) {
        return new ComponentHintKey(
                commonContext.lookupServiceElseFail(HintStore.class),
                null, path, key, null);
    }

    public static ComponentHintKey create(
            final HintStore hintStore,
            final String fullKey) {
        return new ComponentHintKey(hintStore,
                null, null, null, fullKey);
    }

    private transient HintStore hintStore;
    private final SerializableSupplier<Component> componentProvider;
    private Component component;
    private final String keyName;
    private final String fullKey;

    public String getKey() {
        return fullKey != null
                ? fullKey
                : keyFor(component != null
                        ? component
                        : (component = componentProvider.get()), // memoize for de-serialization
                    keyName);
    }

    protected String keyFor(final Component component, final String keyName) {
        return UiHintContainer.Util.hintPathFor(component) + "-" + keyName;
    }

    public boolean matches(final Component component, final String keyName) {
        final String key = getKey();
        final String keyOfProvided = keyFor(component, keyName);
        return keyOfProvided.equals(key);
    }

    public void set(final Bookmark bookmark, final String value) {
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
        return new ComponentHintKey(null, null, null, null, null) {
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

    public HintStore getHintStore() {
        return hintStore = computeIfAbsent(HintStore.class, hintStore);
    }

    // -- HELPER

    private <X> X computeIfAbsent(final Class<X> type, final X existingIfAny) {
        return existingIfAny!=null
                ? existingIfAny
                : WktContext.getMetaModelContext().lookupServiceElseFail(type);
    }

}
