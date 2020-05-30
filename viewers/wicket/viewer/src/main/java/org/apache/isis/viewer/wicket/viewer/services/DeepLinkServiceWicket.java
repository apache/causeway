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
package org.apache.isis.viewer.wicket.viewer.services;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.wicket.Page;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.linking.DeepLinkService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.viewer.wicket.model.models.PageParameterUtil;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;

/**
 * An implementation of {@link org.apache.isis.applib.services.linking.DeepLinkService}
 * for Wicket Viewer
 */
@Service
@Named("isisWicketViewer.DeepLinkServiceWicket")
@Order(OrderPrecedence.EARLY)
@Qualifier("Wicket")
public class DeepLinkServiceWicket implements DeepLinkService {

    @Inject private PageClassRegistry pageClassRegistry;
    @Inject private SpecificationLoader specificationLoader;

    @Override
    public URI deepLinkFor(final Object domainObject) {

        final ManagedObject objectAdapter = ManagedObject.of(specificationLoader::loadSpecification, domainObject); 
                
        final PageParameters pageParameters = PageParameterUtil.createPageParametersForObject(objectAdapter);

        final Class<? extends Page> pageClass = pageClassRegistry.getPageClass(PageType.ENTITY);

        final RequestCycle requestCycle = RequestCycle.get();
        final CharSequence urlForPojo = requestCycle.urlFor(pageClass, pageParameters);
        final String fullUrl = requestCycle.getUrlRenderer().renderFullUrl(Url.parse(urlForPojo));
        try {
            return new URI(fullUrl);
        } catch (final URISyntaxException ex) {
            throw new RuntimeException("Cannot create a deep link to domain object: " + domainObject, ex);
        }
    }


}
