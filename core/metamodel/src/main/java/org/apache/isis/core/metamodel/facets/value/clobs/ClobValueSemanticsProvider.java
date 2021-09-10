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
package org.apache.isis.core.metamodel.facets.value.clobs;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;


public class ClobValueSemanticsProvider
extends ValueSemanticsProviderAndFacetAbstract<Clob>
implements ClobValueFacet {

    private static final int TYPICAL_LENGTH = 0;

    private static Class<? extends Facet> type() {
        return ClobValueFacet.class;
    }

    private static final Clob DEFAULT_VALUE = null;

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public ClobValueSemanticsProvider() {
        this(null);
    }

    public ClobValueSemanticsProvider(final FacetHolder holder) {
        super(type(), holder, Clob.class, TYPICAL_LENGTH, -1, Immutability.IMMUTABLE, EqualByContent.NOT_HONOURED, DEFAULT_VALUE);
    }

    @Override
    public String titleString(final Object object) {
        return object != null? ((Clob)object).getName(): "[null]";
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return titleString(value);
    }


    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    public Parser<Clob> getParser() {
        return null;
    }

    // //////////////////////////////////////////////////////////////////
    // DefaultsProvider
    // //////////////////////////////////////////////////////////////////

    @Override
    public DefaultsProvider<Clob> getDefaultsProvider() {
        return null;
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Clob clob) {
        return clob.getName() + ":" + clob.getMimeType().getBaseType() + ":" + clob.getChars();
    }

    @Override
    protected Clob doRestore(final String data) {
        final int colonIdx = data.indexOf(':');
        final String name  = data.substring(0, colonIdx);
        final int colon2Idx  = data.indexOf(":", colonIdx+1);
        final String mimeTypeBase = data.substring(colonIdx+1, colon2Idx);
        final CharSequence chars = data.substring(colon2Idx+1);
        try {
            return new Clob(name, new MimeType(mimeTypeBase), chars);
        } catch (MimeTypeParseException e) {
            throw new RuntimeException(e);
        }
    }


    // /////// toString ///////

    @Override
    public String toString() {
        return "ClobValueSemanticsProvider";
    }

}
