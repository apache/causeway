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

package org.apache.isis.metamodel.facets.value.localrespath;

import java.nio.file.InvalidPathException;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;

public class LocalResourcePathValueSemanticsProvider
extends ValueSemanticsProviderAndFacetAbstract<LocalResourcePath> implements LocalResourcePathValueFacet {


    public static Class<? extends Facet> type() {
        return LocalResourcePathValueFacet.class;
    }

    public static final int MAX_LENGTH = 2083;
    private static final int TYPICAL_LENGTH = 100;
    private static final LocalResourcePath DEFAULT_VALUE = null; // no default

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public LocalResourcePathValueSemanticsProvider() {
        this(null);
    }

    public LocalResourcePathValueSemanticsProvider(final FacetHolder holder) {
        super(type(), holder, LocalResourcePath.class,
                TYPICAL_LENGTH, MAX_LENGTH, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE
                );
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected LocalResourcePath doParse(final Object context, final String entry) {
        if (entry.trim().equals("")) {
            return null;
        }

        try {
            return new LocalResourcePath(entry);
        } catch (final InvalidPathException ex) {
            throw new IllegalArgumentException("Not parseable as a LocalResourcePath ('" + entry + "')", ex);
        }

    }

    @Override
    public String titleString(final Object object) {
        return object != null ? object.toString(): "";
    }

    @Override
    public String titleStringWithMask(final Object object, final String usingMask) {
        return titleString(object);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Object object) {
        final LocalResourcePath url = (LocalResourcePath) object;
        return url != null ? url.getPath() : "NULL";
    }

    @Override
    protected LocalResourcePath doRestore(final String data) {
        if("NULL".equals(data)) {
            return null;
        }
        try {
            return new LocalResourcePath(data);
        } catch (InvalidPathException e) {
            return null;
        }
    }


    // //////////////////////////////////////////////////////////////////
    // StringValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public LocalResourcePath localResourcePathValue(final ObjectAdapter object) {
        return object == null ? null : (LocalResourcePath) object.getPojo();
    }

    @Override
    public ObjectAdapter createValue(final LocalResourcePath value) {
        return getObjectAdapterProvider().adapterFor(value);
    }

    // /////// toString ///////

    @Override
    public String toString() {
        return "LocalResourcePathValueSemanticsProvider";
    }

}
