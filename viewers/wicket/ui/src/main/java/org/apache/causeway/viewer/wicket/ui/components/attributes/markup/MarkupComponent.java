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
package org.apache.causeway.viewer.wicket.ui.components.attributes.markup;

import java.io.Serializable;
import java.util.Optional;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.model.IModel;
import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

import org.apache.causeway.applib.value.semantics.Renderer.SyntaxHighlighter;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmValueUtils;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.viewer.commons.model.attrib.UiParameter;
import org.apache.causeway.viewer.wicket.model.models.PropertyModel;
import org.apache.causeway.viewer.wicket.model.models.ValueModel;

public class MarkupComponent extends WebComponent {

    private static final long serialVersionUID = 1L;

    public record Options(SyntaxHighlighter syntaxHighlighter) implements Serializable {
        public static Options defaults() {
            return new Options(SyntaxHighlighter.NONE);
        }
    }

    // -- CONSTRUCTION

    private final Options options;

    public MarkupComponent(final String id, final IModel<?> model, final @Nullable Options options) {
        super(id, model);
        this.options = options == null
            ? Options.defaults()
            : options;
    }

    public MarkupComponent(final String id, final IModel<?> model) {
        this(id, model, null);
    }

    // --

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        highlightBehavior()
            .ifPresent(highlighter->highlighter.renderHead(response));
    }

    @Override
    public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
        htmlContent()
            .ifPresentOrElse(html -> {
                replaceComponentTagBody(markupStream, openTag,
                    highlightBehavior().map(highlighter->highlighter.htmlContentPostProcess(html)).orElse(html));
            }, ()->{
                replaceComponentTagBody(markupStream, openTag, "");
            });
    }

    @Override
    protected void onComponentTag(final ComponentTag tag)	{
        super.onComponentTag(tag);
        tag.setType(TagType.OPEN);
    }

    // -- HELPER

    private Optional<HighlightBehavior> highlightBehavior() {
        return HighlightBehavior.lookup(options.syntaxHighlighter());
    }

    /**
     * Optionally returns the underlying model's HTML representation,
     * based on whether it is available and has length.
     */
    protected Optional<String> htmlContent() {
        var modelObject = getDefaultModelObject();

        if(modelObject==null) return Optional.empty();

        if(modelObject instanceof ManagedObject managedObj) {
            var feature = lookupObjectFeatureIn().orElse(null);
            var asHtml = MmValueUtils.htmlStringForValueType(feature, managedObj);
            return StringUtils.hasLength(asHtml)
                ? Optional.of(asHtml)
                : Optional.empty();
        }

        return Optional.ofNullable(modelObject.toString());
    }

    // -- HELPER

    protected Optional<ObjectFeature> lookupObjectFeatureIn() {
        var model = getDefaultModel();

        if(model instanceof PropertyModel propertyModel) {
            return Optional.of(propertyModel.getMetaModel());
        }
        if(model instanceof UiParameter uiParameter) {
            return Optional.of(uiParameter.getMetaModel());
        }
        if(model instanceof ValueModel valueModel) {
            return valueModel.objectMember().map(ObjectFeature.class::cast);
        }
        return Optional.empty();
    }

}
