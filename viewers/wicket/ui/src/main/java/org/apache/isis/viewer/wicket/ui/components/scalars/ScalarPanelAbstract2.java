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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.core.metamodel.interactions.managed.PropertyNegotiationModel;
import org.apache.isis.viewer.wicket.model.models.InlinePromptContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.RegularFrame;
import org.apache.isis.viewer.wicket.ui.components.scalars.blobclob.IsisBlobOrClobPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.primitive.BooleanPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.reference.ReferencePanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.valuechoices.ValueChoicesSelect2Panel;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

/**
 *  Adds inline prompt logic.
 */
public abstract class ScalarPanelAbstract2
extends ScalarPanelAbstract {

    private static final long serialVersionUID = 1L;

    // -- INLINE PROMPT LINK

    protected WebMarkupContainer inlinePromptLink;


    // -- CONSTRUCTION

    protected ScalarPanelAbstract2(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    /**
     * Mandatory hook for implementations to indicate whether it supports the {@link PromptStyle#INLINE inline} or
     * {@link PromptStyle#INLINE_AS_IF_EDIT prompt}s, and if so, how.
     * <p>
     * For those that do, both {@link #createFormFrame()} and
     * {@link #createInlinePromptLink()} must return non-null values (and their corresponding markup
     * must define the corresponding elements).
     * <p>
     * Implementations that support inline prompts are: ({@link ScalarPanelAbstract}, {@link ReferencePanel} and
     * {@link ValueChoicesSelect2Panel}; those that don't are {@link IsisBlobOrClobPanelAbstract} and {@link BooleanPanel}.
     */
    protected abstract InlinePromptConfig getInlinePromptConfig();

    @Override
    protected final void setupInlinePrompt() {

        val scalarModel = scalarModel();
        val regularFrame = getRegularFrame();
        val scalarFrameContainer = getScalarFrameContainer();

        val inlinePromptConfig = getInlinePromptConfig();
        if(inlinePromptConfig.isSupported()) {

            regularFrame
                .add(inlinePromptLink = createInlinePromptLink());

            addOnClickBehaviorTo(inlinePromptLink, inlinePromptConfig);

            // even if this particular scalarModel (property) is not configured for inline edits,
            // it's possible that one of the associated actions is.  Thus we set the prompt context
            scalarModel.setInlinePromptContext(
                    new InlinePromptContext(
                            scalarModel,
                            scalarFrameContainer,
                            regularFrame, getFormFrame()));
        }

        addEditPropertyIf(
                scalarModel.canEnterEditMode()
                && (scalarModel.getPromptStyle().isDialog()
                        || !inlinePromptConfig.isSupported()));

    }

    /**
     * Components returning true for {@link #getInlinePromptConfig()}
     * are required to override and return a non-null value.
     */
    protected IModel<String> obtainOutputFormatModel() {
        return ()->{
            val propertyNegotiationModel = (PropertyNegotiationModel)scalarModel().proposedValue();
            return propertyNegotiationModel.isCurrentValueAbsent().booleanValue()
                    ? ""
                    : propertyNegotiationModel
                        .getValueAsHtml().getValue();
                        //.getValueAsParsableText().getValue();
        };
    }

    /**
     * Optional hook.
     */
    protected void onSwitchFormForInlinePrompt(
            final WebMarkupContainer inlinePromptForm,
            final AjaxRequestTarget target) {
    }

    protected void configureInlinePromptLink(final WebMarkupContainer inlinePromptLink) {
        Wkt.cssAppend(inlinePromptLink, obtainInlinePromptLinkCssIfAny());
    }

    protected String obtainInlinePromptLinkCssIfAny() {
        return "form-control form-control-sm";
    }

    protected Component createInlinePromptComponent(
            final String id, final IModel<String> inlinePromptModel) {
        return Wkt.labelNoTab(id, inlinePromptModel);
    }

    // -- HELPER

    private void addOnClickBehaviorTo(
            final @Nullable MarkupContainer clickReceiver,
            final InlinePromptConfig inlinePromptConfig) {

        if(clickReceiver==null) return;

        val scalarModel = scalarModel();

        // start off assuming that neither the property nor any of the associated actions
        // are using inline prompts
        val componentToHideRef = _Refs.<Component>objectRef(clickReceiver);

        if (_Util.canPropertyEnterInlineEditDirectly(scalarModel)) {

            // we configure the prompt link if _this_ property is configured for inline edits...
            Wkt.behaviorAddOnClick(clickReceiver, this::onPropertyInlineEditClick);
            componentToHideRef.setValue(inlinePromptConfig.getComponentToHide().orElse(null));

        } else {

            _Util.lookupPropertyActionForInlineEdit(scalarModel)
            .ifPresent(actionLinkInlineAsIfEdit->{
                Wkt.behaviorAddOnClick(clickReceiver, actionLinkInlineAsIfEdit::onClick);
                componentToHideRef.setValue(inlinePromptConfig.getComponentToHide().orElse(null));
            });
        }

        componentToHideRef.getValue()
            .ifPresent(componentToHide->componentToHide.setVisibilityAllowed(false));
    }

    private WebMarkupContainer createInlinePromptLink() {
        final IModel<String> inlinePromptModel = obtainOutputFormatModel();
        if(inlinePromptModel == null) {
            throw new IllegalStateException(this.getClass().getName()
                    + ": obtainOutputFormatModel() returning null is not compatible "
                    + "with supportsInlinePrompt() returning true ");
        }

        final WebMarkupContainer inlinePromptLink =
                RegularFrame.SCALAR_VALUE_INLINE_PROMPT_LINK
                    .createComponent(WebMarkupContainer::new);

        inlinePromptLink.setOutputMarkupId(true);
        inlinePromptLink.setOutputMarkupPlaceholderTag(true);

        configureInlinePromptLink(inlinePromptLink);

        final Component editInlineLinkLabel = RegularFrame.OUTPUT_FORMAT_CONTAINER
                .createComponent(id->createInlinePromptComponent(id, inlinePromptModel));

        inlinePromptLink.add(editInlineLinkLabel);

        return inlinePromptLink;
    }

    private void onPropertyInlineEditClick(final AjaxRequestTarget target) {
        scalarModel().toEditMode();

        switchRegularFrameToFormFrame();
        onSwitchFormForInlinePrompt(getFormFrame(), target);

        target.add(getScalarFrameContainer());

        Wkt.focusOnMarkerAttribute(getFormFrame(), target);
    }

}
