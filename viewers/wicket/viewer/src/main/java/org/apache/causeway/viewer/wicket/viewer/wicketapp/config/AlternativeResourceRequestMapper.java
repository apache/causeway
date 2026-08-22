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
package org.apache.causeway.viewer.wicket.viewer.wicketapp.config;

import java.util.List;
import java.util.function.Supplier;

import org.apache.causeway.viewer.commons.model.webjar.WebJar;
import org.apache.wicket.core.request.mapper.ResourceReferenceMapper;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.caching.IResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.ResourceUrl;
import org.apache.wicket.util.string.Strings;

import de.agilecoders.wicket.webjars.request.resource.IWebjarsResourceReference;

/**
 * In support of non standard conforming webjars like those for Vega.
 *
 * <p>Inspired by de.agilecoders.wicket.webjars.request.WebjarsCDNRequestMapper
 */
class AlternativeResourceRequestMapper implements IRequestMapper {

	private final ResourceReferenceMapper chain;
	private final Supplier<IResourceCachingStrategy> cachingStrategyProvider;

	AlternativeResourceRequestMapper(final WebApplication webApplication){
        Supplier<String> parentFolderPlaceholderProvider = () -> webApplication.getResourceSettings().getParentFolderPlaceholder();
        cachingStrategyProvider = () -> webApplication.getResourceSettings().getCachingStrategy();
        this.chain = new ResourceReferenceMapper(new PageParametersEncoder(), parentFolderPlaceholderProvider, cachingStrategyProvider);
	}

	@Override
    public Url mapHandler(final IRequestHandler requestHandler) {
        if (isWebjarsResourceReference(requestHandler)) {
            final Url url = chain.mapHandler(requestHandler);
            final String urlString = urlToStringWithNoVersion(url);
            final int index = urlString.indexOf(WebJar.ROOT);
            if (index < 0)
            	return null;

			var newUrl = Url.parse(Strings.join("/", "..", urlString.substring(index + WebJar.ROOT.length())));
        	return newUrl;
        }
        return null;
    }

	// -- HELPER

    /**
     * @param url to remove version from
     * @return the string representation of the {@link Url} with any version info removed
     */
    private String urlToStringWithNoVersion(final Url url) {
        final Url copy = new Url(url);
        final List<String> segments = copy.getSegments();

        if (!segments.isEmpty()) {
            final int lastSegmentIndex = segments.size() - 1;
            final String filename = segments.get(lastSegmentIndex);

            if (!Strings.isEmpty(filename)) {
                final ResourceUrl resourceUrl = new ResourceUrl(filename, new PageParameters());

                cachingStrategyProvider.get().undecorateUrl(resourceUrl);

                if (Strings.isEmpty(resourceUrl.getFileName()))
					throw new IllegalStateException(
                            "caching strategy returned empty name for "
                            + resourceUrl);

                segments.set(lastSegmentIndex, resourceUrl.getFileName());
            }
        }

        return copy.toString();
    }
    private static boolean isWebjarsResourceReference(final IRequestHandler requestHandler) {
        if (requestHandler instanceof final ResourceReferenceRequestHandler resourceReferenceRequestHandler) {
            final ResourceReference resourceReference = resourceReferenceRequestHandler.getResourceReference();

            if (resourceReference instanceof IWebjarsResourceReference)
				return true;
        }

        return false;
    }

    @Override
    public IRequestHandler mapRequest(final Request request) {
        return null;
    }

    @Override
    public int getCompatibilityScore(final Request request) {
        return 0;
    }

}