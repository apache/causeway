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

import javax.persistence.Entity;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.TypeExcludeMarker;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
//@Log4j2
final class IsisBeanTypeClassifierDefault
implements IsisBeanTypeClassifier {

    private final Can<String> activeProfiles;
    private final Can<IsisBeanTypeClassifier> classifierPlugins = IsisBeanTypeClassifier.get();

    // handle arbitrary types ...
    @SuppressWarnings("deprecation")
    @Override
    public IsisBeanMetaData classify(
            final @NonNull Class<?> type) {

        //debug
//        _Debug.onClassSimpleNameMatch(type, "class of interest", ()->{
//            System.err.printf("classifying %s%n", type);
//        });

        if(ClassUtils.isPrimitiveOrWrapper(type)
                || type.isEnum()) {
            return IsisBeanMetaData.notManaged(BeanSort.VALUE, type);
        }

        if(ProgrammingModelConstants.CollectionSemantics.valueOf(type).isPresent()) {
            return IsisBeanMetaData.isisManaged(BeanSort.COLLECTION, type);
        }

        if(type.isInterface()
                // modifier predicate must be called after testing for non-scalar type above,
                // otherwise we'd get false positives
                || Modifier.isAbstract(type.getModifiers())) {

            // apiNote: abstract types and interfaces cannot be vetoed
            // and should also never be identified as ENTITY, VIEWMODEL or MIXIN
            // however, concrete types that inherit abstract ones with vetoes,
            // will effectively be vetoed through means of annotation synthesis
            return IsisBeanMetaData.indifferent(BeanSort.ABSTRACT, type);
        }

        // handle vetoing ...
        if(TypeExcludeMarker.anyMatchOn(type)) {
            return IsisBeanMetaData.notManaged(BeanSort.VETOED, type); // reject
        }

        val profiles = Can.ofArray(_Annotations.synthesize(type, Profile.class)
                .map(Profile::value)
                .orElse(null));
        if(profiles.isNotEmpty()
                && !profiles.stream().anyMatch(this::isProfileActive)) {
            return IsisBeanMetaData.notManaged(BeanSort.VETOED, type); // reject
        }

        // handle value types ...

        val aValue = _Annotations.synthesize(type, org.apache.isis.applib.annotation.Value.class)
                .orElse(null);
        if(aValue!=null) {
            return IsisBeanMetaData.notManaged(BeanSort.VALUE, type);
        }

        // handle actual bean types ...

        val aDomainService = _Annotations.synthesize(type, DomainService.class);
        if(aDomainService.isPresent()) {
            val logicalType = LogicalType.infer(type);

            // whether overrides Spring naming strategy
            @SuppressWarnings("removal")
            val namedByIsis = aDomainService
                    .map(DomainService::logicalTypeName)
                    .map(_Strings::emptyToNull)
                    .map(logicalType.getLogicalTypeName()::equals)
                    .orElse(false);

            return namedByIsis
                    ? IsisBeanMetaData
                        .injectableNamedByIsis(BeanSort.MANAGED_BEAN_CONTRIBUTING, logicalType)
                    : IsisBeanMetaData
                        .injectable(BeanSort.MANAGED_BEAN_CONTRIBUTING, logicalType);
        }

        // allow ServiceLoader plugins to have a say, eg. when classifying entity types
        for(val classifier : classifierPlugins) {
            val classification = classifier.classify(type);
            if(classification!=null) {
                return classification;
            }
        }

        if(org.apache.isis.applib.ViewModel.class.isAssignableFrom(type)) {
            return IsisBeanMetaData.isisManaged(BeanSort.VIEW_MODEL, type);
        }

        val entityAnnotation = _Annotations.synthesize(type, Entity.class).orElse(null);
        if(entityAnnotation!=null) {
            return IsisBeanMetaData.isisManaged(BeanSort.ENTITY, LogicalType.infer(type));
        }

        val aDomainObject = _Annotations.synthesize(type, DomainObject.class).orElse(null);
        if(aDomainObject!=null) {
            switch (aDomainObject.nature()) {
            case BEAN:
                val logicalType = LogicalType.infer(type);
                return IsisBeanMetaData
                        .injectableNamedByIsis(BeanSort.MANAGED_BEAN_CONTRIBUTING, logicalType);
            case MIXIN:
                return IsisBeanMetaData.isisManaged(BeanSort.MIXIN, type);
            case ENTITY:
                return IsisBeanMetaData.isisManaged(BeanSort.ENTITY, type);
            case VIEW_MODEL:
            case NOT_SPECIFIED:
                //because object is not associated with a persistence context unless discovered above
                return IsisBeanMetaData.isisManaged(BeanSort.VIEW_MODEL, type);
            }
        }

        if(_Annotations.isPresent(type, Component.class)) {
            return IsisBeanMetaData.indifferent(BeanSort.MANAGED_BEAN_NOT_CONTRIBUTING, type);
        }

        if(Serializable.class.isAssignableFrom(type)) {
            return IsisBeanMetaData.indifferent(BeanSort.VALUE, type);
        }

        return IsisBeanMetaData.indifferent(BeanSort.UNKNOWN, type);
    }

    // -- HELPER

    //XXX yet this is a naive implementation, not evaluating any expression logic like eg. @Profile("!dev")
    //either we find a Spring Boot utility class that does this logic for us, or we make it clear with the
    //docs, that we have only limited support for the @Profile annotation
    private boolean isProfileActive(final String profile) {
        return activeProfiles.contains(profile);
    }

}
