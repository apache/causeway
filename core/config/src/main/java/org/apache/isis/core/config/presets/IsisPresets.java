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
package org.apache.isis.core.config.presets;

import org.apache.isis.commons.internal.base._Strings;

/**
 * Supports Spring's {@code @PropertySource} annotation.
 *
 * @since 2.0
 *
 */
public final class IsisPresets  {

    public static final String NoTranslations = "classpath:/org/apache/isis/core/config/presets/NoTranslations.properties";
    public static final String IntrospectLazily = "classpath:/org/apache/isis/core/config/presets/IntrospectLazily.properties";
    public static final String IntrospectFully = "classpath:/org/apache/isis/core/config/presets/IntrospectFully.properties";

    public static final String DatanucleusAutocreateNoValidate = "classpath:/org/apache/isis/core/config/presets/DatanucleusAutocreateNoValidate.properties";

    public static final String DebugPersistence = "classpath:/org/apache/isis/core/config/presets/DebugPersistence.properties";
    public static final String DebugRequestScopedServices = "classpath:/org/apache/isis/core/config/presets/DebugRequestScopedServices.properties";
    public static final String DebugTransactionScopedServices = "classpath:/org/apache/isis/core/config/presets/DebugTransactionScopedServices.properties";

    public static final String DebugDiscovery = "classpath:/org/apache/isis/core/config/presets/DebugDiscovery.properties";

    public static final String DebugProgrammingModel = "classpath:/org/apache/isis/core/config/presets/DebugProgrammingModel.properties";
    public static final String SilenceProgrammingModel = "classpath:/org/apache/isis/core/config/presets/SilenceProgrammingModel.properties";

    public static final String DebugMetaModel = "classpath:/org/apache/isis/core/config/presets/DebugMetaModel.properties";
    public static final String SilenceMetaModel = "classpath:/org/apache/isis/core/config/presets/SilenceMetaModel.properties";

    public static final String DebugIsisInteraction = "classpath:/org/apache/isis/core/config/presets/DebugIsisInteraction.properties";
    public static final String DebugVaadin = "classpath:/org/apache/isis/core/config/presets/DebugVaadin.properties";

    public static final String SilenceWicket = "classpath:/org/apache/isis/core/config/presets/SilenceWicket.properties";
    public static final String UseLog4j2Test = "classpath:/org/apache/isis/core/config/presets/UseLog4j2Test.properties";

    public static final String H2InMemory_withUniqueSchema = "classpath:/org/apache/isis/core/config/presets/H2InMemory_withUniqueSchema.properties";

    /**
     * Use PROTOTYPING mode as the default. Does not override if the system-property
     * 'PROTOTYPING' was already set.
     */
    public static void prototyping() {
        if(_Strings.isEmpty(System.getProperty("PROTOTYPING"))
                && _Strings.isEmpty(System.getenv("PROTOTYPING"))) {
            forcePrototyping();
        }
    }

    /**
     * Use PROTOTYPING mode, overrides the system-property 'PROTOTYPING', if already
     * set via command-line.
     */
    public static void forcePrototyping() {
        System.setProperty("PROTOTYPING", "true");
    }

    public static void logging(final Class<?> clazz, final String loggingLevel) {
        System.setProperty("logging.level." + clazz.getName(), loggingLevel);
    }


}
