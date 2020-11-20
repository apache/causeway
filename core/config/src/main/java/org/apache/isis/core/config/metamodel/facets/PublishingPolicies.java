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
package org.apache.isis.core.config.metamodel.facets;

import org.apache.isis.core.config.IsisConfiguration;

import lombok.NonNull;

public final class PublishingPolicies {

    // -- ACTIONS
    
    public static enum ActionPublishingPolicy {
        ALL,
        IGNORE_SAFE,
        /**
         * Alias for {@link #IGNORE_SAFE}
         */
        IGNORE_QUERY_ONLY,
        NONE;
    }
    
    // -- PROPERTIES
    
    public enum PropertyPublishingPolicy {
        ALL,
        NONE;
    }
    
    // -- ENTITIES
    
    public enum EntityChangePublishingPolicy {
        ALL,
        NONE;
    }

    // -- FACTORIES
    
    public static ActionPublishingPolicy actionCommandPublishingPolicy(
            final @NonNull IsisConfiguration configuration) {
        return configuration.getApplib().getAnnotation().getAction().getCommandPublishing();
    }
    
    public static ActionPublishingPolicy actionExecutionPublishingPolicy(
            final @NonNull IsisConfiguration configuration) {
        return configuration.getApplib().getAnnotation().getAction().getExecutionPublishing();
    }
    
    public static PropertyPublishingPolicy propertyCommandPublishingPolicy(
            final @NonNull IsisConfiguration configuration) {
        return configuration.getApplib().getAnnotation().getProperty().getCommandPublishing();
    }
    
    public static PropertyPublishingPolicy propertyExecutionPublishingPolicy(
            final @NonNull IsisConfiguration configuration) {
        return configuration.getApplib().getAnnotation().getProperty().getExecutionPublishing();
    }
    
    public static EntityChangePublishingPolicy entityChangePublishingPolicy(
            final @NonNull IsisConfiguration configuration) {
        return configuration.getApplib().getAnnotation().getDomainObject().getEntityChangePublishing();
    }

    
}
