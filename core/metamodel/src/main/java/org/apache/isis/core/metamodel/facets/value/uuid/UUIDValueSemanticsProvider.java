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
package org.apache.isis.core.metamodel.facets.value.uuid;

import java.util.UUID;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;

public class UUIDValueSemanticsProvider
extends ValueSemanticsProviderAndFacetAbstract<UUID>
implements UUIDValueFacet {

    private static final Class<? extends Facet> type() {
        return UUIDValueFacet.class;
    }

    public static final int MAX_LENGTH = 36;
    public static final int TYPICAL_LENGTH = MAX_LENGTH;
    private static final UUID DEFAULT_VALUE = null; // no default

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public UUIDValueSemanticsProvider() {
        this(null);
    }

    public UUIDValueSemanticsProvider(final FacetHolder holder) {
        super(type(), holder, UUID.class, TYPICAL_LENGTH, MAX_LENGTH, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE);
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected UUID doParse(final Parser.Context context, final String entry) {
        if (entry.trim().equals("")) {
            return null;
        } else {
            return UUID.fromString(entry);
        }
    }

    @Override
    public String titleString(final Object object) {
        return object == null ? "" : object.toString();
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    public String toEncodedString(final UUID object) {
        return object.toString();
    }

    @Override
    public UUID fromEncodedString(final String data) {
        return UUID.fromString(data);
    }

    // //////////////////////////////////////////////////////////////////
    // UuidValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public UUID uuidValue(final ManagedObject object) {
        return object == null ? null : (UUID) object.getPojo();
    }

    @Override
    public ManagedObject createValue(final UUID value) {
        return getObjectManager().adapt(value);
    }

    // /////// toString ///////

    @Override
    public String toString() {
        return "UuidValueSemanticsProvider";
    }

}
