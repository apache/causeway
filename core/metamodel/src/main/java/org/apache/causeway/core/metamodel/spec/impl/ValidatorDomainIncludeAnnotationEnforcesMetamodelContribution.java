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
package org.apache.causeway.core.metamodel.spec.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.semantics.AccessorSemantics;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.MessageTemplate;
import org.apache.causeway.core.metamodel.commons.MethodUtil;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.HasFacetedMethod;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.NonNull;

/**
 * @since 2.0
 * @see org.apache.causeway.applib.annotation.Domain.Include
 */
class ValidatorDomainIncludeAnnotationEnforcesMetamodelContribution
extends MetaModelValidatorAbstract {

    private final _ClassCache classCache;

    ValidatorDomainIncludeAnnotationEnforcesMetamodelContribution(final MetaModelContext mmc) {
        super(mmc, spec->((spec instanceof ObjectSpecificationDefault)
            && !spec.isAbstract()
            && !spec.getBeanSort().isManagedBeanNotContributing()
            && !spec.isValue()));
        this.classCache = _ClassCache.getInstance();
    }

    @Override
    public void validateObjectEnter(final ObjectSpecification spec) {

        final Class<?> type = spec.getCorrespondingClass();

        // methods picked up by the framework
        var memberMethods = new TreeSet<ResolvedMethod>(ResolvedMethod::methodCompare);
        var supportMethods = new TreeSet<ResolvedMethod>(ResolvedMethod::methodCompare);

        spec
            .streamAnyActions(MixedIn.EXCLUDED)
            .map(HasFacetedMethod.class::cast)
            .map(HasFacetedMethod::getFacetedMethod)
            .map(FacetedMethod::getMethod)
            .map(MethodFacade::asMethodForIntrospection)
            .forEach(memberMethods::add);

        spec
            .streamAssociations(MixedIn.EXCLUDED)
            .map(HasFacetedMethod.class::cast)
            .map(HasFacetedMethod::getFacetedMethod)
            .map(FacetedMethod::getMethod)
            .map(MethodFacade::asMethodForIntrospection)
            .forEach(memberMethods::add);

        spec
            .streamFacetHolders()
            .flatMap(FacetHolder::streamFacetRankings)
            .map(facetRanking->facetRanking.getWinnerNonEvent(facetRanking.facetType()))
            .flatMap(Optional::stream)
            .filter(ImperativeFacet.class::isInstance)
            .map(ImperativeFacet.class::cast)
            .map(ImperativeFacet::getMethods)
            .flatMap(Can::stream)
            .map(MethodFacade::asMethodForIntrospection)
            .forEach(supportMethods::add);

        var methodsIntendedToBeIncludedButNotPickedUp = classCache
            // methods intended to be included with the meta-model but missing
            .streamDeclaredMethodsHaving(
                    type,
                    "domain-include",
                    method->
                        _Annotations.synthesize(method.method(), Domain.Include.class).isPresent())
            // filter away those that are recognized
            .filter(Predicate.not(memberMethods::contains))
            .filter(Predicate.not(supportMethods::contains))
            // filter away classic getters, that shadow record components
            .filter(spec.getCorrespondingClass().isRecord()
                ? Predicate.not(AccessorSemantics::isGetter)
                : _Predicates.alwaysTrue())
            .collect(Collectors.toCollection(HashSet::new));

        // find reasons about why these are not recognized
        methodsIntendedToBeIncludedButNotPickedUp.stream()
            .forEach(notPickedUpMethod->{
                var unmetConstraints =
                    unmetConstraints((ObjectSpecificationDefault) spec, notPickedUpMethod)
                        .stream()
                        .collect(Collectors.joining("; "));

                ValidationFailure.raiseFormatted(spec,
                        MessageTemplate.UNSATISFIED_DOMAIN_INCLUDE_SEMANTICS
                            .builder()
                            .addVariable("type", spec.getFeatureIdentifier().className())
                            .addVariable("member", _Reflect.methodToShortString(notPickedUpMethod.method()))
                            .addVariable("unmetConstraints", unmetConstraints)
                            .buildMessage());
            });

        validateOrphanedSupportingMethod(
                spec, supportMethods, memberMethods, methodsIntendedToBeIncludedButNotPickedUp);
    }

    // -- HELPER - VALIDATION LOGIC

    private List<String> unmetConstraints(
            final ObjectSpecificationDefault spec,
            final ResolvedMethod method) {

        //var type = spec.getCorrespondingClass();
        var unmetContraints = _Lists.<String>newArrayList();

        if(!spec.getIntrospectionPolicy().getEncapsulationPolicy().isEncapsulatedMembersSupported()
                && !MethodUtil.isPublic(method)) {
            unmetContraints.add("method must be 'public'");
            return unmetContraints; // don't check any further
        }

        // find any inherited methods that have Domain.Include semantics
        var inheritedMethodsWithDomainIncludeSemantics =
            _Reflect.streamInheritedMethods(method.method())
            .filter(m->!Objects.equals(method.toString(), m.toString())) // exclude self
            .filter(m->_Annotations.synthesize(m, Domain.Include.class).isPresent())
            .collect(Collectors.toSet());

        if(!inheritedMethodsWithDomainIncludeSemantics.isEmpty()) {
            unmetContraints.add("inherited method(s) having conflicting domain-include semantics: "
                    + inheritedMethodsWithDomainIncludeSemantics);
            return unmetContraints; // don't check any further
        }

        // fallback message
        unmetContraints.add("conflicting domain-include semantics, orphaned support method, "
                + "misspelled prefix or unsupported method signature");
        return unmetContraints;
    }

    private static void validateOrphanedSupportingMethod(
            final @NonNull ObjectSpecification spec,
            final @NonNull Set<ResolvedMethod> supportMethods,
            final @NonNull Set<ResolvedMethod> memberMethods,
            final @NonNull Set<ResolvedMethod> alreadyReported) {

        if(spec.isAbstract()
                || spec.getBeanSort().isManagedBeanNotContributing()
                || spec.isValue()
                || spec.getIntrospectionPolicy()
                    .getSupportMethodAnnotationPolicy()
                    .isSupportMethodAnnotationsRequired()) {
            return; // ignore
        }

        var potentialOrphans = spec instanceof ObjectSpecificationDefault specDefault
            ? specDefault.getPotentialOrphans()
            : Collections.<ResolvedMethod>emptySet();
        if(potentialOrphans.isEmpty()) return; // nothing to do

        // find reasons why these are not recognized
        potentialOrphans.stream()
            .filter(Predicate.not(alreadyReported::contains))
            .filter(Predicate.not(memberMethods::contains))
            .filter(Predicate.not(supportMethods::contains))
            .forEach(orphanedMethod->{

                var methodIdentifier = Identifier
                        .methodIdentifier(spec.getFeatureIdentifier().logicalType(), orphanedMethod);

                ValidationFailure.raise(
                        spec,
                        ProgrammingModelConstants.MessageTemplate.ORPHANED_METHOD
                            .builder()
                            .addVariablesFor(methodIdentifier)
                            .buildMessage());
            });

        potentialOrphans.clear(); // no longer needed
    }

}
