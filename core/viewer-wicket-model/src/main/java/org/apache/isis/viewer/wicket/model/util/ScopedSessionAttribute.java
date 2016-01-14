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

import org.apache.wicket.Session;

import org.apache.isis.applib.layout.v1_0.HasPath;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.runtime.system.context.IsisContext;
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
            final HasPath hasPath,
            final String attributeName) {
        final ObjectAdapterMemento objectAdapterMemento = entityModel.getObjectAdapterMemento();
        return create(objectAdapterMemento, hasPath, attributeName);
    }

    public static <T extends Serializable> ScopedSessionAttribute<T> create(
            final ObjectAdapterMemento objectAdapterMemento,
            final HasPath hasPath,
            final String attributeName) {
        if (hasPath == null) {
            return noop();
        }
        final String path = hasPath.getPath();
        return create(objectAdapterMemento, path, attributeName);
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
        final String key = oidStr + ":" + scopeKey + "#" + attributeName;
        return new ScopedSessionAttribute<T>(key);
    }

    private static String asStr(final ObjectAdapterMemento objectAdapterMemento) {
        final RootOid oid =
                (RootOid) objectAdapterMemento.getObjectAdapter(AdapterManager.ConcurrencyChecking.NO_CHECK).getOid();
        return IsisContext.getOidMarshaller().marshalNoVersion(oid);
    }

    private final String key;

    private ScopedSessionAttribute(final String key) {
        this.key = key;
    }

    public void set(T t) {
        Session.get().setAttribute(key, t);
    }

    public T get() {
        return (T) Session.get().getAttribute(key);
    }

    public void remove() {
        Session.get().removeAttribute(key);
    }

    public static <T extends Serializable> ScopedSessionAttribute<T> noop() {
        return new ScopedSessionAttribute<T>(null) {
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
