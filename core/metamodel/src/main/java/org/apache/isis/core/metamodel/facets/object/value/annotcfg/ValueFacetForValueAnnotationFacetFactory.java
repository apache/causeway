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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.Cardinality;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.commons.internal.reflection._Generics;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.commons.internal.reflection._Reflect.InterfacePolicy;
import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.commons.ClassUtil;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.value.ImmutableFacetViaValueSemantics;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueFacetUsingSemanticsProvider;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.annotation.LogicalTypeFacetForValueAnnotation;

import lombok.SneakyThrows;
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
 * <li> {@link ParseableFacet} - if a {@link Parser} has been specified
 * explicitly in the annotation (or is picked up through an external
 * configuration file)</li>
 * <li> {@link EncodableFacet} - if an {@link EncoderDecoder} has been specified
 * explicitly in the annotation (or is picked up through an external
 * configuration file)</li>
 * <li> {@link ImmutableFacet} - if specified explicitly in the annotation
 * </ul>
 * <p>
 * Note that {@link ParentedCollectionFacet} is <i>not</i> installed.
 */
@Log4j2
public class ValueFacetForValueAnnotationFacetFactory
extends ValueFacetUsingSemanticsProviderFactory {

    @Inject
    public ValueFacetForValueAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        final var facetHolder = processClassContext.getFacetHolder();
        final var valueIfAny = processClassContext.synthesizeOnType(Value.class);

        final var cls = processClassContext.getCls();

        addFacetIfPresent(
                LogicalTypeFacetForValueAnnotation
                .create(valueIfAny, cls, facetHolder));

        valueIfAny
        .ifPresent(value->{

            final var candidates = lookupValueSemantics(cls);
            if(candidates.isCardinalityOne()) {
                super.addAllFacetsForValueSemantics(candidates.getFirstOrFail(), facetHolder);
            } else {

                if(candidates.isCardinalityMultiple()) {
                    log.warn("found multiple ValueSemanticsProvider for value type {}; using the first", cls);
                    super.addAllFacetsForValueSemantics(candidates.getFirstOrFail(), facetHolder);
                } else {
                    log.warn("could not find a ValueSemanticsProvider for value type {}; using a no-op fallback", cls);
                    super.addAllFacetsForValueSemantics(new NoopValueSemantics(), facetHolder);
                }

            }
        });
    }

    // -- HELPER

    private static class NoopValueSemantics implements ValueSemanticsProvider<Object> {

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

    }

    private <T> Can<ValueSemanticsProvider<T>> lookupValueSemantics(final Class<T> valueType) {
        var resolvableType = ResolvableType.forClassWithGenerics(ValueSemanticsProvider.class, valueType);
        return getServiceRegistry()
                .select(ValueSemanticsProvider.class)
                .stream()
                .filter(resolvableType::isInstance)
                .map(provider->_Casts.<ValueSemanticsProvider<T>>uncheckedCast(provider))
                .collect(Can.toCan());
    }

}
