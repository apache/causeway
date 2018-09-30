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

package org.apache.isis.core.metamodel.facets.value.url;

import java.net.MalformedURLException;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;

public class URLValueSemanticsProvider extends ValueSemanticsProviderAndFacetAbstract<java.net.URL> implements URLValueFacet {


    public static Class<? extends Facet> type() {
        return URLValueFacet.class;
    }

    public static final int MAX_LENGTH = 2083;
    private static final int TYPICAL_LENGTH = 100;
    private static final java.net.URL DEFAULT_VALUE = null; // no default

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public URLValueSemanticsProvider() {
        this(null, null);
    }

    public URLValueSemanticsProvider(final FacetHolder holder, final ServicesInjector context) {
        super(type(), holder, java.net.URL.class,
                TYPICAL_LENGTH, MAX_LENGTH, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE,
                context);
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected java.net.URL doParse(final Object context, final String entry) {
        if (entry.trim().equals("")) {
            return null;
        }
        {
            try {
                return new java.net.URL(entry);
            } catch (final MalformedURLException ex) {
                throw new IllegalArgumentException("Not parseable as a URL ('" + entry + "')", ex);
            }
        }
    }

    @Override
    public String titleString(final Object object) {
        return object != null? object.toString(): "";
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
        final java.net.URL url = (java.net.URL) object;
        return url != null? url.toString(): "NULL";
    }

    @Override
    protected java.net.URL doRestore(final String data) {
        if("NULL".equals(data)) {
            return null;
        }
        try {
            return new java.net.URL(data);
        } catch (MalformedURLException e) {
            return null;
        }
    }


    // //////////////////////////////////////////////////////////////////
    // StringValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public java.net.URL urlValue(final ObjectAdapter object) {
        return object == null ? null : (java.net.URL) object.getPojo();
    }

    @Override
    public ObjectAdapter createValue(final java.net.URL value) {
        return getObjectAdapterProvider().adapterFor(value);
    }

    // /////// toString ///////

    @Override
    public String toString() {
        return "URLValueSemanticsProvider";
    }

}
