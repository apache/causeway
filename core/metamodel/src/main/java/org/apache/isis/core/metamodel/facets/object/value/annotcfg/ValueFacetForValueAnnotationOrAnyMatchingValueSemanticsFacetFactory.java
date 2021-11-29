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
package org.apache.isis.core.metamodel.facets.object.value.annotcfg;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.value.semantics.DefaultsProvider;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.applib.value.semantics.ValueSemanticsResolver;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacetUsingDefaultsProvider;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.encoder.EncodableFacetUsingEncoderDecoder;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.title.parser.TitleFacetUsingValueFacet;
import org.apache.isis.core.metamodel.facets.object.value.ImmutableFacetViaValueSemantics;
import org.apache.isis.core.metamodel.facets.object.value.MaxLengthFacetUsingParser;
import org.apache.isis.core.metamodel.facets.object.value.TypicalLengthFacetUsingParser;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueFacetUsingSemanticsProvider;
import org.apache.isis.core.metamodel.facets.value.annotation.LogicalTypeFacetForValueAnnotation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Processes the {@link Value} annotation.
 *
 * <p>
 * As a result, will always install the following facets:
 * <ul>
 * <li> {@link TitleFacet} - based on the <tt>title()</tt> method if present,
 * otherwise uses <tt>toString()</tt></li>
 * <li> {@link IconFacet} - based on the <tt>iconName()</tt> method if present,
 * otherwise derived from the class name</li>
 * </ul>
 * <p>
 * In addition, the following facets may be installed:
 * <ul>
 * <li> {@link EncodableFacet} - if an {@link EncoderDecoder} has been specified
 * explicitly in the annotation (or is picked up through an external
 * configuration file)</li>
 * <li> {@link ImmutableFacet} - if specified explicitly in the annotation
 * </ul>
 * <p>
 * Note that {@link ParentedCollectionFacet} is <i>not</i> installed.
 */
@SuppressWarnings("rawtypes")
@Log4j2
public class ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory
extends FacetFactoryAbstract {

    public ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
        getServiceInjector().injectServicesInto(this);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();
        val valueIfAny = processClassContext.synthesizeOnType(Value.class);

        val logicalTypeFacetIfAny = addFacetIfPresent(
                LogicalTypeFacetForValueAnnotation
                .create(valueIfAny, cls, facetHolder));

        val logicalType = logicalTypeFacetIfAny
                .map(logicalTypeFacet->logicalTypeFacet.getLogicalType())
                .orElseGet(()->LogicalType.fqcn(cls));

        val identifier = Identifier.classIdentifier(logicalType);

        final Can<ValueSemanticsProvider> valueSemantics = _Casts.uncheckedCast(
                getValueSemanticsResolver().selectValueSemantics(identifier, cls));

        if(!valueSemantics.isEmpty()) {
            addAllFacetsForValueSemantics(valueSemantics, facetHolder);
            log.debug("found ValueSemanticsProvider for value type {}", cls);
        }

        valueIfAny
        .ifPresent(value->{

            if(valueSemantics.isCardinalityMultiple()) {
                log.warn("found multiple ValueSemanticsProvider for value type {}; using the first", cls);
            } else if(valueSemantics.isEmpty()) {
                log.warn("could not find a ValueSemanticsProvider for value type {}; ", cls);
                addAllFacetsForValueSemantics(Can.empty(), facetHolder);
            }

        });
    }

    // -- HELPER

    private void addAllFacetsForValueSemantics(
            final Can<ValueSemanticsProvider> semanticsProviders,
            final FacetHolder holder) {

        val valueFacet = new ValueFacetUsingSemanticsProvider(semanticsProviders, holder);

        addFacet(valueFacet);
        addFacet(new ImmutableFacetViaValueSemantics(holder));
        addFacet(TitleFacetUsingValueFacet.create(valueFacet, holder));

        semanticsProviders
        .forEach(semanticsProvider->{

            // install the EncodeableFacet if we've been given an EncoderDecoder
            final EncoderDecoder<?> encoderDecoder = semanticsProvider.getEncoderDecoder();
            if (encoderDecoder != null) {
                //getServiceInjector().injectServicesInto(encoderDecoder);
                //FIXME convert to using value-facet
                addFacet(new EncodableFacetUsingEncoderDecoder(encoderDecoder, holder));
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
                    addFacet(new MaxLengthFacetUsingParser(parser, holder));
                }
            }

            // install the DefaultedFacet if we've been given a DefaultsProvider
            final DefaultsProvider<?> defaultsProvider = semanticsProvider.getDefaultsProvider();
            if (defaultsProvider != null) {
                //holder.getServiceInjector().injectServicesInto(defaultsProvider);
                //FIXME convert to using value-facet
                addFacet(new DefaultedFacetUsingDefaultsProvider(defaultsProvider, holder));
            }

        });

    }

    // -- DEPENDENCIES

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final ValueSemanticsResolver valueSemanticsResolver =
        getServiceRegistry().lookupServiceElseFail(ValueSemanticsResolver.class);

}
