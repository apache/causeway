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

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;
import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FieldFragement;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FieldFrame;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FrameFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;
import org.apache.causeway.viewer.wicket.ui.util.XrayWkt;

import lombok.val;

public abstract class ScalarPanelFormFieldAbstract<T>
extends ScalarPanelAbstract2 {

    private static final long serialVersionUID = 1L;

    protected final Class<T> type;

    protected ScalarPanelFormFieldAbstract(
            final String id,
            final ScalarModel scalarModel,
            final Class<T> type) {
        super(id, scalarModel);
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
        val renderScenario = getRenderScenario();
        final FieldFragement fieldFragement;
        switch (renderScenario) {
        case READONLY:
            // setup as output-format (no links)
            fieldFragement = FieldFragement.NO_LINK_VIEWING;
            break;
        case CAN_EDIT:
        case CAN_EDIT_INLINE:
        case CAN_EDIT_INLINE_VIA_ACTION:
        case EDITING_WITH_LINK_TO_NESTED:
            // setup as output-format (with links to edit)
            fieldFragement = FieldFragement.LINK;
            break;
        case EDITING:
            // setup as input-format
            fieldFragement = scalarModel().isEditMode()
                ? FieldFragement.NO_LINK_EDITING // supports additional buttons (clear, ...)
                : FieldFragement.NO_LINK_VIEWING;
            break;

        default:
            throw _Exceptions.unmatchedCase(renderScenario);
        }
        return Wkt.fragment(fieldFragement.getContainerId(),
                fieldFragement.getFragmentId(),
                this);
    }

    // -- FORM COMPONENT

    private FormComponent<T> formComponent;
    @Nullable
    protected final FormComponent<T> getFormComponent() { return formComponent; }

    /**
     * Builds the component to render the form input field.
     */
    protected abstract FormComponent<T> createFormComponent(String id, ScalarModel scalarModel);

    // -- REGULAR

    @Override
    protected final MarkupContainer createRegularFrame() {
        val scalarModel = scalarModel();

        val friendlyNameModel = Model.of(scalarModel.getFriendlyName());

        formComponent = createFormComponent(ID_SCALAR_VALUE, scalarModel);
        formComponent.setLabel(friendlyNameModel);

        val formGroup = FrameFragment.REGULAR
                .createComponent(id->new FormGroup(id, formComponent));
        formGroup.add(formComponent);

        formGroup.add(fieldFrame = createFieldFrame());

        formComponent.setRequired(scalarModel.isRequired());
        if(scalarModel.isRequired()
                && scalarModel.isEnabled()) {
            Wkt.cssAppend(formGroup, "mandatory");
        }

        formGroup.add(createScalarNameLabel(ID_SCALAR_NAME, friendlyNameModel));

        formComponent.add(_Util.createValidatorFor(scalarModel));

        val renderScenario = getRenderScenario();

        XrayWkt.ifEnabledDo(()->{
            // debug (wicket viewer x-ray)
            val xrayDetails = _Maps.<String, String>newLinkedHashMap();
            xrayDetails.put("panel", this.getClass().getSimpleName());
            xrayDetails.put("renderScenario", renderScenario.name());
            xrayDetails.put("inputFragmentType", getInputFragmentType().map(x->x.name()).orElse("(none)"));
            xrayDetails.put("formComponent", _Strings.nonEmpty(formComponent.getClass().getSimpleName())
                    .orElseGet(()->formComponent.getClass().getName()));
            xrayDetails.put("formComponent.id", formComponent.getId());
            xrayDetails.put("formComponent.validators (count)", ""+_NullSafe.size(formComponent.getValidators()));
            xrayDetails.put("scalarModel.disableReasonIfAny", ""+scalarModel().disableReasonIfAny());
            xrayDetails.put("scalarModel.whetherHidden", ""+scalarModel().whetherHidden());
            xrayDetails.put("scalarModel.identifier", ""+scalarModel().getIdentifier());
            xrayDetails.put("scalarModel.choices (count)", ""+scalarModel().getChoices().size());
            xrayDetails.put("scalarModel.metaModel.featureIdentifier", ""+scalarModel().getMetaModel().getFeatureIdentifier());
            xrayDetails.put("scalarModel.scalarTypeSpec", ""+scalarModel().getScalarTypeSpec().toString());
            xrayDetails.put("scalarModel.proposedValue", ""+scalarModel().proposedValue().getValue().getValue());
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
    protected void onFormGroupCreated(final FormGroup formGroup) {};

    @Override
    protected void onInitializeNotEditable() {
        if(getFormComponent()!=null) {
            //keep inlinePromptLink (if any) enabled
            getFormComponent().setEnabled(false);
        }
        if(getWicketViewerSettings().isReplaceDisabledTagWithReadonlyTag()) {
            Wkt.behaviorAddReplaceDisabledTagWithReadonlyTag(getFormComponent());
        }
        clearTooltip();
    }

    @Override
    protected void onInitializeReadonly(final String disableReason) {
        formComponentEnable(false);
        if(getWicketViewerSettings().isReplaceDisabledTagWithReadonlyTag()) {
            Wkt.behaviorAddReplaceDisabledTagWithReadonlyTag(getFormComponent());
        }
        setTooltip(disableReason);
    }

    @Override
    protected void onInitializeEditable() {
        formComponentEnable(true);
        clearTooltip();
    }

    @Override
    protected void onNotEditable(final String disableReason, final Optional<AjaxRequestTarget> target) {
        formComponentEnable(false);
        setTooltip(disableReason);
        target.ifPresent(this::formComponentAddTo);
    }

    @Override
    protected void onEditable(final Optional<AjaxRequestTarget> target) {
        formComponentEnable(true);
        clearTooltip();
        target.ifPresent(this::formComponentAddTo);
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

    private void formComponentAddTo(final AjaxRequestTarget ajax) {
        if(getFormComponent()!=null) {
            ajax.add(getFormComponent());
        }
        if(inlinePromptLink!=null) {
            ajax.add(inlinePromptLink);
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



}
