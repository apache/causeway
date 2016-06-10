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

import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 * Implementation of a Wicket {@link IConverter} for {@link ObjectAdapter}s,
 * converting to-and-from their {@link Oid}'s string representation.
 */
public class ConverterForObjectAdapter implements IConverter<ObjectAdapter> {

    private static final long serialVersionUID = 1L;

    private static final OidMarshaller OID_MARSHALLER = new OidMarshaller();

    /**
     * Converts string representation of {@link Oid} to
     * {@link ObjectAdapter}.
     */
    @Override
    public ObjectAdapter convertToObject(final String value, final Locale locale) {
        final Oid oid = RootOid.deStringEncoded(value, OID_MARSHALLER);
        return getPersistenceSession().getAdapterFor(oid);
    }

    /**
     * Converts {@link ObjectAdapter} to string representation of {@link Oid}.
     */
    @Override
    public String convertToString(final ObjectAdapter adapter, final Locale locale) {
        final Oid oid = adapter.getOid();
        if (oid == null) {
            // values don't have an Oid
            return null;
        }

        return oid.enString(OID_MARSHALLER);
    }
    


    // //////////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////////

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getSessionFactory().getCurrentSession().getPersistenceSession();
    }

}
