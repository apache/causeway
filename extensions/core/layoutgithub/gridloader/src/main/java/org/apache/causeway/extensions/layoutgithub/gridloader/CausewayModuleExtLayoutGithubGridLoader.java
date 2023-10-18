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
package org.apache.causeway.extensions.layoutgithub.gridloader;

import lombok.val;

import javax.inject.Named;

import org.apache.causeway.core.config.CausewayConfiguration;

import org.apache.causeway.extensions.layoutgithub.gridloader.spi.LayoutResourceLoaderFromGithub;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.applib.layout.menubars.Menu;
import org.apache.causeway.extensions.layoutgithub.gridloader.menu.GridLoaderMenu;

import org.springframework.web.client.RestTemplate;

/**
 * Adds the {@link Menu} with its auto-configured menu entries.
 * @since 2.0 {@index}
 */
@Configuration
@Import({
    // menu providers
    GridLoaderMenu.class,

    // services
    LayoutResourceLoaderFromGithub.class,

})
// keep class-name in sync with CausewayExtSecmanRegularUserRoleAndPermissions
public class CausewayModuleExtLayoutGithubGridLoader {

    // keep in sync with CausewayExtLayoutGithubRoleAndPermissions.NAMESPACE
    public static final String NAMESPACE = "causeway.ext.layoutGithub";

    /**
     * Returns a template configured to search for a file in the git repo.
     *
     * <p>
     * Append for example:
     *
     * <pre>?q=SimpleObject.layout.xml+in:path+repo:apache/causeway-app-simpleapp</pre>
     *
     * <p>
     * Returns for example:
     *
     * <pre>
     * {
     *   "total_count": 1,
     *   "incomplete_results": false,
     *   "items": [
     *     {
     *       "name": "SimpleObject.layout.xml",
     *       "path": "module-simple/src/main/java/domainapp/modules/simple/dom/so/SimpleObject.layout.xml",
     *       ...
     *     },
     *      ...
     *    ]
     *    ...
     * }
     * </pre>
     * @param causewayConfiguration
     * @return
     */
    @Bean(name = "GithubSearch")
    public RestTemplate restTemplateGithubSearch(final CausewayConfiguration causewayConfiguration) {

        val apiKey = causewayConfiguration.getExtensions().getLayoutGithub().getApiKey();

        return new RestTemplateBuilder()
                .rootUri("https://api.github.com/search/code")
                .additionalInterceptors((request, body, execution) -> {
                    request.getHeaders().add("Authorization", "Bearer " + apiKey);
                    request.getHeaders().add("Accept", "application/vnd.github+json");
                    request.getHeaders().add("X-GitHub-Api-Version", "2022-11-28");
                    return execution.execute(request, body);
                })
                .build();
    }

    /**
     * Returns a template configured to obtain the content of a file in a repo (from the default branch).
     *
     * Append for example:
     *
     * <pre>module-simple/src/main/java/domainapp/modules/simple/dom/so/SimpleObject.layout.xml?branch=<branchName></branchName></pre>
     *
     * The <i>branchName</i> can be obtained from {@link GridLoaderMenu#getBranch()}.
          * @param causewayConfiguration
     * @return
     */
    @Bean(name = "GithubContent")
    public RestTemplate restTemplateGithubContent(final CausewayConfiguration causewayConfiguration) {

        val apiKey = causewayConfiguration.getExtensions().getLayoutGithub().getApiKey();
        val repo = causewayConfiguration.getExtensions().getLayoutGithub().getRepository();

        return new RestTemplateBuilder()
                .rootUri(String.format("https://api.github.com/repos/%s/contents/", repo))
                .additionalInterceptors((request, body, execution) -> {
                    request.getHeaders().add("Authorization", "Bearer " + apiKey);
                    request.getHeaders().add("Accept", "application/vnd.github.v3.raw");
                    request.getHeaders().add("X-GitHub-Api-Version", "2022-11-28");
                    return execution.execute(request, body);
                })
                .build();
    }
}
