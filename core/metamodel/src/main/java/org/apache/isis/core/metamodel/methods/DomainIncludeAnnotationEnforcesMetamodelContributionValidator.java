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
package org.apache.isis.core.metamodel.methods;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.Domain;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.commons.internal.reflection._ClassCache;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.metamodel.commons.MethodUtil;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectMemberAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelVisitingValidatorAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.val;

/**
 * @since 2.0
 * @see org.apache.isis.applib.annotations.Domain.Include
 */
public class DomainIncludeAnnotationEnforcesMetamodelContributionValidator
extends MetaModelVisitingValidatorAbstract {

    private final _ClassCache classCache;

    @Inject
    public DomainIncludeAnnotationEnforcesMetamodelContributionValidator(final MetaModelContext mmc) {
        super(mmc);
        this.classCache = _ClassCache.getInstance();
    }

    @Override
    public void validate(final ObjectSpecification spec) {

        if(!(spec instanceof ObjectSpecificationAbstract)
                || spec.isAbstract()
                || spec.getBeanSort().isManagedBeanNotContributing()
                || spec.isValue()) {
            return;
        }

        final Class<?> type = spec.getCorrespondingClass();

        // methods picked up by the framework
        // assuming 'weak' equality, treating overwritten and overriding methods as same
        val memberMethods = new TreeSet<Method>(_Reflect::methodWeakCompare);
        val supportMethods = new TreeSet<Method>(_Reflect::methodWeakCompare);

        spec
        .streamAnyActions(MixedIn.EXCLUDED)
        .map(ObjectMemberAbstract.class::cast)
        .map(ObjectMemberAbstract::getFacetedMethod)
        .map(FacetedMethod::getMethod)
        .forEach(memberMethods::add);

        spec
        .streamAssociations(MixedIn.EXCLUDED)
        .map(ObjectMemberAbstract.class::cast)
        .map(ObjectMemberAbstract::getFacetedMethod)
        .map(FacetedMethod::getMethod)
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
        .forEach(supportMethods::add);

        val methodsIntendedToBeIncludedButNotPickedUp = _Sets.<Method>newHashSet();

        classCache
        // methods intended to be included with the meta-model but missing
        .streamDeclaredMethodsHaving(
                type,
                "domain-include",
                method->_Annotations.synthesizeInherited(method, Domain.Include.class).isPresent())
        // filter away those that are recognized
        .filter(Predicate.not(memberMethods::contains))
        .filter(Predicate.not(supportMethods::contains))
        .forEach(methodsIntendedToBeIncludedButNotPickedUp::add);

        // find reasons about why these are not recognized
        methodsIntendedToBeIncludedButNotPickedUp
        .forEach(notPickedUpMethod->{
            val unmetContraints =
                    unmetContraints((ObjectSpecificationAbstract) spec, notPickedUpMethod)
                    .stream()
                    .collect(Collectors.joining("; "));

            //FIXME[ISIS-2774] - update message to a more generic one
            ValidationFailure.raiseFormatted(spec,
                    "%s#%s: has annotation @%s, is assumed to support "
                            + "a property, collection or action. Unmet constraint(s): %s",
                    spec.getFeatureIdentifier().getClassName(),
                    _Reflect.methodToShortString(notPickedUpMethod),
                    "Domain.Include",
                    unmetContraints);
        });

        _OrphanedSupportingMethodValidator.validate((ObjectSpecificationAbstract)spec,
                supportMethods, memberMethods, methodsIntendedToBeIncludedButNotPickedUp);

    }

    // -- HELPER - VALIDATION LOGIC

    private List<String> unmetContraints(
            final ObjectSpecificationAbstract spec,
            final Method method) {

        //val type = spec.getCorrespondingClass();
        val unmetContraints = _Lists.<String>newArrayList();

        if(!spec.getIntrospectionPolicy().getEncapsulationPolicy().isEncapsulatedMembersSupported()
                && !MethodUtil.isPublic(method)) {
            unmetContraints.add("method must be 'public'");
            return unmetContraints; // don't check any further
        }

        unmetContraints.add("misspelled prefix or unsupported method signature");
        return unmetContraints;

    }



}
