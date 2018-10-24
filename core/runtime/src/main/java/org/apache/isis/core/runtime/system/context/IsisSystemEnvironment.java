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

package org.apache.isis.core.runtime.system.context;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.runtime.system.DeploymentType;

/**
 * Represents configuration, that is available in an early phase of bootstrapping 
 * and should is regarded immutable during application's life-cycle.
 * 
 * @since 2.0.0-M2
 */
public interface IsisSystemEnvironment {

    public DeploymentCategory getDeploymentCategory();

    public static IsisSystemEnvironment of(IsisConfiguration config) {
        
        final String deploymentTypeLiteral = config.getString("isis.deploymentType");
        if(_Strings.isNullOrEmpty(deploymentTypeLiteral)) {
            return getDefault();
        }
        
        // at this point, the deploymentType seem explicitly set via config, so we override any 
        // environment variables that might be present
        
        // throws if type can not be parsed
        final DeploymentType deploymentType = DeploymentType.lookup(deploymentTypeLiteral);
        
        switch(deploymentType.getDeploymentCategory()) {
        case PROTOTYPING:
            return getPrototyping();
        default:
            return getProduction();
        }
    }    
    
    // -- DEFAULT IMPLEMENTATIONS 
    
    public static IsisSystemEnvironment getDefault() {
        return () -> {
            final DeploymentCategory deploymentCategory =
                    "true".equalsIgnoreCase(System.getenv("PROTOTYPING"))
                        ? DeploymentCategory.PROTOTYPING
                                : DeploymentCategory.PRODUCTION;

            return deploymentCategory;
        };
    }
    
    public static IsisSystemEnvironment getPrototyping() {
        return () -> DeploymentCategory.PROTOTYPING;
    }
    
    public static IsisSystemEnvironment getProduction() {
        return () -> DeploymentCategory.PRODUCTION;
    }

    

    
    
}


