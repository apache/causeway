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
package org.apache.isis.commons.internal.plugins.environment;

import org.apache.isis.commons.internal.base._Lazy;
import org.springframework.stereotype.Service;

/**
 * Represents configuration, that is available in an early phase of bootstrapping. 
 * It is regarded immutable for an application's life-cycle.
 * 
 * @since 2.0
 */
@Service
public class IsisSystemEnvironment {

    private static _Lazy<IsisSystemEnvironment> singleton = _Lazy.threadSafe(IsisSystemEnvironment::new);

    /**
     * @deprecated - this is provided only as a stepping stone for code that currently uses static method calls
     *               rather than having this bean injected.
     */
    @Deprecated
    public static IsisSystemEnvironment get() {
        return singleton.get();
    }

    // -- INIT

    /**
     * For framework internal unit tests.<p>
     * Let the framework know what context we are running on.
     * Must be set prior to configuration bootstrapping.
     * @param isUnitTesting
     */
    public void setUnitTesting(boolean isUnitTesting) {
        System.setProperty("UNITTESTING", ""+isUnitTesting);
    }

    /**
     * To set the framework's deployment-type programmatically.<p>
     * Must be set prior to configuration bootstrapping.
     * @param isPrototyping
     */
    public void setPrototyping(boolean isPrototyping) {
        System.setProperty("PROTOTYPING", ""+isPrototyping);
    }


    public DeploymentType getDeploymentType() {
        return decideDeploymentType();
    }

    public boolean isUnitTesting() {
        return "true".equalsIgnoreCase(System.getProperty("UNITTESTING"));
    }

    public boolean isPrototyping() {
        return getDeploymentType().isPrototyping();
    }

    // -- HELPER

    private DeploymentType decideDeploymentType() {
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


}


