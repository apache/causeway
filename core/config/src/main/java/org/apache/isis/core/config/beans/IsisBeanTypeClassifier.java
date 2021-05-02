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

import org.springframework.core.env.Environment;

import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.context._Plugin;

import lombok.NonNull;
import lombok.Value;

/**
 * ServiceLoader SPI that allows for implementing instances to have a say during bean type scanning.
 * @since 2.0
 */
public interface IsisBeanTypeClassifier {

    // -- INTERFACE
   
    /**
     * @param type
     * @return optionally the bean classification for given {@code type},
     * based on whether this classifier feels responsible for the {@code type}.  
     */
    BeanClassification classify(Class<?> type);

    // -- FACTORY
    
    /**
     * in support of JUnit testing
     */
    static IsisBeanTypeClassifier createInstance() {
        return new IsisBeanTypeClassifierImpl(Can.empty());
    }
    
    static IsisBeanTypeClassifier createInstance(final @NonNull Environment environment) {
        return new IsisBeanTypeClassifierImpl(Can.ofArray(environment.getActiveProfiles()));
    }
    
    // -- LOOKUP

    public static Can<IsisBeanTypeClassifier> get() {
        return Can.ofCollection(_Plugin.loadAll(IsisBeanTypeClassifier.class));
    }
    
    // -- BEAN CLASSIFICATION RESULT
    
    @Value(staticConstructor = "of")
    public static class BeanClassification {
        
        BeanSort beanSort;
        String explicitObjectType;
        boolean delegateLifecycleManagement;
        
        // -- FACTORIES
        
        public static BeanClassification delegated(BeanSort beanSort, String explicitObjectType) {
            return of(beanSort, explicitObjectType, true);
        }
        
        public static BeanClassification delegated(BeanSort beanSort) {
            return delegated(beanSort, null);
        }
        
        public static BeanClassification selfManaged(BeanSort beanSort, String explicitObjectType) {
            return of(beanSort, explicitObjectType, false);
        }
        
        public static BeanClassification selfManaged(BeanSort beanSort) {
            return selfManaged(beanSort, null);
        }
        
    }
    
}
