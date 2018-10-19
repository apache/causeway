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
     * The default; the service's actions appear on menus and can be contributed to domain objects as actions,
     * properties or collections.
     */
    VIEW,
    /**
     * The service's actions appear on menus but do not contribute.
     */
    VIEW_MENU_ONLY,
    /**
     * The service's actions can be contributed to domain objects as actions, properties or collections but do not
     * appear on menus.
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
     * The service's actions do not appear on menus and are not contributed, but can be {@link org.apache.isis.applib.services.wrapper.WrapperFactory#wrap(Object) wrapped}.
     */
    DOMAIN_API,
    /**
     * The domain service methods are NOT part of the metamodel and instead are intended only to be invoked programmatically.
     */
    DOMAIN;

    public boolean isViewRestOnly() {
        return this == VIEW_REST_ONLY;
    }
    public boolean isView() {
        return this == VIEW;
    }
    public boolean isViewMenuOnly() {
        return this == VIEW_MENU_ONLY;
    }
    public boolean isViewContributionsOnly() {
        return this == VIEW_CONTRIBUTIONS_ONLY;
    }

    /**
     * Is either {@link #DOMAIN domain (implementation)} or {@link #DOMAIN_API domain (api)}.
     */
    public boolean isDomain() {
        return this == DOMAIN || this == DOMAIN_API;
    }
    public boolean isDomainImpl() {
        return this == DOMAIN;
    }
    public boolean isDomainApi() {
        return this == DOMAIN_API;
    }

    public boolean isViewOrViewMenuOnly() {
        return isView() || isViewMenuOnly();
    }

}
