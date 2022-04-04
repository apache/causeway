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
import org.apache.wicket.markup.html.basic.Label;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.viewer.wicket.model.models.InlinePromptContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.CompactFragment;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FieldFragement;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FieldFrame;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.PromptFragment;
import org.apache.isis.viewer.wicket.ui.components.scalars.markup.MarkupComponent;
import org.apache.isis.viewer.wicket.ui.panels.FormExecutorDefault;
import org.apache.isis.viewer.wicket.ui.util.Wkt;
import org.apache.isis.viewer.wicket.ui.util.WktTooltips;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;

/**
 *  Adds inline prompt logic.
 */
public abstract class ScalarPanelAbstract2
extends ScalarPanelAbstract {

    private static final long serialVersionUID = 1L;

    // -- FIELD FRAME

    @Getter(AccessLevel.PROTECTED)
    protected MarkupContainer fieldFrame;

    // -- INLINE PROMPT LINK

    protected WebMarkupContainer inlinePromptLink;

    // -- CONSTRUCTION

    protected ScalarPanelAbstract2(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    @Override
    protected final void setupInlinePrompt() {

        val scalarModel = scalarModel();
        val regularFrame = getRegularFrame();
        val fieldFrame = getFieldFrame();
        val scalarFrameContainer = getScalarFrameContainer();

        // even if this particular scalarModel (property) is not configured for inline edits,
        // it's possible that one of the associated actions is.  Thus we set the prompt context
        scalarModel.setInlinePromptContext(
                new InlinePromptContext(
                        scalarModel,
                        scalarFrameContainer,
                        regularFrame,
                        getFormFrame()));

        if(FieldFragement.LINK.isMatching(fieldFrame)) {

            fieldFrame
                .addOrReplace(inlinePromptLink = createInlinePromptLink());

            // needs InlinePromptContext to properly initialize
            addOnClickBehaviorTo(inlinePromptLink);
        }

        //XXX support for legacy panels, remove eventually
        {
            if(fieldFrame!=null
                && fieldFrame.get(ID_SCALAR_VALUE)==null) {
                Wkt.labelAdd(fieldFrame, ID_SCALAR_VALUE, "∅");
            }
        }
    }

    /**
     * Builds the component to render the model when in COMPACT frame,
     * or when in REGULAR frame rendering the OUTPUT-FORMAT.
     * <p>
     * The (textual) default implementation uses a {@link Label}.
     * However, it may be overridden if required.
     */
    protected Component createComponentForOutput(final String id) {
        if(getFormatModifiers().contains(FormatModifier.MULITLINE)
                && !getFormatModifiers().contains(FormatModifier.MARKUP)
                && !getRenderScenario().isCompact()) {
            return PromptFragment.TEXTAREA
                    .createFragment(id, this, scalarValueId->{
                        val textArea = Wkt.textAreaNoTab(scalarValueId, this::obtainOutputFormat);
                        if(this instanceof ScalarPanelTextFieldAbstract) {
                            ((ScalarPanelTextFieldAbstract)this).setFormComponentAttributes(textArea);
                        }
                        return textArea;
                    });
        }
        return CompactFragment.LABEL
                    .createFragment(id, this, scalarValueId->
                        new MarkupComponent(scalarValueId, this::obtainOutputFormat));
    }

    /**
     * Output format (usually HTML) as String, for any non editing scenario.
     */
    protected String obtainOutputFormat() {
        return _Strings.nonEmpty(
                    scalarModel().proposedValue().getValueAsHtml().getValue())
                .orElseGet(()->translate(ValueSemanticsAbstract.NULL_REPRESENTATION));
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

    // -- HELPER

    private void addOnClickBehaviorTo(
            final @Nullable MarkupContainer clickReceiver) {

        if(clickReceiver==null) return;

        val scalarModel = scalarModel();

        if (_Util.canPropertyEnterInlineEditDirectly(scalarModel)) {

            _Util.lookupMixinForCompositeValueUpdate(scalarModel)
            .ifPresentOrElse(mixinForCompositeValueEdit->{
                // composite value type support
                Wkt.behaviorAddOnClick(clickReceiver, mixinForCompositeValueEdit.getUiComponent()::onClick);
            },()->{
                // we configure the prompt link if _this_ property is configured for inline edits...
                Wkt.behaviorAddOnClick(clickReceiver, this::onPropertyInlineEditClick);
            });

        } else {

            _Util.lookupPropertyActionForInlineEdit(scalarModel)
            .ifPresent(actionLinkInlineAsIfEdit->{
                Wkt.behaviorAddOnClick(clickReceiver, actionLinkInlineAsIfEdit.getUiComponent()::onClick);
            });
        }
    }

    private WebMarkupContainer createInlinePromptLink() {

        final WebMarkupContainer inlinePromptLink =
                FieldFrame.SCALAR_VALUE_INLINE_PROMPT_LINK
                    .createComponent(WebMarkupContainer::new);

        inlinePromptLink.setOutputMarkupId(true);
        inlinePromptLink.setOutputMarkupPlaceholderTag(true);
        configureInlinePromptLink(inlinePromptLink);

        Wkt.add(inlinePromptLink, FieldFrame.SCALAR_VALUE_CONTAINER
                .createComponent(id->createComponentForOutput(id)));

        val buttonContainer = FieldFragement.LINK.createButtonContainer(inlinePromptLink);

        // add clear-field-button (only if feature is not required and not already cleared)
        val isClearFieldButtonVisible =
                scalarModel().proposedValue().isPresent()
                    && !scalarModel().isRequired();

        if(isClearFieldButtonVisible) {
            val clearFieldButton = Wkt.linkAddWithBody(buttonContainer,
                    Wkt.faIcon("fa-regular fa-trash-can"), this::onClearFieldButtonClick);

            Wkt.cssAppend(clearFieldButton, "btn-warning");
            WktTooltips.addTooltip(clearFieldButton, translate("Click to clear the field"));
        }

        return inlinePromptLink;
    }

    private void onPropertyInlineEditClick(final AjaxRequestTarget target) {
        scalarModel().toEditMode();

        switchRegularFrameToFormFrame();
        onSwitchFormForInlinePrompt(getFormFrame(), target);

        target.add(getScalarFrameContainer());

        Wkt.focusOnMarkerAttribute(getFormFrame(), target);
    }

    private void onClearFieldButtonClick(final AjaxRequestTarget target) {
        scalarModel().proposedValue().clear();
        scalarModel().getSpecialization().accept(
                param->{
                    this.setupInlinePrompt(); // recreate the param field
                    target.add(this);
                },
                prop->{
                    FormExecutorDefault.forProperty(prop)
                        .executeAndProcessResults(target, null, prop);
                });
    }

}
