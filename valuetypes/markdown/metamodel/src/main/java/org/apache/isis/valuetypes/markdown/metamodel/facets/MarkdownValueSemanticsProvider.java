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
package org.apache.isis.valuetypes.markdown.metamodel.facets;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.valuetypes.markdown.applib.value.Markdown;

public class MarkdownValueSemanticsProvider
extends ValueSemanticsProviderAndFacetAbstract<Markdown>
implements MarkdownValueFacet {

    private static final int TYPICAL_LENGTH = 0;

    private static Class<? extends Facet> type() {
        return MarkdownValueFacet.class;
    }

    private static final Markdown DEFAULT_VALUE = null;

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public MarkdownValueSemanticsProvider() {
        this(null);
    }

    public MarkdownValueSemanticsProvider(final FacetHolder holder) {
        super(type(), holder, Markdown.class, TYPICAL_LENGTH, -1, Immutability.IMMUTABLE, EqualByContent.NOT_HONOURED, DEFAULT_VALUE);
    }


    @Override
    protected Markdown doParse(final Object context, final String html) {
        return doRestore(html);
    }

    @Override
    public String titleString(final Object object) {
        return object != null? ((Markdown)object).asHtml(): "[null]";
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return titleString(value);
    }

    @Override
    protected String doEncode(final Markdown markdown) {
        return markdown.getMarkdown();
    }

    @Override
    protected Markdown doRestore(final String markdown) {
        return new Markdown(markdown);
    }


    @Override
    public String toString() {
        return "MarkdownValueSemanticsProvider";
    }

    // -- MarkdownValueFacet

    @Override
    public String markdownValue(final ManagedObject object) {
        if (object == null) {
            return "";
        }
        final Markdown markdown = (Markdown) object.getPojo();
        return markdown.getMarkdown();
    }

    @Override
    public ManagedObject createValue(final ManagedObject object, final String md) {
        final Markdown markdown = Markdown.valueOfMarkdown(md);
        return getObjectManager().adapt(markdown);
    }

}
