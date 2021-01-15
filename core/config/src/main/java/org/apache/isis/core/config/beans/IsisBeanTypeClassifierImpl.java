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
package org.apache.isis.core.config.beans;

import java.io.Serializable;
import java.util.Collection;

import javax.enterprise.inject.Vetoed;
import javax.persistence.Entity;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;

import static org.apache.isis.commons.internal.base._With.requires;
import static org.apache.isis.commons.internal.reflection._Annotations.findNearestAnnotation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = AccessLevel.PACKAGE) 
//@Log4j2
final class IsisBeanTypeClassifierImpl 
implements IsisBeanTypeClassifier {

    private final Can<IsisBeanTypeClassifier> classifierPlugins = IsisBeanTypeClassifier.get();
    
    @Override
    public BeanClassification classify(Class<?> type) {

        requires(type, "type");
        
        if(findNearestAnnotation(type, Vetoed.class).isPresent()) {
            return BeanClassification.selfManaged(BeanSort.UNKNOWN); // reject
        }

        val aDomainService = findNearestAnnotation(type, DomainService.class);
        if(aDomainService.isPresent()) {
            return BeanClassification
                    .delegated(BeanSort.MANAGED_BEAN_CONTRIBUTING, objectType(aDomainService.get()));
        }

        // allow ServiceLoader plugins to have a say, eg. when classifying entity types
        for(val classifier : classifierPlugins) {
            val classification = classifier.classify(type);
            if(classification!=null) {
                return classification;
            }
        }

        if(org.apache.isis.applib.ViewModel.class.isAssignableFrom(type)) {
            return BeanClassification.selfManaged(BeanSort.VIEW_MODEL);
        }

        val entityAnnotation = findNearestAnnotation(type, Entity.class).orElse(null);
        if(entityAnnotation!=null) {
            return BeanClassification.selfManaged(BeanSort.ENTITY);
        }
        
        val aDomainObject = findNearestAnnotation(type, DomainObject.class).orElse(null);
        if(aDomainObject!=null) {
            switch (aDomainObject.nature()) {
            case BEAN:
                return BeanClassification.delegated(BeanSort.MANAGED_BEAN_CONTRIBUTING, objectType(aDomainObject));
            case MIXIN:
                return BeanClassification.selfManaged(BeanSort.MIXIN);
            case ENTITY:
                return BeanClassification.selfManaged(BeanSort.ENTITY);
            case VIEW_MODEL:
            case NOT_SPECIFIED:
                //because object is not associated with a persistence context unless discovered above
                return BeanClassification.selfManaged(BeanSort.VIEW_MODEL);
            } 
        }

        if(findNearestAnnotation(type, Component.class).isPresent()) {
            return BeanClassification.delegated(BeanSort.MANAGED_BEAN_NOT_CONTRIBUTING);
        }
        
        if(Collection.class.isAssignableFrom(type)
                || Can.class.isAssignableFrom(type)
                || type.isArray()) {
            return BeanClassification.selfManaged(BeanSort.COLLECTION);
        }

        if(Serializable.class.isAssignableFrom(type)) {
            return BeanClassification.delegated(BeanSort.VALUE);
        }

        return BeanClassification.delegated(BeanSort.UNKNOWN);
    }
    
    // -- HELPER

    private String objectType(DomainService aDomainService) {
        if(aDomainService!=null) {
            val objectType = aDomainService.objectType();
            if(_Strings.isNotEmpty(objectType)) {
                return objectType; 
            }
        }
        return null;
    }

    private String objectType(DomainObject aDomainObject) {
        if(aDomainObject!=null) {
            val objectType = aDomainObject.objectType();
            if(_Strings.isNotEmpty(objectType)) {
                return objectType; 
            }
        }
        return null;
    }


}