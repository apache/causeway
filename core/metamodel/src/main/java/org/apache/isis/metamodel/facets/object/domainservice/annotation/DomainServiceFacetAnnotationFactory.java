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
package org.apache.isis.metamodel.facets.object.domainservice.annotation;


import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.config.internal._Config;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.metamodel.facets.Annotations;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorForValidationFailures;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.metamodel.specloader.validator.ValidationFailures;

import lombok.val;

public class DomainServiceFacetAnnotationFactory extends FacetFactoryAbstract 
implements MetaModelValidatorRefiner {

    @Deprecated
    public static final String ISIS_REFLECTOR_VALIDATOR_MIXINS_ONLY_KEY =
            "isis.reflector.validator.mixinsOnly";
    public static final boolean ISIS_REFLECTOR_VALIDATOR_MIXINS_ONLY_DEFAULT = true;

    private MetaModelValidatorForValidationFailures mixinOnlyValidator = 
            new MetaModelValidatorForValidationFailures();

    public DomainServiceFacetAnnotationFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @SuppressWarnings("deprecation") // because we specifically want to handle deprecated enum use here
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
                domainServiceAnnotation.repositoryFor(), domainServiceAnnotation.nature());
        FacetUtil.addFacet(domainServiceFacet);


        FacetUtil.addFacet(
                new IconFacetDerivedFromDomainServiceAnnotation(
                        facetHolder,
                        domainServiceAnnotation.repositoryFor()));

        val natureOfService = domainServiceFacet.getNatureOfService();
        
        // Note: mixinOnlyValidator is only added to metaModelValidator if config option
        // isis.reflector.validator.mixinsOnly == true
        // see code at the end of #refineMetaModelValidator(...)
        
        switch (natureOfService) {
        case VIEW_CONTRIBUTIONS_ONLY:
            val msg = String.format("%s: menu/contributed services (nature == %s) are prohibited ('%s' config property);"
                    + " convert into a mixin (@Mixin annotation) instead",
                    cls.getName(),
                    natureOfService,
                    ISIS_REFLECTOR_VALIDATOR_MIXINS_ONLY_KEY);
            
            mixinOnlyValidator.addFailure(Identifier.classIdentifier(cls), msg);
            break;
        default:
            // no op
        }
    }


    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator) {

        final boolean serviceActionsOnly = getConfiguration().getReflector().getValidator().isServiceActionsOnly();
        if (serviceActionsOnly) {
            metaModelValidator.add(new MetaModelValidatorVisiting(new MetaModelValidatorVisiting.Visitor() {

                @Override
                public boolean visit(final ObjectSpecification thisSpec, final ValidationFailures validationFailures) {
                    validate(thisSpec, validationFailures);
                    return true;
                }

                private void validate(
                        final ObjectSpecification thisSpec,
                        final ValidationFailures validationFailures) {

                    if(!thisSpec.containsFacet(DomainServiceFacet.class)) {
                        return;
                    }

                    final Stream<ObjectAssociation> associations = thisSpec.streamAssociations(Contributed.EXCLUDED);

                    final String associationNames = associations
                            .map(ObjectAssociation::getName)
                            // it's okay to have an "association" called "Id" (corresponding to getId() method)
                            .filter(associationName->!"Id".equalsIgnoreCase(associationName))
                            .collect(Collectors.joining(", "));

                    if(associationNames.isEmpty()) {
                        return;
                    }

                    validationFailures.add(
                            thisSpec.getIdentifier(),
                            "%s: services can only have actions ('%s' config property), not properties or collections; annotate with @Programmatic if required.  Found: %s",
                            thisSpec.getFullIdentifier(),
                            "isis.reflector.validator.serviceActionsOnly",
                            associationNames);
                }
            }));
        }

        boolean mixinsOnly = _Config.getConfiguration().getBoolean(
                ISIS_REFLECTOR_VALIDATOR_MIXINS_ONLY_KEY,
                ISIS_REFLECTOR_VALIDATOR_MIXINS_ONLY_DEFAULT);
        if (mixinsOnly) {
            metaModelValidator.add(mixinOnlyValidator);
        }

    }
    

}
