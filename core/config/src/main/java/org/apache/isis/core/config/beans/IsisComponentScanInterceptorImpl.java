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

import java.util.Map;

import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @since 2.0
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE) 
@Log4j2
final class IsisComponentScanInterceptorImpl 
implements IsisComponentScanInterceptor {

    private final @NonNull IsisBeanTypeClassifier isisBeanTypeClassifier;
    
    /**
     * Inbox for introspection, as used by the SpecificationLoader
     */
    private Map<Class<?>, IsisBeanMetaData> introspectableTypes = _Maps.newConcurrentHashMap();

    @Override
    public Can<IsisBeanMetaData> getAndDrainIntrospectableTypes() {

        if(introspectableTypes==null) {
            throw _Exceptions.illegalState("introspectable types had already been drained (one shot)");
        }
        
        val defensiveCopy = Can.ofCollection(introspectableTypes.values());
        
        if(log.isDebugEnabled()) {
            defensiveCopy.forEach(type->{
                log.debug("to be introspected: {}", type);
            });
        }

        introspectableTypes = null;
        
        return defensiveCopy;
    }

    // -- FILTER
    
    @Override
    public void intercept(ScannedTypeMetaData typeMeta) {
        
        val classOrFailure = typeMeta.getUnderlyingClassOrFailure();
        if(classOrFailure.isFailure()) {
            log.warn(classOrFailure.getFailure());
            return;
        }
        
        val type = classOrFailure.getUnderlyingClass();
        val classification = isisBeanTypeClassifier.classify(type);
        
        val delegated = classification.isDelegateLifecycleManagement();
        typeMeta.setInjectable(delegated);
        if(delegated) {
            typeMeta.setBeanNameOverride(classification.getExplicitObjectType());    
        }
        
        val beanSort = classification.getBeanSort();
        
        if(beanSort.isToBeIntrospected()) {
            addIntrospectableType(beanSort, typeMeta);
            
            if(log.isDebugEnabled()) {
                log.debug("to-be-introspected: {} [{}]",                        
                                type,
                                beanSort.name());
            }
        }
        
    }
    
    // -- HELPER

    private void addIntrospectableType(BeanSort sort, ScannedTypeMetaData typeMeta) {
        val correspondingClass = typeMeta.getUnderlyingClassOrFailure().getUnderlyingClass();
        val type = IsisBeanMetaData.of(correspondingClass, sort, typeMeta.getEffectiveBeanName());
        introspectableTypes.put(correspondingClass, type);
    }

}