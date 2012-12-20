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

package org.apache.isis.core.commons.config;

import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.commons.resource.ResourceStreamSourceChainOfResponsibility;

/**
 * Holds a mutable set of properties representing the configuration.
 * 
 * <p>
 * Mutable/immutable pair with the {@link IsisConfiguration}. To obtain the
 * configuration, use {@link #getConfiguration()}.
 * 
 * @see IsisConfiguration for more details on the mutable/immutable pair
 *      pattern.
 */
public interface IsisConfigurationBuilder extends Injectable {

    /**
     * Returns a currently known {@link IsisConfiguration}.
     */
    IsisConfiguration getConfiguration();

    void addDefaultConfigurationResources();
    
    void addConfigurationResource(final String installerName, final NotFoundPolicy notFoundPolicy);

    void add(final String key, final String value);

    /**
     * The underlying {@link ResourceStreamSource} from which the configuration
     * is being read.
     * 
     * <p>
     * Note that this may be a {@link ResourceStreamSourceChainOfResponsibility composite}.
     */
    ResourceStreamSource getResourceStreamSource();
    
    /**
     * Log a summary of resources found or not found.
     */
    void dumpResourcesToLog();

    void lockConfiguration();
}
