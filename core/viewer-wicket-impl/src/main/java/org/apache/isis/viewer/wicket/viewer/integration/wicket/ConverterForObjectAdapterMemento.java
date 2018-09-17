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

import org.apache.isis.commons.internal.base._Strings;

import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

/**
 * Implementation of a Wicket {@link IConverter} for
 * {@link ObjectAdapterMemento}s, converting to-and-from their {@link Oid}'s
 * string representation.
 */
public class ConverterForObjectAdapterMemento implements IConverter<ObjectAdapterMemento> {

    private static final long serialVersionUID = 1L;

    /**
     * Converts string representation of {@link Oid} to
     * {@link ObjectAdapterMemento}.
     */
    @Override
    public ObjectAdapterMemento convertToObject(final String value, final Locale locale) {
        if (_Strings.isNullOrEmpty(value)) {
            return null;
        }
        final RootOid oid = RootOid.deStringEncoded(value);
        
        final ObjectAdapter adapter = getPersistenceSession().adapterFor(oid);
        return ObjectAdapterMemento.createOrNull(adapter);
    }

    /**
     * Converts {@link ObjectAdapterMemento} to string representation of
     * {@link RootOid}.
     */
    @Override
    public String convertToString(final ObjectAdapterMemento memento, final Locale locale) {
        if (memento == null) {
            return null;
        }
        final Oid oid = memento.getObjectAdapter(ConcurrencyChecking.NO_CHECK, getPersistenceSession(),
                getSpecificationLoader()).getOid();
        if (oid == null) {
            // values don't have an Oid...
            // REVIEW: is this right?
            return memento.toString();
        }
        return oid.enString();
    }

    // //////////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////////

    SpecificationLoader getSpecificationLoader() {
        return getIsisSessionFactory().getSpecificationLoader();
    }

    PersistenceSession getPersistenceSession() {
        return getIsisSessionFactory().getCurrentSession().getPersistenceSession();
    }

    IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

}
