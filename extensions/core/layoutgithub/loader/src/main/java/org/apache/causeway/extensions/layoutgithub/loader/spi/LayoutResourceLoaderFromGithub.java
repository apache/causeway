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
package org.apache.causeway.extensions.layoutgithub.loader.spi;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.util.HashMap;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResource;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResourceLoader;
import org.apache.causeway.extensions.layoutgithub.loader.CausewayModuleExtLayoutGithubLoader;

import org.apache.causeway.extensions.layoutgithub.loader.menu.LayoutLoaderMenu;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Named(CausewayModuleExtLayoutGithubLoader.NAMESPACE + ".LayoutResourceLoaderFromGithub")
@Priority(PriorityPrecedence.MIDPOINT - 100)
@Qualifier("Github")
@Log4j2
public class LayoutResourceLoaderFromGithub implements LayoutResourceLoader {

    final RestTemplate restTemplateForSearch;
    final RestTemplate restTemplateForContent;
    final CausewayConfiguration causewayConfiguration;
    final LayoutLoaderMenu layoutLoaderMenu;
    final QueryResultsCache queryResultsCache;

    @Inject
    public LayoutResourceLoaderFromGithub(
            final @Qualifier("GithubSearch")  RestTemplate restTemplateForSearch,
            final @Qualifier("GithubContent") RestTemplate restTemplateForContent,
            final CausewayConfiguration causewayConfiguration,
            final LayoutLoaderMenu layoutLoaderMenu,
            final QueryResultsCache queryResultsCache) {
        this.restTemplateForSearch = restTemplateForSearch;
        this.restTemplateForContent = restTemplateForContent;
        this.causewayConfiguration = causewayConfiguration;
        this.layoutLoaderMenu = layoutLoaderMenu;
        this.queryResultsCache = queryResultsCache;
    }

    @Override
    public Optional<LayoutResource> tryLoadLayoutResource(
            final @NonNull Class<?> type,
            final @NonNull String candidateResourceName) {
        if (!layoutLoaderMenu.isEnabled()) {
            return Optional.empty();
        }
        return queryResultsCache.execute(() -> tryLoadLayoutResource(candidateResourceName),
                getClass(), "tryLoadLayoutResource", candidateResourceName);
    }

    private Optional<LayoutResource> tryLoadLayoutResource(String candidateResourceName) {
        return search(candidateResourceName)
                .flatMap(x -> content(candidateResourceName, x));
    }

    /**
     * eg:
     * <code>/search/code?q=SimpleObject.layout.xml+in:path+repo:apache/causeway-app-simpleapp</code>
     */
    private Optional<String> search(final @NonNull String candidateResourceName) {

        try {
            val searchParams = new HashMap<String,String>() {{
                val repo = causewayConfiguration.getExtensions().getLayoutGithub().getRepository();
                put("q", String.format("%s+in:path+repo:%s", candidateResourceName, repo));
            }};
            val responseEntity = restTemplateForSearch.exchange("/search/code?q={q}", HttpMethod.GET, null, new ParameterizedTypeReference<GitHubResponse>() {}, searchParams);

            GitHubResponse searchResponse = responseEntity.getBody();
            if (searchResponse.getTotal_count() != 1) {
                return Optional.empty();
            }
            return Optional.of(searchResponse.getItems().get(0).getPath());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /**
     * eg:
     * <code>/contents/module-simple/src/main/java/domainapp/modules/simple/dom/so/SimpleObject.layout.xml</code>
     */
    private Optional<LayoutResource> content(
            final @NonNull String candidateResourceName,
            final @NonNull String path) {

        try {
            val contentResponse = restTemplateForContent.exchange("/contents/" + path, HttpMethod.GET, null, String.class);
            val content = contentResponse.getBody();

            return Optional
                    .ofNullable(content)
                    .map(body -> new LayoutResource(candidateResourceName, NamedWithMimeType.CommonMimeType.XML, content));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

}

