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
package org.apache.causeway.core.metamodel.facets.members.iconfa.annotprop;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.base._StableValue;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaImperativeFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaLayersProvider;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaStaticFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaStaticFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.postprocessors.all.CssOnActionFromConfiguredRegexPostProcessor;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

/**
 * Installed by {@link CssOnActionFromConfiguredRegexPostProcessor},
 * but only if no other fa-icon is declared
 * already either via layout XML or {@link ActionLayout} annotation.
 *
 * <p>Supports imperative action naming, if required.
 */
public record FaFacetOnMemberFromConfiguredRegex(
    ObjectSpecification objectSpecification,
    Map<Pattern, String> faIconByPattern,
    MemberNamedFacet memberNamedFacet,
    /**
     * If the memberNamedFacet provides static names,
     * we can also provide a static {@link FaLayersProvider}.
     */
    _StableValue<Optional<FaLayersProvider>> staticCssClassFaFactoryRef,

    FacetHolder facetHolder
) implements FaImperativeFacet {

    @Override public Class<? extends Facet> facetType() { return FaFacet.class; }
    @Override public Precedence precedence() { return Precedence.DEFAULT; }

    @Override
    public Either<FaStaticFacet, FaImperativeFacet> getSpecialization() {
        return Either.right(this);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("facet", ClassUtils.getShortName(getClass()));
        visitor.accept("precedence", precedence().name());
        visitor.accept("position", "!imperative");
        visitor.accept("classes", "!imperative");
    }

    public static Optional<FaFacet> create(
            final ObjectSpecification objectSpecification,
            final ObjectAction objectAction) {
        return objectAction.lookupFacet(MemberNamedFacet.class)
            .map(memberNamedFacet->
                new FaFacetOnMemberFromConfiguredRegex(
                    objectSpecification, memberNamedFacet, objectAction));
    }

    // non-canonical constructor
    private FaFacetOnMemberFromConfiguredRegex(
            final ObjectSpecification objectSpecification,
            final MemberNamedFacet memberNamedFacet,
            final FacetHolder facetHolder) {
        this(
            objectSpecification,
            objectSpecification.getConfiguration().applib().annotation().actionLayout().cssClassFa().patternsAsMap(),
            memberNamedFacet,
            new _StableValue<>(),
            facetHolder);
    }

    @Override
    public FaLayersProvider getFaLayersProvider(final ManagedObject domainObject) {
        var staticCssClassFaFactory = staticCssClassFaFactoryRef.orElseSet(()->
            memberNamedFacet
                .getSpecialization()
                .left()
                .map(hasStaticName->hasStaticName.translated())
                .flatMap(this::faLayersProviderForMemberFriendlyName));
        return staticCssClassFaFactory
            .orElse(() -> faLayersProviderForConfiguredRegexIfPossible(domainObject)
                .map(FaLayersProvider::getLayers)
                .orElseGet(FontAwesomeLayers::empty));
    }

    // -- HELPER

    private Optional<String> faIconForName(final String name) {
        for (Map.Entry<Pattern, String> entry : faIconByPattern.entrySet()) {
            final Pattern pattern = entry.getKey();
            final String faIcon = entry.getValue();
            if (pattern.matcher(name).matches()) {
                return _Strings.nonEmpty(faIcon);
            }
        }
        return Optional.empty();
    }

    private Optional<FaLayersProvider> faLayersProviderForConfiguredRegexIfPossible(
            final ManagedObject domainObject) {

        final String memberFriendlyName = memberNamedFacet
        .getSpecialization()
        .fold(
                hasStaticName->hasStaticName.translated(), // unexpected code reach, due to optimization above
                hasImperativeName->hasImperativeName.textElseNull(targetFor(domainObject)));

        return faLayersProviderForMemberFriendlyName(memberFriendlyName);
    }

    private ManagedObject targetFor(final ManagedObject domainObject) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(domainObject)) {
            return ManagedObject.empty(objectSpecification);
        }
        return objectSpecification.isMixin()
                ? ManagedObject.mixin(
                        objectSpecification,
                        objectSpecification
                            .getFactoryService()
                            .mixin(objectSpecification.getCorrespondingClass(), domainObject.getPojo()))
                : domainObject;
    }

    private Optional<FaLayersProvider> faLayersProviderForMemberFriendlyName(
            final String memberFriendlyName) {

        return _Strings.nonEmpty(memberFriendlyName)
            .flatMap(this::faIconForName)
            .map(faIcon->{
                final String faCssClasses;
                final CssClassFaPosition position;
                int idxOfSeparator = faIcon.indexOf(':');
                if (idxOfSeparator > -1) {
                    faCssClasses = faIcon.substring(0, idxOfSeparator);
                    String rest = faCssClasses.substring(idxOfSeparator + 1);
                    position = CssClassFaPosition.valueOf(rest.toUpperCase());
                } else {
                    faCssClasses = faIcon;
                    position = CssClassFaPosition.LEFT;
                }
                return faIconProvider(faCssClasses, position);
            });
    }

    /**
     * @implNote because {@link FaStaticFacetAbstract} has all the fa-icon logic,
     * we simply reuse it here by creating an anonymous instance
     */
    private static FaLayersProvider faIconProvider(final String faIcon, final CssClassFaPosition position) {
        return new FaStaticFacetAbstract(
                faIcon, position, null) {};
    }

}
