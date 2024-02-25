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

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.protocol.http.WebApplication;

import org.springframework.context.annotation.Configuration;

import org.apache.causeway.viewer.wicket.model.causeway.WicketApplicationInitializer;
import org.apache.causeway.viewer.wicket.ui.pages.common.datatables.DatatablesCssBootstrap5ReferenceWkt;
import org.apache.causeway.viewer.wicket.ui.pages.common.datatables.DatatablesCssReferenceWkt;
import org.apache.causeway.viewer.wicket.ui.pages.common.datatables.DatatablesJavaScriptBootstrap5ReferenceWkt;
import org.apache.causeway.viewer.wicket.ui.pages.common.datatables.DatatablesJavaScriptReferenceWkt;

@Configuration
public class DatatablesNetInitWkt implements WicketApplicationInitializer {

    @Override
    public void init(final WebApplication webApplication) {

        webApplication.getHeaderContributorListeners().add(new IHeaderContributor() {
            private static final long serialVersionUID = 1L;
            @Override
            public void renderHead(final IHeaderResponse response) {
                response.render(DatatablesJavaScriptReferenceWkt.asHeaderItem());
                response.render(DatatablesJavaScriptBootstrap5ReferenceWkt.asHeaderItem());
                response.render(DatatablesCssReferenceWkt.asHeaderItem());
                response.render(DatatablesCssBootstrap5ReferenceWkt.asHeaderItem());
            }
        });
    }

}
