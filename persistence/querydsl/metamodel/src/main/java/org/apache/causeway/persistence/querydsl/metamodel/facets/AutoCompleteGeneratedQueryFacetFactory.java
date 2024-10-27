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
 *
 */
package org.apache.causeway.persistence.querydsl.metamodel.facets;

import lombok.val;

import java.util.Optional;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;

import org.apache.causeway.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailureUtils;

import org.apache.causeway.persistence.querydsl.applib.services.support.QueryDslSupport;

import org.springframework.util.ReflectionUtils;

public class AutoCompleteGeneratedQueryFacetFactory extends FacetFactoryAbstract {

    public AutoCompleteGeneratedQueryFacetFactory(MetaModelContext metaModelContext) {
        super(metaModelContext, FeatureType.OBJECTS_AND_PROPERTIES);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {

        if (processClassContext.getFacetHolder().containsFacet(AutoCompleteUsingQueryDslFacet.class)) {
            return;
        }

        val isEntity = processClassContext.getFacetHolder().containsFacet(EntityFacet.class);
        if(!isEntity) {
            return;
        }

        val domainObjectIfAny= processClassContext.synthesizeOnType(DomainObject.class);

        val repositoryAdditionalPredicateClass = domainObjectIfAny
                .map(DomainObject::queryDslAutoCompleteAdditionalPredicateRepository)
                .filter(clz -> clz != Object.class)
                .orElse(null);

        val repositoryAdditionalPredicateObject = Optional.ofNullable(repositoryAdditionalPredicateClass)
                .flatMap(this::lookupService)
                .orElse(null);

        val repositoryAdditionalPredicateMethod = domainObjectIfAny
                .filter(x -> repositoryAdditionalPredicateClass != null)
                .map(DomainObject::queryDslAutoCompleteAdditionalPredicateMethod)
                .map(methodName -> ReflectionUtils.findMethod(repositoryAdditionalPredicateClass, methodName, String.class))
                .orElse(null);

        val limitResults = domainObjectIfAny
                .map(DomainObject::queryDslAutoCompleteLimitResults)
                .orElse(DomainObject.QueryDslAutoCompleteConstants.LIMIT_RESULTS);

        val minLength = domainObjectIfAny
                .map(DomainObject::queryDslAutoCompleteMinLength)
                .filter(x -> x > DomainObject.QueryDslAutoCompleteConstants.MIN_LENGTH)
                .orElse(DomainObject.QueryDslAutoCompleteConstants.MIN_LENGTH);

        val queryDslSupport = queryDslSupport();

        addFacet(new AutoCompleteUsingQueryDslFacet(
                processClassContext.getCls(),
                processClassContext.getFacetHolder(),
                repositoryAdditionalPredicateObject, repositoryAdditionalPredicateMethod,
                limitResults, minLength,
                queryDslSupport
        ));
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {

        val facetedMethod = processMethodContext.getFacetHolder();
        val owningType = facetedMethod.getOwningType();
        val declaringSpec = specForTypeElseFail(owningType);

        val propertyIfAny = propertyIfAny(processMethodContext);

        propertyIfAny
                .ifPresent(property -> {
                    val queryDslAutoCompletePolicy = property.queryDslAutoComplete();
                    if(queryDslAutoCompletePolicy.isIncluded()) {
                        updateAutoCompleteQueryDslFacet(declaringSpec, facetedMethod.getFeatureIdentifier().getMemberLogicalName(), queryDslAutoCompletePolicy);
                    }
                });
    }

    private void updateAutoCompleteQueryDslFacet(
            final ObjectSpecification declaringSpec,
            final String propertyId,
            final Property.QueryDslAutoCompletePolicy queryDslAutoCompletePolicy
    ) {
        // TODO: this ought to take into account if the field is persistable

        // we update the existing facet
        val autoCompleteUsingQueryDslFacet = Optional.ofNullable(declaringSpec.getFacet(AutoCompleteFacet.class))
                .filter(x -> x instanceof AutoCompleteUsingQueryDslFacet)
                .map(AutoCompleteUsingQueryDslFacet.class::cast)
                .orElseGet(() -> {
                    var newFacet = new AutoCompleteUsingQueryDslFacet(declaringSpec.getCorrespondingClass(), declaringSpec, null, null, null, null, queryDslSupport());
                    declaringSpec.addFacet(newFacet);
                    return newFacet;
                });

        autoCompleteUsingQueryDslFacet.addSearchableProperty(propertyId, queryDslAutoCompletePolicy);
    }

    Optional<Property> propertyIfAny(final ProcessMethodContext processMethodContext) {
        return processMethodContext
                .synthesizeOnMethodOrMixinType(
                        Property.class,
                        () -> ValidationFailureUtils
                                .raiseAmbiguousMixinAnnotations(processMethodContext.getFacetHolder(), Property.class));
    }

    private QueryDslSupport queryDslSupport() {
        return getMetaModelContext().lookupServiceElseFail(QueryDslSupport.class);
    }

}
