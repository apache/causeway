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
package org.apache.causeway.persistence.jdo.provider.config;

import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManagerFactory;

import org.apache.causeway.core.config.DatanucleusConfiguration;

import lombok.NonNull;

/**
 * Any implementations will be called during bootstrapping, after the {@link PersistenceManagerFactory} has been
 * created.
 *
 * <p>
 *     This is a good way to perform any eager initialization.  However, for the specific use case of creating the
 *     database tables (eg if integration testing) a better approach is to simply set the
 *     {@link DatanucleusConfiguration.Schema.GenerateDatabase#setMode(String) datanucleus.schema.generate-database.mode}
 *     configuration property; there's no need to implement a listener for this use case.
 * </p>
 */
public interface JdoEntityDiscoveryListener {

    /**
     * Called during bootstrapping
     *
     * @param properties - both "datanucleus.*" and "javax.jdo.*".
     */
    public void onEntitiesDiscovered(
            PersistenceManagerFactory persistenceManagerFactory,
            @NonNull Set<Class<?>> entityTypes,
            @NonNull Map<String, Object> properties);

}
