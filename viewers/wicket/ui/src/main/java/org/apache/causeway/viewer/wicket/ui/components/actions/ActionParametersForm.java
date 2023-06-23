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
package org.apache.causeway.viewer.wicket.ui.components.actions;

import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.commons.model.decorators.ConfirmDecorator.ConfirmDecorationModel;
import org.apache.causeway.viewer.commons.model.layout.UiPlacementDirection;
import org.apache.causeway.viewer.commons.model.pop.UiParameter;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarParameterModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.causeway.viewer.wicket.model.models.interaction.act.UiParameterWkt;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelAbstract.Repaint;
import org.apache.causeway.viewer.wicket.ui.panels.PromptFormAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktDecorators;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationBehavior;
import lombok.val;

class ActionParametersForm
extends PromptFormAbstract<ActionModel> {

    private static final long serialVersionUID = 1L;

    public ActionParametersForm(
            final String id,
            final Component parentPanel,
            final ActionModel actionModel) {
        super(id, parentPanel, actionModel);
    }

    protected ActionModel actionModel() {
        return (ActionModel) super.getModel();
    }

    @Override
    protected void addParameters() {
        val actionModel = actionModel();

        val repeatingView =
                Wkt.add(this, new RepeatingView(ActionParametersFormPanel.ID_ACTION_PARAMETERS));

        paramPanels.clear();

        actionModel.streamPendingParamUiModels()
        .map(UiParameterWkt.class::cast)
        .forEach(paramModel->{

            val container = Wkt.containerAdd(repeatingView, repeatingView.newChildId());

            newParamPanel(container, paramModel, paramPanels::add);

        });

        setOutputMarkupId(true);
    }

    private void newParamPanel(
            final WebMarkupContainer container,
            final UiParameterWkt paramModel,
            final Consumer<ScalarPanelAbstract> onNewScalarPanel) {

        val scalarParamModel = ScalarParameterModel.wrap(paramModel);

        // returned ScalarPanelAbstract should already have added any associated LinkAndLabel(s)
        val component = getComponentFactoryRegistry()
                .addOrReplaceComponent(container, ActionParametersFormPanel.ID_SCALAR_NAME_AND_VALUE,
                        UiComponentType.SCALAR_NAME_AND_VALUE, scalarParamModel);

        _Casts.castTo(ScalarPanelAbstract.class, component)
        .ifPresent(scalarPanel->{
            scalarPanel.addChangeListener(this); // handling onUpdate and onError
            onNewScalarPanel.accept(scalarPanel);
        });

    }

    @Override
    protected void configureOkButton(final AjaxButton okButton) {
        applyAreYouSure(okButton);
    }

    /**
     * If the {@literal @}Action has "are you sure?" semantics then apply {@link ConfirmationBehavior}
     * that will ask for confirmation before executing the Ajax request.
     *
     * @param button The button which action should be confirmed
     */
    private void applyAreYouSure(final AjaxButton button) {
        val actionModel = actionModel();
        val action = actionModel.getAction();

        if (action.getSemantics().isAreYouSure()) {
            val confirmUiModel = ConfirmDecorationModel.areYouSure(getTranslationService(), UiPlacementDirection.BOTTOM);
            WktDecorators.getConfirm().decorate(button, confirmUiModel);
        }
    }

    @Override
    public void onUpdate(final AjaxRequestTarget target, final ScalarPanelAbstract scalarPanelUpdated) {

        val actionModel = actionModel();
        val updatedParamModel = (UiParameter)scalarPanelUpdated.getModel();
        val paramNegotiationModel = updatedParamModel.getParameterNegotiationModel();
        val pendingParamModels = actionModel.streamPendingParamUiModels().collect(Can.toCan());

        final int paramIndexOfUpdated = updatedParamModel.getParameterIndex();
        _Xray.beforeParamFormUpdate(paramIndexOfUpdated, paramNegotiationModel);

        // only updates subsequent parameter panels starting from (paramIndexOfUpdated + 1)
        IntStream.range(paramIndexOfUpdated + 1, paramNegotiationModel.getParamCount())
        .forEach(paramIndexForReassessment->{
            var paramRepaint =
                    // potentially updates the paramNegotiationModel
                    Repaint.required(paramNegotiationModel.reassessDefaults(paramIndexForReassessment));
            _Xray.reassessedDefault(paramIndexForReassessment, paramNegotiationModel);

            val paramPanel = paramPanels.get(paramIndexForReassessment);
            val paramModel = pendingParamModels.getElseFail(paramIndexForReassessment);
            /* repaint is required, either because of a changed value during reassessment above
             * or because visibility or usability have changed */
            paramRepaint = paramRepaint.max(
                    paramPanel.updateIfNecessary(paramModel));

            switch (paramRepaint) {
            case REQUIRED:
                target.add(paramPanel);
                break;
            case REQUIRED_ON_PARENT:
                target.add(paramPanel.getParent());
                break;
            default:
            }
        });

        _Xray.afterParamFormUpdate(paramIndexOfUpdated, paramNegotiationModel);

        // previously this method was also doing:
        // target.add(this);
        // ie to update the entire form (in addition to the updates to the individual impacted parameter fields
        // done in the loop above).  However, that logic is wrong, because any values entered in the browser
        // get trampled over (CAUSEWAY-629).
    }

    @Override
    protected Either<ActionModel, ScalarPropertyModel> getMemberModel() {
        return Either.left(actionModel());
    }

}
