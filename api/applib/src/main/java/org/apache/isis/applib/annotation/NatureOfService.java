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
 * @since 1.x {@index}
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

    ;

    // -- BASIC PREDICATES

    /**
     * Whether a service contributes its actions to viewers and to the REST API.
     * @see {@link NatureOfService#VIEW}
     */
    public boolean isView() {
        return this == VIEW;
    }

    /**
     * Whether a service contributes its actions exclusively to the REST API.
     * @see {@link NatureOfService#REST}
     */
    public boolean isRestOnly() {
        return this == REST;
    }

    // tag::refguide[]

}
// end::refguide[]
