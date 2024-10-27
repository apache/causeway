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
package org.apache.causeway.core.metamodel.methods;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.MessageTemplate;
import org.apache.causeway.core.metamodel.commons.MethodUtil;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectMemberAbstract;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

/**
 * @since 2.0
 * @see org.apache.causeway.applib.annotation.Domain.Include
 */
public class DomainIncludeAnnotationEnforcesMetamodelContributionValidator
extends MetaModelValidatorAbstract {

    private final _ClassCache classCache;

    @Inject
    public DomainIncludeAnnotationEnforcesMetamodelContributionValidator(final MetaModelContext mmc) {
        super(mmc, spec->((spec instanceof ObjectSpecificationAbstract) && !spec.isAbstract() && !spec.getBeanSort().isManagedBeanNotContributing() && !spec.isValue()));
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
        .map(ObjectMemberAbstract.class::cast)
        .map(ObjectMemberAbstract::getFacetedMethod)
        .map(FacetedMethod::getMethod)
        .map(MethodFacade::asMethodForIntrospection)
        .forEach(memberMethods::add);

        spec
        .streamAssociations(MixedIn.EXCLUDED)
        .map(ObjectMemberAbstract.class::cast)
        .map(ObjectMemberAbstract::getFacetedMethod)
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

        var methodsIntendedToBeIncludedButNotPickedUp = _Sets.<ResolvedMethod>newHashSet();

        classCache
        // methods intended to be included with the meta-model but missing
        .streamDeclaredMethodsHaving(
                type,
                "domain-include",
                method->
                    _Annotations.synthesize(method.method(), Domain.Include.class).isPresent())
        // filter away those that are recognized
        .filter(Predicate.not(memberMethods::contains))
        .filter(Predicate.not(supportMethods::contains))
        //[CAUSEWAY-3571] perhaps no longer required, because we only collect resolvable methods ...
        // special lookup for generic type bounds
//        .filter(method->{
//            if(_Reflect.hasGenericBounds(method)) {
//                if(memberMethods.stream().anyMatch(m->_Reflect.methodsWeaklySame(m, method))) {
//                    return false; // found
//                }
//                if(supportMethods.stream().anyMatch(m->_Reflect.methodsWeaklySame(m, method))) {
//                    return false; // found
//                }
//            }
//            return true; // pass-through
//        })
        .forEach(methodsIntendedToBeIncludedButNotPickedUp::add);

        // find reasons about why these are not recognized
        methodsIntendedToBeIncludedButNotPickedUp.stream()
        .forEach(notPickedUpMethod->{
            var unmetContraints =
                    unmetContraints((ObjectSpecificationAbstract) spec, notPickedUpMethod)
                    .stream()
                    .collect(Collectors.joining("; "));

            ValidationFailure.raiseFormatted(spec,
                    MessageTemplate.UNSATISFIED_DOMAIN_INCLUDE_SEMANTICS
                        .builder()
                        .addVariable("type", spec.getFeatureIdentifier().getClassName())
                        .addVariable("member", _Reflect.methodToShortString(notPickedUpMethod.method()))
                        .buildMessage()

                    + " Unmet constraint(s): %s",
                    unmetContraints);
        });

        _OrphanedSupportingMethodValidator.validate((ObjectSpecificationAbstract)spec,
                supportMethods, memberMethods, methodsIntendedToBeIncludedButNotPickedUp);

    }

    // -- HELPER - VALIDATION LOGIC

    private List<String> unmetContraints(
            final ObjectSpecificationAbstract spec,
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

}
