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
package org.apache.isis.core.metamodel.facets.actions.command.configuration;

import org.apache.isis.core.commons.config.IsisConfiguration;

public enum ActionConfiguration {
    ALL,
    IGNORE_SAFE,
    NONE;

    private static final String  COMMAND_ACTIONS_KEY = "isis.services.command.actions";

    public static ActionConfiguration parse(final IsisConfiguration configuration) {
        return parse(configuration.getString(COMMAND_ACTIONS_KEY));
    }

    private static ActionConfiguration parse(final String value) {
        if ("ignoreQueryOnly".equalsIgnoreCase(value) || "ignoreSafe".equalsIgnoreCase(value)) {
            return IGNORE_SAFE;
        } else if ("all".equals(value)) {
            return ALL;
        } else {
            return NONE;
        }
    }
}
