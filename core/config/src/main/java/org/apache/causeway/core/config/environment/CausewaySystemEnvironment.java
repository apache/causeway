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
package org.apache.causeway.core.config.environment;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.ioc._IocContainer;
import org.apache.causeway.core.config.CausewayModuleCoreConfig;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents configuration, that is required in an early bootstrapping phase.
 * Regarded immutable during an application's life-cycle.
 *
 * @since 2.0
 * @implNote acts as the framework's bootstrapping entry-point for Spring
 */
@Service
@Named(CausewayModuleCoreConfig.NAMESPACE + "..CausewaySystemEnvironment")
@Priority(0) // same as PriorityPrecedence#FIRST
@Qualifier("Default")
@Singleton
@Slf4j
public class CausewaySystemEnvironment {

    @Inject private ApplicationContext springContext;

    @Getter private _IocContainer iocContainer;

    // -- LIFE-CYCLE

    @PostConstruct
    public void postConstruct() {

        this.iocContainer = _IocContainer.spring(springContext);

        log.info("postConstruct (hashCode = {})", this.hashCode());

        // when NOT bootstrapped with Spring, postConstruct() never gets called

        // when bootstrapped with Spring, postConstruct() must happen before any call to get() above,
        // otherwise we copy over settings from the primed instance already created with get() above,
        // then on the _Context replace the primed with this one
        var primed = _Context.getIfAny(CausewaySystemEnvironment.class);
        if(primed!=null) {
            _Context.remove(CausewaySystemEnvironment.class);
            this.setPrototyping(primed.isPrototyping());
        }
        _Context.putSingleton(CausewaySystemEnvironment.class, this);
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy (hashCode = {})", this.hashCode());
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed(final ContextRefreshedEvent event) {
        // happens after all @PostConstruct
        log.info("onContextRefreshed");
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextAboutToClose(final ContextClosedEvent event) {
        // happens before any @PostConstruct
        // as a consequence, no managed bean should touch the _Context during its post-construct phase
        // as it has already been cleared here
        log.info("Context about to close.");
        this.iocContainer = null;
        _Context.clear();
    }

    @EventListener(ApplicationFailedEvent.class)
    public void onContextRefreshed(final ApplicationFailedEvent event) {
        // happens eg. when DN finds non enhanced entity classes
        log.error("Application failed to start", event.getException());
    }

    // -- SHORTCUTS

    public _IocContainer ioc() {
        return getIocContainer();
    }

    // -- SETUP

    /**
     * For framework internal unit tests.<p>
     * Let the framework know what context we are running on.
     * Must be set prior to configuration bootstrapping.
     * @param isUnitTesting
     */
    public static void setUnitTesting(final boolean isUnitTesting) {
        System.setProperty("UNITTESTING", ""+isUnitTesting);
    }

    /**
     * To set the framework's deployment-type programmatically.<p>
     * Must be set prior to configuration bootstrapping.
     * @param isPrototyping
     */
    public void setPrototyping(final boolean isPrototyping) {
        System.setProperty("PROTOTYPING", ""+isPrototyping);
    }

    public DeploymentType getDeploymentType() {
        return decideDeploymentType();
    }

    public static boolean isUnitTesting() {
        return "true".equalsIgnoreCase(getProperty("UNITTESTING"));
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
                isSet(getEnv("PROTOTYPING"));

        // system property priming (medium prio)

        anyVoteForPrototyping|=
                isSet(getProperty("PROTOTYPING"));

        anyVoteForPrototyping|=
                "PROTOTYPING".equalsIgnoreCase(getProperty("causeway.deploymentType"));

        // system property override (highest prio)

        anyVoteForProduction|= isNotSet(getProperty("PROTOTYPING"));

        anyVoteForProduction|=
                "PRODUCTION".equalsIgnoreCase(getProperty("causeway.deploymentType"));

        final boolean isPrototyping = anyVoteForPrototyping && !anyVoteForProduction;

        final DeploymentType deploymentType =
                isPrototyping
                        ? DeploymentType.PROTOTYPING
                        : DeploymentType.PRODUCTION;

        return deploymentType;

    }

    private static String getEnv(final String envVar) {
        return trim(System.getenv(envVar));
    }

    private static String getProperty(final String key) {
        return trim(System.getProperty(key));
    }

    private static String trim(final String value) {
        return _Strings.isNullOrEmpty(value) ? null : value.trim();
    }

    private static boolean isSet(final String value) {
        return "true".equalsIgnoreCase(value);
    }

    private static boolean isNotSet(final String value) {
        return "false".equalsIgnoreCase(value);
    }

}
