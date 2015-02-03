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
import com.google.common.collect.Lists;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.ResourceModel;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.hints.IsisActionCompletedEvent;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionExecutor;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actionprompt.ActionPromptModalWindow;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarModelSubscriber;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldValueModel.ScalarModelProvider;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.FormFeedbackPanel;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlBehaviour;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlUtil;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import static org.hamcrest.CoreMatchers.*;

/**
 * {@link PanelAbstract Panel} to capture the arguments for an action
 * invocation.
 */
public class ActionParametersFormPanel extends PanelAbstract<ActionModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_OK_BUTTON = "okButton";
    private static final String ID_CANCEL_BUTTON = "cancelButton";
    private static final String ID_ACTION_PARAMETERS = "parameters";

    private final ActionExecutor actionExecutor;
    //private final ActionPrompt actionPromptIfAny;

    public ActionParametersFormPanel(final String id, final ActionModel model) {
        super(id, model);

        Ensure.ensureThatArg(model.getExecutor(), is(not(nullValue())));

        this.actionExecutor = model.getExecutor();
        //this.actionPromptIfAny = model.getActionPrompt();
        buildGui();
    }

    private void buildGui() {
        ActionModel model = getModel();
        // in case previously used, eg prompt displayed then cancelled
        model.clearArguments();
        
        add(new ActionParameterForm("inputForm", model));
    }

    class ActionParameterForm extends Form<ObjectAdapter> implements ScalarModelSubscriber  {

        private static final long serialVersionUID = 1L;

        private static final String ID_FEEDBACK = "feedback";
        
        private final List<ScalarPanelAbstract> paramPanels = Lists.newArrayList();

        public ActionParameterForm(final String id, final ActionModel actionModel) {
            super(id, actionModel);

            setOutputMarkupId(true); // for ajax button
            
            addParameters();

            FormFeedbackPanel formFeedback = new FormFeedbackPanel(ID_FEEDBACK);
            addOrReplace(formFeedback);
            addButtons();
        }

        private ActionModel getActionModel() {
            return (ActionModel) super.getModel();
        }

        private void addParameters() {
            final ActionModel actionModel = getActionModel();
            List<ActionParameterMemento> parameterMementos = actionModel.primeArgumentModels();
            
            final RepeatingView rv = new RepeatingView(ID_ACTION_PARAMETERS);
            add(rv);
            
            paramPanels.clear();
            for (final ActionParameterMemento apm : parameterMementos) {
                final WebMarkupContainer container = new WebMarkupContainer(rv.newChildId());
                rv.add(container);

                final ScalarModel argumentModel = actionModel.getArgumentModel(apm);
                argumentModel.setActionArgsHint(actionModel.getArgumentsAsArray());
                final Component component = getComponentFactoryRegistry().addOrReplaceComponent(container, ComponentType.SCALAR_NAME_AND_VALUE, argumentModel);
                final ScalarPanelAbstract paramPanel = component instanceof ScalarPanelAbstract ? (ScalarPanelAbstract) component : null;
                paramPanels.add(paramPanel);
                if(paramPanel != null) {
                    paramPanel.setOutputMarkupId(true);
                    paramPanel.notifyOnChange(this);
                }
            }
        }


        private void addButtons() {
            AjaxButton okButton = new AjaxButton(ID_OK_BUTTON, new ResourceModel("okLabel")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    boolean succeeded = actionExecutor.executeActionAndProcessResults(target, form);
                    if(succeeded) {
                        // the Wicket ajax callbacks will have just started to hide the veil
                        // we now show it once more, so that a veil continues to be shown until the
                        // new page is rendered.
                        target.appendJavaScript("isisShowVeil();\n");

                        send(getPage(), Broadcast.EXACT, new IsisActionCompletedEvent(getActionModel(), target, form));

                        target.add(form);
                    } else {
                        //if (actionPromptIfAny != null) {
                            
                            final StringBuilder builder = new StringBuilder();

                            // ensure any jGrowl errors are shown
                            // (normally would be flushed when traverse to next page).
                            String errorMessagesIfAny = JGrowlUtil.asJGrowlCalls(IsisContext.getMessageBroker());
                            builder.append(errorMessagesIfAny);

                            // append the JS to the response. 
                            String buf = builder.toString();
                            target.appendJavaScript(buf);
                            target.add(form);
                        //}
                    }
                };

                /**
                 * On validation error
                 */
                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    super.onError(target, form);
                    target.add(form);
                }
            };
            okButton.add(new JGrowlBehaviour());
            setDefaultButton(okButton);
            add(okButton);
            
            AjaxButton cancelButton = new AjaxButton(ID_CANCEL_BUTTON, new ResourceModel("cancelLabel")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit(final AjaxRequestTarget target, Form<?> form) {
                    final ActionPrompt actionPromptIfAny = ActionPromptProvider.Util.getFrom(ActionParametersFormPanel.this).getActionPrompt();
                    if(actionPromptIfAny != null) {
                        actionPromptIfAny.closePrompt(target);
                    }
                }
            };
            // so can submit with invalid content (eg mandatory params missing)
            cancelButton.setDefaultFormProcessing(false);
            add(cancelButton);
            
            // TODO: hide cancel button if dialogs disabled, as not yet implemented.
            if(ActionPromptModalWindow.isActionPromptModalDialogDisabled()) {
                cancelButton.setVisible(false);
            }
        }

        @Override
        public void onUpdate(AjaxRequestTarget target, ScalarModelProvider provider) {

            final ActionModel actionModel = getActionModel();
            
            final ObjectAdapter[] pendingArguments = actionModel.getArgumentsAsArray();
            
            try {
                final ObjectAction action = actionModel.getActionMemento().getAction();
                final int numParams = action.getParameterCount();
                for (int i = 0; i < numParams; i++) {
                    final ScalarPanelAbstract paramPanel = paramPanels.get(i);
                    if(paramPanel != null) {
                        // this could throw a ConcurrencyException as we may have to reload the 
                        // object adapter of the action in order to compute the choices
                        // (and that object adapter might have changed)
                        if(paramPanel.updateChoices(pendingArguments)) {
                            target.add(paramPanel);
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
        
        @Override
        public void onError(AjaxRequestTarget target, ScalarModelProvider provider) {
            if(provider instanceof Component) {
                // ensure that any feedback error associated with the providing component is shown.
                target.add((Component)provider); 
            }
        }

    }

}
