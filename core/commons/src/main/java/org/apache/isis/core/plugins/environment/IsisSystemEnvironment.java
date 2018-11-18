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
package org.apache.isis.core.plugins.environment;

/**
 * Represents configuration, that is available in an early phase of bootstrapping. 
 * It is regarded immutable for an application's life-cycle.
 * 
 * @since 2.0.0-M2
 */
public interface IsisSystemEnvironment {

    // -- INTERFACE
    
    public DeploymentType getDeploymentType();
    
    // -- FACTORIES
    
    public static IsisSystemEnvironment getDefault() {
        return DEFAULT;
    }
    
    public static IsisSystemEnvironment of(DeploymentType deploymentType) {
        return ()->deploymentType;        
    }

    // -- DEFAULT IMPLEMENTATION
    
    public static final IsisSystemEnvironment DEFAULT = new IsisSystemEnvironment() {
        @Override
        public DeploymentType getDeploymentType() {
            boolean anyVoteForPrototyping = false;
            boolean anyVoteForProduction = false;
            
            // system environment priming (lowest prio)
            
            anyVoteForPrototyping|=
                    "true".equalsIgnoreCase(System.getenv("PROTOTYPING"));
            
            // system property priming (medium prio)
            
            anyVoteForPrototyping|=
                    "true".equalsIgnoreCase(System.getProperty("PROTOTYPING"));
            
            anyVoteForPrototyping|=
                    "PROTOTYPING".equalsIgnoreCase(System.getProperty("isis.deploymentType"));
            
            // system property override (highest prio)
            
            anyVoteForProduction|=
                    "false".equalsIgnoreCase(System.getProperty("PROTOTYPING"));
            
            anyVoteForProduction|=
                    "PRODUCTION".equalsIgnoreCase(System.getProperty("isis.deploymentType"));
            
            final boolean isPrototyping = anyVoteForPrototyping && !anyVoteForProduction;
            
            final DeploymentType deploymentType =
                    isPrototyping
                        ? DeploymentType.PROTOTYPING
                                : DeploymentType.PRODUCTION;
            
            return deploymentType;
        }
    };
        
    
}


