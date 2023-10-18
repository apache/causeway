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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.services.grid.GridLoaderService;
import org.apache.causeway.applib.services.grid.GridMarshallerService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.services.grid.GridLoaderServiceDefault;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResource;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResourceLoader;
import org.apache.causeway.extensions.layoutgithub.gridloader.CausewayModuleExtLayoutGithubGridLoader;
import org.apache.causeway.extensions.layoutgithub.gridloader.menu.GridLoaderMenu;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Named(CausewayModuleExtLayoutGithubGridLoader.NAMESPACE + ".LayoutResourceLoaderFromGithub")
@Priority(PriorityPrecedence.MIDPOINT - 100)
@Qualifier("Github")
@Log4j2
public class LayoutResourceLoaderFromGithub implements LayoutResourceLoader {

    @Inject @Qualifier("GithubSearch") RestTemplate restTemplateForSearch;
    @Inject @Qualifier("GithubContent") RestTemplate restTemplateForContent;

    @Override
    public Optional<LayoutResource> tryLoadLayoutResource(@NonNull Class<?> type, @NonNull String candidateResourceName) {
        Map<String, String> params = new HashMap<>();
        // ?q=SimpleObject.layout.xml+in:path+repo:apache/causeway-app-simpleapp
        params.put("q", String.format("%s+in:path+repo:%s", candidateResourceName, causewayConfiguration.getExtensions().getLayoutGithub().getRepository()));
        ResponseEntity<String> responseEntity = restTemplateForSearch.exchange("", HttpMethod.GET, null, String.class);
        throw new RuntimeException("not yet implemented");
    }

    @Inject CausewayConfiguration causewayConfiguration;
    @Inject GridLoaderMenu gridLoaderMenu;
}
