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

import java.util.HashMap;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResource;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResourceLoader;
import org.apache.causeway.extensions.layoutloaders.github.CausewayModuleExtLayoutLoadersGithub;
import org.apache.causeway.extensions.layoutloaders.github.menu.LayoutLoadersGitHubMenu;

import lombok.NonNull;

@Service
@Named(CausewayModuleExtLayoutLoadersGithub.NAMESPACE + ".LayoutResourceLoaderFromGithub")
@Priority(PriorityPrecedence.MIDPOINT - 100)
@Qualifier("Github")
//@Log4j2
public class LayoutResourceLoaderFromGithub implements LayoutResourceLoader {

    final RestTemplate restTemplateForSearch;
    final RestTemplate restTemplateForContent;
    final CausewayConfiguration causewayConfiguration;
    final LayoutLoadersGitHubMenu layoutLoadersGitHubMenu;
    final Provider<QueryResultsCache> queryResultsCacheProvider;

    @Inject
    public LayoutResourceLoaderFromGithub(
            final @Qualifier("GithubSearch")  RestTemplate restTemplateForSearch,
            final @Qualifier("GithubContent") RestTemplate restTemplateForContent,
            final CausewayConfiguration causewayConfiguration,
            final LayoutLoadersGitHubMenu layoutLoadersGitHubMenu,
            final Provider<QueryResultsCache> queryResultsCacheProvider) {
        this.restTemplateForSearch = restTemplateForSearch;
        this.restTemplateForContent = restTemplateForContent;
        this.causewayConfiguration = causewayConfiguration;
        this.layoutLoadersGitHubMenu = layoutLoadersGitHubMenu;
        this.queryResultsCacheProvider = queryResultsCacheProvider;
    }

    @Override
    public Try<LayoutResource> tryLoadLayoutResource(
            final @NonNull Class<?> type,
            final @NonNull String candidateResourceName) {
        return layoutLoadersGitHubMenu.isEnabled()
                ? queryResultsCacheProvider.get().execute(() -> tryLoadLayoutResource(candidateResourceName),
                        getClass(), "tryLoadLayoutResource", candidateResourceName)
                : Try.empty();
    }

    private Try<LayoutResource> tryLoadLayoutResource(final String candidateResourceName) {
        return search(candidateResourceName)
            .flatMapSuccessAsNullable(path->content(candidateResourceName, path));
    }

    /**
     * eg:
     * <code>/search/code?q=SimpleObject.layout.xml+in:path+repo:apache/causeway-app-simpleapp</code>
     */
    private Try<String> search(final @NonNull String candidateResourceName) {

        try {
            var repo = causewayConfiguration.getExtensions().getLayoutLoaders().getGithub().getRepository();
            var searchParams = new HashMap<String, String>();
            searchParams.put("q", String.format("%s+in:path+repo:%s", candidateResourceName, repo));

            var responseEntity = restTemplateForSearch
                    .exchange("/search/code?q={q}", HttpMethod.GET, null,
                            new ParameterizedTypeReference<GitHubResponse>() {}, searchParams);

            GitHubResponse searchResponse = responseEntity.getBody();
            if (searchResponse.getTotal_count() != 1) {
                return Try.empty();
            }
            return Try.success(searchResponse.getItems().get(0).getPath());
        } catch (Exception ex) {
            return Try.failure(ex);
        }
    }

    /**
     * eg:
     * <code>/contents/module-simple/src/main/java/domainapp/modules/simple/dom/so/SimpleObject.layout.xml</code>
     */
    private Try<LayoutResource> content(
            final @NonNull String candidateResourceName,
            final @Nullable String path) {

        if(path==null) return Try.empty();

        try {
            var contentResponse = restTemplateForContent.exchange("/contents/" + path, HttpMethod.GET, null, String.class);
            var content = contentResponse.getBody();

            return StringUtils.hasLength(content)
                    ? Try.call(()->new LayoutResource(candidateResourceName, NamedWithMimeType.CommonMimeType.XML, content))
                    : Try.empty();

        } catch (Exception ex) {
            return Try.failure(ex);
        }
    }

}
