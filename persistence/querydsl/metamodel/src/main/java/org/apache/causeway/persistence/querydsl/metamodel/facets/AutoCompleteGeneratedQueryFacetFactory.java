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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Optional;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.ObjectTypeFacetFactory;

import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;

import org.springframework.util.ReflectionUtils;


public class AutoCompleteGeneratedQueryFacetFactory extends FacetFactoryAbstract implements ObjectTypeFacetFactory {

    public AutoCompleteGeneratedQueryFacetFactory(MetaModelContext metaModelContext) {
        super(metaModelContext, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessObjectTypeContext processClassContext) {


        if (processClassContext.getFacetHolder().containsFacet(AutoCompleteGeneratedQueryFacet.class)) {
            return;
        }

        val isEntity = processClassContext.getFacetHolder().containsFacet(EntityFacet.class);
        if(!isEntity) {
            return;
        }

        val domainObjectIfAny= processClassContext.synthesizeOnType(DomainObject.class);

        // TODO: this ought to only look at the properties, rather than every field of the class.
        //  perhaps it should be refactored into a processing on Property, and then use a Post processor to add in the
        //  autoCompleteQueryFacet on the class afterwards
        //  should also take into account if the field is persistable
        val fields = new ArrayList<Field>();
        ReflectionUtils.doWithFields(processClassContext.getCls(), field -> {
            val propertyIfAny = _Annotations.synthesize(field, Property.class);
            propertyIfAny
                    .filter(property -> property.queryDslAutoComplete().isIncluded())
                    .ifPresent(property -> {
                        fields.add(field);
            });
        });

        // If no fields with AutoComplete annotation found, search for string fields that have a getter
        // TODO: should use the metamodel instead here, look for presence of AccessorFacet.
        if (fields.isEmpty()) {
            ReflectionUtils.doWithFields(processClassContext.getCls(), field -> {
                if (getClassCache().getterForField(processClassContext.getCls(), field).isPresent() &&
                        field.getType() == String.class)
                    fields.add(field);
            });
        }

        if (!fields.isEmpty()) {

            final Class<?> repositoryClass = domainObjectIfAny
                    .map(DomainObject::queryDslAutoCompleteAdditionalPredicateRepository)
                    .filter(clz -> clz != Object.class)
                    .orElse(null);

            final Object repository = Optional.ofNullable(repositoryClass)
                    .map(this::lookupService)
                    .orElse(null);

            final Method method = domainObjectIfAny
                    .filter(x -> repositoryClass != null)
                    .map(DomainObject::queryDslAutoCompleteAdditionalPredicateMethod)
                    .map(methodName -> ReflectionUtils.findMethod(repositoryClass, methodName, String.class))
                    .orElse(null);

            final Integer limit = domainObjectIfAny
                    .map(DomainObject::queryDslAutoCompleteLimitResults)
                    .orElse(DomainObject.QueryDslAutoCompleteConstants.LIMIT_RESULTS);

            final Integer minLength = domainObjectIfAny
                    .map(DomainObject::queryDslAutoCompleteMinLength)
                    .filter(x -> x > DomainObject.QueryDslAutoCompleteConstants.MIN_LENGTH)
                    .orElse(DomainObject.QueryDslAutoCompleteConstants.MIN_LENGTH);

            addFacet(new AutoCompleteGeneratedQueryFacet(processClassContext.getCls(),
                    processClassContext.getFacetHolder(), fields, repository, method, limit, minLength));
        }
    }

}
