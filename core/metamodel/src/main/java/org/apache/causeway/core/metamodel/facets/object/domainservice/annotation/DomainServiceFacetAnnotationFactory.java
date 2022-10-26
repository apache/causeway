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
package org.apache.causeway.core.metamodel.facets.object.domainservice.annotation;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.val;

public class DomainServiceFacetAnnotationFactory
extends FacetFactoryAbstract
implements MetaModelRefiner {

    @Inject
    public DomainServiceFacetAnnotationFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        val domainServiceIfAny = processClassContext.synthesizeOnType(DomainService.class);
        if (!domainServiceIfAny.isPresent()) {
            return;
        }
        val facetHolder = processClassContext.getFacetHolder();
        addFacet(
                new DomainServiceFacetForAnnotation(
                        facetHolder,
                        domainServiceIfAny.get().nature()));

        addFacetIfPresent(
                AliasedFacetForDomainServiceAnnotation
                    .create(domainServiceIfAny, processClassContext.getCls(), facetHolder));
    }

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {

        programmingModel.addVisitingValidatorSkipManagedBeans(spec->{

            if(!spec.containsFacet(DomainServiceFacet.class)) {
                return;
            }

            final String associationNames = spec
                    .streamAssociations(MixedIn.EXCLUDED)
                    .map(ObjectAssociation::getId)
//                    // it's okay to have an "association" called "Id" (corresponding to getId() method)
//                    .filter(associationName->!"Id".equalsIgnoreCase(associationName))
                    .collect(Collectors.joining(", "));

            if(associationNames.isEmpty()) {
                return;
            }

            ValidationFailure.raiseFormatted(
                    spec,
                    "%s: services can only have actions ('%s' config property), "
                    + "not properties or collections; "
                    + "annotate with @Programmatic if required. Found: %s",
                    spec.getFullIdentifier(),
                    "'causeway.core.meta-model.validator.serviceActionsOnly'",
                    associationNames);
        });

    }


}
