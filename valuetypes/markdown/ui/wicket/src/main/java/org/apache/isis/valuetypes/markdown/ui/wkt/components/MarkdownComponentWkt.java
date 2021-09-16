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
package org.apache.isis.valuetypes.markdown.ui.wkt.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.ResourceReference;

import org.apache.isis.valuetypes.prism.wkt.PrismResourcesWkt;
import org.apache.isis.viewer.wicket.ui.components.scalars.markup.MarkupComponent;
import org.apache.isis.viewer.wicket.ui.components.scalars.markup.MarkupComponent_reloadJs;

import lombok.val;

public class MarkdownComponentWkt extends MarkupComponent {

    private static final long serialVersionUID = 1L;

    public MarkdownComponentWkt(final String id, final IModel<?> model) {
        super(id, model);
    }

    @Override
    public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
        val htmlContent = extractHtmlOrElse(getDefaultModelObject(), "" /*fallback*/);
        replaceComponentTagBody(markupStream, openTag,
                MarkupComponent_reloadJs.decorate(htmlContent, jsRef()));
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(PrismResourcesWkt.getCssResourceReferenceWkt()));
        response.render(JavaScriptHeaderItem.forReference(jsRef()));
    }

    private static final ResourceReference jsRef() {
        return PrismResourcesWkt.getJsResourceReferenceWkt();
    }

}
