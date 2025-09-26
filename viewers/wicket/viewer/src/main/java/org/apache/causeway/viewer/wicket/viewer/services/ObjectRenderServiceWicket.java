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

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.ResourceReference;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.ObjectSupport.IconSize;
import org.apache.causeway.applib.services.render.ObjectIcon;
import org.apache.causeway.applib.services.render.ObjectIconEmbedded;
import org.apache.causeway.applib.services.render.ObjectIconFa;
import org.apache.causeway.applib.services.render.ObjectIconUrlBased;
import org.apache.causeway.applib.services.render.ObjectRenderService;
import org.apache.causeway.viewer.wicket.model.models.IconResourceReferenceFactory;
import org.apache.causeway.viewer.wicket.viewer.CausewayModuleViewerWicketViewer;

@Service
@Named(CausewayModuleViewerWicketViewer.NAMESPACE + ".ObjectRenderServiceWicket")
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
@Qualifier("Wicket")
public class ObjectRenderServiceWicket implements ObjectRenderService {

    @Inject private IconResourceReferenceFactory iconResourceReferenceFactory;

    @Override
    public String iconToHtml(@Nullable final ObjectIcon objectIcon, final IconSize iconSize) {
        if(objectIcon instanceof ObjectIconUrlBased urlBased) {
            ResourceReference resourceReference = iconResourceReferenceFactory.resourceReferenceForObjectIcon(urlBased);
            // Get the URL for the ResourceReference
            String iconUrl = RequestCycle.get().urlFor(resourceReference, null).toString();
            String fullUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(iconUrl));

            return "<img %s %s/>".formatted(cssClass("objectIcon", iconSize), src(fullUrl));
        }
        if(objectIcon instanceof ObjectIconEmbedded embedded)
            return "<img %s %s/>".formatted(cssClass("objectIconEmbedded", iconSize), src(embedded.dataUri().toExternalForm()));
        if(objectIcon instanceof ObjectIconFa fa)
            return fa.fontAwesomeLayers().toHtml();

        return null;
    }

    private String src(final String url) {
        return "src=\"%s\"".formatted(url);
    }
    private String cssClass(final String primaryCssClass, final IconSize iconSize) {
        return "class=\"%s %s-%s\"".formatted(
            primaryCssClass, primaryCssClass, iconSize.name().toLowerCase());
    }
}
