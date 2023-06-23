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
package org.apache.causeway.viewer.wicket.ui.components.pops;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.causeway.viewer.commons.model.components.UiString;
import org.apache.causeway.viewer.wicket.model.models.InlinePromptContext;
import org.apache.causeway.viewer.wicket.model.models.PopModel;
import org.apache.causeway.viewer.wicket.ui.components.pops.PopFragmentFactory.CompactFragment;
import org.apache.causeway.viewer.wicket.ui.components.pops.PopFragmentFactory.FieldFragement;
import org.apache.causeway.viewer.wicket.ui.components.pops.PopFragmentFactory.FieldFrame;
import org.apache.causeway.viewer.wicket.ui.components.pops.PopFragmentFactory.PromptFragment;
import org.apache.causeway.viewer.wicket.ui.panels.FormExecutorDefault;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.BootstrapFileInputField;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 *  Adds inline prompt logic.
 */
public abstract class PopPanelAbstract2
extends PopPanelAbstract {

    private static final long serialVersionUID = 1L;

    // -- FIELD FRAME

    @Getter(AccessLevel.PROTECTED)
    protected MarkupContainer fieldFrame;

    // -- INLINE PROMPT LINK

    protected WebMarkupContainer inlinePromptLink;

    // -- CONSTRUCTION

    protected PopPanelAbstract2(final String id, final PopModel popModel) {
        super(id, popModel);
    }

    @Override
    protected final void setupInlinePrompt() {

        val popModel = popModel();
        val regularFrame = getRegularFrame();
        val fieldFrame = getFieldFrame();
        val scalarFrameContainer = getScalarFrameContainer();

        // even if this particular popModel (property) is not configured for inline edits,
        // it's possible that one of the associated actions is.  Thus we set the prompt context
        popModel.setInlinePromptContext(
                new InlinePromptContext(
                        popModel,
                        scalarFrameContainer,
                        regularFrame,
                        getFormFrame()));


        FieldFragement.matching(fieldFrame)
        .ifPresent(fieldFragement->{

            switch (fieldFragement) {
            case LINK_TO_PROMT: {

                fieldFrame
                    .addOrReplace(inlinePromptLink = createInlinePromptLink());

                // needs InlinePromptContext to properly initialize
                addOnClickBehaviorTo(inlinePromptLink);

                val additionalButtonContainer = fieldFragement.createButtonContainer(inlinePromptLink);
                addAdditionalButtonsTo(additionalButtonContainer, fieldFragement);
                return;
            }
            case NO_LINK_VIEWING:
            case NO_LINK_EDITING: {

                val additionalButtonContainer = fieldFragement.createButtonContainer(fieldFrame);
                addAdditionalButtonsTo(additionalButtonContainer, fieldFragement);

                return;
            }
            default:
                break;
            }
        });

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
        if(isUsingTextarea()) {
            return PromptFragment.TEXTAREA
                    .createFragment(id, this, scalarValueId->{
                        val textArea = Wkt.textAreaNoTab(scalarValueId, this::outputFormatAsString);
                        val popModel = popModel();
                        Wkt.setFormComponentAttributes(textArea,
                                popModel::multilineNumberOfLines,
                                popModel::maxLength,
                                popModel::typicalLength);
                        return textArea;
                    });
        }
        return CompactFragment.LABEL
                    .createFragment(id, this, scalarValueId->
                        Wkt.labelWithDynamicEscaping(scalarValueId, this::obtainOutputFormat));
    }


    // -- SEMANTICS

    private boolean isUsingTextarea() {
        if(getRenderScenario().isCompact()
                || getFormatModifiers().contains(FormatModifier.MARKUP)
                || !getFormatModifiers().contains(FormatModifier.MULTILINE)) {
            return false;
        }
        // only render a text-area if it has content
        return !popModel().isEmpty();
    }

    /**
     * @see #obtainOutputFormat()
     */
    protected final String outputFormatAsString() {
        return obtainOutputFormat().getString();
    }

    /**
     * Output format (usually HTML) as String, for any non editing scenario.
     * <p>
     * Usually HTML, except for (non-empty) text-areas or badges (that are already modeled in HTML).
     */
    protected UiString obtainOutputFormat() {
        val proposedValue = popModel().proposedValue();
        if(!proposedValue.isPresent()) {
            return UiString.markup(
                    getPlaceholderRenderService()
                    .asHtml(PlaceholderLiteral.NULL_REPRESENTATION));
        }
        return isUsingTextarea()
                || getFormatModifiers().contains(FormatModifier.TEXT_ONLY)
                        ? UiString.text(proposedValue.getValueAsTitle().getValue())
                        : UiString.markup(proposedValue.getValueAsHtml().getValue());
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

    @Override
    protected void onMakeNotEditable(final String disableReason) {
        this.setupInlinePrompt(); // recreate additional buttons
    }

    @Override
    protected void onMakeEditable() {
        this.setupInlinePrompt(); // recreate additional buttons
    }

    // -- HELPER

    private void addOnClickBehaviorTo(
            final @Nullable MarkupContainer clickReceiver) {

        if(clickReceiver==null) return;

        val popModel = popModel();

        if (_Util.canPropertyEnterInlineEditDirectly(popModel)) {

            _Util.lookupMixinForCompositeValueUpdate(popModel)
            .ifPresentOrElse(mixinForCompositeValueEdit->{
                // composite value type support
                Wkt.behaviorAddOnClick(clickReceiver, mixinForCompositeValueEdit.getUiComponent()::onClick);
            },()->{
                // we configure the prompt link if _this_ property is configured for inline edits...
                Wkt.behaviorAddOnClick(clickReceiver, this::onPropertyInlineEditClick);
            });

        } else {

            _Util.lookupPropertyActionForInlineEdit(popModel)
            .ifPresent(actionLinkInlineAsIfEdit->{
                Wkt.behaviorAddOnClick(clickReceiver, actionLinkInlineAsIfEdit.getUiComponent()::onClick);
            });
        }
    }

    private void addAdditionalButtonsTo(
            final @NonNull RepeatingView buttonContainer, final FieldFragement fieldFragement) {

        for(var additionalButton : PopPanelAdditionalButton.values()) {
            if(additionalButton.isVisible(popModel(), getRenderScenario(), fieldFragement)) {
                switch (additionalButton) {
                case COPY_TO_CLIPBOARD:
                    //XXX Future extension
                    break;
                case DISABLED_REASON:
                    addDisabledReasonIcon(buttonContainer, "fa-solid fa-ban", "");
                    break;
                case DISABLED_REASON_PROTOTYPING:
                    addDisabledReasonIcon(buttonContainer, "fa-solid fa-text-slash icon-prototyping",
                            " Note: This icon only appears in prototyping mode.");
                    break;
                case CLEAR_FIELD:
                    addClearFieldButton(buttonContainer);
                    break;
                default:
                    break;
                }
            }
        }
    }

    private void addDisabledReasonIcon(
            final @NonNull RepeatingView buttonContainer,
            final @NonNull String faClass,
            final @NonNull String reasonSuffix) {
        val disableReasonButton = Wkt.linkAddWithBody(buttonContainer,
                Wkt.faIcon(faClass), ajaxTarget->{/*no-op*/});

        val disabledReason = popModel().disabledReason()
                .flatMap(InteractionVeto::getReasonAsString)
                .orElse("framework bug: should provide a reason");

        WktTooltips.addTooltip(disableReasonButton, translate(disabledReason) + translate(reasonSuffix));

        if(popModel().isParameter()) {
            // allow the client-side popover cleaner to kick in
            disableReasonButton.setEventPropagation(EventPropagation.BUBBLE);
        } // properties otherwise do recreate the entire page anyway
    }

    private void addClearFieldButton(final @NonNull RepeatingView buttonContainer) {
        val clearFieldButton = Wkt.linkAddWithBody(buttonContainer,
                Wkt.faIcon("fa-regular fa-trash-can"), this::onClearFieldButtonClick);

        Wkt.cssAppend(clearFieldButton, "btn-warning");
        WktTooltips.addTooltip(clearFieldButton, translate("Click to clear the field"));

        if(popModel().isParameter()) {
            // allow the client-side popover cleaner to kick in
            clearFieldButton.setEventPropagation(EventPropagation.BUBBLE);
        } // properties otherwise do recreate the entire page anyway
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

        return inlinePromptLink;
    }

    private void onPropertyInlineEditClick(final AjaxRequestTarget target) {
        popModel().toEditingMode();

        switchRegularFrameToFormFrame();
        onSwitchFormForInlinePrompt(getFormFrame(), target);

        target.add(getScalarFrameContainer());

        Wkt.focusOnMarkerAttribute(getFormFrame(), target);
    }

    private void onClearFieldButtonClick(final AjaxRequestTarget target) {
        popModel().proposedValue().clear();
        popModel().getSpecialization().accept(
                param->{
                    clearBootstrapFileInputField();
                    this.setupInlinePrompt(); // recreate the param field
                    target.add(this);
                },
                prop->{
                    FormExecutorDefault.forProperty(prop)
                        .executeAndProcessResults(target, null, prop);
                });
    }

    /**
     * Workaround {@link BootstrapFileInputField} not reacting to clearing of underlying model.
     * @implNote recreates the entire {@link FormComponent}.
     */
    private void clearBootstrapFileInputField() {
        if(this instanceof PopPanelFormFieldAbstract) {
            val formContainer = ((PopPanelFormFieldAbstract<?>)this);
            val formComponent = formContainer.getFormComponent();
            if(formComponent instanceof BootstrapFileInputField) {
                // recreate from scratch
                val replacement = formContainer.createFormComponent(formComponent.getId(), popModel());
                formComponent.replaceWith(replacement);
            }
        }
    }


}
