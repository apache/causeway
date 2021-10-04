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

import javax.inject.Inject;

import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.annotation.LogicalTypeFacetForValueAnnotation;
import org.apache.isis.schema.common.v2.ValueType;

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
@Log4j2
public class ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory
extends ValueFacetUsingSemanticsProviderFactory {

    @Inject
    public ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory(final MetaModelContext mmc) {
        super(mmc);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();
        val valueIfAny = processClassContext.synthesizeOnType(Value.class);

        addFacetIfPresent(
                LogicalTypeFacetForValueAnnotation
                .create(valueIfAny, cls, facetHolder));

        final var valueSemantics = lookupValueSemantics(cls);
        //FIXME install them all, then enable qualifiers
        if(!valueSemantics.isEmpty()) {
            super.addAllFacetsForValueSemantics(valueSemantics, facetHolder);
            log.debug("found ValueSemanticsProvider for value type {}", cls);
        }

//        if(valueIfAny.isPresent()
//                || ClassUtils.isPrimitiveOrWrapper(cls)
//                || Number.class.isAssignableFrom(cls)) {
//
//        }

        valueIfAny
        .ifPresent(value->{

            if(valueSemantics.isCardinalityMultiple()) {
                log.warn("found multiple ValueSemanticsProvider for value type {}; using the first", cls);
            } else if(valueSemantics.isEmpty()) {
                log.warn("could not find a ValueSemanticsProvider for value type {}; using a no-op fallback",cls);
                super.addAllFacetsForValueSemantics(getFallbackValueSemantics(), facetHolder);
            }

        });
    }

    // -- HELPER

    private static class NoopValueSemantics implements ValueSemanticsProvider<Object> {

        @Override
        public Renderer<Object> getRenderer() {
            return null;
        }

        @Override
        public Parser<Object> getParser() {
            return null;
        }

        @Override
        public EncoderDecoder<Object> getEncoderDecoder() {
            return null;
        }

        @Override
        public DefaultsProvider<Object> getDefaultsProvider() {
            return null;
        }

        @Override
        public Class<Object> getCorrespondingClass() {
            return null;
        }

        @Override
        public ValueType getSchemaValueType() {
            return null;
        }

    }

    @Getter(lazy = true)
    private final Can<ValueSemanticsProvider<?>> fallbackValueSemantics = Can.of(new NoopValueSemantics());

    @Getter(lazy = true)
    private final Can<ValueSemanticsProvider<?>> allValueSemanticsProviders = getServiceRegistry()
            .select(ValueSemanticsProvider.class)
            .map(_Casts::uncheckedCast);

    private <T> Can<ValueSemanticsProvider<T>> lookupValueSemantics(final Class<T> valueType) {
        var resolvableType = ResolvableType
                .forClassWithGenerics(ValueSemanticsProvider.class, ClassUtils.resolvePrimitiveIfNecessary(valueType));
        return getAllValueSemanticsProviders()
                .stream()
                .filter(resolvableType::isInstance)
                .map(provider->_Casts.<ValueSemanticsProvider<T>>uncheckedCast(provider))
                .collect(Can.toCan());
    }

}
