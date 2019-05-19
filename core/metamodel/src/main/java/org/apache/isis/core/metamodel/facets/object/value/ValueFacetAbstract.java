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

package org.apache.isis.core.metamodel.facets.object.value;

import java.util.stream.Stream;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.core.commons.lang.ClassExtensions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facets.MultipleValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacetUsingDefaultsProvider;
import org.apache.isis.core.metamodel.facets.object.encodeable.encoder.EncodableFacetUsingEncoderDecoder;
import org.apache.isis.core.metamodel.facets.object.parseable.parser.ParseableFacetUsingParser;
import org.apache.isis.core.metamodel.facets.object.title.parser.TitleFacetUsingParser;

public abstract class ValueFacetAbstract extends MultipleValueFacetAbstract implements ValueFacet {

    public static Class<? extends Facet> type() {
        return ValueFacet.class;
    }

    private static ValueSemanticsProvider<?> newValueSemanticsProviderOrNull(
            final Class<?> semanticsProviderClass, final FacetHolder holder) {
        if (semanticsProviderClass == null) {
            return null;
        }

        return (ValueSemanticsProvider<?>) ClassExtensions.newInstance(semanticsProviderClass, 
                new Class<?>[] { FacetHolder.class/*, ServiceInjector.class*/ }, new Object[] { holder });
    }

    // to look after the facets (since MultiTyped)
    private final FacetHolder facetHolder = new FacetHolderImpl();

    private final ValueSemanticsProvider<?> semanticsProvider;

    public enum AddFacetsIfInvalidStrategy {
        DO_ADD(true), DONT_ADD(false);
        private boolean addFacetsIfInvalid;

        private AddFacetsIfInvalidStrategy(final boolean addFacetsIfInvalid) {
            this.addFacetsIfInvalid = addFacetsIfInvalid;
        }

        public boolean shouldAddFacetsIfInvalid() {
            return addFacetsIfInvalid;
        }
    }

    public ValueFacetAbstract(
            final Class<?> semanticsProviderClass, 
            final AddFacetsIfInvalidStrategy addFacetsIfInvalid, 
            final FacetHolder holder) {
        
        this(newValueSemanticsProviderOrNull(semanticsProviderClass, holder), addFacetsIfInvalid, holder);
    }

    public ValueFacetAbstract(
            final ValueSemanticsProvider<?> semanticsProvider, 
            final AddFacetsIfInvalidStrategy addFacetsIfInvalid, 
            final FacetHolder holder) {
        
        super(type(), holder);

        this.semanticsProvider = semanticsProvider;

        // note: we can't use the runtimeContext to inject dependencies into the
        // semanticsProvider,
        // because there won't be any PersistenceSession when initially building
        // the metamodel.
        // so, we defer until we use the parser.

        if (!isValid() && !addFacetsIfInvalid.shouldAddFacetsIfInvalid()) {
            return;
        }

        // we now figure add all the facets supported. Note that we do not use
        // FacetUtil.addFacet,
        // because we need to add them explicitly to our delegate facetholder
        // but have the
        // facets themselves reference this value's holder.

        facetHolder.addFacet((Facet) this); // add just ValueFacet.class
        // initially.

        // we used to add aggregated here, but this was wrong.
        // An immutable value is not aggregated, it is shared.

        // ImmutableFacet, if appropriate
        final boolean immutable = semanticsProvider == null || semanticsProvider.isImmutable();
        if (immutable) {
            facetHolder.addFacet(new ImmutableFacetViaValueSemantics(holder));
        }

        // EqualByContentFacet, if appropriate
        final boolean equalByContent = semanticsProvider == null || semanticsProvider.isEqualByContent();
        if (equalByContent) {
            facetHolder.addFacet(new EqualByContentFacetViaValueSemantics(holder));
        }

        if (semanticsProvider != null) {

            // install the EncodeableFacet if we've been given an EncoderDecoder
            final EncoderDecoder<?> encoderDecoder = semanticsProvider.getEncoderDecoder();
            if (encoderDecoder != null) {
                facetHolder.addFacet(new EncodableFacetUsingEncoderDecoder(encoderDecoder, holder));
            }

            // install the ParseableFacet and other facets if we've been given a
            // Parser
            final Parser<?> parser = semanticsProvider.getParser();
            if (parser != null) {
                facetHolder.addFacet(new ParseableFacetUsingParser(parser, holder));
                facetHolder.addFacet(new TitleFacetUsingParser(parser, holder));
                facetHolder.addFacet(new TypicalLengthFacetUsingParser(parser, holder));
                final int maxLength = parser.maxLength();
                if(maxLength >=0) {
                    facetHolder.addFacet(new MaxLengthFacetUsingParser(parser, holder));
                }
            }

            // install the DefaultedFacet if we've been given a DefaultsProvider
            final DefaultsProvider<?> defaultsProvider = semanticsProvider.getDefaultsProvider();
            if (defaultsProvider != null) {
                facetHolder.addFacet(new DefaultedFacetUsingDefaultsProvider(defaultsProvider, holder));
            }
        }
    }

    public boolean isValid() {
        return this.semanticsProvider != null;
    }

    // /////////////////////////////
    // MultiTypedFacet impl
    // /////////////////////////////
    
    @Override
    public Stream<Class<? extends Facet>> facetTypes() {
        return facetHolder.streamFacets()
                .map(Facet::facetType);
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> facetType) {
        return facetHolder.getFacet(facetType);
    }

    @Override
    public boolean containsFacetTypeOf(final Class<? extends Facet> requiredFacetType) {
        return facetHolder.containsFacet(requiredFacetType);
    }

}
