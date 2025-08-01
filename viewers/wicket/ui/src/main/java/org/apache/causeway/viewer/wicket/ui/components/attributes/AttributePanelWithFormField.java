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
package org.apache.causeway.viewer.wicket.ui.components.attributes;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.FieldFragment;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.FieldFrame;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.FrameFragment;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.attributes.choices.AttributePanelWithSelect;
import org.apache.causeway.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;
import org.apache.causeway.viewer.wicket.ui.util.XrayWkt;

public abstract class AttributePanelWithFormField<T>
extends AttributePanelWithInlinePrompt {

    private static final long serialVersionUID = 1L;

    protected final Class<T> type;

    protected AttributePanelWithFormField(
            final String id,
            final UiAttributeWkt attributeModel,
            final Class<T> type) {
        super(id, attributeModel);
        this.type = type;
    }

    @Override
    protected final Component getValidationFeedbackReceiver() {
        return getFormComponent();
    }

    // -- FIELD FRAME

    /**
     * Builds the field frame when in REGULAR format.
     * <p>Is added to {@link #getRegularFrame()}.
     */
    protected MarkupContainer createFieldFrame() {
        var renderScenario = getRenderScenario();
        final FieldFragment fieldFragment;
        switch (renderScenario) {
        case READONLY:
            // setup as output-format (no links)
            fieldFragment = FieldFragment.NO_LINK_VIEWING;
            break;
        case CAN_EDIT:
        case CAN_EDIT_INLINE:
        case CAN_EDIT_INLINE_VIA_ACTION:
        case EDITING_WITH_LINK_TO_NESTED:
            // setup as output-format (with links to edit)
            fieldFragment = FieldFragment.LINK_TO_PROMT;
            break;
        case EDITING:
            // setup as input-format
            fieldFragment = attributeModel().isEditingMode()
                ? FieldFragment.NO_LINK_EDITING // supports additional buttons (clear, ...)
                : FieldFragment.NO_LINK_VIEWING;
            break;

        default:
            throw _Exceptions.unmatchedCase(renderScenario);
        }
        return Wkt.fragment(fieldFragment.getContainerId(),
                fieldFragment.getFragmentId(),
                this);
    }

    // -- FORM COMPONENT

    private FormComponent<T> formComponent;
    @Nullable
    protected final FormComponent<T> getFormComponent() { return formComponent; }

    /**
     * Builds the component to render the form input field.
     */
    protected abstract FormComponent<T> createFormComponent(String id, UiAttributeWkt attributeModel);

    // -- REGULAR

    @Override
    protected final MarkupContainer createRegularFrame() {
        var attributeModel = attributeModel();

        var friendlyNameModel = Model.of(attributeModel.getFriendlyName());

        formComponent = createFormComponent(ID_SCALAR_VALUE, attributeModel);
        formComponent.setLabel(friendlyNameModel);

        var formGroup = FrameFragment.REGULAR
                .createComponent(id->new FormGroup(id, formComponent));
        formGroup.add(formComponent);

        formGroup.add(fieldFrame = createFieldFrame());

        formComponent.setRequired(attributeModel.isRequired());

        if(attributeModel.isShowMandatoryIndicator()) {
            Wkt.cssAppend(formGroup, "mandatory");
        }

        scalarNameLabelAddTo(formGroup, friendlyNameModel);

        formComponent.add(_Util.createValidatorFor(attributeModel));

        var renderScenario = getRenderScenario();

        XrayWkt.ifEnabledDo(()->{
            // debug (wicket viewer x-ray)
            var xrayDetails = _Maps.<String, String>newLinkedHashMap();
            xrayDetails.put("panel", this.getClass().getSimpleName());
            xrayDetails.put("renderScenario", renderScenario.name());
            xrayDetails.put("inputFragmentType", getInputFragmentType().map(x->x.name()).orElse("(none)"));
            xrayDetails.put("formComponent", _Strings.nonEmpty(formComponent.getClass().getSimpleName())
                    .orElseGet(()->formComponent.getClass().getName()));
            xrayDetails.put("formComponent.id", formComponent.getId());
            xrayDetails.put("formComponent.validators (count)", ""+_NullSafe.size(formComponent.getValidators()));
            xrayDetails.put("attributeModel.disableReason", ""+attributeModel().disabledReason().map(InteractionVeto::getReason).orElse(null));
            xrayDetails.put("attributeModel.whetherHidden", ""+attributeModel().whetherHidden());
            xrayDetails.put("attributeModel.identifier", ""+attributeModel().getIdentifier());
            xrayDetails.put("attributeModel.choices (count)", ""+attributeModel().getChoices().size());
            xrayDetails.put("attributeModel.metaModel.featureIdentifier", ""+attributeModel().getMetaModel().getFeatureIdentifier());
            xrayDetails.put("attributeModel.elementTypeSpec", ""+attributeModel().getElementType().toString());
            xrayDetails.put("attributeModel.proposedValue", ""+attributeModel().proposedValue().getValue().getValue());
//                    getSpecialization()
//                    .fold(
//                            param->""+param.getValue(),
//                            prop->""+prop.getPendingPropertyModel().getValueAsTitle().getValue()));
            Wkt.markupAdd(fieldFrame, ID_XRAY_DETAILS, XrayWkt.formatAsListGroup(xrayDetails));

        });

        if(renderScenario.isReadonly()) {
            fieldFrame.add(FieldFrame.SCALAR_VALUE_CONTAINER
                    .createComponent(this::createComponentForOutput));
        } else if(renderScenario.isViewingAndCanEditAny()) {

            // this results in a link created;
            // link stuff is handled later in ScalarPanelAbstract2.setupInlinePrompt

        } else {
            getInputFragmentType()
            .ifPresent(inputFragmentType->
                fieldFrame.add(inputFragmentType.createFragment(this, formComponent)));
        }

        onFormGroupCreated(formGroup);

        formComponent.setVisible(true);
        formComponent.setVisibilityAllowed(true);

        return formGroup;
    }

    // -- COMPACT

    @Override
    protected final Component createCompactFrame() {
        return FrameFragment.COMPACT
                .createComponent(this::createComponentForOutput);
    }

    // -- HOOKS

    protected Optional<InputFragment> getInputFragmentType() {
        return Optional.empty();
    }

    /**
     * Optional hook, to eg. add additional components (like Blob which adds preview image)
     */
    protected void onFormGroupCreated(final FormGroup formGroup) {}

    @Override
    protected void onInitializeNotEditable() {
        if(getFormComponent()!=null) {
            //keep inlinePromptLink (if any) enabled
            getFormComponent().setEnabled(false);
        }
        replaceDisabledTagWithReadonlyTagIfApplicable();
        clearTooltip();
    }

    @Override
    protected void onInitializeReadonly(final String disableReason) {
        formComponentEnable(false);
        replaceDisabledTagWithReadonlyTagIfApplicable();
        setTooltip(disableReason);
    }

    @Override
    protected void onInitializeEditable() {
        formComponentEnable(true);
        clearTooltip();
    }

    @Override
    protected void onMakeNotEditable(final String disableReason) {
        super.onMakeNotEditable(disableReason);
        formComponentEnable(false);
        setTooltip(disableReason);
    }

    @Override
    protected void onMakeEditable() {
        super.onMakeEditable();
        formComponentEnable(true);
        clearTooltip();
    }

    // -- XRAY

    @Override
    public String getVariation() {
        return XrayWkt.isEnabled()
                ? "xray"
                : super.getVariation();
    }

    // -- HELPER

    private void formComponentEnable(final boolean b) {
        if(getFormComponent()!=null) {
            getFormComponent().setEnabled(b);
        }
        if(inlinePromptLink!=null) {
            inlinePromptLink.setEnabled(b);
        }
    }

    private void setTooltip(final String tooltip) {
        WktTooltips.addTooltip(getFormComponent(), tooltip);
        WktTooltips.addTooltip(inlinePromptLink, tooltip);
    }

    private void clearTooltip() {
        WktTooltips.clearTooltip(getFormComponent());
        WktTooltips.clearTooltip(inlinePromptLink);
    }

    private void replaceDisabledTagWithReadonlyTagIfApplicable() {
        if(!getWicketViewerSettings().replaceDisabledTagWithReadonlyTag()) return;
        //[CAUSEWAY-3727] select2 does not seem to support this replacement behavior
        if(this instanceof AttributePanelWithSelect) return;
        Wkt.behaviorAddReplaceDisabledTagWithReadonlyTag(getFormComponent());
    }
}
