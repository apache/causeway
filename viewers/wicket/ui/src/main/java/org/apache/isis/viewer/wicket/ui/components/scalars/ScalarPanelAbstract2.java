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
import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.InlinePromptContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.ui.components.property.PropertyEditPanel;
import org.apache.isis.viewer.wicket.ui.components.propertyheader.PropertyEditPromptHeaderPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.CompactFragment;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FieldFragement;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FieldFrame;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.PromptFragment;
import org.apache.isis.viewer.wicket.ui.util.Wkt;
import org.apache.isis.viewer.wicket.ui.util.WktComponents;
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

        if(FieldFragement.LINK.isMatching(fieldFrame)) {

            fieldFrame
                .add(inlinePromptLink = createInlinePromptLink());

            // even if this particular scalarModel (property) is not configured for inline edits,
            // it's possible that one of the associated actions is.  Thus we set the prompt context
            scalarModel.setInlinePromptContext(
                    new InlinePromptContext(
                            scalarModel,
                            scalarFrameContainer,
                            regularFrame,
                            getFormFrame()));

            // needs InlinePromptContext to properly initialize
            addOnClickBehaviorTo(inlinePromptLink);
        }

        addEditPropertyIf(
                scalarModel.canEnterEditMode()
                && (scalarModel.getPromptStyle().isDialog()
                        || fieldFrame==null),
                fieldFrame);

        //XXX support for legacy panels, remove eventually
        {
            if(fieldFrame!=null
                &&fieldFrame.get(ID_SCALAR_VALUE)==null) {
                Wkt.labelAdd(fieldFrame, ID_SCALAR_VALUE, "∅");
            }
            FieldFrame.EDIT_PROPERTY
                .addComponentIfMissing(regularFrame, id->Wkt.label(id, "∅"));
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
                &&!getFormatModifiers().contains(FormatModifier.MARKUP)
                &&!getRenderScenario().isCompact()) {
            return PromptFragment.TEXTAREA
                    .createFragment(id, this, scalarValueId->{
                        val textArea = Wkt.textAreaNoTab(scalarValueId, obtainOutputFormatModel());
                        if(this instanceof ScalarPanelTextFieldAbstract) {
                            ((ScalarPanelTextFieldAbstract)this).setFormComponentAttributes(textArea);
                        }
                        return textArea;
                    });
        }
        return CompactFragment.LABEL
                    .createFragment(id, this, scalarValueId->
                        Wkt.label(scalarValueId, obtainOutputFormatModel()));
    }

    /**
     * Model for any non editing scenario.
     */
    protected IModel<String> obtainOutputFormatModel() {
        return ()->{
            val propertyNegotiationModel = scalarModel().proposedValue();
            return _Strings.nullToEmpty(
                    propertyNegotiationModel
                        .getValueAsHtml().getValue()
                        //.getValueAsParsableText().getValue()
                        );
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

    // -- HELPER

    private WebMarkupContainer addEditPropertyIf(final boolean condition, final MarkupContainer fieldFrame) {
        if(fieldFrame==null) return null;
        val editLinkId = FieldFrame.EDIT_PROPERTY.getContainerId();
        if(condition) {
            val editProperty = Wkt.containerAdd(fieldFrame, editLinkId);
            Wkt.behaviorAddOnClick(editProperty, this::onPropertyEditClick);
            WktTooltips.addTooltip(editProperty, "Click to edit");
            return editProperty;
        } else {
            WktComponents.permanentlyHide(fieldFrame, editLinkId);
            return null;
        }
    }

    private void onPropertyEditClick(final AjaxRequestTarget target) {
        val scalarModel = scalarModel();
        final ObjectSpecification specification = scalarModel.getScalarTypeSpec();
        final MetaModelService metaModelService = getServiceRegistry()
                .lookupServiceElseFail(MetaModelService.class);
        final BeanSort sort = metaModelService.sortOf(specification.getCorrespondingClass(), MetaModelService.Mode.RELAXED);

        final ActionPrompt prompt = ActionPromptProvider
                .getFrom(ScalarPanelAbstract2.this).getActionPrompt(scalarModel.getPromptStyle(), sort);

        PropertyEditPromptHeaderPanel titlePanel = new PropertyEditPromptHeaderPanel(
                prompt.getTitleId(),
                (ScalarPropertyModel)ScalarPanelAbstract2.this.scalarModel());

        final PropertyEditPanel propertyEditPanel =
                (PropertyEditPanel) getComponentFactoryRegistry().createComponent(
                        ComponentType.PROPERTY_EDIT_PROMPT, prompt.getContentId(),
                        ScalarPanelAbstract2.this.scalarModel());

        propertyEditPanel.setShowHeader(false);

        prompt.setTitle(titlePanel, target);
        prompt.setPanel(propertyEditPanel, target);
        prompt.showPrompt(target);
    }

    private void addOnClickBehaviorTo(
            final @Nullable MarkupContainer clickReceiver) {

        if(clickReceiver==null) return;

        val scalarModel = scalarModel();

        if (_Util.canPropertyEnterInlineEditDirectly(scalarModel)) {

            _Util.lookupMixinForCompositeValueUpdate(scalarModel)
            .ifPresentOrElse(mixinForCompositeValueEdit->{
                // composite value type support
                Wkt.behaviorAddOnClick(clickReceiver, mixinForCompositeValueEdit::onClick);
            },()->{
                // we configure the prompt link if _this_ property is configured for inline edits...
                Wkt.behaviorAddOnClick(clickReceiver, this::onPropertyInlineEditClick);
            });

        } else {

            _Util.lookupPropertyActionForInlineEdit(scalarModel)
            .ifPresent(actionLinkInlineAsIfEdit->{
                Wkt.behaviorAddOnClick(clickReceiver, actionLinkInlineAsIfEdit::onClick);
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

        final Component editInlineLinkLabel = FieldFrame.SCALAR_VALUE_CONTAINER
                .createComponent(id->createComponentForOutput(id));

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
