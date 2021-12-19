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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmUiModel;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmUiModel.Placement;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;
import org.apache.isis.viewer.wicket.model.hints.IsisActionCompletedEvent;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ScalarParameterModel;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.model.models.interaction.act.ParameterUiModelWkt;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PromptFormAbstract;
import org.apache.isis.viewer.wicket.ui.util.Decorators;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

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

    private ActionModel getActionModel() {
        return (ActionModel) super.getModel();
    }

    @Override
    protected void addParameters() {
        val actionModel = getActionModel();

        val repeatingView =
                Wkt.add(this, new RepeatingView(ActionParametersFormPanel.ID_ACTION_PARAMETERS));

        paramPanels.clear();

        actionModel.streamPendingParamUiModels()
        .map(ParameterUiModelWkt.class::cast)
        .forEach(paramModel->{

            val container = Wkt.containerAdd(repeatingView, repeatingView.newChildId());

            newParamPanel(container, paramModel)
            .ifPresent(paramPanels::add);

        });

        setOutputMarkupId(true);
    }

    private Optional<ScalarPanelAbstract> newParamPanel(
            final WebMarkupContainer container,
            final ParameterUiModelWkt paramModel) {

        val id = "scalarNameAndValue";

        val scalarParamModel = ScalarParameterModel.wrap(paramModel);

        final Component component = getComponentFactoryRegistry()
                .addOrReplaceComponent(container, id, ComponentType.SCALAR_NAME_AND_VALUE, scalarParamModel);

        if(!(component instanceof ScalarPanelAbstract)) {
            return Optional.empty();
        }

        if(component instanceof MarkupContainer) {
            Wkt.cssAppend(component, scalarParamModel.getCssClass());
        }

        // ScalarPanelAbstract at this point should have added any associated LinkAndLabel(s)

        val paramPanel = (ScalarPanelAbstract) component;
        paramPanel.setOutputMarkupId(true);
        paramPanel.notifyOnChange(this); // this is a ScalarModelSubscriber, handling onUpdate and onError
        return Optional.of(paramPanel);
    }

    @Override
    protected Object newCompletedEvent(final AjaxRequestTarget target, final Form<?> form) {
        return new IsisActionCompletedEvent(getActionModel(), target, form);
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
        val actionModel = getActionModel();
        val action = actionModel.getAction();

        if (action.getSemantics().isAreYouSure()) {
            val confirmUiModel = ConfirmUiModel.ofAreYouSure(getTranslationService(), Placement.BOTTOM);
            Decorators.getConfirm().decorate(button, confirmUiModel);
        }
    }

    @Override
    public void onUpdate(final AjaxRequestTarget target, final ScalarPanelAbstract scalarPanelUpdated) {

        _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
            _Debug.log("about to update Param Form ..");
        });

        val actionModel = getActionModel();

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
    protected _Either<ActionModel, ScalarPropertyModel> getMemberModel() {
        return _Either.left(getActionModel());
    }

}
