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


import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

public class DomainServiceFacetAnnotationFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    public static final String ISIS_REFLECTOR_VALIDATOR_SERVICE_ACTIONS_ONLY_KEY =
            "isis.reflector.validator.serviceActionsOnly";
    public static final boolean ISIS_REFLECTOR_VALIDATOR_SERVICE_ACTIONS_ONLY_DEFAULT = false;

    public DomainServiceFacetAnnotationFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainService annotation = Annotations.getAnnotation(cls, DomainService.class);
        if (annotation == null) {
            return;
        }
        FacetUtil.addFacet(
                new DomainServiceFacetAnnotation(
                        processClassContext.getFacetHolder(),
                        annotation.repositoryFor(), annotation.nature()));
        FacetUtil.addFacet(
                new IconFacetDerivedFromDomainServiceAnnotation(
                        processClassContext.getFacetHolder(),
                        annotation.repositoryFor()));
    }


    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {

        boolean serviceActionsOnly = configuration.getBoolean(
                ISIS_REFLECTOR_VALIDATOR_SERVICE_ACTIONS_ONLY_KEY,
                ISIS_REFLECTOR_VALIDATOR_SERVICE_ACTIONS_ONLY_DEFAULT);
        if(!serviceActionsOnly) {
            return;
        }

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

                final List<String> associationNames = Lists.newArrayList();

                final List<ObjectAssociation> associations = thisSpec.getAssociations(Contributed.EXCLUDED);
                for (ObjectAssociation association: associations) {
                    final String associationName = association.getName();
                    // it's okay to have an "association" called "Id" (corresponding to getId() method)
                    if("Id".equalsIgnoreCase(associationName)) {
                        continue;
                    }
                    associationNames.add(associationName);
                }

                if(associationNames.isEmpty()) {
                    return;
                }

                validationFailures.add(
                        "%s: services can only have actions, not properties or collections (annotate with @Programmatic if required).  Found: %s",
                        thisSpec.getFullIdentifier(),
                        Joiner.on(", ").join(associationNames));
            }
        }));
    }

}
