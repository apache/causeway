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
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.ioc.IocContainer;
import org.apache.isis.commons.internal.ioc.spring.IocContainerSpring;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Represents configuration, that is required in an early bootstrapping phase. 
 * Regarded immutable during an application's life-cycle.
 * 
 * @since 2.0
 * @implNote acts as the framework's bootstrapping entry-point for Spring  
 */
@Service @Singleton @Log4j2
public class IsisSystemEnvironment {
    
    public static final String VERSION = "2.0.0-M3"; // landed here, but could be anywhere else if reasonable
    
    @Inject private ApplicationContext springContext;
    
    @Getter private IocContainer iocContainer; 
    
    // -- LIFE-CYCLE
    
    @PostConstruct
    public void postConstruct() {
        
        this.iocContainer = IocContainerSpring.of(springContext);
        
        System.err.println("####### IsisSystemEnvironment postconst " + this.hashCode());    
        
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
    
    @PreDestroy
    public void preDestroy() {
        System.err.println("####### IsisSystemEnvironment destroy " + this.hashCode());
    }
    
    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed(ContextRefreshedEvent event) {
        // happens after all @PostConstruct
        log.info("Context was refreshed.");
    }
    
    @EventListener(ContextClosedEvent.class)
    public void onContextAboutToClose(ContextClosedEvent event) {
        // happens before any @PostConstruct
        // as a consequence, no managed bean should touch the _Context during its post-construct phase
        // as it has already been cleared here
        log.info("Context about to close.");
        this.iocContainer = null;
        _Context.clear();
    }
    
    // -- SHORTCUTS
    
    public IocContainer ioc() {
        return getIocContainer();
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


