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
     * The default; the service's actions appear on menus but do not (ni onger) contribute.
     */
    VIEW,
    /**
     * The service's actions appear on menus but do not contribute.
     *
     * @deprecated - replaced in isis v2 with VIEW.
     */
    @Deprecated
    VIEW_MENU_ONLY,
    /**
     * The services actions should only be visible in the REST API exposed by the Restful Objects viewer.
     */
    REST,
    /**
     * @deprecated - replaced in isis v2 with REST
     */
    @Deprecated
    VIEW_REST_ONLY,

    /**
     * The service's actions do not appear on menus and are not contributed.
     *
     * <p>
     * Equivalent to annotating all actions with both (the now deprecated) {@link org.apache.isis.applib.annotation.NotInServiceMenu} and {@link org.apache.isis.applib.annotation.NotContributed} annotations).
     * </p>
     *
     * @deprecated - replaced in isis v2 with @Service or @Component annotation
     */
    @Deprecated
    DOMAIN;

    boolean isView() {
        return this == VIEW;
    }
    boolean isRest() {
        return this == REST || this == VIEW_REST_ONLY;
    }
}
