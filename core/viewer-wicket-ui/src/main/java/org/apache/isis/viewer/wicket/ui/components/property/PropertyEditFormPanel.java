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

package org.apache.isis.viewer.wicket.ui.components.property;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.ResourceModel;

import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.hints.IsisPropertyEditCompletedEvent;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.ExecutingPanel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarModelSubscriber;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldValueModel.ScalarModelProvider;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.FormFeedbackPanel;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlBehaviour;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlUtil;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * {@link PanelAbstract Panel} to capture the arguments for an action
 * invocation.
 */
public class PropertyEditFormPanel extends PanelAbstract<ScalarModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_OK_BUTTON = "okButton";
    private static final String ID_CANCEL_BUTTON = "cancelButton";

    private static final String ID_PROPERTY = "property";

    private final ExecutingPanel executingPanel;

    public PropertyEditFormPanel(final String id, final ScalarModel model) {
        super(id, model);

        Ensure.ensureThatArg(model.getExecutingPanel(), is(not(nullValue())));

        this.executingPanel = model.getExecutingPanel();

        buildGui();
    }

    private void buildGui() {
        ScalarModel model = getModel();

        add(new PropertyEditForm("inputForm", model));
    }

    class PropertyEditForm extends Form<ObjectAdapter> implements ScalarModelSubscriber  {

        private static final long serialVersionUID = 1L;

        private static final String ID_FEEDBACK = "feedback";
        
        private ScalarPanelAbstract propertyPanel = null;

        public PropertyEditForm(final String id, final ScalarModel actionModel) {
            super(id, actionModel);

            setOutputMarkupId(true); // for ajax button
            
            addParameters();

            FormFeedbackPanel formFeedback = new FormFeedbackPanel(ID_FEEDBACK);
            addOrReplace(formFeedback);
            addButtons();
        }

        private ScalarModel getScalarModel() {
            return (ScalarModel) super.getModel();
        }

        private void addParameters() {
            final ScalarModel scalarModel = getScalarModel();

            final WebMarkupContainer container = new WebMarkupContainer(ID_PROPERTY);
            add(container);

            final Component component = getComponentFactoryRegistry().addOrReplaceComponent(container, ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);
            final ScalarPanelAbstract paramPanel = component instanceof ScalarPanelAbstract ? (ScalarPanelAbstract) component : null;
            propertyPanel = paramPanel;
            if(paramPanel != null) {
                paramPanel.setOutputMarkupId(true);
                paramPanel.notifyOnChange(this);
            }
        }


        private void addButtons() {
            AjaxButton okButton = new AjaxButton(ID_OK_BUTTON, new ResourceModel("okLabel")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    boolean succeeded = executingPanel.executeAndProcessResults(target, form);
                    if(succeeded) {
                        // the Wicket ajax callbacks will have just started to hide the veil
                        // we now show it once more, so that a veil continues to be shown until the
                        // new page is rendered.
                        target.appendJavaScript("isisShowVeil();\n");

                        send(getPage(), Broadcast.EXACT, new IsisPropertyEditCompletedEvent(getScalarModel(), target, form));

                        target.add(form);
                    } else {

                        final StringBuilder builder = new StringBuilder();

                        // ensure any jGrowl errors are shown
                        // (normally would be flushed when traverse to next page).
                        final MessageBroker messageBroker = getIsisSessionFactory().getCurrentSession()
                                .getAuthenticationSession().getMessageBroker();
                        String errorMessagesIfAny = JGrowlUtil.asJGrowlCalls(messageBroker);
                        builder.append(errorMessagesIfAny);

                        // append the JS to the response.
                        String buf = builder.toString();
                        target.appendJavaScript(buf);
                        target.add(form);
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
                    final ActionPrompt actionPromptIfAny = ActionPromptProvider.Util.getFrom(PropertyEditFormPanel.this).getActionPrompt();
                    if(actionPromptIfAny != null) {
                        actionPromptIfAny.closePrompt(target);
                    }
                }
            };
            // so can submit with invalid content (eg mandatory params missing)
            cancelButton.setDefaultFormProcessing(false);
            add(cancelButton);

        }

        @Override
        public void onUpdate(
                final AjaxRequestTarget target, final ScalarModelProvider provider) {

        }

        public void onError(AjaxRequestTarget target, ScalarModelProvider provider) {
            if(provider instanceof Component) {
                // ensure that any feedback error associated with the providing component is shown.
                target.add((Component)provider); 
            }
        }

    }

}
