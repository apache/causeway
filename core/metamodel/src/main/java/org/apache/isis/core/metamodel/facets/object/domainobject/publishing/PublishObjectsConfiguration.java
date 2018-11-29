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
package org.apache.isis.core.metamodel.facets.object.domainobject.publishing;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.object.domainobject.Util;

public enum PublishObjectsConfiguration {
    ALL,
    NONE;

    private static final String PUBLISH_OBJECTS_KEY = "isis.services.publish.objects";

    public static PublishObjectsConfiguration parse(IsisConfiguration configuration) {
        final String configuredValue = configuration.getString(PUBLISH_OBJECTS_KEY);
        return PublishObjectsConfiguration.parse(configuredValue);
    }

    private static PublishObjectsConfiguration parse(final String value) {
        // must be explicitly enabled
        // behavior must conform to the doc https://isis.apache.org/guides/rgant/rgant.html#_rgant-DomainObject_publishing
        return Util.parseYes(value)? ALL: NONE;
    }

}
