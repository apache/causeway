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
package org.apache.causeway.viewer.wicket.ui.components.scalars;

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
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.InlinePromptContext;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.CompactFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FieldFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FieldFrame;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.PromptFragment;
import org.apache.causeway.viewer.wicket.ui.components.widgets.actionlink.ActionLink;
import org.apache.causeway.viewer.wicket.ui.panels.FormExecutorDefault;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.BootstrapFileInputField;

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

        var scalarModel = scalarModel();
        var regularFrame = getRegularFrame();
        var fieldFrame = getFieldFrame();
        var scalarFrameContainer = getScalarFrameContainer();

        // even if this particular scalarModel (property) is not configured for inline edits,
        // it's possible that one of the associated actions is.  Thus we set the prompt context
        scalarModel.setInlinePromptContext(
                new InlinePromptContext(
                        scalarModel,
                        scalarFrameContainer,
                        regularFrame,
                        getFormFrame()));

        FieldFragment.matching(fieldFrame)
        .ifPresent(fieldFragment ->{

            switch (fieldFragment) {
            case LINK_TO_PROMT: {

                fieldFrame
                    .addOrReplace(inlinePromptLink = createInlinePromptLink());

                // needs InlinePromptContext to properly initialize
                addOnClickBehaviorTo(inlinePromptLink);

                var additionalButtonContainer = fieldFragment.createButtonContainer(inlinePromptLink);
                addAdditionalButtonsTo(additionalButtonContainer, fieldFragment);
                return;
            }
            case NO_LINK_VIEWING:
            case NO_LINK_EDITING: {

                var additionalButtonContainer = fieldFragment.createButtonContainer(fieldFrame);
                addAdditionalButtonsTo(additionalButtonContainer, fieldFragment);

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
                        var textArea = Wkt.textAreaNoTab(scalarValueId, this::outputFormatAsString);
                        var scalarModel = scalarModel();
                        Wkt.setFormComponentAttributes(textArea,
                                scalarModel::multilineNumberOfLines,
                                scalarModel::maxLength,
                                scalarModel::typicalLength);
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
        return !scalarModel().isEmpty();
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
        var proposedValue = scalarModel().proposedValue();
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

        var scalarModel = scalarModel();

        if (_Util.canPropertyEnterInlineEditDirectly(scalarModel)) {

            _Util.lookupMixinForCompositeValueUpdate(scalarModel)
            .ifPresentOrElse((final ActionModel mixinForCompositeValueEdit)->{
                // composite value type support
                var actionLink = ActionLink.create(mixinForCompositeValueEdit);
                Wkt.behaviorAddOnClick(clickReceiver, actionLink::onClick);
            },()->{
                // we configure the prompt link if _this_ property is configured for inline edits...
                Wkt.behaviorAddOnClick(clickReceiver, this::onPropertyInlineEditClick);
            });

        } else {

            _Util.lookupPropertyActionForInlineEdit(scalarModel)
            .ifPresent((final ActionModel actionLinkInlineAsIfEdit)->{
                var actionLink = ActionLink.create(actionLinkInlineAsIfEdit);
                Wkt.behaviorAddOnClick(clickReceiver, actionLink::onClick);
            });
        }
    }

    private void addAdditionalButtonsTo(
            final @NonNull RepeatingView buttonContainer, final FieldFragment fieldFragment) {

        for(var additionalButton : ScalarPanelAdditionalButton.values()) {
            if(additionalButton.isVisible(scalarModel(), getRenderScenario(), fieldFragment)) {
                switch (additionalButton) {
                case COPY_TO_CLIPBOARD:
                    //XXX Future extension
                    break;
                case DISABLED_REASON:
                    addDisabledReasonIcon(buttonContainer, "fa-solid fa-ban veto-reason-icon", "");
                    break;
                case DISABLED_REASON_PROTOTYPING:
                    addDisabledReasonIcon(buttonContainer, "fa-solid fa-text-slash veto-reason-icon prototyping",
                            "\nNote: This icon only appears in prototyping mode "
                            + "(unless disabled via config option "
                            + "causeway.viewer.wicket.disable-reason-explanation-in-prototyping-mode-enabled).");
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
        var disableReasonButton = Wkt.linkAddWithBody(buttonContainer,
                Wkt.faIcon(faClass), ajaxTarget->{/*no-op*/});

        var disabledReason = scalarModel().disabledReason()
                .flatMap(InteractionVeto::getReasonAsString)
                .orElse("framework bug: should provide a reason");

        WktTooltips.addTooltip(disableReasonButton, translate(disabledReason) + translate(reasonSuffix));
        Wkt.noTabbing(disableReasonButton);

        if(scalarModel().isParameter()) {
            // allow the client-side popover cleaner to kick in
            disableReasonButton.setEventPropagation(EventPropagation.BUBBLE);
        } // properties otherwise do recreate the entire page anyway
    }

    private void addClearFieldButton(final @NonNull RepeatingView buttonContainer) {
        var clearFieldButton = Wkt.linkAddWithBody(buttonContainer,
                Wkt.faIcon("fa-regular fa-trash-can"), this::onClearFieldButtonClick);

        Wkt.cssAppend(clearFieldButton, "btn-warning");
        WktTooltips.addTooltip(clearFieldButton, translate("Click to clear the field"));

        if(scalarModel().isParameter()) {
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
        scalarModel().toEditingMode();

        switchRegularFrameToFormFrame();
        onSwitchFormForInlinePrompt(getFormFrame(), target);

        target.add(getScalarFrameContainer());

        Wkt.focusOnMarkerAttribute(getFormFrame(), target); // not sure this works...
        Wkt.javaScriptAdd(target, Wkt.EventTopic.FOCUS_FIRST_PARAMETER, getMarkupId());  // .. javascript equivalent.

    }

    private void onClearFieldButtonClick(final AjaxRequestTarget target) {
        scalarModel().proposedValue().clear();
        scalarModel().getSpecialization().accept(
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
        if(this instanceof ScalarPanelFormFieldAbstract) {
            var formContainer = ((ScalarPanelFormFieldAbstract<?>)this);
            var formComponent = formContainer.getFormComponent();
            if(formComponent instanceof BootstrapFileInputField) {
                // recreate from scratch
                var replacement = formContainer.createFormComponent(formComponent.getId(), scalarModel());
                formComponent.replaceWith(replacement);
            }
        }
    }

}
