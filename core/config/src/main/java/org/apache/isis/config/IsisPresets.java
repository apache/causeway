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
package org.apache.isis.config;

/**
 * Supports Spring's {@code @PropertySource} annotation.
 * 
 * @since 2.0
 *
 */
public final class IsisPresets  {

    public static final String NoTranslations = "classpath:/presets/NoTranslations.properties";
    
    public static final String H2InMemory = "classpath:/presets/H2InMemory.properties";
    
    /** randomized (unique) database name, eg. to allow for concurrent testing */
    public static final String H2InMemory_withUniqueSchema = "classpath:/presets/H2InMemory_withUniqueSchema.properties";
    
    public static final String HsqlDbInMemory = "classpath:/presets/HsqlDbInMemory.properties";
    public static final String DataNucleusAutoCreate = "classpath:/presets/DataNucleusAutoCreate.properties";
    
    public static final String DebugPersistence = "classpath:/presets/DebugPersistence.properties";
    public static final String DebugDiscovery = "classpath:/presets/DebugDiscovery.properties";
    
    public static final String DebugProgrammingModel = "classpath:/presets/DebugProgrammingModel.properties";
    public static final String SilenceProgrammingModel = "classpath:/presets/SilenceProgrammingModel.properties";
    
    public static final String DebugMetaModel = "classpath:/presets/DebugMetaModel.properties";
    public static final String SilenceMetaModel = "classpath:/presets/SilenceMetaModel.properties";
    
    /**
     * @deprecated seems no longer required anyway
     */
    @Deprecated
    public static final String IsisIntegTest = "classpath:/presets/IsisIntegTest.properties";
    
    /**
     * Use PROTOTYPING mode as the default. Does not override if the system-property 
     * 'PROTOTYPING' was already set.
     */
    public static void prototyping() {
        if(System.getProperty("PROTOTYPING")==null) {
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

    public static void logging(Class<?> clazz, String loggingLevel) {
        System.setProperty("logging.level." + clazz.getName(), loggingLevel);
    }

}
