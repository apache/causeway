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
package org.apache.causeway.viewer.wicket.viewer.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.wicket.Page;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.homepage.HomePageResolverService;
import org.apache.causeway.applib.services.linking.DeepLinkService;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;

import lombok.RequiredArgsConstructor;

/**
 * An implementation of {@link org.apache.causeway.applib.services.linking.DeepLinkService}
 * for Wicket Viewer.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@Service
@Named("causeway.viewer.wicket.DeepLinkServiceWicket")
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@Qualifier("Wicket")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class DeepLinkServiceWicket implements DeepLinkService {

    private final PageClassRegistry pageClassRegistry;
    private final SpecificationLoader specificationLoader;
    private final HomePageResolverService homePageResolverService;

    // also supports ManagedObject and Bookmark
    @Override
    public URI deepLinkFor(final @Nullable Object domainObject) {

        var requestCycle = RequestCycle.get();

        var pageParameters = pageParametersFor(domainObject)
                .or(()->pageParametersFor(homePageResolverService.getHomePage()))
                .orElseThrow(()->
                    new RuntimeException("Cannot create a deep link to domain object: " + domainObject));

        final Class<? extends Page> pageClass = pageClassRegistry.getPageClass(PageType.ENTITY);
        final CharSequence urlForPojo = requestCycle.urlFor(pageClass, pageParameters);
        final String fullUrl = requestCycle.getUrlRenderer().renderFullUrl(Url.parse(urlForPojo));
        try {
            return new URI(fullUrl);
        } catch (final URISyntaxException ex) {
            throw new RuntimeException("Cannot create a deep link to domain object: " + domainObject, ex);
        }
    }

    /**
     * Introduced to allow for optimized framework internal use as well.
     */
    private Optional<PageParameters> pageParametersFor(final @Nullable Object anyObject) {
        if(anyObject==null) {
            return Optional.empty();
        }
        try {
            if(anyObject instanceof ManagedObject) {
                return Optional.ofNullable(PageParameterUtils.createPageParametersForObject((ManagedObject) anyObject));
            }
            if(anyObject instanceof Bookmark) {
                return Optional.ofNullable(PageParameterUtils.createPageParametersForBookmark((Bookmark) anyObject));
            }
            final ManagedObject objectAdapter = ManagedObject.adaptSingular(specificationLoader, anyObject);
            return Optional.ofNullable(PageParameterUtils.createPageParametersForObject(objectAdapter));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
