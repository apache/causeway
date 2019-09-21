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
package org.apache.isis.applib.annotation;

/**
 * The different sorts of domain services recognized by Isis, as specified in {@link DomainService#nature()}
 * 
 * @apiNote
 * [EXPERIMENTAL]
 * Any service might also act as a stateless <em>Viewmodel</em>, eg. when used as return type of 
 * an <em>Action</em>. 
 * It's not well defined, what a viewer will render in such cases.
 */
public enum NatureOfService {

    /**
     * The service's actions appear in the REST API and in 'viewers', eg. in the menu bar.
     * 
     * @apiNote
     * Contributing actions to the 'viewer' implies, that these must also be exposed to the REST API,
     * simply because alternative viewers might be solely based on the provided REST end-points.  
     */
    VIEW,

    /**
     * The service's actions should only be visible in the REST API exposed by the Restful Objects viewer.
     */
    REST,
    
    /**
     * A <em>programmatic</em> service.
     * <p>
     * The service's actions do not appear on any viewer and are not visible in the REST API. In other words 
     * these are not contributed to the domain-model. However, the service is injectable into domain objects.  
     * </p>
     */
    DOMAIN,
    
    // -- DEPRECATIONS
    
    /**
     * @deprecated was renamed, use {@link #REST} instead
     * @apiNote since 2.0 used as synonym for {@link #REST} 
     */
    @Deprecated
    VIEW_REST_ONLY,
    
    /**
     * @deprecated was removed, use {@link #VIEW} instead
     * @apiNote since 2.0 used as synonym for {@link #VIEW}
     */
    @Deprecated
    VIEW_MENU_ONLY,
    
    /**
     * @deprecated was removed, contributing actions are simply ignored
     * <p>
     * Instead, for each {@code Action} write a mixin class. 
     * see {@link Mixin}
     * @apiNote since 2.0 used as synonym for {@link #DOMAIN}
     */
    @Deprecated
    VIEW_CONTRIBUTIONS_ONLY,

    ;

    // -- BASIC PREDICATES

    /**
     * @see {@link NatureOfService#VIEW}
     */
    public boolean isView() {
        return this == VIEW || this == VIEW_MENU_ONLY;
    }
    
    /**
     * Whether a service contributes its actions exclusively to the REST API.
     * @see {@link NatureOfService#REST}
     */
    public boolean isRestOnly() {
        return this == REST || this == VIEW_REST_ONLY;
    }
    
    /**
     * @see {@link NatureOfService#DOMAIN}
     */
    public boolean isDomain() {
        return this == DOMAIN || this == VIEW_CONTRIBUTIONS_ONLY;
    }

    // -- SEMANTIC PREDICATES

    /**
     * Whether a service contributes its actions (not necessarily exclusive) to the REST API.
     */
    public boolean isRestAlso() {
        return isRestOnly() || isView();
    }

    /**
     * Whether a service contributes no actions at all.
     */
    public boolean isProgrammatic() {
        return isDomain();
    }


}
