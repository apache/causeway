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

package org.apache.isis.viewer.wicket.viewer.integration.wicket;

import java.util.Locale;

import com.google.common.base.Strings;

import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

/**
 * Implementation of a Wicket {@link IConverter} for
 * {@link ObjectAdapterMemento}s, converting to-and-from their {@link Oid}'s
 * string representation.
 */
public class ConverterForObjectAdapterMemento implements IConverter {

    private static final long serialVersionUID = 1L;

    /**
     * Converts string representation of {@link Oid} to
     * {@link ObjectAdapterMemento}.
     */
    @Override
    public Object convertToObject(final String value, final Locale locale) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        final Oid oid = RootOidDefault.deStringEncoded(value, getOidMarshaller());
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);
        return ObjectAdapterMemento.createOrNull(adapter);
    }

    /**
     * Converts {@link ObjectAdapterMemento} to string representation of
     * {@link RootOid}.
     */
    @Override
    public String convertToString(final Object object, final Locale locale) {
        if (object == null) {
            return null;
        }
        final ObjectAdapterMemento memento = (ObjectAdapterMemento) object;
        final Oid oid = memento.getObjectAdapter(ConcurrencyChecking.NO_CHECK).getOid();
        if (oid == null) {
            // values don't have an Oid...
            // REVIEW: is this right?
            return memento.toString();
        }
        return oid.enString(getOidMarshaller());
    }
    


    // //////////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////////

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }

}
