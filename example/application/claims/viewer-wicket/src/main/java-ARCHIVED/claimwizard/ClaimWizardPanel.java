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

package org.apache.isis.example.claims.viewer.wicket.claimwizard;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.progmodel.facets.object.validate.ValidateObjectFacet;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.panels.ProcessObjectPanelAbstract;
import org.apache.wicket.Session;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

public class ClaimWizardPanel extends ProcessObjectPanelAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_CLAIM_WIZARD_PROPERTIES_FORM = "claimWizardPropertiesForm";
    private static final String ID_PROPERTIES = "properties";
    private static final String ID_PROPERTY = "property";

    public ClaimWizardPanel(String id, EntityModel model) {
        super(id, model);
        buildGui();
    }

    private ClaimWizardForm claimWizardForm;

    private void buildGui() {
        EntityModel entityModel = getModel();
        entityModel.toEditMode();

        claimWizardForm = new ClaimWizardForm(ID_CLAIM_WIZARD_PROPERTIES_FORM, getModel());
        addOrReplace(claimWizardForm);
    }

    class ClaimWizardForm extends Form<ObjectAdapter> {

        private static final long serialVersionUID = 1L;

        private static final String ID_FEEDBACK = "feedback";
        private static final String ID_PREVIOUS = "previous";
        private static final String ID_NEXT = "next";
        private static final String ID_FINISH = "finish";

        private static final String PREVIOUS_ACTION_ID = "previous()";
        private static final String NEXT_ACTION_ID = "next()";
        private static final String FINISH_ACTION_ID = "finish()";

        private FeedbackPanel feedback;

        public ClaimWizardForm(String id, EntityModel entityModel) {
            super(id, entityModel);

            buildFormGui();
        }

        private void buildFormGui() {
            addProperties(this, ID_PROPERTIES, ID_PROPERTY);
            addButtons();
            addFeedbackGui();

            addValidator();
        }

        private void addButtons() {
            add(createButton(ID_PREVIOUS, "Previous", PREVIOUS_ACTION_ID));
            add(createButton(ID_NEXT, "Next", NEXT_ACTION_ID));
            add(createButton(ID_FINISH, "Finish", FINISH_ACTION_ID));
        }

        private Button createButton(final String id, final String label, final String actionId) {
            return new Button(id, Model.of(label)) {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    boolean isValid = ClaimWizardPanel.this.isValid(getForm());
                    if (!isValid) {
                        return;
                    }

                    executeNoArgAction(actionId);
                }
            };
        }

        private void addFeedbackGui() {
            final FeedbackPanel feedback = addOrReplaceFeedback();

            ObjectAdapter adapter = getModel().getObject();
            if (adapter == null) {
                feedback.error("cannot locate object:" + getEntityModel().getObjectAdapterMemento().toString());
            }
        }

        private FeedbackPanel addOrReplaceFeedback() {
            feedback = new ComponentFeedbackPanel(ID_FEEDBACK, this);
            feedback.setOutputMarkupPlaceholderTag(true);
            addOrReplace(feedback);
            return feedback;
        }

        private void addValidator() {
            add(new AbstractFormValidator() {

                private static final long serialVersionUID = 1L;

                @Override
                public FormComponent<?>[] getDependentFormComponents() {
                    return new FormComponent<?>[0];
                }

                @Override
                public void validate(Form<?> form) {
                    EntityModel entityModel = (EntityModel) getModel();
                    ObjectAdapter adapter = entityModel.getObject();
                    ValidateObjectFacet facet = adapter.getSpecification().getFacet(ValidateObjectFacet.class);
                    if (facet == null) {
                        return;
                    }
                    String invalidReasonIfAny = facet.invalidReason(adapter);
                    if (invalidReasonIfAny != null) {
                        Session.get().getFeedbackMessages()
                            .add(new FeedbackMessage(form, invalidReasonIfAny, FeedbackMessage.ERROR));
                    }
                }
            });
        }
    }

}
