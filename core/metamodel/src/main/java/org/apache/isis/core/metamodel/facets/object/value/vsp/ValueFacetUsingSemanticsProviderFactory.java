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
package org.apache.isis.core.metamodel.facets.object.value.vsp;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacetUsingDefaultsProvider;
import org.apache.isis.core.metamodel.facets.object.encodeable.encoder.EncodableFacetUsingEncoderDecoder;
import org.apache.isis.core.metamodel.facets.object.title.parser.TitleFacetUsingValueFacet;
import org.apache.isis.core.metamodel.facets.object.value.ImmutableFacetViaValueSemantics;
import org.apache.isis.core.metamodel.facets.object.value.MaxLengthFacetUsingParser;
import org.apache.isis.core.metamodel.facets.object.value.TypicalLengthFacetUsingParser;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;

import lombok.val;

public abstract class ValueFacetUsingSemanticsProviderFactory<T>
extends FacetFactoryAbstract {

    protected ValueFacetUsingSemanticsProviderFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Deprecated
    protected final void addValueFacet(final ValueSemanticsProviderAndFacetAbstract<T> valueSemantics) {
        val valueFacet = new ValueFacetUsingSemanticsProvider(
                Can.ofSingleton(valueSemantics), valueSemantics.getFacetHolder());
        installRelatedFacets(valueFacet, Can.of(valueSemantics), valueSemantics.getFacetHolder());
    }

    protected final void addAllFacetsForValueSemantics(
            final Can<ValueSemanticsProvider<T>> valueSemantics,
            final FacetHolder holder) {

        val valueFacet = new ValueFacetUsingSemanticsProvider<>(valueSemantics, holder);
        installRelatedFacets(valueFacet, valueSemantics, holder);
    }

    // -- HELPER

    private void installRelatedFacets(
            final ValueFacet<T> valueFacet,
            final Can<ValueSemanticsProvider<T>> semanticsProviders,
            final FacetHolder holder) {

        FacetUtil.addFacet(valueFacet);

        holder.addFacet(new ImmutableFacetViaValueSemantics(holder));
        holder.addFacet(TitleFacetUsingValueFacet.create(valueFacet, holder));

        semanticsProviders
        .forEach(semanticsProvider->{

            // install the EncodeableFacet if we've been given an EncoderDecoder
            final EncoderDecoder<?> encoderDecoder = semanticsProvider.getEncoderDecoder();
            if (encoderDecoder != null) {
                //getServiceInjector().injectServicesInto(encoderDecoder);
                //FIXME convert to using value-facet
                holder.addFacet(new EncodableFacetUsingEncoderDecoder(encoderDecoder, holder));
            }

            // install the ParseableFacet and other facets if we've been given a
            // Parser
            final Parser<?> parser = semanticsProvider.getParser();
            if (parser != null) {

                //holder.getServiceInjector().injectServicesInto(parser);
               //FIXME convert to using value-facet
                holder.addFacet(new TypicalLengthFacetUsingParser(parser, holder));
                final int maxLength = parser.maxLength();
                if(maxLength >=0) {
                   //FIXME convert to using value-facet
                    holder.addFacet(new MaxLengthFacetUsingParser(parser, holder));
                }
            }

            // install the DefaultedFacet if we've been given a DefaultsProvider
            final DefaultsProvider<?> defaultsProvider = semanticsProvider.getDefaultsProvider();
            if (defaultsProvider != null) {
                //holder.getServiceInjector().injectServicesInto(defaultsProvider);
                //FIXME convert to using value-facet
                holder.addFacet(new DefaultedFacetUsingDefaultsProvider(defaultsProvider, holder));
            }

        });

    }

}
