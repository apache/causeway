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

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;
import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.causeway.viewer.wicket.model.models.PopModel;
import org.apache.causeway.viewer.wicket.ui.components.pops.ScalarFragmentFactory.FieldFragement;
import org.apache.causeway.viewer.wicket.ui.components.pops.ScalarFragmentFactory.FieldFrame;
import org.apache.causeway.viewer.wicket.ui.components.pops.ScalarFragmentFactory.FrameFragment;
import org.apache.causeway.viewer.wicket.ui.components.pops.ScalarFragmentFactory.InputFragment;
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
            final PopModel popModel,
            final Class<T> type) {
        super(id, popModel);
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
            fieldFragement = FieldFragement.LINK_TO_PROMT;
            break;
        case EDITING:
            // setup as input-format
            fieldFragement = popModel().isEditingMode()
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
    protected abstract FormComponent<T> createFormComponent(String id, PopModel popModel);

    // -- REGULAR

    @Override
    protected final MarkupContainer createRegularFrame() {
        val popModel = popModel();

        val friendlyNameModel = Model.of(popModel.getFriendlyName());

        formComponent = createFormComponent(ID_SCALAR_VALUE, popModel);
        formComponent.setLabel(friendlyNameModel);

        val formGroup = FrameFragment.REGULAR
                .createComponent(id->new FormGroup(id, formComponent));
        formGroup.add(formComponent);

        formGroup.add(fieldFrame = createFieldFrame());

        formComponent.setRequired(popModel.isRequired());

        if(popModel.isShowMandatoryIndicator()) {
            Wkt.cssAppend(formGroup, "mandatory");
        }

        scalarNameLabelAddTo(formGroup, friendlyNameModel);

        formComponent.add(_Util.createValidatorFor(popModel));

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
            xrayDetails.put("popModel.disableReason", ""+popModel().disabledReason().map(InteractionVeto::getReason).orElse(null));
            xrayDetails.put("popModel.whetherHidden", ""+popModel().whetherHidden());
            xrayDetails.put("popModel.identifier", ""+popModel().getIdentifier());
            xrayDetails.put("popModel.choices (count)", ""+popModel().getChoices().size());
            xrayDetails.put("popModel.metaModel.featureIdentifier", ""+popModel().getMetaModel().getFeatureIdentifier());
            xrayDetails.put("popModel.scalarTypeSpec", ""+popModel().getScalarTypeSpec().toString());
            xrayDetails.put("popModel.proposedValue", ""+popModel().proposedValue().getValue().getValue());
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
}
