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
package org.apache.causeway.applib.annotation;

/**
 * The different sorts of domain services recognized by Causeway, as specified in {@link DomainService#nature()}
 *
 * @since 1.x {@index}
 */
public enum NatureOfService {
    /**
     * The service's actions appear only in the menu bar human-usable Web UIs (such as Wicket viewer).
     * They do <i>not</i> appear in any REST or GraphQL APIs.
     */
    WEB_UI,

    /**
     * The service's actions should only be visible in the Web API exposed by the Restful Objects viewer and the
     * GraphQL viewer.  They do <i>not</i> appear in any human-usable Web UIs (such as Wicket viewer)
     */
    WEB_API,

    /**
     * The service's actions appear in the menu bar of Web UIs (such as Wicket viewer), and also appear in the
     * Web APIs (Restful Objects viewer and GraphQL viewer).
     *
     * @apiNote
     * Contributing actions to the 'viewer' implies, that these must also be exposed to the REST API,
     * simply because alternative viewers might be solely based on the provided REST end-points.
     */
    BOTH,
    ;

    // -- BASIC PREDICATES

    /**
     * Whether a service contributes its actions to both human-usable Web UIs and the Web APIs.
     *
     * @see NatureOfService#BOTH
     */
    public boolean isBoth() {
        return this == BOTH;
    }

    /**
     * Whether a service contributes its actions exclusively to human-usable Web UIs.
     * @see NatureOfService#WEB_UI
     */
    public boolean isWebUi() {
        return this == WEB_UI || this == BOTH;
    }

    /**
     * Whether a service contributes its actions to Web APIs (REST and GraphQL)
     * @see NatureOfService#WEB_API
     */
    public boolean isWebApi() {
        return this == WEB_API || this == BOTH;
    }


}
