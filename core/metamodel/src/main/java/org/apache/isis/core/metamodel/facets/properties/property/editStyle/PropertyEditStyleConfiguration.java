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
package org.apache.isis.core.metamodel.facets.properties.property.editStyle;

import org.apache.isis.applib.annotation.PropertyEditStyle;
import org.apache.isis.core.commons.config.IsisConfiguration;


public class PropertyEditStyleConfiguration {

    private PropertyEditStyleConfiguration() {}

    public static final String EDIT_STYLE_KEY = "isis.properties.editStyle";

    public static PropertyEditStyle parse(final IsisConfiguration configuration) {
        final String configuredValue = configuration.getString(EDIT_STYLE_KEY);
        return PropertyEditStyleConfiguration.parse(configuredValue);
    }

    private static PropertyEditStyle parse(final String value) {
        return value != null && value.trim().equalsIgnoreCase(PropertyEditStyle.INLINE.name())
                ? PropertyEditStyle.INLINE
                : PropertyEditStyle.DIALOG;
    }

}
