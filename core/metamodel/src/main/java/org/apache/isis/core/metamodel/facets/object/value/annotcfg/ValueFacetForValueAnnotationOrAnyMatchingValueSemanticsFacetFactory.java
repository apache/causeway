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

import java.util.Optional;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.applib.value.semantics.ValueSemanticsResolver;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacet;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacetFromValueFacet;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.title.parser.TitleFacetFromValueFacet;
import org.apache.isis.core.metamodel.facets.object.value.ImmutableFacetViaValueSemantics;
import org.apache.isis.core.metamodel.facets.object.value.MaxLengthFacetFromValueFacet;
import org.apache.isis.core.metamodel.facets.object.value.TypicalLengthFacetFromValueFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueFacetUsingSemanticsProvider;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;

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
 * <li> {@link DefaultedFacet} - if a {@link ValueSemanticsProvider} has been specified
 * explicitly in the annotation (or is picked up through component registration), that provides defaults</li>
 * <li> {@link ImmutableFacet} - if specified explicitly in the annotation
 * </ul>
 * <p>
 * Note that {@link ParentedCollectionFacet} is <i>not</i> installed.
 */
@Log4j2
public class ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory
extends FacetFactoryAbstract {

    public ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
        getServiceInjector().injectServicesInto(this);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        val valueClass = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();
        val valueIfAny = processClassContext.synthesizeOnType(Value.class);

        val logicalType = LogicalType.infer(valueClass);
        val identifier = Identifier.classIdentifier(logicalType);

        val valueFacetIfAny = addAllFacetsForValueSemantics(identifier, valueClass, facetHolder, valueIfAny);

        emitValueFacetProcessed(facetHolder, _Casts.uncheckedCast(valueFacetIfAny));
    }

    // -- HELPER

    private <T> Optional<ValueFacet<T>> addAllFacetsForValueSemantics(
            final Identifier identifier,
            final Class<T> valueClass,
            final FacetHolder holder,
            final Optional<Value> valueIfAny) {

        val semanticsProviders = getValueSemanticsResolver().selectValueSemantics(identifier, valueClass);
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

        val valueFacet = ValueFacetUsingSemanticsProvider.create(valueClass, semanticsProviders, holder);

        addFacet(valueFacet);
        addFacet(new ImmutableFacetViaValueSemantics(holder));
        addFacet(TitleFacetFromValueFacet.create(valueFacet, holder));

        addFacetIfPresent(TypicalLengthFacetFromValueFacet.create(valueFacet, holder));
        addFacetIfPresent(MaxLengthFacetFromValueFacet.create(valueFacet, holder));
        addFacetIfPresent(DefaultedFacetFromValueFacet.create(valueFacet, holder));

        return Optional.of(valueFacet);
    }

    // optimization
    private void emitValueFacetProcessed(
            final FacetHolder holder,
            final Optional<ValueFacet> valueFacetIfAny) {
        _Casts.castTo(ObjectSpecificationDefault.class, holder)
        .ifPresent(receiver->receiver.onValueFacetProcessed(valueFacetIfAny));
    }

    // -- DEPENDENCIES

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final ValueSemanticsResolver valueSemanticsResolver =
        getServiceRegistry().lookupServiceElseFail(ValueSemanticsResolver.class);

}
