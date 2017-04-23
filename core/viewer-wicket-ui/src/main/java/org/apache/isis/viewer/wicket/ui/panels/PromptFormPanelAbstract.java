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

package org.apache.isis.viewer.wicket.ui.panels;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.FormExecutor;
import org.apache.isis.viewer.wicket.model.models.HasFormExecutor;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarModelSubscriber;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldValueModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.FormFeedbackPanel;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlBehaviour;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlUtil;

/**
 * {@link PanelAbstract Panel} to capture the arguments for an action
 * invocation.
 */
public abstract class PromptFormPanelAbstract<T extends IModel<?> & HasFormExecutor> extends PanelAbstract<T> {

    private static final String ID_OK_BUTTON = "okButton";
    private static final String ID_CANCEL_BUTTON = "cancelButton";



    public PromptFormPanelAbstract(final String id, final T model) {
        super(id, model);
    }

    public static abstract class FormAbstract<T extends IModel<ObjectAdapter> & HasFormExecutor> extends Form<ObjectAdapter>
            implements ScalarModelSubscriber {

        protected static final String ID_FEEDBACK = "feedback";

        protected final List<ScalarPanelAbstract> paramPanels = Lists.newArrayList();
        protected final Component parentPanel;
        private final WicketViewerSettings settings;
        private final FormExecutor formExecutor;

        public FormAbstract(
                final String id,
                final Component parentPanel,
                final WicketViewerSettings settings,
                final T model) {
            super(id, model);
            this.parentPanel = parentPanel;
            this.settings = settings;
            this.formExecutor = model.getFormExecutor();

            setOutputMarkupId(true); // for ajax button
            addParameters();

            FormFeedbackPanel formFeedback = new FormFeedbackPanel(ID_FEEDBACK);
            addOrReplace(formFeedback);

            AjaxButton okButton = addOkButton();
            addCancelButton();
            configureButtons(okButton);
        }

        protected abstract void addParameters();

        protected AjaxButton addOkButton() {
            AjaxButton okButton = settings.isUseIndicatorForFormSubmit()
                    ? new IndicatingAjaxButton(ID_OK_BUTTON, new ResourceModel("okLabel")) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                            onSubmitOf(target, form, this);
                        }

                        @Override
                        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                            super.updateAjaxAttributes(attributes);
                            if(settings.isPreventDoubleClickForFormSubmit()) {
                                PanelUtil.disableBeforeReenableOnComplete(attributes, this);
                            }
                        }

                        @Override
                        protected void onError(AjaxRequestTarget target, Form<?> form) {
                            super.onError(target, form);
                            target.add(form);
                        }
                    }
                    : new AjaxButton(ID_OK_BUTTON, new ResourceModel("okLabel")) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                            onSubmitOf(target, form, this);
                        }

                        @Override
                        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                            super.updateAjaxAttributes(attributes);
                            if(settings.isPreventDoubleClickForFormSubmit()) {
                                PanelUtil.disableBeforeReenableOnComplete(attributes, this);
                            }
                        }

                        @Override
                        protected void onError(AjaxRequestTarget target, Form<?> form) {
                            super.onError(target, form);
                            target.add(form);
                        }
                    };
            okButton.add(new JGrowlBehaviour());
            setDefaultButton(okButton);
            add(okButton);
            return okButton;
        }


        protected void addCancelButton() {
            AjaxButton cancelButton = new AjaxButton(ID_CANCEL_BUTTON, new ResourceModel("cancelLabel")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit(final AjaxRequestTarget target, Form<?> form) {
                    final ActionPrompt actionPromptIfAny = ActionPromptProvider.Util.getFrom(parentPanel).getActionPrompt();
                    if(actionPromptIfAny != null) {
                        actionPromptIfAny.closePrompt(target);
                    }
                    // HACK: redraw the entire page...
                    target.add(parentPanel.getPage());
                }
            };
            // so can submit with invalid content (eg mandatory params missing)
            cancelButton.setDefaultFormProcessing(false);

            add(cancelButton);
        }

        protected void configureButtons(final AjaxButton okButton) {
        }

        private void onSubmitOf(
                final AjaxRequestTarget target,
                final Form<?> form,
                final AjaxButton ajaxButton) {
            boolean succeeded = formExecutor.executeAndProcessResults(target, form);
            if(succeeded) {
                // the Wicket ajax callbacks will have just started to hide the veil
                // we now show it once more, so that a veil continues to be shown until the
                // new page is rendered.
                target.appendJavaScript("isisShowVeil();\n");

                ajaxButton.send(getPage(), Broadcast.EXACT, newCompletedEvent(target, form));

                target.add(form);
            } else {

                final StringBuilder builder = new StringBuilder();

                // ensure any jGrowl errors are shown
                // (normally would be flushed when traverse to next page).
                String errorMessagesIfAny = JGrowlUtil.asJGrowlCalls(getAuthenticationSession().getMessageBroker());
                builder.append(errorMessagesIfAny);

                // append the JS to the response.
                String buf = builder.toString();
                target.appendJavaScript(buf);
                target.add(form);
            }
        }

        private AuthenticationSession getAuthenticationSession() {
            return getIsisSessionFactory().getCurrentSession().getAuthenticationSession();
        }

        protected IsisSessionFactory getIsisSessionFactory() {
            return IsisContext.getSessionFactory();
        }


        protected abstract Object newCompletedEvent(
                final AjaxRequestTarget target,
                final Form<?> form);

        @Override
        public void onError(AjaxRequestTarget target, TextFieldValueModel.ScalarModelProvider provider) {
            if(provider instanceof Component) {
                // ensure that any feedback error associated with the providing component is shown.
                target.add((Component)provider);
            }
        }
    }

    //region > dependencies
    @com.google.inject.Inject
    private WicketViewerSettings settings;
    protected WicketViewerSettings getSettings() {
        return settings;
    }
    //endregion

}
