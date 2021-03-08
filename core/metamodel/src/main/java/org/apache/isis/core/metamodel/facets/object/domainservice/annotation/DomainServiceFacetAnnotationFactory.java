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
package org.apache.isis.core.metamodel.facets.object.domainservice.annotation;

import java.util.stream.Collectors;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;

import lombok.val;

public class DomainServiceFacetAnnotationFactory
extends FacetFactoryAbstract 
implements MetaModelRefiner {

    public DomainServiceFacetAnnotationFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }
    
    @Override
    public void setMetaModelContext(MetaModelContext metaModelContext) {
        super.setMetaModelContext(metaModelContext);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val domainServiceAnnotation = Annotations.getAnnotation(cls, DomainService.class);
        if (domainServiceAnnotation == null) {
            return;
        }
        val facetHolder = processClassContext.getFacetHolder();
        val domainServiceFacet = new DomainServiceFacetAnnotation(
                facetHolder,
                domainServiceAnnotation.nature());
        super.addFacet(domainServiceFacet);
    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        
        programmingModel.addValidatorSkipManagedBeans(new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(final ObjectSpecification thisSpec, final MetaModelValidator validator) {
                validate(thisSpec, validator);
                return true;
            }

            private void validate(
                    final ObjectSpecification objectSpec,
                    final MetaModelValidator validator) {

                if(!objectSpec.containsFacet(DomainServiceFacet.class)) {
                    return;
                }

                System.err.println("validate " + objectSpec);
                
                final String associationNames = objectSpec
                        .streamAssociations(MixedIn.EXCLUDED)
                        .map(ObjectAssociation::getName)
                        // it's okay to have an "association" called "Id" (corresponding to getId() method)
                        .filter(associationName->!"Id".equalsIgnoreCase(associationName))
                        .collect(Collectors.joining(", "));

                if(associationNames.isEmpty()) {
                    return;
                }

                validator.onFailure(
                        objectSpec,
                        objectSpec.getIdentifier(),
                        "%s: services can only have actions ('%s' config property), not properties or collections; annotate with @Programmatic if required.  Found: %s",
                        objectSpec.getFullIdentifier(),
                        "'isis.core.meta-model.validator.serviceActionsOnly'",
                        associationNames);
            }
        });

    }
    

}
