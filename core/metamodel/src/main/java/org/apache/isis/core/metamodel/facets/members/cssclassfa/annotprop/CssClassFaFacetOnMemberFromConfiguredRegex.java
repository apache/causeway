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

package org.apache.isis.core.metamodel.facets.members.cssclassfa.annotprop;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderAbstract;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFactory;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaImperativeFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaStaticFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.NonNull;

/**
 * Installed by {@link CssClassFaFacetOnMemberPostProcessor},
 * but only if no other fa-icon is declared
 * already either via layout XML or {@link ActionLayout} annotation.
 * <p>
 * Supports imperative action naming, if required.
 */
public class CssClassFaFacetOnMemberFromConfiguredRegex
extends CssClassFaImperativeFacetAbstract {

    private final @NonNull Map<Pattern, String> faIconByPattern;
    private final @NonNull MemberNamedFacet memberNamedFacet;

    /**
     * if the memberNamedFacet provides static names, we can also provide a static CssClassFaFactory
     */
    private final @NonNull Optional<CssClassFaFactory> staticCssClassFaFactory;

    public static Optional<CssClassFaFacet> create(final ObjectAction objectAction) {
        return objectAction.lookupFacet(MemberNamedFacet.class)
        .map(memberNamedFacet->new CssClassFaFacetOnMemberFromConfiguredRegex(memberNamedFacet, objectAction));
    }

    private CssClassFaFacetOnMemberFromConfiguredRegex(
            final MemberNamedFacet memberNamedFacet,
            final FacetHolder holder) {
        super(holder);
        this.faIconByPattern = getConfiguration().getApplib().getAnnotation().getActionLayout().getCssClassFa().getPatterns();
        this.memberNamedFacet = memberNamedFacet;

        // an optimization, not strictly required
        this.staticCssClassFaFactory = memberNamedFacet
                .getSpecialization()
                .left()
                .map(hasStaticName->hasStaticName.translated())
                .flatMap(this::cssClassFaFactoryForMemberFriendlyName);
    }

    @Override
    public CssClassFaFactory getCssClassFaFactory(final Supplier<ManagedObject> domainObjectProvider) {

        return staticCssClassFaFactory
        .orElseGet(()->new CssClassFaFactory() {

            @Override
            public CssClassFaPosition getPosition() {
                return cssClassFaFactoryForConfiguredRegexIfPossible(domainObjectProvider)
                        .map(CssClassFaFactory::getPosition)
                        .orElse(CssClassFaPosition.LEFT);
            }

            @Override
            public Stream<String> streamCssClasses() {
                return cssClassFaFactoryForConfiguredRegexIfPossible(domainObjectProvider)
                        .map(CssClassFaFactory::streamCssClasses)
                        .orElseGet(Stream::empty);
            }

        });
    }

    // -- HELPER

    private Optional<String> faIconIfAnyFor(final String name) {

        for (Map.Entry<Pattern, String> entry : faIconByPattern.entrySet()) {
            final Pattern pattern = entry.getKey();
            final String faIcon = entry.getValue();
            if (pattern.matcher(name).matches()) {
                return Optional.ofNullable(faIcon);
            }
        }
        return Optional.empty();
    }

    private Optional<CssClassFaFactory> cssClassFaFactoryForConfiguredRegexIfPossible(
            final Supplier<ManagedObject> domainObjectProvider) {

        final String memberFriendlyName = memberNamedFacet
        .getSpecialization()
        .fold(
                hasStaticName->hasStaticName.translated(), // unexpected code reach, due to optimization above
                hasImperativeName->hasImperativeName.textElseNull(domainObjectProvider.get()));

        return cssClassFaFactoryForMemberFriendlyName(memberFriendlyName);

    }

    /**
     * @implNote because {@link CssClassFaStaticFacetAbstract} has all the fa-icon logic,
     * we simply reuse it here by creating an anonymous instance
     */
    private Optional<CssClassFaFactory> cssClassFaFactoryForMemberFriendlyName(
            final String memberFriendlyName) {

        return _Strings.nonEmpty(memberFriendlyName)
        .flatMap(this::faIconIfAnyFor)
        .map(_faIcon->{
            final String faIcon;
            final CssClassFaPosition position;
            int idxOfSeparator = _faIcon.indexOf(':');
            if (idxOfSeparator > -1) {
                faIcon = _faIcon.substring(0, idxOfSeparator);
                String rest = faIcon.substring(idxOfSeparator + 1);
                position = CssClassFaPosition.valueOf(rest.toUpperCase());
            } else {
                faIcon = _faIcon;
                position = CssClassFaPosition.LEFT;
            }
            return new CssClassFaStaticFacetAbstract(
                    faIcon, position, FacetHolderAbstract.simple(getMetaModelContext(), null)) {};
        });

    }

}
