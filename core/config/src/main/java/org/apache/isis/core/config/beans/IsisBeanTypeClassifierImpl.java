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
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Locale;

import org.checkerframework.checker.nullness.qual.Nullable;
import javax.enterprise.inject.Vetoed;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.util.LogicalTypeNameUtil;

import static org.apache.isis.commons.internal.reflection._Annotations.findNearestAnnotation;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
//@Log4j2
final class IsisBeanTypeClassifierImpl
implements IsisBeanTypeClassifier {

    private final Can<String> activeProfiles;
    private final Can<IsisBeanTypeClassifier> classifierPlugins = IsisBeanTypeClassifier.get();

    @Override
    public BeanClassification classify(
            final @NonNull Class<?> type,
            final @Nullable BeanClassificationContext beanClassificationContext) {

        // handle arbitrary types ...

        if(type.isPrimitive()) {
            return BeanClassification.delegated(BeanSort.VALUE);
        }

        if(Collection.class.isAssignableFrom(type)
                || Can.class.isAssignableFrom(type)
                || type.isArray()) {
            return BeanClassification.selfManaged(BeanSort.COLLECTION);
        }

        if(type.isInterface()
                // modifier predicate must be called after testing for non-scalar type above,
                // otherwise we'd get false positives
                || Modifier.isAbstract(type.getModifiers())) {

            // apiNote: abstract types and interfaces cannot be vetoed
            // and should also never be identified as ENTITY, VIEWMODEL or MIXIN
            // however, concrete types that inherit abstract ones with vetoes,
            // will effectively be vetoed through means of annotation synthesis
            return BeanClassification.delegated(BeanSort.ABSTRACT);
        }

        // handle vetoing ...

        if(findNearestAnnotation(type, Vetoed.class).isPresent()
                || findNearestAnnotation(type, Programmatic.class).isPresent()) {
            return BeanClassification.selfManaged(BeanSort.VETOED); // reject
        }

        val profiles = Can.ofArray(findNearestAnnotation(type, Profile.class)
                .map(Profile::value)
                .orElse(null));
        if(profiles.isNotEmpty()
                && !profiles.stream().anyMatch(this::isProfileActive)) {
            return BeanClassification.selfManaged(BeanSort.VETOED); // reject
        }

        // handle value types ...

        if(beanClassificationContext!=null
                && beanClassificationContext.getIsRegisteredValueType().test(type)) {
            return BeanClassification.delegated(BeanSort.VALUE);
        }

        val aValue = findNearestAnnotation(type, org.apache.isis.applib.annotation.Value.class)
                .orElse(null);
        if(aValue!=null) {
            return BeanClassification.delegated(BeanSort.VALUE);
        }

        // handle actual bean types ...

        val aDomainService = findNearestAnnotation(type, DomainService.class);
        if(aDomainService.isPresent()) {
            return BeanClassification
                    .delegated(BeanSort.MANAGED_BEAN_CONTRIBUTING,
                            LogicalTypeNameUtil.logicalTypeName(aDomainService.get()));
        }

        // allow ServiceLoader plugins to have a say, eg. when classifying entity types
        for(val classifier : classifierPlugins) {
            val classification = classifier.classify(type, beanClassificationContext);
            if(classification!=null) {
                return classification;
            }
        }

        if(org.apache.isis.applib.ViewModel.class.isAssignableFrom(type)) {
            return BeanClassification.selfManaged(BeanSort.VIEW_MODEL);
        }

        val entityAnnotation = findNearestAnnotation(type, Entity.class).orElse(null);
        if(entityAnnotation!=null) {

            String logicalTypeName = null;

            val aDomainObject = findNearestAnnotation(type, DomainObject.class).orElse(null);
            if(aDomainObject!=null) {
                logicalTypeName = LogicalTypeNameUtil.logicalTypeName(aDomainObject);
            }

            // don't trample over the @DomainObject(logicalTypeName=..) if present
            if(_Strings.isEmpty(logicalTypeName)) {
                val aTable = findNearestAnnotation(type, Table.class).orElse(null);
                if(aTable!=null) {
                    val schema = aTable.schema();
                    if(_Strings.isNotEmpty(schema)) {
                        val table = aTable.name();
                        logicalTypeName = String.format("%s.%s", schema.toLowerCase(Locale.ROOT),
                                _Strings.isNotEmpty(table)
                                    ? table
                                    : type.getSimpleName());
                    }
                }
            }

            if(_Strings.isNotEmpty(logicalTypeName)) {
                BeanClassification.selfManaged(
                        BeanSort.ENTITY, logicalTypeName);
            }
            return BeanClassification.selfManaged(BeanSort.ENTITY);
        }

        val aDomainObject = findNearestAnnotation(type, DomainObject.class).orElse(null);
        if(aDomainObject!=null) {
            switch (aDomainObject.nature()) {
            case BEAN:
                return BeanClassification.delegated(
                        BeanSort.MANAGED_BEAN_CONTRIBUTING,
                        LogicalTypeNameUtil.logicalTypeName(aDomainObject));
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

        if(Serializable.class.isAssignableFrom(type)) {
            return BeanClassification.delegated(BeanSort.VALUE);
        }

        return BeanClassification.delegated(BeanSort.UNKNOWN);
    }

    // -- HELPER

    //XXX yet this is a naive implementation, not evaluating any expression logic like eg. @Profile("!dev")
    //either we find a Spring Boot utility class that does this logic for us, or we make it clear with the
    //docs, that we have only limited support for the @Profile annotation
    private boolean isProfileActive(final String profile) {
        return activeProfiles.contains(profile);
    }

}