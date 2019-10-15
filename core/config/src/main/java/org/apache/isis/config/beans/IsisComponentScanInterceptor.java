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
package org.apache.isis.config.beans;

import org.springframework.stereotype.Component;

import org.apache.isis.config.registry.TypeMetaData;

/**
 * @apiNote implementing classes must not rely on IsisConfiguration or other provisioned 
 * services to be available; type classification happens before the post-construct phase
 * 
 * @since 2.0 
 */
public interface IsisComponentScanInterceptor {

    /**
     * Allows for the given type-meta to by modified before bean-definition registration 
     * is finalized by the IoC immediately after the type-scan phase. 
     * Supported aspects to be modified: 
     * <p>- Whether given {@link Component} annotated or meta-annotated type should be made
     * available for injection.
     * <p>- Naming strategy to override that of the IoC.
     * 
     * @param type
     * @apiNote implementing classes might have side effects, eg. intercept 
     * discovered types into a type registry
     */
    void intercept(TypeMetaData type);

    /**
     * If given type is available for injection, returns the <em>Managed Bean's</em> name (id) as
     * recognized by the IoC container, {@code null} otherwise;
     * @param type
     * @return
     */
    String getManagedBeanNameForType(Class<?> type);
    
    /**
     * Whether given type is available for injection. Is a <em>Managed Bean</em>. 
     * @param type
     * @return 
     */
    default boolean isManagedBean(Class<?> type) {
        return getManagedBeanNameForType(type)!=null;
    }
    
}
