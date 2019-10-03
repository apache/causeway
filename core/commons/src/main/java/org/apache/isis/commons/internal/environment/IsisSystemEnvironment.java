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
package org.apache.isis.commons.internal.environment;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.commons.internal.context._Context;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Represents configuration, that is available in an early phase of bootstrapping. 
 * It is regarded immutable for an application's life-cycle.
 * 
 * @since 2.0
 */
@Service @Singleton @Log4j2
public class IsisSystemEnvironment {

    /**
     * @deprecated - this is provided only as a stepping stone for code that currently uses static method calls
     *               rather than having this bean injected.
     */
    @Deprecated
    public static IsisSystemEnvironment get() {
        return _Context.computeIfAbsent(IsisSystemEnvironment.class, IsisSystemEnvironment::new);
    }

    // -- LIFE-CYCLE
    
    @PostConstruct
    public void postConstruct() {
        // when NOT bootstrapped with Spring, postConstruct() never gets called
        
        // when bootstrapped with Spring, postConstruct() must happen before any call to get() above,
        // otherwise we copy over settings from the primed instance already created with get() above,
        // then on the _Context replace the primed with this one
        val primed = _Context.getIfAny(IsisSystemEnvironment.class);
        if(primed!=null) {
            _Context.remove(IsisSystemEnvironment.class);
            this.setPrototyping(primed.isPrototyping());
            this.setUnitTesting(primed.isUnitTesting());
        }
        _Context.putSingleton(IsisSystemEnvironment.class, this);
    }
    
    @EventListener(ContextClosedEvent.class)
    public void onContextAboutToClose(ContextClosedEvent event) {
        // happens before any @PostConstruct
        // as a consequence, no managed bean should touch the _Context during its post-construct phase
        // as it has already been cleared here
        log.info("Context about to close.");
        _Context.clear();
    }
    
    // -- SETUP
    
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


