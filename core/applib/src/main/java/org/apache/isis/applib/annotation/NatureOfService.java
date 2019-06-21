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
 */
public enum NatureOfService {

    /**
     * The service's actions appear in the viewer, for example in the menu bar.
     */
    VIEW,
    /**
     * TODO: v2: remove for v2
     *
     * @deprecated - use {@link #VIEW} instead
     */
    @Deprecated
    VIEW_MENU_ONLY,
    /**
     * TODO: v2: remove for v2
     *
     * @deprecated - use mixins instead
     */
    @Deprecated
    VIEW_CONTRIBUTIONS_ONLY,
    /**
     * The services actions should only be visible in the REST API exposed by the Restful Objects viewer.
     */
    VIEW_REST_ONLY,
    /**
     * The service's actions do not appear on menus and are not contributed.
     */
    DOMAIN;

    boolean isViewRestOnly() {
        return this == VIEW_REST_ONLY;
    }
    boolean isView() {
        return this == VIEW;
    }
    @Deprecated
    boolean isViewMenuOnly() {
        return this == VIEW_MENU_ONLY;
    }
    @Deprecated
    boolean isViewContributionsOnly() {
        return this == VIEW_CONTRIBUTIONS_ONLY;
    }
    boolean isDomain() {
        return this == DOMAIN;
    }

    @Deprecated
    boolean isViewOrViewMenuOnly() {
        return isView() || isViewMenuOnly();
    }

}
