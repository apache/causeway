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
package org.apache.isis.config.registry;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Vetoed;
import javax.inject.Singleton;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.ViewModel;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.components.ApplicationScopedComponent;
import org.apache.isis.commons.internal.components.SessionScopedComponent;
import org.apache.isis.commons.internal.components.TransactionScopedComponent;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.ioc.BeanSort;
import org.apache.isis.commons.internal.ioc.BeanSortClassifier;
import org.apache.isis.commons.internal.ioc.spring._Spring;

import static org.apache.isis.commons.internal.base._With.requires;
import static org.apache.isis.commons.internal.reflection._Reflect.containsAnnotation;
import static org.apache.isis.commons.internal.reflection._Reflect.getAnnotation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Holds the set of domain services, persistent entities and fixture scripts.services etc.
 * @since 2.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE) @Log4j2
public final class IsisBeanTypeRegistry implements BeanSortClassifier, AutoCloseable {

    public static IsisBeanTypeRegistry current() {
        return _Context.computeIfAbsent(IsisBeanTypeRegistry.class, IsisBeanTypeRegistry::new);
    }

    /**
     * inbox for introspection, as used by the spec loader
     */
    private final Map<Class<?>, BeanSort> inbox = new HashMap<>();

    //TODO replace this getters: don't expose the sets for modification!?
    @Getter private final Set<Class<?>> beanTypes = new HashSet<>();
    @Getter private final Set<Class<?>> entityTypes = new HashSet<>();
    @Getter private final Set<Class<?>> mixinTypes = new HashSet<>();
    //    @Getter private final Set<Class<? extends FixtureScript>> fixtureScriptTypes = new HashSet<>();
    @Getter private final Set<Class<?>> viewModelTypes = new HashSet<>();
    //    @Getter private final Set<Class<?>> xmlElementTypes = new HashSet<>();

    //@Getter private final Set<Class<?>> iocManaged = new HashSet<>();
    //@Getter private final Set<Class<?>> domainObjectTypes = new HashSet<>();
    //
    private final List<Set<? extends Class<? extends Object>>> allTypeSets = _Lists.of(
            beanTypes,
            entityTypes,
            mixinTypes,
            viewModelTypes
            );

    @Override
    public void close() {

        if(!_Spring.isContextAvailable()) {
            // this instance needs to survive a _Context.clear() call when Spring's context 
            // gets passed over to Isis
            return;
        }

        inbox.clear();
        allTypeSets.forEach(Set::clear);
    }

    // -- STREAM ALL

    //	public Stream<Class<?>> streamAllTypes() {
    //
    //		return _Lists.of(
    //				iocManaged,
    //				entityTypes,
    //				mixinTypes,
    //				fixtureScriptTypes,
    //				domainServiceTypes,
    //				domainObjectTypes,
    //				viewModelTypes,
    //				xmlElementTypes)
    //				.stream()
    //				.distinct()
    //				.flatMap(Collection::stream)
    //				;
    //	}

    // -- INBOX

    public void addToInbox(BeanSort sort, Class<?> type) {
        synchronized (inbox) {
            inbox.put(type, sort);
        }
    }

    /**
     * Implemented as a one-shot, that clears the inbox afterwards.
     * @return
     */
    public Stream<Map.Entry<Class<?>, BeanSort>> streamAndClearInbox() {

        final Map<Class<?>, BeanSort> defensiveCopy;

        synchronized (inbox) {
            defensiveCopy = new HashMap<>(inbox);
            inbox.clear();
        }

        if(log.isDebugEnabled()) {
            defensiveCopy.forEach((k, v)->{
                log.debug("to be specloaded: {} [{}]", k.getName(), v.name());
            });
        }

        return defensiveCopy.entrySet().stream();
    }

    // -- FILTER

    // don't categorize this early, instead push candidate classes onto a queue for 
    // later processing when the SpecLoader initializes.
    public boolean isIoCManagedType(TypeMetaData typeMetaData) {

        val type = typeMetaData.getUnderlyingClass();
        val beanSort = quickClassify(type);

        if(beanSort.isEntity()) {
            // event though passed to the inbox for introspection we populate this 
            // for the persistence layer not having to wait on the spec -loader for 
            // processing of the inbox
            entityTypes.add(type); 
        }

        val isToBeProvisioned = beanSort.isManagedBean();
        val isToBeInspected = !beanSort.isUnknown();

        if(isToBeInspected) {
            addToInbox(beanSort, type);
        }

        if(log.isDebugEnabled()) {
            if(isToBeInspected || isToBeProvisioned) {
                log.debug("{} {} [{}]",
                        isToBeProvisioned ? "provision" : beanSort.isEntity() ? "entity" : "skip",
                                type,
                                beanSort.name());
            }
        }

        return isToBeProvisioned;

    }

    // the SpecLoader does a better job at this
    @Override
    public BeanSort quickClassify(Class<?> type) {

        requires(type, "type");

        if(getAnnotation(type, Vetoed.class)!=null) {
            return BeanSort.UNKNOWN; // exclude from provisioning
        }

        if(Collection.class.isAssignableFrom(type)) {
            return BeanSort.COLLECTION;
        }

        val aDomainService = getAnnotation(type, DomainService.class);
        if(aDomainService!=null) {
            return BeanSort.MANAGED_BEAN;
        }

        //this takes precedence over whatever @DomainObject has to say
        if(containsAnnotation(type, "javax.jdo.annotations.PersistenceCapable")) {
            return BeanSort.ENTITY;
        }

        val aDomainObject = getAnnotation(type, DomainObject.class);
        if(aDomainObject!=null) {
            switch (aDomainObject.nature()) {
            case EXTERNAL_ENTITY:
            case INMEMORY_ENTITY:
            case JDO_ENTITY:
                return BeanSort.VIEW_MODEL; //because object is not associated with a persistence context unless discovered above
            case MIXIN:
                return BeanSort.MIXIN;
            case VIEW_MODEL:
                return BeanSort.VIEW_MODEL;

            case NOT_SPECIFIED:
                if(getAnnotation(type, ViewModel.class)!=null) {
                    return BeanSort.VIEW_MODEL;
                }
                if(org.apache.isis.applib.ViewModel.class.isAssignableFrom(type)) {
                    return BeanSort.VIEW_MODEL;
                }
                // fall through

            default:
                // continue
                break; 
            } 
        }

        if(getAnnotation(type, Mixin.class)!=null) {
            return BeanSort.MIXIN;
        }

        if(getAnnotation(type, ViewModel.class)!=null) {
            return BeanSort.VIEW_MODEL;
        }

        if(org.apache.isis.applib.ViewModel.class.isAssignableFrom(type)) {
            return BeanSort.VIEW_MODEL;
        }

        if(ApplicationScopedComponent.class.isAssignableFrom(type)) {
            return BeanSort.MANAGED_BEAN;
        }

        if(SessionScopedComponent.class.isAssignableFrom(type)) {
            return BeanSort.MANAGED_BEAN;
        }

        if(TransactionScopedComponent.class.isAssignableFrom(type)) {
            return BeanSort.MANAGED_BEAN;
        }

        if(getAnnotation(type, RequestScoped.class)!=null) {
            return BeanSort.MANAGED_BEAN;
        }

        if(getAnnotation(type, Singleton.class)!=null) {
            return BeanSort.MANAGED_BEAN;
        }

        if(getAnnotation(type, Service.class)!=null) {
            return BeanSort.MANAGED_BEAN;
        }

        if(getAnnotation(type, Component.class)!=null) {
            return BeanSort.MANAGED_BEAN;
        }

        if(Serializable.class.isAssignableFrom(type)) {
            return BeanSort.VALUE;
        }

        return BeanSort.UNKNOWN;
    }

    // if we ever want to ever implement an alternative solution to the autoclose hack above ...
    //    public IsisBeanTypeRegistry copy() {
    //        
    //        val copy = new IsisBeanTypeRegistry();
    //        
    //        val it1 = this.allTypeSets.iterator();
    //        val it2 = copy.allTypeSets.iterator();
    //        
    //        it1.forEachRemaining(src->{
    //            val dst = it2.next();
    //            dst.addAll((Collection) src);    
    //        });
    //        
    //        return copy;
    //    }


}