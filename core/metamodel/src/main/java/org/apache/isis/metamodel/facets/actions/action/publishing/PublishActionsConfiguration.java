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
package org.apache.isis.metamodel.facets.actions.action.publishing;

import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.metamodel.facets.object.domainobject.Util;

public enum PublishActionsConfiguration {
    ALL,
    IGNORE_SAFE,
    NONE;

    private static final String PUBLISH_ACTIONS_KEY = "isis.services.publish.actions";

    public static PublishActionsConfiguration parse(IsisConfigurationLegacy configuration) {
        final String configuredValue = configuration.getString(PUBLISH_ACTIONS_KEY);
        return PublishActionsConfiguration.parse(configuredValue);
    }

    private static PublishActionsConfiguration parse(final String value) {
        if ("ignoreQueryOnly".equalsIgnoreCase(value) || "ignoreSafe".equalsIgnoreCase(value)) {
            return IGNORE_SAFE;
        }
        return Util.parseYes(value)? ALL: NONE;
    }

}
