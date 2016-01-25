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
import org.apache.wicket.Session;

import org.apache.isis.applib.layout.v1_0.HasPath;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.hints.HintUtil;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;

/**
 * Scoped by both {@link EntityModel object} and either {@link HasPath path} (if view metadata is in use)
 * or simply a string constructed by some other mechanism ("legacy").
 */
public class ScopedSessionAttribute<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static <T extends Serializable> ScopedSessionAttribute<T> create(
            final EntityModel entityModel,
            final Component component,
            final String attributeName) {
        final ObjectAdapterMemento objectAdapterMemento = entityModel.getObjectAdapterMemento();
        return create(objectAdapterMemento, component, attributeName);
    }

    public static <T extends Serializable> ScopedSessionAttribute<T> create(
            final ObjectAdapterMemento objectAdapterMemento,
            final Component scopeComponent,
            final String attributeName) {
        if (scopeComponent == null) {
            return noop();
        }
        final String oidStr = asStr(objectAdapterMemento);
        return new ScopedSessionAttribute<T>(oidStr, null, scopeComponent, attributeName);
    }

    public static <T extends Serializable> ScopedSessionAttribute<T> create(
            final EntityModel entityModel,
            final String scopeKey,
            final String attributeName) {
        final ObjectAdapterMemento objectAdapterMemento = entityModel.getObjectAdapterMemento();
        return create(objectAdapterMemento, scopeKey, attributeName);
    }

    public static <T extends Serializable> ScopedSessionAttribute<T> create(
            final ObjectAdapterMemento objectAdapterMemento,
            final String scopeKey,
            final String attributeName) {
        if (objectAdapterMemento == null) {
            return noop();
        }
        final String oidStr = asStr(objectAdapterMemento);
        return new ScopedSessionAttribute<T>(oidStr, scopeKey, null, attributeName);
    }

    private static String asStr(final ObjectAdapterMemento objectAdapterMemento) {
        final RootOid oid =
                (RootOid) objectAdapterMemento.getObjectAdapter(AdapterManager.ConcurrencyChecking.NO_CHECK).getOid();
        return IsisContext.getOidMarshaller().marshalNoVersion(oid);
    }

    private final String oidStr;
    private final String scopeKey;
    private final Component component;
    private final String attributeName;

    private ScopedSessionAttribute(
            final String oidStr,
            final String scopeKey,
            final Component component,
            final String attributeName) {
        this.oidStr = oidStr;
        this.scopeKey = scopeKey;
        this.component = component;
        this.attributeName = attributeName;
    }

    String getKey() {
        return oidStr + ":" + getScopeKey() + "-" + attributeName;
    }

    private String getScopeKey() {
        if (scopeKey != null) {
            return scopeKey;
        }
        else {
            return HintUtil.hintPathFor(component);
        }
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void set(T t) {
        if(t != null) {
            final String key = getKey();
            Session.get().setAttribute(key, t);
        } else {
            remove();
        }
    }

    public T get() {
        final String key = getKey();
        return (T) Session.get().getAttribute(key);
    }

    public void remove() {
        final String key = getKey();
        Session.get().removeAttribute(key);
    }

    public static <T extends Serializable> ScopedSessionAttribute<T> noop() {
        return new ScopedSessionAttribute<T>(null, null, null, null) {
            @Override public void set(final T serializable) {
            }

            @Override public T get() {
                return null;
            }

            @Override public void remove() {
            }
        };
    }


}
