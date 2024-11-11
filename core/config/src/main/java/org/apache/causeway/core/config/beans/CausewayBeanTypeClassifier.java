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
package org.apache.causeway.core.config.beans;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Supplier;

import jakarta.persistence.Entity;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.annotations.BeanInternal;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.TypeExcludeMarker;

import lombok.NonNull;

@BeanInternal
public record CausewayBeanTypeClassifier(
        @NonNull Can<String> activeProfiles,
        @NonNull Can<CausewayBeanTypeClassifierSpi> classifierPlugins,
        @NonNull _ClassCache classCache) {
    
    public enum Attributes {
        /**
         * Corresponds to presence of a {@link DomainService} annotation.
         * @see _ClassCache#lookupAttribute(Class, String)
         */
        HAS_DOMAIN_SERVICE_SEMANTICS,

        /**
         * Corresponds to {@link DomainObject#mixinMethod()}.
         * @see _ClassCache#lookupAttribute(Class, String)
         */
        MIXIN_MAIN_METHOD_NAME;

        public void set(final _ClassCache classCache, final Class<?> type, final String attributeValue) {
            classCache.setAttribute(type, this.name(), attributeValue);
        }
        public Optional<String> lookup(final _ClassCache classCache, final Class<?> type) {
            return classCache.lookupAttribute(type, this.name());
        }
    }
    
    // -- CONSTRUCTION
    
    CausewayBeanTypeClassifier(final ApplicationContext applicationContext) {
        this(Can.ofArray(applicationContext.getEnvironment().getActiveProfiles()));
    }
    
    CausewayBeanTypeClassifier(Can<String> activeProfiles) {
        this(activeProfiles, 
                Can.ofCollection(SpringFactoriesLoader
                        .loadFactories(CausewayBeanTypeClassifierSpi.class, _Context.getDefaultClassLoader())), 
                _ClassCache.getInstance());
    }

    public CausewayBeanMetaData classify(final @NonNull LogicalType logicalType, boolean alreadyInferred) {
        
        var type = logicalType.getCorrespondingClass();
        
        Supplier<LogicalType> named = ()->alreadyInferred
            ? logicalType
            : LogicalType.infer(type); // use only if NOT managed by Spring

        if(ClassUtils.isPrimitiveOrWrapper(type)
                || type.isEnum()) {
            return CausewayBeanMetaData.notManaged(BeanSort.VALUE, named.get());
        }

        if(CollectionSemantics.valueOf(type).isPresent()) {
            return CausewayBeanMetaData.causewayManaged(BeanSort.COLLECTION, named.get());
        }

        if(type.isInterface()
                // modifier predicate must be called after testing for non-scalar type above,
                // otherwise we'd get false positives
                || Modifier.isAbstract(type.getModifiers())) {

            // apiNote: abstract types and interfaces cannot be vetoed
            // and should also never be identified as ENTITY, VIEWMODEL or MIXIN
            // however, concrete types that inherit abstract ones with vetoes,
            // will effectively be vetoed through means of annotation synthesis
            return CausewayBeanMetaData.indifferent(BeanSort.ABSTRACT, named.get());
        }

        // handle vetoing ...
        if(TypeExcludeMarker.anyMatchOn(type)) {
            return CausewayBeanMetaData.notManaged(BeanSort.VETOED, named.get()); // reject
        }

        var profiles = Can.ofArray(_Annotations.synthesize(type, Profile.class)
                .map(Profile::value)
                .orElse(null));
        if(profiles.isNotEmpty()
                && !profiles.stream().anyMatch(this::isProfileActive)) {
            return CausewayBeanMetaData.notManaged(BeanSort.VETOED, named.get()); // reject
        }

        // handle value types ...

        var aValue = _Annotations.synthesize(type, org.apache.causeway.applib.annotation.Value.class)
                .orElse(null);
        if(aValue!=null) {
            return CausewayBeanMetaData.notManaged(BeanSort.VALUE, named.get());
        }
        
        // handle internal bean types ...
        
        if(classCache.hasInternalBeanSemantics(type)) {
            return CausewayBeanMetaData.injectable(BeanSort.MANAGED_BEAN_NOT_CONTRIBUTING, logicalType);
        }

        // handle actual bean types ...

        var aDomainService = _Annotations.synthesize(type, DomainService.class);
        if(aDomainService.isPresent()) {
            Attributes.HAS_DOMAIN_SERVICE_SEMANTICS.set(classCache, type, "true");
            return CausewayBeanMetaData.injectable(BeanSort.MANAGED_BEAN_CONTRIBUTING, logicalType);
        }

        //[CAUSEWAY-3585] when implements ViewModel, then don't consider alternatives, yield VIEW_MODEL
        if(org.apache.causeway.applib.ViewModel.class.isAssignableFrom(type)) {
            return CausewayBeanMetaData.causewayManaged(BeanSort.VIEW_MODEL, named.get());
        }

        // allow ServiceLoader plugins to have a say, eg. when classifying entity types
        for(var classifier : classifierPlugins) {
            var classification = classifier.classify(named.get());
            if(classification!=null) return classification;
        }

        var entityAnnotation = _Annotations.synthesize(type, Entity.class).orElse(null);
        if(entityAnnotation!=null) {
            return CausewayBeanMetaData.entity(PersistenceStack.JPA, named.get());
        }

        var aDomainObject = _Annotations.synthesize(type, DomainObject.class).orElse(null);
        if(aDomainObject!=null) {
            switch (aDomainObject.nature()) {
            case BEAN:
                return CausewayBeanMetaData
                        .indifferent(BeanSort.MANAGED_BEAN_CONTRIBUTING, named.get());
            case MIXIN:
                // memoize mixin main name
                Attributes.MIXIN_MAIN_METHOD_NAME.set(classCache, type, aDomainObject.mixinMethod());
                return CausewayBeanMetaData.causewayManaged(BeanSort.MIXIN, named.get());
            case ENTITY:
                return CausewayBeanMetaData.entity(PersistenceStack.UNSPECIFIED, named.get());
            case VIEW_MODEL:
            case NOT_SPECIFIED:
                //because object is not associated with a persistence context unless discovered above
                return CausewayBeanMetaData.causewayManaged(BeanSort.VIEW_MODEL, named.get());
            }
        }

        if(_ClassCache.getInstance().hasJaxbRootElementSemantics(type)) {
            return CausewayBeanMetaData.causewayManaged(BeanSort.VIEW_MODEL, named.get());
        }

        if(_Annotations.isPresent(type, Component.class)) {
            return CausewayBeanMetaData.indifferent(BeanSort.MANAGED_BEAN_NOT_CONTRIBUTING, logicalType);
        }

        // unless explicitly declared otherwise, map records to viewmodels
        if(type.isRecord()) {
            return CausewayBeanMetaData.indifferent(BeanSort.VIEW_MODEL, named.get());
        }

        if(Serializable.class.isAssignableFrom(type)) {
            return CausewayBeanMetaData.indifferent(BeanSort.VALUE, named.get());
        }

        return CausewayBeanMetaData.indifferent(BeanSort.UNKNOWN, named.get());
    }

    // -- HELPER

    //TODO yet this is a naive implementation, not evaluating any expression logic like eg. @Profile("!dev")
    //either we find a Spring Boot utility class that does this logic for us, or we make it clear with the
    //docs, that we have only limited support for the @Profile annotation
    private boolean isProfileActive(final String profile) {
        return activeProfiles.contains(profile);
    }

}
