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

import org.apache.causeway.commons.internal.base._StableValue;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.ioc.SpringContextHolder;
import org.apache.causeway.core.config.CausewayModuleCoreConfig;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents configuration, that is required in an early bootstrapping phase.
 * Regarded immutable during an application's life-cycle.
 *
 * @apiNote acts as the framework's bootstrapping entry-point for Spring
 * @implNote not a record, since {@link SpringContextHolder} reference is cleared on pre-destroy
 * @since 2.0
 */
@Service
@Named(CausewayModuleCoreConfig.NAMESPACE + ".CausewaySystemEnvironment")
@Priority(0) // same as PriorityPrecedence#FIRST
@Qualifier("Default")
@Slf4j
public class CausewaySystemEnvironment {

    @Getter @Accessors(fluent=true)
    private SpringContextHolder springContextHolder;

    @Getter @Accessors(fluent=true)
    private final DeploymentType deploymentType;

    @Autowired
    public CausewaySystemEnvironment(final ApplicationContext springContext) {
        this.springContextHolder = new SpringContextHolder(springContext);
        this.deploymentType = deploymentTypeFromSpringContextOrSystemEnvironment(springContext.getEnvironment());
        log.info("init for {} (hashCode = {})", deploymentType, this.hashCode());
    }

	//JUnit
    public CausewaySystemEnvironment(final DeploymentType deploymentType) {
        this.springContextHolder = null;
        this.deploymentType = deploymentType;
    }

    // -- LIFE-CYCLE

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
        // happens before any @PreDestroy
        // as a consequence, no managed bean should touch the _Context during its pre-detroy phase
        // as it has already been cleared here
        log.info("Context about to close.");
        this.springContextHolder = null;
        _Context.clear();
    }

    @EventListener(ApplicationFailedEvent.class)
    public void onApplicationFailed(final ApplicationFailedEvent event) {
        // happens eg. when DN finds non enhanced entity classes
        log.error("Application failed to start", event.getException());
    }

    // -- SETUP

    /**
     * Whether a Spring context is missing. (But could be integration testing instead.)
     */
    public boolean isUnitTesting() {
        return springContextHolder==null;
    }

    public boolean isIntegrationTesting() {
        return _isIntegrationTesting.orElseSet(CausewaySystemEnvironment::checkWhetherIntegrationTesting);
    }

    public boolean isPrototyping() {
        return deploymentType.isPrototyping();
    }

    // -- UTIL

    public static DeploymentType deploymentTypeFromSpringContextOrSystemEnvironment(final Environment environment) {

    	// preferred for integration tests @SpringBootTest(properties = {"causeway.deploymentType=PROTOTYPING"})
    	final @Nullable String deploymentTypeFromSpring = environment.getProperty("causeway.deploymentType");
    	final @Nullable String deploymentTypeFromSystemProperties = getProperty("causeway.deploymentType");

        boolean anyVoteForPrototyping = false;
        boolean anyVoteForProduction = false;

        // system environment priming (lowest priority)

        anyVoteForPrototyping|= isSet(getEnv("PROTOTYPING"));

        // system property priming (medium priority)

        anyVoteForPrototyping|= isSet(getProperty("PROTOTYPING"));

        anyVoteForPrototyping|= "PROTOTYPING".equalsIgnoreCase(deploymentTypeFromSystemProperties);
        anyVoteForPrototyping|= "PROTOTYPING".equalsIgnoreCase(deploymentTypeFromSpring);

        // system property override (highest priority)

        anyVoteForProduction|= isNotSet(getProperty("PROTOTYPING"));
        anyVoteForProduction|= "PRODUCTION".equalsIgnoreCase(deploymentTypeFromSystemProperties);
        anyVoteForProduction|= "PRODUCTION".equalsIgnoreCase(deploymentTypeFromSpring);

        var isPrototyping = anyVoteForPrototyping
        		&& !anyVoteForProduction;
        return isPrototyping
            ? DeploymentType.PROTOTYPING
            : DeploymentType.PRODUCTION;
    }

    // -- HELPER

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

    private final _StableValue<Boolean> _isIntegrationTesting = new _StableValue<Boolean>();
    /**
     * Whether we find Spring's ContextCache on the class path.
     */
    private static boolean checkWhetherIntegrationTesting() {
        try {
            Class.forName("org.springframework.test.context.cache.ContextCache");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
