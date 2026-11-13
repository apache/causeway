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

import java.util.Map;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.resource.LayoutResource;
import org.apache.causeway.applib.layout.resource.LayoutResourceLoader;
import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.layoutloaders.github.CausewayModuleExtLayoutLoadersGithub;
import org.apache.causeway.extensions.layoutloaders.github.menu.LayoutLoadersGitHubMenu;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import jakarta.annotation.Priority;
import jakarta.inject.Named;
import jakarta.inject.Provider;

@Service
@Named(CausewayModuleExtLayoutLoadersGithub.NAMESPACE + ".LayoutResourceLoaderFromGithub")
@Priority(PriorityPrecedence.MIDPOINT - 100)
@Qualifier("Github")
//@Slf4j
public record LayoutResourceLoaderFromGithub(
		@Qualifier("GithubSearch")  RestClient restClientForSearch,
        @Qualifier("GithubContent") RestClient restClientForContent,
        CausewayConfiguration causewayConfiguration,
        LayoutLoadersGitHubMenu layoutLoadersGitHubMenu,
        Provider<QueryResultsCache> queryResultsCacheProvider)
implements LayoutResourceLoader {

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
            var repo = causewayConfiguration.extensions().layoutLoaders().github().repository();
            var q = "filename:%s+repo:%s".formatted(candidateResourceName, repo);

            var responseEntity = restClientForSearch
            		.get()
            		.uri("/search/code?q={q}", Map.of("q", q))
            		.retrieve()
            		.toEntity(new ParameterizedTypeReference<GitHubResponse>() {});

            GitHubResponse searchResponse = responseEntity.getBody();
            if (searchResponse.getTotal_count() != 1)
				return Try.empty();
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

        if(path==null)
        	return Try.empty();

        try {
            var contentResponse = restClientForContent
            		.get()
            		.uri("/contents/" + path)
            		.retrieve()
            		.toEntity(String.class);
            var content = contentResponse.getBody();

            return StringUtils.hasLength(content)
                    ? Try.call(()->new LayoutResource(candidateResourceName, NamedWithMimeType.CommonMimeType.XML, content))
                    : Try.empty();

        } catch (Exception ex) {
            return Try.failure(ex);
        }
    }

}
