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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Domain;
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
import org.apache.isis.core.metamodel.specloader.validator.MetaModelVisitingValidatorAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.val;

/**
 * @since 2.0
 * @see org.apache.isis.applib.annotation.Domain.Include
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

        if(spec.isManagedBean()
                || spec.isAbstract()) {
            return;
        }

        final Class<?> type = spec.getCorrespondingClass();

        // assuming 'weak' equality, treating overwritten and overriding methods as same
        val recognizedMemberMethods = new TreeSet<Method>(_Reflect::methodWeakCompare);

        spec
        .streamAnyActions(MixedIn.EXCLUDED)
        .map(ObjectMemberAbstract.class::cast)
        .map(ObjectMemberAbstract::getFacetedMethod)
        .map(FacetedMethod::getMethod)
        .forEach(recognizedMemberMethods::add);

        spec
        .streamAssociations(MixedIn.EXCLUDED)
        .map(ObjectMemberAbstract.class::cast)
        .map(ObjectMemberAbstract::getFacetedMethod)
        .map(FacetedMethod::getMethod)
        .forEach(recognizedMemberMethods::add);

        // support methods known to the meta-model
        val recognizedSupportMethods = new HashSet<Method>();

        spec
        .streamFacetHolders()
        .flatMap(FacetHolder::streamFacetRankings)
        .map(facetRanking->facetRanking.getWinnerNonEvent(facetRanking.facetType()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(ImperativeFacet.class::isInstance)
        .map(ImperativeFacet.class::cast)
        .map(ImperativeFacet::getMethods)
        .flatMap(Can::stream)
        .forEach(recognizedSupportMethods::add);

        // methods intended to be included with the meta-model
        val notRecognizedMethods = _Sets.<Method>newHashSet();

        classCache
        // methods intended to be included with the meta-model but missing
        .streamDeclaredMethodsHaving(
                type,
                "domain-include",
                method->_Annotations.synthesizeInherited(method, Domain.Include.class).isPresent())
        // filter away those that are recognized
        .filter(intendedMethod->!recognizedSupportMethods.contains(intendedMethod))
        .filter(intendedMethod->!recognizedMemberMethods.contains(intendedMethod))
        .forEach(notRecognizedMethods::add);

        // find reasons about why these are not recognized
        notRecognizedMethods.forEach(notRecognizedMethod->{
            final List<String> unmetContraints = unmetContraints(spec, notRecognizedMethod);

            //FIXME[ISIS-2774] - update message to a more generic one
            String messageFormat = "%s#%s: has annotation @%s, is assumed to support "
                    + "a property, collection or action. Unmet constraint(s): %s";
            ValidationFailure.raiseFormatted(
                    spec,
                    messageFormat,
                    spec.getFeatureIdentifier().getClassName(),
                    _Reflect.methodToShortString(notRecognizedMethod),
                    "Domain.Include",
                    unmetContraints.stream()
                    .collect(Collectors.joining("; ")));
        });

    }

    // -- VALIDATION LOGIC

    private List<String> unmetContraints(
            final ObjectSpecification spec,
            final Method method) {

        //val type = spec.getCorrespondingClass();
        final List<String> unmetContraints = _Lists.<String>newArrayList();

        if (!MethodUtil.isPublic(method)) {
            unmetContraints.add("method must be 'public'");
            return unmetContraints; // don't check any further
        }

        unmetContraints.add("misspelled prefix or unsupported method signature");
        return unmetContraints;

    }

}
