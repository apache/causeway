/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */
package org.apache.causeway.regressiontests.referenceapp.htmx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.config.util.SpringProfileUtil;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.persistence.jpa.eclipselink.CausewayModulePersistenceJpaEclipselink;
import org.apache.causeway.regressiontests.referenceapp.support.ReferenceAppDeterministicFixtureConfiguration;
import org.apache.causeway.viewer.graphql.viewer.CausewayModuleViewerGraphqlViewer;
import org.apache.causeway.viewer.webcomponents.htmx.CausewayModuleViewerWebcomponentsHtmx;
import org.apache.causeway.viewer.wicket.viewer.CausewayModuleViewerWicketViewer;

import demoapp.web.ReferenceAppManifestJpa;

@SpringBootApplication
@Import({
        CausewayModuleCoreRuntimeServices.class,
        CausewayModulePersistenceJpaEclipselink.class,
        CausewayModuleViewerGraphqlViewer.class,
        CausewayModuleViewerWebcomponentsHtmx.class,
        CausewayModuleViewerWicketViewer.class,
        ReferenceAppManifestJpa.class,
        ReferenceAppDeterministicFixtureConfiguration.class
})
@PropertySources({
        @PropertySource(CausewayPresets.H2InMemory_withUniqueSchema),
        @PropertySource(CausewayPresets.SilenceMetaModel),
        @PropertySource(CausewayPresets.SilenceProgrammingModel)
})
public class ReferenceAppHtmxApplication extends SpringBootServletInitializer {

    static {
        CausewayPresets.prototyping();
    }

    public static void main(final String[] args) {
        SpringProfileUtil.removeActiveProfile("demo-jdo");
        SpringProfileUtil.addActiveProfile("demo-jpa");
        SpringApplication.run(ReferenceAppHtmxApplication.class, args);
    }
}
