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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;

public class StoreUsingSession implements Store {

    private static final long serialVersionUID = 1L;

    private final String oidStr;

    public StoreUsingSession(final EntityModel entityModel) {
        final ObjectAdapterMemento objectAdapterMemento = entityModel.getObjectAdapterMemento();
        oidStr = asStr(objectAdapterMemento);
    }

    private static String asStr(final ObjectAdapterMemento objectAdapterMemento) {
        final ObjectAdapter objectAdapter =
                objectAdapterMemento.getObjectAdapter(AdapterManager.ConcurrencyChecking.NO_CHECK);
        final RootOid oid = (RootOid) objectAdapter.getOid();
        return IsisContext.getOidMarshaller().marshalNoVersion(oid);
    }

    @Override
    public Serializable get(final String key) {
        return Session.get().getAttribute(getOidAndKey(key));
    }

    @Override
    public void set(final String key, final Serializable value) {
        Session.get().setAttribute(getOidAndKey(key), value);
    }

    @Override
    public void remove(final String key) {
        Session.get().removeAttribute(getOidAndKey(key));
    }

    protected String getOidAndKey(final String key) {
        return oidStr + ":" + key;
    }

}
