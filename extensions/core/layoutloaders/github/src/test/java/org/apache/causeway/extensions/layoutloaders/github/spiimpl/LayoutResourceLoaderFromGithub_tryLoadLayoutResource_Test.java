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
package org.apache.causeway.extensions.layoutloaders.github.spiimpl;

import java.nio.charset.StandardCharsets;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.layoutloaders.github.CausewayModuleExtLayoutLoadersGithub;
import org.apache.causeway.extensions.layoutloaders.github.menu.LayoutLoadersGitHubMenu;

import lombok.SneakyThrows;

class LayoutResourceLoaderFromGithub_tryLoadLayoutResource_Test {

    LayoutResourceLoaderFromGithub loader;

    @BeforeEach
    void preconditions() {
        String apiKey = getApiKey();
        assumeThat(apiKey).isNotNull();
    }

    @BeforeEach
    void setup() {

        var causewayConfiguration = CausewayConfiguration.builder().build();
        causewayConfiguration.getExtensions().getLayoutLoaders().getGithub().setApiKey(getApiKey());
        causewayConfiguration.getExtensions().getLayoutLoaders().getGithub().setRepository("apache/causeway-app-simpleapp");

        var module = new CausewayModuleExtLayoutLoadersGithub();
        var restTemplateForSearch = module.restTemplateForGithubSearch(causewayConfiguration);
        var restTemplateForContent = module.restTemplateForGithubContent(causewayConfiguration);
        var layoutLoaderMenu = new LayoutLoadersGitHubMenu(causewayConfiguration);
        var queryResultsCache = new QueryResultsCache();

        layoutLoaderMenu.new enableDynamicLayoutLoading().act();
        Assertions.assertThat(layoutLoaderMenu.isEnabled()).isTrue();

        loader = new LayoutResourceLoaderFromGithub(restTemplateForSearch, restTemplateForContent, causewayConfiguration, layoutLoaderMenu, () -> queryResultsCache);
    }

    @Test
    public void happy_case() {

        var layoutResourceIfAny = loader.lookupLayoutResource(SimpleObject.class, "SimpleObject.layout.xml");
        assertThat(layoutResourceIfAny).isPresent();

    }

    @Test
    public void sad_case() {

        var layoutResourceIfAny = loader.lookupLayoutResource(SimpleObject.class, "Unknown.layout.xml");
        assertThat(layoutResourceIfAny).isEmpty();

    }

    @SneakyThrows
    private String getApiKey() {
        return _Resources.loadAsString(getClass(), "apikey.txt", StandardCharsets.UTF_8);
    }

    // unused
    static class SimpleObject {
    }
}
