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
import java.util.function.Supplier;

import jakarta.persistence.Entity;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData.DiscoveredBy;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData.PersistenceStack;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.TypeProgrammaticMarker;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.TypeVetoMarker;

import lombok.NonNull;

@Programmatic
public record CausewayBeanTypeClassifier(
        @NonNull Can<String> activeProfiles,
        @NonNull _ClassCache classCache,
        @NonNull ContextType contextType) {

    public enum ContextType {
        SPRING,
        MOCKUP
    }

    // -- CONSTRUCTION

    CausewayBeanTypeClassifier(final ApplicationContext applicationContext) {
        this(Can.ofArray(applicationContext.getEnvironment().getActiveProfiles()), ContextType.SPRING);
    }

    CausewayBeanTypeClassifier(final Can<String> activeProfiles, final ContextType contextType) {
        this(activeProfiles, _ClassCache.getInstance(), contextType);
    }

    // -- CLASSIFY

    /**
     * Classify {@link LogicalType} as detected and named by either Causeway or Spring.
     * <p>
     * Typically Causeway we will use a different fallback naming strategy for 'unnamed' types,
     * that is, it uses the fully qualified class name.
     *
     * @param logicalType with name as either forced by Causeway or suggested by Spring
     */
    public CausewayBeanMetaData classify(@NonNull final LogicalType logicalType, final DiscoveredBy discoveredBy) {

        var type = logicalType.correspondingClass();

        Supplier<LogicalType> named = ()->discoveredBy.isSpring()
            ? LogicalType.infer(type) // use only if discovered by Spring but NOT managed by Spring
            : logicalType; // name is already inferred, when discovered by Causeway

        if(ClassUtils.isPrimitiveOrWrapper(type)
                || type.isEnum()) {
            return CausewayBeanMetaData.value(named.get(), discoveredBy);
        }

        if(CollectionSemantics.valueOf(type).isPresent()) {
            return CausewayBeanMetaData.collection(named.get(), discoveredBy);
        }

        if(type.isInterface()
                // modifier predicate must be called after testing for non-scalar type above,
                // otherwise we'd get false positives
                || Modifier.isAbstract(type.getModifiers())) {

            // apiNote: abstract types and interfaces cannot be vetoed
            // and should also never be identified as ENTITY, VIEWMODEL or MIXIN
            // however, concrete types that inherit abstract ones with vetoes,
            // will effectively be vetoed through means of annotation synthesis
            return CausewayBeanMetaData.interfaceOrAbstract(named.get(), discoveredBy);
        }

        var typeHead = classCache().head(type);

        // handle vetoing ...
        if(TypeVetoMarker.anyMatchOn(typeHead)) {
            return CausewayBeanMetaData.vetoed(named.get(), discoveredBy); // reject
        }

        var profiles = typeHead.springProfiles();
        if(profiles.isNotEmpty()
                && !profiles.stream().anyMatch(this::isProfileActive)) {
            return CausewayBeanMetaData.vetoed(named.get(), discoveredBy); // reject
        }

        // handle introspection veto (programmatic bean) ...
        if(TypeProgrammaticMarker.anyMatchOn(typeHead)) {
            if(contextType==ContextType.MOCKUP) return CausewayBeanMetaData.springNotContributing(logicalType);
            return switch (discoveredBy) {
                 case SPRING, CAUSEWAY_UPFRONT -> CausewayBeanMetaData.springNotContributing(logicalType);
                 case CAUSEWAY_ONTHEFLY -> CausewayBeanMetaData.programmatic(named.get());
            };
        }

        // when implements ViewModel, yield VIEW_MODEL unless vetoed
        if(org.apache.causeway.applib.ViewModel.class.isAssignableFrom(type)) {
            return CausewayBeanMetaData.viewModel(named.get(), discoveredBy);
        }

        // value types
        if(typeHead.hasAnnotation(org.apache.causeway.applib.annotation.Value.class)) {
            return CausewayBeanMetaData.value(named.get(), discoveredBy);
        }

        // domain service
        if(typeHead.hasAnnotation(DomainService.class)) {
            return CausewayBeanMetaData.springContributing(logicalType);
        }

        // entity support
        if(typeHead.isJdoPersistenceCapable()){
            return CausewayBeanMetaData.entity(named.get(), discoveredBy, PersistenceStack.JDO);
        }
        if(typeHead.hasAnnotation(Entity.class)) {
            return CausewayBeanMetaData.entity(named.get(), discoveredBy, PersistenceStack.JPA);
        }

        // domain object
        var aDomainObject = typeHead.annotation(DomainObject.class).orElse(null);
        if(aDomainObject!=null) {
            switch (aDomainObject.nature()) {
            case BEAN:
                return CausewayBeanMetaData.unspecified(named.get(), discoveredBy, BeanSort.MANAGED_BEAN_CONTRIBUTING);
            case MIXIN:
                // memoize mixin main name
                typeHead.attributeMap().put(_ClassCache.Attribute.MIXIN_MAIN_METHOD_NAME, aDomainObject.mixinMethod());
                return CausewayBeanMetaData.mixin(named.get(), discoveredBy);
            case ENTITY:
                return CausewayBeanMetaData.entity(named.get(), discoveredBy, PersistenceStack.UNSPECIFIED);
            case VIEW_MODEL:
            case NOT_SPECIFIED:
                //because object is not associated with a persistence context unless discovered above
                return CausewayBeanMetaData.viewModel(named.get(), discoveredBy);
            }
        }

        if(typeHead.hasJaxbRootElementSemantics()) {
            return CausewayBeanMetaData.viewModel(named.get(), discoveredBy);
        }

        if(typeHead.hasAnnotation(Component.class)) {
            return CausewayBeanMetaData.unspecified(logicalType, discoveredBy, BeanSort.MANAGED_BEAN_NOT_CONTRIBUTING);
        }

        // unless explicitly declared otherwise, map records to viewmodels
        if(type.isRecord()) {
            return CausewayBeanMetaData.unspecified(named.get(), discoveredBy, BeanSort.VIEW_MODEL);
        }

        if(Serializable.class.isAssignableFrom(type)) {
            return CausewayBeanMetaData.unspecified(named.get(), discoveredBy, BeanSort.VALUE);
        }

        return CausewayBeanMetaData.unspecified(named.get(), discoveredBy, BeanSort.UNKNOWN);
    }

    // -- HELPER

    /*TODO yet this is a naive implementation, not evaluating any expression logic like eg. @Profile("!dev")
      either we find a Spring Boot utility class that does this logic for us, or we make it clear with the
      docs, that we have only limited support for the @Profile annotation*/
    private boolean isProfileActive(final String profile) {
        return activeProfiles.contains(profile);
    }

}
