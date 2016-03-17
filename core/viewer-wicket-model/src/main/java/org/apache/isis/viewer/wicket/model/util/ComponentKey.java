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

import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;

/**
 * Scoped by the {@link Component component's path}.
 */
public class ComponentKey<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static <T extends Serializable> ComponentKey<T> create(
            final Component path,
            final String key,
            final Store store) {
        return new ComponentKey<T>(path, key, null, store);
    }

    public static <T extends Serializable> ComponentKey<T> create(
            final String fullKey,
            final Store store) {
        return new ComponentKey<T>(null, null, fullKey, store);
    }

    private final Component component;
    private final String keyName;
    private final String fullKey;
    private final Store store;

    private ComponentKey(
            final Component component,
            final String keyName,
            final String fullKey,
            final Store store) {
        this.component = component;
        this.keyName = keyName;
        this.fullKey = fullKey;
        this.store = store;
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

    public void set(T t) {
        if(t != null) {
            getStore().set(getKey(), t);
        } else {
            remove();
        }
    }

    public T get() {
        return (T) getStore().get(getKey());
    }

    protected Store getStore() {
        return store;
    }

    public void remove() {
        final String key = getKey();
        getStore().remove(key);
    }

    public void hintTo(final PageParameters pageParameters, final String prefix) {
        String value = (String) get();
        final String prefixedKey = prefix + getKey();
        if(value == null) {
            return;
        }
        pageParameters.add(prefixedKey, value);
    }


    public static <T extends Serializable> ComponentKey<T> noop() {
        return new ComponentKey<T>(null, null, null, null) {
            @Override
            public String getKey() {
                return null;
            }

            @Override
            public void set(final T serializable) {
            }

            @Override
            public T get() {
                return null;
            }

            @Override
            public void remove() {
            }
        };
    }

}
