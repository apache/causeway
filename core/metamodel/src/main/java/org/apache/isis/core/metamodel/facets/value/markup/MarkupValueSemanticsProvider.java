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

package org.apache.isis.core.metamodel.facets.value.markup;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.value.Markup;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;

public class MarkupValueSemanticsProvider extends ValueSemanticsProviderAndFacetAbstract<Markup> implements MarkupValueFacet {

    private static final int TYPICAL_LENGTH = 0;

    private static Class<? extends Facet> type() {
        return MarkupValueFacet.class;
    }

    private static final Markup DEFAULT_VALUE = null;

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public MarkupValueSemanticsProvider() {
        this(null, null);
    }

    public MarkupValueSemanticsProvider(final FacetHolder holder, final ServicesInjector context) {
        super(type(), holder, Markup.class, TYPICAL_LENGTH, -1, Immutability.IMMUTABLE, EqualByContent.NOT_HONOURED, DEFAULT_VALUE, context);
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Markup doParse(final Object context, final String html) {
        return doRestore(html);
    }

    @Override
    public String titleString(final Object object) {
        return object != null? ((Markup)object).asString(): "[null]";
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return titleString(value);
    }

    // //////////////////////////////////////////////////////////////////
    // MarkupValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public String markupValue(final ObjectAdapter object) {
        if (object == null) {
            return "";
        }
        final Markup markup = (Markup) object.getObject();
        return markup.asString();
    }

    @Override
    public ObjectAdapter createValue(final ObjectAdapter object, final String html) {
        final Markup markup = new Markup(html);
        return getObjectAdapterProvider().adapterFor(markup);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Object object) {
        Markup markup = (Markup)object;
        return markup.asString();
    }

    @Override
    protected Markup doRestore(final String html) {
        return new Markup(html);
    }

    // /////// toString ///////

    @Override
    public String toString() {
        return "MarkupValueSemanticsProvider";
    }

}
