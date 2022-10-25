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

import java.io.Serializable;
import java.util.Optional;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.value.semantics.Renderer.SyntaxHighlighter;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmRenderUtil;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.viewer.commons.model.scalar.UiParameter;
import org.apache.causeway.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.causeway.viewer.wicket.model.models.ValueModel;

import lombok.Builder;
import lombok.Value;
import lombok.val;

public class MarkupComponent extends WebComponent {

    private static final long serialVersionUID = 1L;

    @Value @Builder
    public static class Options implements Serializable {
        private static final long serialVersionUID = 1L;

        @Builder.Default
        private SyntaxHighlighter syntaxHighlighter = SyntaxHighlighter.NONE;

        public static Options defaults() {
            return Options.builder().build();
        }

        private _HighlightBehavior highlightBehavior() {
            return _HighlightBehavior.valueOf(getSyntaxHighlighter());
        }

    }

    // -- CONSTRUCTION

    private final Options options;

    public MarkupComponent(final String id, final IModel<?> model, final Options options) {
        super(id, model);
        this.options = options;
    }

    public MarkupComponent(final String id, final IModel<?> model) {
        this(id, model, Options.defaults());
    }

    // --

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        options.highlightBehavior().renderHead(response);
    }

    @Override
    public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag){
        val htmlContent = extractHtmlOrElse(getDefaultModelObject(), "" /*fallback*/);
        replaceComponentTagBody(markupStream, openTag,
                options.highlightBehavior().htmlContentPostProcess(htmlContent));
    }

    @Override
    protected void onComponentTag(final ComponentTag tag)	{
        super.onComponentTag(tag);
        tag.setType(TagType.OPEN);
    }

    // -- HELPER

    protected CharSequence extractHtmlOrElse(final Object modelObject, final String fallback) {

        if(modelObject==null) {
            return fallback;
        }

        if(modelObject instanceof ManagedObject) {
            val adapter = (ManagedObject) modelObject;
            val feature = lookupObjectFeatureIn(getDefaultModel()).orElse(null);
            val asHtml = MmRenderUtil.htmlStringForValueType(adapter, feature);
            return asHtml != null
                ? asHtml
                : fallback;
        }

        return modelObject.toString();
    }

    // -- HELPER

    protected Optional<ObjectFeature> lookupObjectFeatureIn(final IModel<?> model) {
        if(model instanceof ScalarPropertyModel) {
            return Optional.of(((ScalarPropertyModel)model).getMetaModel());
        }
        if(model instanceof UiParameter) {
            return Optional.of(((UiParameter)model).getMetaModel());
        }
        if(model instanceof ValueModel) {
            return Optional.ofNullable(((ValueModel)model).getActionModelHint())
                    .map(act->act.getAction());
        }
        return Optional.empty();
    }

}
