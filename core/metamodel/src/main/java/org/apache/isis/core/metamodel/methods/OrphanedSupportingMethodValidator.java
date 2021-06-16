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
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelVisitingValidatorAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.NonNull;
import lombok.val;

/**
 *
 * @since 2.0
 *
 */
public class OrphanedSupportingMethodValidator
extends MetaModelVisitingValidatorAbstract {

    @Inject
    public OrphanedSupportingMethodValidator(final MetaModelContext mmc) {
        super(mmc);
    }

    @Override
    public void validate(@NonNull final ObjectSpecification spec) {

        if(!(spec instanceof ObjectSpecificationAbstract)) {
            return; // continue
        }

        if(spec.isAbstract()) {
            return; // continue - we don't care about abstract types
        }

        val potentialOrphans = ((ObjectSpecificationAbstract) spec).getPotentialOrphans();
        if(potentialOrphans.isEmpty()) {
            return; // continue
        }

        // methods known to the meta-model
        val recognizedMethods = spec.streamFacetHolders()
                .flatMap(FacetHolder::streamFacets)
                .filter(ImperativeFacet.class::isInstance)
                .map(ImperativeFacet.class::cast)
                .map(ImperativeFacet::getMethods)
                .flatMap(Can::stream)
                .collect(Collectors.toCollection(HashSet::new));

        // methods intended to be included with the meta-model but missing
        val notRecognizedMethods =
                _Sets.minus(potentialOrphans, recognizedMethods);

        // find reasons why these are not recognized
        notRecognizedMethods.forEach(notRecognizedMethod->{

            val unmetContraints = unmetContraints(spec, notRecognizedMethod);

            val messageFormat = "%s#%s: is assumed to support "
                    + "a property, collection or action. Unmet constraint(s): %s";

            ValidationFailure.raiseFormatted(
                    spec,
                    String.format(
                            messageFormat,
                            spec.getFeatureIdentifier().getClassName(),
                            notRecognizedMethod.getName(),
                            unmetContraints.stream()
                            .collect(Collectors.joining("; "))));
        });

        potentialOrphans.clear(); // no longer needed

    }

    // -- VALIDATION LOGIC

    private List<String> unmetContraints(
            final ObjectSpecification spec,
            final Method method) {

        val unmetContraints = _Lists.<String>newArrayList();

        unmetContraints.add("unsupported method signature or "
                + "orphaned (not associated with a member)");
        return unmetContraints;

    }





}
