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

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.hints.IsisActionCompletedEvent;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionArgumentModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.scalars.PanelWithChoices;
import org.apache.isis.viewer.wicket.ui.panels.PromptFormPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.property.PropertyEditFormPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarModelSubscriber;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldValueModel.ScalarModelProvider;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationBehavior;

/**
 * {@link PanelAbstract Panel} to capture the arguments for an action
 * invocation.
 */
public class ActionParametersFormPanel extends PromptFormPanelAbstract<ActionModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ACTION_PARAMETERS = "parameters";

    public ActionParametersFormPanel(final String id, final ActionModel model) {
        super(id, model);
        buildGui();
    }

    private void buildGui() {
        ActionModel model = getModel();
        model.clearArguments();  // in case previously used, eg prompt displayed then cancelled
        add(new ActionParameterForm("inputForm", this, this.getSettings(), model));
    }

    class ActionParameterForm extends PropertyEditFormPanel.FormAbstract<ActionModel> implements ScalarModelSubscriber  {

        private static final long serialVersionUID = 1L;

        public ActionParameterForm(
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
            final ActionModel actionModel = getActionModel();
            List<ActionParameterMemento> parameterMementos = actionModel.primeArgumentModels();
            
            final RepeatingView rv = new RepeatingView(ID_ACTION_PARAMETERS);
            add(rv);
            
            paramPanels.clear();
            for (final ActionParameterMemento apm : parameterMementos) {
                final WebMarkupContainer container = new WebMarkupContainer(rv.newChildId());
                rv.add(container);

                final ActionArgumentModel actionArgumentModel = actionModel.getArgumentModel(apm);
                actionArgumentModel.setActionArgsHint(actionModel.getArgumentsAsArray());
                final Component component = getComponentFactoryRegistry().addOrReplaceComponent(container, ComponentType.SCALAR_NAME_AND_VALUE,
                        actionArgumentModel);
                final ScalarPanelAbstract paramPanel = component instanceof ScalarPanelAbstract ? (ScalarPanelAbstract) component : null;
                paramPanels.add(paramPanel);
                if(paramPanel != null) {
                    paramPanel.setOutputMarkupId(true);
                    paramPanel.notifyOnChange(this);
                }
            }
        }

        @Override
        protected void configureButtons(final AjaxButton okButton, final AjaxButton cancelButton) {
            applyAreYouSure(okButton);
        }


        @Override
        protected Object newCompletedEvent(final AjaxRequestTarget target, final Form<?> form) {
            return new IsisActionCompletedEvent(getActionModel(), target, form);
        }

        /**
         * If the {@literal @}Action has "are you sure?" semantics then apply {@link ConfirmationBehavior}
         * that will ask for confirmation before executing the Ajax request.
         *
         * @param button The button which action should be confirmed
         */
        private void applyAreYouSure(AjaxButton button) {
            ActionModel actionModel = getActionModel();
            final ObjectAction action = actionModel.getActionMemento().getAction(getSpecificationLoader());
            SemanticsOf semanticsOf = SemanticsOf.from(action.getSemantics());

            addConfirmationDialogIfAreYouSureSemantics(button, semanticsOf);
        }

        @Override
        public void onUpdate(AjaxRequestTarget target, ScalarModelProvider provider) {

            final ActionModel actionModel = getActionModel();
            
            final ObjectAdapter[] pendingArguments = actionModel.getArgumentsAsArray();
            
            try {
                final ObjectAction action = actionModel.getActionMemento().getAction(getSpecificationLoader());
                final int numParams = action.getParameterCount();
                for (int i = 0; i < numParams; i++) {
                    final ScalarPanelAbstract paramPanel = paramPanels.get(i);
                    if(paramPanel != null && paramPanel instanceof PanelWithChoices) {
                        final PanelWithChoices panelWithChoices = (PanelWithChoices) paramPanel;

                        // this could throw a ConcurrencyException as we may have to reload the
                        // object adapter of the action in order to compute the choices
                        // (and that object adapter might have changed)
                        if (panelWithChoices.updateChoices(pendingArguments)) {
                            paramPanel.repaint(target);
                        }
                    }
                }
            } catch(ConcurrencyException ex) {
                
                // second attempt should succeed, because the Oid would have
                // been updated in the attempt
                ObjectAdapter targetAdapter = getActionModel().getTargetAdapter();

                // forward onto the target page with the concurrency exception
                final EntityPage entityPage = new EntityPage(targetAdapter, ex);
                
                ActionParametersFormPanel.this.setResponsePage(entityPage);
                
                getAuthenticationSession().getMessageBroker().addWarning(ex.getMessage());
                return;
            }
            
            // previously this method was also doing: 
            // target.add(this);
            // ie to update the entire form (in addition to the updates to the individual impacted parameter fields
            // done in the loop above).  However, that logic is wrong, because any values entered in the browser
            // get trampled over (ISIS-629).
        }
        

    }

}
