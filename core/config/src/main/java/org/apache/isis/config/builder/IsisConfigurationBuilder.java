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
package org.apache.isis.config.builder;

import java.util.List;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.Module;
import org.apache.isis.applib.PropertyResource;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.NotFoundPolicy;
import org.apache.isis.config.resource.ResourceStreamSource;

public interface IsisConfigurationBuilder {
    
    void addPropertyResource(PropertyResource propertyResource);

    void addResourceStreamSource(ResourceStreamSource resourceStreamSource);
    void addResourceStreamSources(ResourceStreamSource... resourceStreamSources);
    void addResourceStreamSources(List<ResourceStreamSource> resourceStreamSources);

    /**
     * Registers the configuration resource (usually, a file) with the specified
     * name from the first {@link ResourceStreamSource} available.
     *
     * <p>
     * If the configuration resource cannot be found then the provided
     * {@link NotFoundPolicy} determines whether an exception is thrown or not.
     */
    void addConfigurationResource(String configurationResource, NotFoundPolicy notFoundPolicy,
            IsisConfigurationDefault.ContainsPolicy containsPolicy);

    /**
     * Adds additional property; if already present then will _not_ be replaced.
     */
    void add(String key, String value);

    /**
     * Adds/updates property; if already present then _will_ be replaced.
     */
    void put(String key, String value);

    void primeWith(Primer primer);

    String peekAtString(String key);
    String peekAtString(String key, String defaultValue);

    boolean peekAtBoolean(String key);
    boolean peekAtBoolean(String key, boolean defaultValue);

    String[] peekAtList(String key);
    
    /**
     * Walks the module tree starting at the topModule to resolve all key/value pairs that 
     * contribute to the configuration.
     * @param topModule
     * @since 2.0.0-M2
     */
    void addTopModule(Module topModule);
    
    /**
     * @param appManifest
     * @since 2.0.0-M2
     */
    void addAppManifest(AppManifest appManifest);

    /** internal only **/
    IsisConfiguration build();

    /**
     * Log a summary of resources found or not found.
     */
    void dumpResourcesToLog();
    
    // -- PRIMING
    
    public interface Primer {
        void prime(IsisConfigurationBuilder isisConfigurationBuilder);
    }

    // -- FACTORIES
    
    static IsisConfigurationBuilder getDefault() {
        return IsisConfigurationBuilderDefault.getDefault();
    }

    static IsisConfigurationBuilder empty() {
        return IsisConfigurationBuilderDefault.empty();
    }

    

}