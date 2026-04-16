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

import jakarta.inject.Inject;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.CssResourceReference;

import org.springframework.context.annotation.Configuration;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.viewer.commons.model.css.CausewayCssResources;
import org.apache.causeway.viewer.wicket.model.causeway.WicketApplicationInitializer;

// fa icon overlay support, etc.
@Configuration
public class CausewaySupplementalCssInitWkt implements WicketApplicationInitializer {

    @Inject ServiceRegistry serviceRegistry;

    @Override
    public void init(final WebApplication webApplication) {
        webApplication.getHeaderContributorListeners().add(new IHeaderContributor() {
            private static final long serialVersionUID = 1L;

            final static CssResourceReference CSS_REF =
                    new CssResourceReference(CausewayCssResources.class, CausewayCssResources.RESOURCE_NAME);

            @Override
            public void renderHead(final IHeaderResponse response) {
                response.render(CssHeaderItem.forReference(CSS_REF));
            }
        });
    }

}
