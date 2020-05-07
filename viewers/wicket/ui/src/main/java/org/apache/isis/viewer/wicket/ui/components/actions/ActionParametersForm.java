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

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmUiModel;
import org.apache.isis.viewer.common.model.decorator.confirm.ConfirmUiModel.Placement;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;
import org.apache.isis.viewer.wicket.model.hints.IsisActionCompletedEvent;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ScalarParameterModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract2;
import org.apache.isis.viewer.wicket.ui.panels.FormExecutorStrategy;
import org.apache.isis.viewer.wicket.ui.panels.PromptFormAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.Decorators;

import lombok.val;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationBehavior;

class ActionParametersForm extends PromptFormAbstract<ActionModel> {

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
        
        val repeatingView = new RepeatingView(ActionParametersFormPanel.ID_ACTION_PARAMETERS);
        add(repeatingView);

        paramPanels.clear();
        
        actionModel.streamPendingParamUiModels()
        .forEach(argsAndConsents->{
            
            val paramModel = (ScalarParameterModel) argsAndConsents.getParamModel(); 
            
            val container = new WebMarkupContainer(repeatingView.newChildId());
            repeatingView.add(container);
            
            newParamPanel(container, paramModel)
            .ifPresent(paramPanel->{
                paramPanels.add(paramPanel);
                //val paramModel = (ScalarParameterModel) paramPanel.getModel();
                paramPanel.postInit(argsAndConsents);
            });
            
        });

        setOutputMarkupId(true);


    }

    private Optional<ScalarPanelAbstract2> newParamPanel(
            final WebMarkupContainer container, 
            final ScalarParameterModel paramModel) {
        
        final Component component = getComponentFactoryRegistry()
                .addOrReplaceComponent(container, ComponentType.SCALAR_NAME_AND_VALUE, paramModel);

        if(component instanceof MarkupContainer) {
            val markupContainer = (MarkupContainer) component;
            val css = paramModel.getCssClass();
            if (!_Strings.isNullOrEmpty(css)) {
                CssClassAppender.appendCssClassTo(markupContainer, CssClassAppender.asCssStyle(css));    
            }
        }
        
        val paramPanel =
                component instanceof ScalarPanelAbstract2
                ? (ScalarPanelAbstract2) component
                : null;
                
        if (paramPanel != null) {
            paramPanel.setOutputMarkupId(true);
            paramPanel.notifyOnChange(this);
        }
        return Optional.of(paramPanel);
    }

    @Override
    protected Object newCompletedEvent(final AjaxRequestTarget target, final Form<?> form) {
        return new IsisActionCompletedEvent(getActionModel(), target, form);
    }

    @Override
    protected void doConfigureOkButton(final AjaxButton okButton) {
        applyAreYouSure(okButton);
    }

    /**
     * If the {@literal @}Action has "are you sure?" semantics then apply {@link ConfirmationBehavior}
     * that will ask for confirmation before executing the Ajax request.
     *
     * @param button The button which action should be confirmed
     */
    private void applyAreYouSure(AjaxButton button) {
        val actionModel = getActionModel();
        val action = actionModel.getAction();
        
        if (action.getSemantics().isAreYouSure()) {
            val confirmUiModel = ConfirmUiModel.ofAreYouSure(getTranslationService(), Placement.RIGHT);
            Decorators.getConfirm().decorate(button, confirmUiModel);
        }
    }

    @Override
    public void onUpdate(final AjaxRequestTarget target, final ScalarPanelAbstract2 scalarPanelUpdated) {

        val actionModel = getActionModel();
        val paramModel = (ParameterUiModel)scalarPanelUpdated.getModel();
        final int paramNumberUpdated = paramModel.getNumber();
        // only updates subsequent parameter panels starting from (paramNumberUpdated + 1)
        final int skipCount = paramNumberUpdated + 1;   
        
        actionModel.reassessPendingParamUiModels(skipCount);
        
        actionModel.streamPendingParamUiModels()
        .skip(skipCount)
        .forEach(argAndConsents->{
            
            val paramNumToUpdate = argAndConsents.getParamModel().getNumber();
            val paramPanel = paramPanels.get(paramNumToUpdate);
            val repaint = paramPanel.updateIfNecessary(argAndConsents, Optional.of(target));
            
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
    protected FormExecutorStrategy<ActionModel> getFormExecutorStrategy() {
        ActionModel actionModel = getActionModel();
        return new ActionFormExecutorStrategy(actionModel);
    }

}
