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
package org.apache.isis.core.metamodel.facets.object.domainobject.auditing;

import org.apache.isis.config.IsisConfiguration;

public enum DefaultViewConfiguration {
    HIDDEN("hidden"),
    TABLE("table");

    private static final String DEFAULT_VIEW_KEY = "isis.viewers.collectionLayout.defaultView";

    private final String defaultView;

    DefaultViewConfiguration(
            final String defaultView) {

        this.defaultView = defaultView;
    }

    public String getDefaultView() {
        return defaultView;
    }

    public static DefaultViewConfiguration parse(IsisConfiguration configuration) {
        final String configuredValue = configuration.getString(DEFAULT_VIEW_KEY);
        return DefaultViewConfiguration.parseValue(configuredValue);
    }

    static DefaultViewConfiguration parseValue(final String value) {
        return value != null && value.trim().toLowerCase().equals("table") ? TABLE : HIDDEN;
    }

}
