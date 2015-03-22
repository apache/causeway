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

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.wicket.Page;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.guice.GuiceBeanProvider;
import org.apache.isis.applib.services.linking.DeepLinkService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassListDefault;

/**
 * An implementation of {@link org.apache.isis.applib.services.linking.DeepLinkService}
 * for Wicket Viewer
 */
@DomainService(
        nature = NatureOfService.DOMAIN
)
public class DeepLinkServiceWicket implements DeepLinkService {

    @Programmatic
    @Override
    public URI deepLinkFor(final Object domainObject) {

        final AdapterManagerDefault adapterManager = getAdapterManager();
        final ObjectAdapter objectAdapter = adapterManager.adapterFor(domainObject);
        final PageParameters pageParameters = EntityModel.createPageParameters(objectAdapter);

        PageClassRegistry pageClassRegistry = guiceBeanProvider.lookup(PageClassRegistry.class);
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

    protected AdapterManagerDefault getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    @Inject
    private GuiceBeanProvider guiceBeanProvider;
}
