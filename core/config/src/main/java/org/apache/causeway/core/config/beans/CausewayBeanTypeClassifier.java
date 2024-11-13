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
        @NonNull _ClassCache classCache) {

    // -- CONSTRUCTION

    CausewayBeanTypeClassifier(final ApplicationContext applicationContext) {
        this(Can.ofArray(applicationContext.getEnvironment().getActiveProfiles()));
    }

    CausewayBeanTypeClassifier(final Can<String> activeProfiles) {
        this(activeProfiles, _ClassCache.getInstance());
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

        // handle introspection veto ...
        if(TypeProgrammaticMarker.anyMatchOn(typeHead)) {
            return CausewayBeanMetaData.springManaged(discoveredBy, BeanSort.MANAGED_BEAN_NOT_CONTRIBUTING, logicalType);
        }
        // programmatic bean types
        if(typeHead.hasIntrospectionVetoingSemantics()) {
            return CausewayBeanMetaData.springManaged(discoveredBy, BeanSort.MANAGED_BEAN_NOT_CONTRIBUTING, logicalType);
        }

        //[CAUSEWAY-3585] when implements ViewModel, then don't consider alternatives, yield VIEW_MODEL
        if(org.apache.causeway.applib.ViewModel.class.isAssignableFrom(type)) {
            return CausewayBeanMetaData.causewayManaged(discoveredBy, BeanSort.VIEW_MODEL, named.get());
        }

        // value types
        if(typeHead.hasAnnotation(org.apache.causeway.applib.annotation.Value.class)) {
            return CausewayBeanMetaData.value(named.get(), discoveredBy);
        }

        // domain service
        if(typeHead.hasAnnotation(DomainService.class)) {
            return CausewayBeanMetaData.springManaged(discoveredBy, BeanSort.MANAGED_BEAN_CONTRIBUTING, logicalType);
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
                return CausewayBeanMetaData.unspecified(discoveredBy, BeanSort.MANAGED_BEAN_CONTRIBUTING, named.get());
            case MIXIN:
                // memoize mixin main name
                typeHead.attributeMap().put(_ClassCache.Attribute.MIXIN_MAIN_METHOD_NAME, aDomainObject.mixinMethod());
                return CausewayBeanMetaData.causewayManaged(discoveredBy, BeanSort.MIXIN, named.get());
            case ENTITY:
                return CausewayBeanMetaData.entity(named.get(), discoveredBy, PersistenceStack.UNSPECIFIED);
            case VIEW_MODEL:
            case NOT_SPECIFIED:
                //because object is not associated with a persistence context unless discovered above
                return CausewayBeanMetaData.causewayManaged(discoveredBy, BeanSort.VIEW_MODEL, named.get());
            }
        }

        if(typeHead.hasJaxbRootElementSemantics()) {
            return CausewayBeanMetaData.causewayManaged(discoveredBy, BeanSort.VIEW_MODEL, named.get());
        }

        if(typeHead.hasAnnotation(Component.class)) {
            return CausewayBeanMetaData.unspecified(discoveredBy, BeanSort.MANAGED_BEAN_NOT_CONTRIBUTING, logicalType);
        }

        // unless explicitly declared otherwise, map records to viewmodels
        if(type.isRecord()) {
            return CausewayBeanMetaData.unspecified(discoveredBy, BeanSort.VIEW_MODEL, named.get());
        }

        if(Serializable.class.isAssignableFrom(type)) {
            return CausewayBeanMetaData.unspecified(discoveredBy, BeanSort.VALUE, named.get());
        }

        return CausewayBeanMetaData.unspecified(discoveredBy, BeanSort.UNKNOWN, named.get());
    }

    //TODO yet this is a naive implementation, not evaluating any expression logic like eg. @Profile("!dev")
    //either we find a Spring Boot utility class that does this logic for us, or we make it clear with the
    //docs, that we have only limited support for the @Profile annotation
    private boolean isProfileActive(final String profile) {
        return activeProfiles.contains(profile);
    }

}
