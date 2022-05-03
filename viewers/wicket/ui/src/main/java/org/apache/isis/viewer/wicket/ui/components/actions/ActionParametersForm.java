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
package org.apache.isis.viewer.wicket.ui.components.actions;

import java.util.Optional;
import java.util.function.Consumer;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.commons.functional.Either;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.viewer.common.model.PlacementDirection;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.common.model.decorators.ConfirmDecorator.ConfirmDecorationModel;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ScalarParameterModel;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.model.models.interaction.act.ParameterUiModelWkt;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PromptFormAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;
import org.apache.isis.viewer.wicket.ui.util.WktDecorators;

import lombok.val;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationBehavior;

class ActionParametersForm
extends PromptFormAbstract<ActionModel> {

    private static final long serialVersionUID = 1L;

    public ActionParametersForm(
            final String id,
            final Component parentPanel,
            final WicketViewerSettings settings,
            final ActionModel actionModel) {
        super(id, parentPanel, settings, actionModel);
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
        .map(ParameterUiModelWkt.class::cast)
        .forEach(paramModel->{

            val container = Wkt.containerAdd(repeatingView, repeatingView.newChildId());

            newParamPanel(container, paramModel, paramPanels::add);

        });

        setOutputMarkupId(true);
    }

    private void newParamPanel(
            final WebMarkupContainer container,
            final ParameterUiModelWkt paramModel,
            final Consumer<ScalarPanelAbstract> onNewScalarPanel) {

        val scalarParamModel = ScalarParameterModel.wrap(paramModel);

        // returned ScalarPanelAbstract should already have added any associated LinkAndLabel(s)
        val component = getComponentFactoryRegistry()
                .addOrReplaceComponent(container, ActionParametersFormPanel.ID_SCALAR_NAME_AND_VALUE,
                        ComponentType.SCALAR_NAME_AND_VALUE, scalarParamModel);

        _Casts.castTo(ScalarPanelAbstract.class, component)
        .ifPresent(scalarPanel->{
            scalarPanel.notifyOnChange(this); // handling onUpdate and onError
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
            val confirmUiModel = ConfirmDecorationModel.areYouSure(getTranslationService(), PlacementDirection.BOTTOM);
            WktDecorators.getConfirm().decorate(button, confirmUiModel);
        }
    }

    @Override
    public void onUpdate(final AjaxRequestTarget target, final ScalarPanelAbstract scalarPanelUpdated) {

        _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
            _Debug.log("about to update Param Form ..");
        });

        val actionModel = actionModel();

        val updatedParamModel = (ParameterUiModel)scalarPanelUpdated.getModel();
        final int paramNumberUpdated = updatedParamModel.getParameterIndex();
        // only updates subsequent parameter panels starting from (paramNumberUpdated + 1)
        final int skipCount = paramNumberUpdated + 1;

        actionModel.streamPendingParamUiModels()
        .skip(skipCount)
        .forEach(paramModel->{

            val paramIndex = paramModel.getParameterIndex();
            val pendingArgs = paramModel.getParameterNegotiationModel();

            val actionParameter = paramModel.getMetaModel();
            // reassess defaults
            val paramDefaultValue = actionParameter.getDefault(pendingArgs);

            if (ManagedObjects.isNullOrUnspecifiedOrEmpty(paramDefaultValue)) {
                pendingArgs.clearParamValue(paramIndex);
            } else {
                pendingArgs.setParamValue(paramIndex, paramDefaultValue);
            }

            val paramPanel = paramPanels.get(paramIndex);
            val repaint = paramPanel.updateIfNecessary(paramModel, Optional.of(target));

            switch (repaint) {
            case ENTIRE_FORM:
                target.add(this);
                break;
            case PARAM_ONLY:
                paramPanel.repaint(target);
                break;
            case NOTHING:
                break;
            default:
                throw _Exceptions.unmatchedCase(repaint);
            }

        });


        // previously this method was also doing:
        // target.add(this);
        // ie to update the entire form (in addition to the updates to the individual impacted parameter fields
        // done in the loop above).  However, that logic is wrong, because any values entered in the browser
        // get trampled over (ISIS-629).
    }

    @Override
    protected Either<ActionModel, ScalarPropertyModel> getMemberModel() {
        return Either.left(actionModel());
    }

}
