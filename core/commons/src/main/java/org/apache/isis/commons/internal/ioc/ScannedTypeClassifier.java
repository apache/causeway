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
package org.apache.isis.commons.internal.ioc;

/**
 * @apiNote implementing classes must not rely on IsisConfiguration or other provisioned 
 * services to be available; type classification happens before the post-construct phase
 * 
 * @since 2.0 
 */
public interface ScannedTypeClassifier {

    BeanSort quickClassify(Class<?> type);

    /**
     * Whether given type is available for injection.
     * 
     * @param type
     * @return
     */
    default boolean isInjectable(Class<?> type) {
        return quickClassify(type) == BeanSort.MANAGED_BEAN;
    }

    /**
     * Whether given type is to be introspected by the framework.
     * 
     * @param type
     * @return
     */
    default boolean isIntrospectable(Class<?> type) {
        return quickClassify(type) != BeanSort.UNKNOWN;
    }
    
    // -- SYNONYMS
    
    /**
     * Whether given type is available for injection. Is a <em>Managed Bean</em>. 
     * <p> synonym for {@link #isInjectable(Class)}
     * @param type
     * @return 
     */
    default boolean isManagedBean(Class<?> type) {
        return isInjectable(type);
    }

    /**
     * Whether given type is to be introspected by the framework. Is a <em>Managed Object</em>. 
     * <p> synonym for {@link #isIntrospectable(Class)}
     * @param type
     * @return
     */
    default boolean isManagedObject(Class<?> type) {
        return isIntrospectable(type);
    }
    
}
