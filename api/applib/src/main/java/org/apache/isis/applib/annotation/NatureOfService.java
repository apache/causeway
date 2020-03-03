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
 */
// tag::refguide[]
public enum NatureOfService {

    // end::refguide[]
    /**
     * The service's actions appear in the REST API and in 'viewers', eg. in the menu bar.
     *
     * @apiNote
     * Contributing actions to the 'viewer' implies, that these must also be exposed to the REST API,
     * simply because alternative viewers might be solely based on the provided REST end-points.
     */
    // tag::refguide[]
    VIEW,

    // end::refguide[]
    /**
     * The service's actions should only be visible in the REST API exposed by the Restful Objects viewer.
     */
    // tag::refguide[]
    REST,

    // end::refguide[]

    //
    // -- DEPRECATIONS
    //

    /**
     * A <em>programmatic</em> service.
     * <p>
     * The service's actions do not appear on any viewer and are not visible in the REST API. In other words
     * these are not contributed to the domain-model. However, the service is injectable into domain objects.
     * </p>
     *
     * @deprecated will be removed with 2.0.0 release! use Spring's {@link org.springframework.stereotype.Service @Service} instead;
     * @apiNote For now, used as synonym for {@link #REST}
     */
    @Deprecated
    DOMAIN,

    /**
     * @deprecated will be removed with 2.0.0 release! use {@link #REST} instead;
     * @apiNote For now, used as synonym for {@link #REST}
     */
    @Deprecated
    VIEW_REST_ONLY,

    /**
     * @deprecated will be removed with 2.0.0 release! use {@link #VIEW} instead
     * @apiNote For now, used as synonym for {@link #VIEW}
     */
    @Deprecated
    VIEW_MENU_ONLY,

    /**
     * @deprecated will be removed with 2.0.0 release!
     * <p>
     * For now, contributing actions will be gathered to show up in the 'others' menu to ease migration.
     * These will likely not work.
     * <p>
     * Migration Note: For each {@code Action} write a new mixin class.
     * see {@link Mixin}
     * @apiNote For now, used as synonym for {@link #VIEW}
     */
    @Deprecated
    VIEW_CONTRIBUTIONS_ONLY,

    ;

    // -- BASIC PREDICATES

    /**
     * @see {@link NatureOfService#VIEW}
     */
    public boolean isView() {
        return this == VIEW || this == VIEW_MENU_ONLY  || this == VIEW_CONTRIBUTIONS_ONLY;
    }

    /**
     * Whether a service contributes its actions exclusively to the REST API.
     * @see {@link NatureOfService#REST}
     */
    public boolean isRestOnly() {
        return this == REST || this == VIEW_REST_ONLY;
    }

    /**
     * Whether a service contributes no actions at all.
     * @see {@link NatureOfService#DOMAIN}
     */
    public boolean isProgrammatic() {
        return this == DOMAIN;
    }

    // -- SEMANTIC PREDICATES

    /**
     * Whether a service contributes its actions (not necessarily exclusive) to the REST API.
     */
    public boolean isRestAlso() {
        return isRestOnly() || isView();
    }

    // tag::refguide[]

}
// end::refguide[]
