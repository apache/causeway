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
     * The service's actions appear only in the menu bar human-usable UIs (such as Wicket viewer).
     * They do <i>not</i> appear in any REST or GraphQL APIs.
     */
    WEB_UI, //TODO perhaps rename to UI_ONLY?

    /**
     * The service's actions should only be visible in the Web API exposed by the Restful Objects viewer and the
     * GraphQL viewer.  They do <i>not</i> appear in any human-usable Web UIs (such as Wicket viewer)
     */
    WEB_API, //TODO perhaps rename to WEBAPI_ONLY?

    /**
     * The service's actions appear in the menu bar of Web UIs (such as Wicket viewer), and also appear in the
     * Web APIs (Restful Objects viewer and GraphQL viewer).
     *
     * @apiNote
     * Contributing actions to the 'viewer' implies, that these must also be exposed to the REST API,
     * simply because alternative viewers might be solely based on the provided REST end-points.
     */
    BOTH, //TODO perhaps rename to ENABLED_EVERYWHERE?

    /**
     * @deprecated use {@link #BOTH} instead
     * @see NatureOfService#BOTH
     */
    @Deprecated
    VIEW,

    /**
     * @deprecated use {@link #WEB_API} instead
     * @see NatureOfService#WEB_API
     */
    @Deprecated
    REST

    ;

    // -- BASIC PREDICATES

    /**
     * Whether a service contributes its actions to both human-usable UIs and the Web APIs.
     *
     * @see NatureOfService#BOTH
     */
    public boolean isEnabledEverywhere() {
        return this == BOTH
                || this == VIEW;
    }

    /**
     * Whether a service contributes its actions to human-usable UIs.
     * @see NatureOfService#WEB_UI
     */
    public boolean isEnabledForUi() {
        return isEnabledEverywhere()
                || this == WEB_UI;
    }

    /**
     * Whether a service contributes its actions to Web APIs (REST and GraphQL)
     * @see NatureOfService#WEB_API
     */
    public boolean isEnabledForWebApi() {
        return isEnabledEverywhere()
                || this == WEB_API
                || this == REST;
    }

}
