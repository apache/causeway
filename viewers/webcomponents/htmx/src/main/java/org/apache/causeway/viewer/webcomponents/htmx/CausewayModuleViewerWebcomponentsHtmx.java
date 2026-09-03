/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.htmx;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(HtmxViewerProperties.class)
@Import(HtmxViewerController.class)
public class CausewayModuleViewerWebcomponentsHtmx {

    @Bean
    HtmxRouteCodec htmxRouteCodec(final HtmxViewerProperties properties) {
        return new HtmxRouteCodec(properties.getBasePath());
    }

    @Bean
    HtmxPageFragmentRegistry htmxPageFragmentRegistry(
            final List<HtmxPageFragmentFactory> factories,
            final ApplicationContext applicationContext,
            final HtmxViewerProperties properties) {
        final var resourcePages = new HtmxClasspathPageLoader(
                applicationContext,
                properties.getResourcePageMode()).load();
        return new HtmxPageFragmentRegistry(factories, resourcePages);
    }

    @Bean
    HtmxCollectionPresentationRegistry htmxCollectionPresentationRegistry(
            final ApplicationContext applicationContext,
            final HtmxViewerProperties properties) {
        return new HtmxCollectionPresentationRegistry(
                new HtmxClasspathCollectionPresentationLoader(
                        applicationContext,
                        properties.getResourcePageMode()).load());
    }

    @Bean
    HtmxPreviewRegistry htmxPreviewRegistry(
            final ApplicationContext applicationContext,
            final HtmxViewerProperties properties) {
        return new HtmxPreviewRegistry(
                new HtmxClasspathPreviewLoader(
                        applicationContext,
                        properties.getResourcePageMode()).load());
    }

    @Bean
    HtmxShellDefinition htmxShellDefinition(
            final ApplicationContext applicationContext,
            final HtmxViewerProperties properties) {
        return new HtmxClasspathShellLoader(
                applicationContext,
                properties.getResourcePageMode()).load();
    }

    @Bean
    HtmxPageRenderer htmxPageRenderer(
            final HtmxRouteCodec routeCodec,
            final HtmxViewerProperties properties,
            final HtmxPageFragmentRegistry fragmentRegistry,
            final HtmxShellDefinition shellDefinition) {
        return new HtmxPageRenderer(routeCodec, properties, fragmentRegistry, shellDefinition);
    }
}
