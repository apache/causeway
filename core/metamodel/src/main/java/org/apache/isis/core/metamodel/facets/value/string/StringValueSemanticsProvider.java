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

package org.apache.isis.core.metamodel.facets.value.string;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;

public class StringValueSemanticsProvider extends ValueSemanticsProviderAndFacetAbstract<String> implements StringValueFacet {

    public static Class<? extends Facet> type() {
        return StringValueFacet.class;
    }

    public static final int TYPICAL_LENGTH = 25;
    private static final String DEFAULT_VALUE = null; // no default

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public StringValueSemanticsProvider() {
        this(null, null);
    }

    public StringValueSemanticsProvider(final FacetHolder holder, final ServicesInjector context) {
        super(type(), holder, String.class, TYPICAL_LENGTH, null, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE, context);
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doParse(final Object context, final String entry) {
        if (entry.trim().equals("")) {
            return null;
        } else {
            return entry;
        }
    }

    @Override
    public String titleString(final Object object) {
        final String string = (String) (object == null ? "" : object);
        return string;
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
        final String text = (String) object;
        if (text.equals("NULL") || isEscaped(text)) {
            return escapeText(text);
        } else {
            return text;
        }
    }

    @Override
    protected String doRestore(final String data) {
        if (isEscaped(data)) {
            return data.substring(1);
        } else {
            return data;
        }
    }

    private boolean isEscaped(final String text) {
        return text.startsWith("/");
    }

    private String escapeText(final String text) {
        return "/" + text;
    }

    // //////////////////////////////////////////////////////////////////
    // StringValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public String stringValue(final ObjectAdapter object) {
        return object == null ? "" : (String) object.getObject();
    }

    @Override
    public ObjectAdapter createValue(final String value) {
        return getObjectAdapterProvider().adapterFor(value);
    }

    // /////// toString ///////

    @Override
    public String toString() {
        return "StringValueSemanticsProvider";
    }

}
