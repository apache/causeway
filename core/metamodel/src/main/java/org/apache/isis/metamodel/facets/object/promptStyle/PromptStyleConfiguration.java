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
package org.apache.isis.metamodel.facets.object.promptStyle;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.config.IsisConfigurationLegacy;


public class PromptStyleConfiguration {

    private PromptStyleConfiguration() {}

    public static final String PROMPT_STYLE_KEY = "isis.viewer.wicket.promptStyle";

    public static PromptStyle parse(final IsisConfigurationLegacy configuration) {
        final String configuredValue = configuration.getString(PROMPT_STYLE_KEY);
        return PromptStyleConfiguration.parse(configuredValue);
    }

    private static PromptStyle parse(final String value) {
        return value != null && value.trim().equalsIgnoreCase(PromptStyle.DIALOG.name())
                ? PromptStyle.DIALOG
                        : PromptStyle.INLINE;
    }

}
