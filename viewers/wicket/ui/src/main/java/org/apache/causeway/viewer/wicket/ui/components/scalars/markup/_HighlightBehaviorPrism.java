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
package org.apache.causeway.viewer.wicket.ui.components.scalars.markup;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.ResourceReference;

import org.apache.causeway.viewer.commons.prism.Prism;
import org.apache.causeway.viewer.wicket.ui.util.PrismResourcesWkt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class _HighlightBehaviorPrism implements _HighlightBehavior {

    private final Prism theme;

    @Getter(lazy = true) private final ResourceReference cssResourceReference =
            PrismResourcesWkt.cssResource(theme);

    @Getter(lazy = true) private final List<ResourceReference> jsResourceReferences =
            PrismResourcesWkt.jsResources(theme);

    @Override
    public void renderHead(final IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(getCssResourceReference()));
        for(ResourceReference jsRef : getJsResourceReferences()) {
            response.render(JavaScriptHeaderItem.forReference(jsRef));
        }
    }

    @Override
    public CharSequence htmlContentPostProcess(final CharSequence htmlContent) {
        return MarkupComponent_reloadJs.decorate(htmlContent, getJsResourceReferences());
    }

}
