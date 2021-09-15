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

import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.value.Markup;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;

public class MarkupValueSemanticsProvider
extends ValueSemanticsProviderAndFacetAbstract<Markup>
implements MarkupValueFacet {

    private static final int TYPICAL_LENGTH = 0;

    private static Class<? extends Facet> type() {
        return MarkupValueFacet.class;
    }

    private static final Markup DEFAULT_VALUE = null;

    public MarkupValueSemanticsProvider(final FacetHolder holder) {
        super(type(), holder, Markup.class, TYPICAL_LENGTH, -1, Immutability.IMMUTABLE, EqualByContent.NOT_HONOURED, DEFAULT_VALUE);
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Markup doParse(final Parser.Context context, final String html) {
        return fromEncodedString(html);
    }

    @Override
    public String titleString(final Object object) {
        return object != null? ((Markup)object).asHtml(): "[null]";
    }

    // //////////////////////////////////////////////////////////////////
    // MarkupValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public String markupValue(final ManagedObject object) {
        if (object == null) {
            return "";
        }
        final Markup markup = (Markup) object.getPojo();
        return markup.asHtml();
    }

    @Override
    public ManagedObject createValue(final ManagedObject object, final String html) {
        final Markup markup = new Markup(html);
        return getObjectManager().adapt(markup);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    public String toEncodedString(final Markup markup) {
        return markup.asHtml();
    }

    @Override
    public Markup fromEncodedString(final String html) {
        return new Markup(html);
    }

}
