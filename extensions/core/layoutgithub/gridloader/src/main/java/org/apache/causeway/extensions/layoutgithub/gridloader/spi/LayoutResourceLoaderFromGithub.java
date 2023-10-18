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
package org.apache.causeway.extensions.layoutgithub.gridloader.spi;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.util.HashMap;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResource;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResourceLoader;
import org.apache.causeway.extensions.layoutgithub.gridloader.CausewayModuleExtLayoutGithubLoader;

import org.apache.causeway.extensions.layoutgithub.gridloader.menu.LayoutLoaderMenu;

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

    @Inject
    public LayoutResourceLoaderFromGithub(
            @Qualifier("GithubSearch") final RestTemplate restTemplateForSearch,
            @Qualifier("GithubContent") final RestTemplate restTemplateForContent,
            final CausewayConfiguration causewayConfiguration,
            final LayoutLoaderMenu layoutLoaderMenu) {
        this.restTemplateForSearch = restTemplateForSearch;
        this.restTemplateForContent = restTemplateForContent;
        this.causewayConfiguration = causewayConfiguration;
        this.layoutLoaderMenu = layoutLoaderMenu;
    }

    @Override
    public Optional<LayoutResource> tryLoadLayoutResource(@NonNull Class<?> type, @NonNull String candidateResourceName) {
        return search(candidateResourceName)
              .flatMap(x -> content(x, candidateResourceName));
    }

    private Optional<String> search(String candidateResourceName) {

        // /search/code?q=SimpleObject.layout.xml+in:path+repo:apache/causeway-app-simpleapp
        try {
            val searchParams = new HashMap<String,String>() {{
                put("q", String.format("%s+in:path+repo:%s", candidateResourceName, causewayConfiguration.getExtensions().getLayoutGithub().getRepository()));
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

    private Optional<LayoutResource> content(String candidateResourceName, String path) {

        // /contents/module-simple/src/main/java/domainapp/modules/simple/dom/so/SimpleObject.layout.xml?branch=branchName
        try {

            String url = "/contents/" + path;
            val contentParams = new HashMap<String, String>();

            val branchIfAny = layoutLoaderMenu.getBranch();
            if (branchIfAny.isPresent()) {
                url += "?branch={branch}";
                contentParams.put("branch", branchIfAny.get());
            }

            val contentResponse = restTemplateForContent.exchange(url, HttpMethod.GET, null, String.class, contentParams);
            val content = contentResponse.getBody();

            return Optional
                    .ofNullable(content)
                    .map(body -> new LayoutResource(candidateResourceName, NamedWithMimeType.CommonMimeType.XML, content));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

}

