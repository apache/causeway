/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */
package org.apache.causeway.regressiontests.referenceapp.support;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;

import demoapp.dom._infra.fixtures.DemoFixtureScript;

@Configuration(proxyBeanMethods = false)
public class ReferenceAppDeterministicFixtureConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = "causeway.regressiontests.referenceapp.fixtures.enabled",
            havingValue = "true",
            matchIfMissing = true)
    ApplicationRunner referenceAppDeterministicFixtureInstaller(
            final FixtureScripts fixtureScripts,
            final InteractionService interactionService) {
        return arguments -> interactionService.runAnonymous(() ->
                fixtureScripts.runFixtureScript(new DemoFixtureScript(), "referenceapp-regression"));
    }
}
