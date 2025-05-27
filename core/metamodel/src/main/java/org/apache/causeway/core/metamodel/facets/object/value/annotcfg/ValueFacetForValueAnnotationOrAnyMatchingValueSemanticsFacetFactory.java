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
package org.apache.causeway.core.metamodel.facets.object.value.annotcfg;

import java.util.Optional;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Value;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.object.defaults.DefaultedFacet;
import org.apache.causeway.core.metamodel.facets.object.defaults.DefaultedFacetFromValueFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.causeway.core.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleFacet;
import org.apache.causeway.core.metamodel.facets.object.title.parser.TitleFacetFromValueFacet;
import org.apache.causeway.core.metamodel.facets.object.value.ImmutableFacetViaValueSemantics;
import org.apache.causeway.core.metamodel.facets.object.value.MaxLengthFacetFromValueFacet;
import org.apache.causeway.core.metamodel.facets.object.value.TypicalLengthFacetFromValueFacet;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.facets.object.value.vsp.ValueFacetUsingSemanticsProvider;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.util.Facets;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
 * <li> {@link DefaultedFacet} - if a {@link ValueSemanticsProvider} has been specified
 * explicitly in the annotation (or is picked up through component registration), that provides defaults</li>
 * <li> {@link ImmutableFacet} - if specified explicitly in the annotation
 * </ul>
 * <p>
 * Note that {@link ParentedCollectionFacet} is <i>not</i> installed.
 */
@Slf4j
public class ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory
extends FacetFactoryAbstract {

    public ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
        getServiceInjector().injectServicesInto(this);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        var valueClass = processClassContext.getCls();
        var facetHolder = processClassContext.getFacetHolder();
        var valueIfAny = processClassContext.synthesizeOnType(Value.class);

        var logicalType = LogicalType.fqcn(valueClass);
        var identifier = Identifier.classIdentifier(logicalType);

        _Casts.castTo(ObjectSpecification.class, facetHolder)
        .ifPresent(valueSpec->
            addAllFacetsForValueSemantics(identifier, valueClass, valueSpec, valueIfAny));
    }

    // -- HELPER

    private <T> Optional<ValueFacet<T>> addAllFacetsForValueSemantics(
            final Identifier identifier,
            final Class<T> valueClass,
            final ObjectSpecification valueSpec,
            final Optional<Value> valueIfAny) {

        var semanticsProviders = getValueSemanticsResolver().selectValueSemantics(identifier, valueClass);
        if(semanticsProviders.isEmpty()) {
            if(valueIfAny.isPresent()) {
                log.warn("could not find a ValueSemanticsProvider for value type {}; "
                        + "the type was found to be annotated with @Value", valueClass);
                // fall through, so gets a ValueFacet
            } else {
                // don't install a ValueFacet
                return Optional.empty();
            }
        } else {
            log.debug("found {} ValueSemanticsProvider(s) for value type {}", semanticsProviders.size(), valueClass);
        }

        var valueFacet = installValueFacet(valueClass, semanticsProviders, valueSpec);
        return Optional.of(valueFacet);
    }

    public static <T> ValueFacet<T> installValueFacet(
            final Class<T> valueClass,
            final Can<ValueSemanticsProvider<T>> valueSemanticsProviders,
            final ObjectSpecification valueSpec) {

        final ValueFacet<T> valueFacet = ValueFacetUsingSemanticsProvider
                .create(valueClass, valueSemanticsProviders, valueSpec);

        valueSpec.addFacet(valueFacet);
        valueSpec.addFacet(new ImmutableFacetViaValueSemantics(valueSpec));
        valueSpec.addFacet(TitleFacetFromValueFacet.create(valueFacet, valueSpec));

        FacetUtil.addFacetIfPresent(TypicalLengthFacetFromValueFacet.create(valueFacet, valueSpec));
        FacetUtil.addFacetIfPresent(MaxLengthFacetFromValueFacet.create(valueFacet, valueSpec));
        FacetUtil.addFacetIfPresent(DefaultedFacetFromValueFacet.create(valueFacet, valueSpec));

        _Assert.assertTrue(valueSpec.valueFacet().isPresent());
        _Assert.assertTrue(valueSpec.lookupNonFallbackFacet(TitleFacet.class).isPresent());
        _Assert.assertNotNull(Facets.valueSerializerElseFail(valueSpec, valueSpec.getCorrespondingClass()));

        return valueFacet;
    }

    // -- DEPENDENCIES

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final ValueSemanticsResolver valueSemanticsResolver =
        getServiceRegistry().lookupServiceElseFail(ValueSemanticsResolver.class);

}
