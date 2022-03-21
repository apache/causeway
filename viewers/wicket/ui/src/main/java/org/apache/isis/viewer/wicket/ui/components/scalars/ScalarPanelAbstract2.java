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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.InlinePromptContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelFactory;
import org.apache.isis.viewer.wicket.ui.components.property.PropertyEditFormPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FrameFragment;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.RegularFrame;
import org.apache.isis.viewer.wicket.ui.components.scalars.blobclob.IsisBlobOrClobPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.primitive.BooleanPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.reference.ReferencePanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.valuechoices.ValueChoicesSelect2Panel;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLink;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;

/**
 *  Adds inline prompt logic.
 */
public abstract class ScalarPanelAbstract2
extends ScalarPanelAbstract {

    private static final long serialVersionUID = 1L;

    // -- INLINE PROMPT LINK

    protected WebMarkupContainer inlinePromptLink;

    // -- INLINE PROMPT FORM CONTAINER

    /**
     * Used by most subclasses
     * ({@link ScalarPanelAbstract}, {@link ReferencePanel}, {@link ValueChoicesSelect2Panel})
     * but not all ({@link IsisBlobOrClobPanelAbstract}, {@link BooleanPanel})
     */
    @Getter(AccessLevel.PROTECTED)
    private WebMarkupContainer inlinePromptFormContainer;

    // -- CONSTRUCTION

    protected ScalarPanelAbstract2(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    /**
     * Mandatory hook for implementations to indicate whether it supports the {@link PromptStyle#INLINE inline} or
     * {@link PromptStyle#INLINE_AS_IF_EDIT prompt}s, and if so, how.
     * <p>
     * For those that do, both {@link #createInlinePromptFormContainer()} and
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
        val frameIfRegular = getRegularFrame();
        val scalarFrameContainer = getScalarFrameContainer();

        scalarFrameContainer.add(inlinePromptFormContainer = createInlinePromptFormContainer());

        val inlinePromptConfig = getInlinePromptConfig();
        if(inlinePromptConfig.isSupported()) {

            frameIfRegular
                .add(inlinePromptLink = createInlinePromptLink());

            // even if this particular scalarModel (property) is not configured for inline edits,
            // it's possible that one of the associated actions is.  Thus we set the prompt context
            scalarModel.setInlinePromptContext(
                    new InlinePromptContext(
                            scalarModel,
                            scalarFrameContainer,
                            frameIfRegular, getInlinePromptFormContainer()));

            // start off assuming that neither the property nor any of the associated actions
            // are using inline prompts

            val componentToHideRef = _Refs.<Component>objectRef(inlinePromptLink);

            if (scalarModel.getPromptStyle().isInline()
                    && scalarModel.canEnterEditMode()) {

                // we configure the prompt link if _this_ property is configured for inline edits...
                Wkt.behaviorAddOnClick(inlinePromptLink, this::onPropertyInlineEditClick);
                componentToHideRef.setValue(inlinePromptConfig.getComponentToHideIfAny());

            } else {

                val inlineActionIfAny =
                        scalarModel.getAssociatedActions().getFirstAssociatedWithInlineAsIfEdit();

                // not editable property, but maybe one of the actions is.
                inlineActionIfAny
                .map(LinkAndLabelFactory.forPropertyOrParameter(scalarModel))
                .map(LinkAndLabel::getUiComponent)
                .map(ActionLink.class::cast)
                .filter(ActionLink::isVisible)
                .filter(ActionLink::isEnabled)
                .ifPresent(actionLinkInlineAsIfEdit->{
                    Wkt.behaviorAddOnClick(inlinePromptLink, actionLinkInlineAsIfEdit::onClick);
                    componentToHideRef.setValue(inlinePromptConfig.getComponentToHideIfAny());
                });
            }

            componentToHideRef.getValue()
            .ifPresent(componentToHide->componentToHide.setVisibilityAllowed(false));
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
    protected IModel<String> obtainInlinePromptModel() {
        return null;
    }

    /**
     * Optional hook.
     */
    protected void onSwitchFormForInlinePrompt(
            final WebMarkupContainer inlinePromptForm,
            final AjaxRequestTarget target) {
    }

    // -- HELPER

    private WebMarkupContainer createInlinePromptLink() {
        final IModel<String> inlinePromptModel = obtainInlinePromptModel();
        if(inlinePromptModel == null) {
            throw new IllegalStateException(this.getClass().getName()
                    + ": obtainInlinePromptModel() returning null is not compatible "
                    + "with supportsInlinePrompt() returning true ");
        }

        final WebMarkupContainer inlinePromptLink =
                new WebMarkupContainer(ID_SCALAR_VALUE_INLINE_PROMPT_LINK);
        inlinePromptLink.setOutputMarkupId(true);
        inlinePromptLink.setOutputMarkupPlaceholderTag(true);

        configureInlinePromptLink(inlinePromptLink);

        final Component editInlineLinkLabel = RegularFrame.OUTPUT_FORMAT_CONTAINER
                .createComponent(id->createInlinePromptComponent(id, inlinePromptModel));

        inlinePromptLink.add(editInlineLinkLabel);

        return inlinePromptLink;
    }

    /**
     * Returns a container holding an empty form.
     * This can be switched out using {@link #switchFormForInlinePrompt(AjaxRequestTarget)}.
     */
    private WebMarkupContainer createInlinePromptFormContainer() {

        val inlinePromptFormContainer = FrameFragment.INLINE_PROMPT_FORM
                .createComponent(WebMarkupContainer::new);
        inlinePromptFormContainer.setOutputMarkupId(true);
        inlinePromptFormContainer.setVisible(false);

        return inlinePromptFormContainer;
    }

    private void onPropertyInlineEditClick(final AjaxRequestTarget target) {
        val scalarModel = scalarModel();
        val scalarFrameContainer = getScalarFrameContainer();

        scalarModel.toEditMode();

        switchFormForInlinePrompt(target);

        getRegularFrame().setVisible(false);
        inlinePromptFormContainer.setVisible(true);

        target.add(scalarFrameContainer);

        Wkt.focusOnMarkerAttribute(inlinePromptFormContainer, target);
    }

    private void switchFormForInlinePrompt(final AjaxRequestTarget target) {
        val scalarFrameContainer = getScalarFrameContainer();

        inlinePromptFormContainer = (PropertyEditFormPanel) getComponentFactoryRegistry()
                .addOrReplaceComponent(
                    scalarFrameContainer,
                    FrameFragment.INLINE_PROMPT_FORM.getContainerId(),
                    ComponentType.PROPERTY_EDIT_FORM,
                    scalarModel());

        onSwitchFormForInlinePrompt(inlinePromptFormContainer, target);
    }

}
