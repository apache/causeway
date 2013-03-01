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
package org.apache.isis.viewer.wicket.ui.components.entity.properties;

import java.util.List;
import java.util.Map;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.progmodel.facets.object.validate.ValidateObjectFacet;
import org.apache.isis.core.runtime.memento.Memento;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.util.ObjectAssociations;
import org.apache.isis.viewer.wicket.model.util.ObjectSpecifications;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.CancelHintRequired;
import org.apache.isis.viewer.wicket.ui.notifications.JGrowlBehaviour;
import org.apache.isis.viewer.wicket.ui.panels.ButtonWithPreValidateHook;
import org.apache.isis.viewer.wicket.ui.panels.FormAbstract;
import org.apache.isis.viewer.wicket.ui.util.EvenOrOddCssClassAppenderFactory;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

class EntityPropertiesForm extends FormAbstract<ObjectAdapter> {

    private static final long serialVersionUID = 1L;

    private static final String ID_MEMBER_GROUP = "memberGroup";
    private static final String ID_MEMBER_GROUP_NAME = "memberGroupName";

    private static final String ID_PROPERTIES = "properties";
    private static final String ID_PROPERTY = "property";
    private static final String ID_EDIT_BUTTON = "edit";
    private static final String ID_OK_BUTTON = "ok";
    private static final String ID_CANCEL_BUTTON = "cancel";
    private static final String ID_FEEDBACK = "feedback";

    private final Component owningPanel;
    private Button editButton;
    private Button okButton;
    private Button cancelButton;
    private FeedbackPanel feedback;

    public EntityPropertiesForm(final String id, final EntityModel entityModel, final Component owningPanel) {
        super(id, entityModel);
        this.owningPanel = owningPanel; // for repainting

        buildGui();
        
        // add any concurrency exception that might have been propogated into the entity model 
        // as a result of a previous action invocation
        final String concurrencyExceptionIfAny = entityModel.getAndClearConcurrencyExceptionIfAny();
        if(concurrencyExceptionIfAny != null) {
            error(concurrencyExceptionIfAny);
        }
    }

    private void buildGui() {
        addPropertiesAndOrCollections();
        addButtons();
        addFeedbackGui();

        addValidator();
    }

    private void addPropertiesAndOrCollections() {
        final EntityModel entityModel = (EntityModel) getModel();
        final ObjectAdapter adapter = entityModel.getObject();
        final ObjectSpecification objSpec = adapter.getSpecification();

        final List<ObjectAssociation> associations = visibleAssociations(adapter, objSpec, Where.OBJECT_FORMS);

        final RepeatingView memberGroupRv = new RepeatingView(ID_MEMBER_GROUP);
        add(memberGroupRv);

        Map<String, List<ObjectAssociation>> associationsByGroup = ObjectAssociations.groupByMemberOrderName(associations);
        final List<String> groupNames = ObjectSpecifications.orderByMemberGroups(objSpec, associationsByGroup.keySet());
        
        for(String groupName: groupNames) {
            final List<ObjectAssociation> associationsInGroup = associationsByGroup.get(groupName);

            final WebMarkupContainer memberGroupRvContainer = new WebMarkupContainer(memberGroupRv.newChildId());
            memberGroupRv.add(memberGroupRvContainer);
            memberGroupRvContainer.add(new Label(ID_MEMBER_GROUP_NAME, groupName));


            final RepeatingView propertyRv = new RepeatingView(ID_PROPERTIES);
            final EvenOrOddCssClassAppenderFactory eo = new EvenOrOddCssClassAppenderFactory();
            memberGroupRvContainer.add(propertyRv);

            @SuppressWarnings("unused")
            Component component;
            for (final ObjectAssociation association : associationsInGroup) {
                final WebMarkupContainer propertyRvContainer = new WebMarkupContainer(propertyRv.newChildId());
                propertyRv.add(propertyRvContainer);
                propertyRvContainer.add(eo.nextClass());
                addPropertyToForm(entityModel, association, propertyRvContainer);
            }
        }
    }

    private void addPropertyToForm(final EntityModel entityModel,
			final ObjectAssociation association,
			final WebMarkupContainer container) {
		final OneToOneAssociation otoa = (OneToOneAssociation) association;
		final PropertyMemento pm = new PropertyMemento(otoa);

		final ScalarModel scalarModel = entityModel.getPropertyModel(pm);
		getComponentFactoryRegistry().addOrReplaceComponent(container, ID_PROPERTY, ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);
	}

    private List<ObjectAssociation> visibleAssociations(final ObjectAdapter adapter, final ObjectSpecification objSpec, Where where) {
        return objSpec.getAssociations(visibleAssociationFilter(adapter, where));
    }

    @SuppressWarnings("unchecked")
    private Filter<ObjectAssociation> visibleAssociationFilter(final ObjectAdapter adapter, Where where) {
        return Filters.and(ObjectAssociationFilters.PROPERTIES, ObjectAssociationFilters.dynamicallyVisible(getAuthenticationSession(), adapter, where));
    }

    private void addButtons() {
        editButton = new AjaxButton(ID_EDIT_BUTTON, Model.of("Edit")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate() {

                // same logic as in cancelButton; should this be factored out?
                try {
                    getEntityModel().load(ConcurrencyChecking.CHECK);
                } catch(ConcurrencyException ex) {
                    getMessageBroker().addMessage("Object changed by " + ex.getOid().getVersion().getUser() + ", automatically reloading");
                    getEntityModel().load(ConcurrencyChecking.NO_CHECK);
                }
                
                super.validate();
            }
            
            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                getEntityModel().resetPropertyModels();
                toEditMode(target);
            }

            @Override
            protected void onError(final AjaxRequestTarget target, final Form<?> form) {
                toEditMode(target);
            }
        };
        add(editButton);

        
        okButton = new ButtonWithPreValidateHook(ID_OK_BUTTON, Model.of("OK")) {
            private static final long serialVersionUID = 1L;


            @Override
            public String preValidate() {
                // attempt to load with concurrency checking, catching recognized exceptions
                try {
                    getEntityModel().load(ConcurrencyChecking.CHECK); // could have also just called #getObject(), since CHECK is the default

                } catch(RuntimeException ex){
                    String recognizedErrorMessage = recognizeException(ex);
                    if(recognizedErrorMessage == null) {
                        throw ex;
                    }

                    // reload
                    getEntityModel().load(ConcurrencyChecking.NO_CHECK);
                    
                    getForm().clearInput();
                    getEntityModel().resetPropertyModels();
                    
                    toViewMode(null);
                    toEditMode(null);
                    
                    return recognizedErrorMessage;
                }
                
                return null;
            }

            @Override
            public void validate() {

                // add in any error message that we might have recognized from above
                EntityPropertiesForm form = EntityPropertiesForm.this;
                String preValidationErrorIfAny = form.getPreValidationErrorIfAny();
                
                if(preValidationErrorIfAny != null) {
                    feedbackOrNotifyAnyRecognizedError(preValidationErrorIfAny, form);
                    // skip validation, because would relate to old values
                } else {
                    // run Wicket's validation
                    super.validate();
                }
            }
            
            @Override
            public void onSubmit() {
                if (getForm().hasError()) {
                    // stay in edit mode
                    return;
                } 
                
                final ObjectAdapter object = getEntityModel().getObject();
                final Memento snapshotToRollbackToIfInvalid = new Memento(object);
                // to perform object-level validation, we must apply the
                // changes first
                // contrast this with ActionPanel (for validating action
                // arguments) where
                // we do the validation prior to the execution of the
                // action
                getEntityModel().apply();
                final String invalidReasonIfAny = getEntityModel().getReasonInvalidIfAny();
                if (invalidReasonIfAny != null) {
                    getForm().error(invalidReasonIfAny);
                    snapshotToRollbackToIfInvalid.recreateObject();
                    return;
                }
                
                try {
                    EntityPropertiesForm.this.getTransactionManager().flushTransaction();
                } catch(RuntimeException ex) {
                    
                    // There's no need to abort the transaction here, as it will have already been done
                    // (in IsisTransactionManager#executeWithinTransaction(...)).

                    String message = recognizeExceptionAndNotify(ex, EntityPropertiesForm.this);
                    if(message == null) {
                        throw ex;
                    }
                    toEditMode(null);
                    return;
                }

                try {
                    getEntityModel().resetPropertyModels();
                } catch(RuntimeException ex) {
                    throw ex;
                }

                toViewMode(null);
            }

        };
        add(okButton);
        
        okButton.add(new IValidator<String>(){

            private static final long serialVersionUID = 1L;

            @Override
            public void validate(IValidatable<String> validatable) {


                //validatable.error(new ValidationError("testing 1,2,3"));
            }
        });

        cancelButton = new AjaxButton(ID_CANCEL_BUTTON, Model.of("Cancel")) {
            private static final long serialVersionUID = 1L;
            
            {
                setDefaultFormProcessing(false);
            }

            @Override
            public void validate() {

                // same logic as in editButton; should this be factored out?
                try {
                    getEntityModel().load(ConcurrencyChecking.CHECK);
                } catch(ConcurrencyException ex) {
                    getMessageBroker().addMessage("Object changed by " + ex.getOid().getVersion().getUser() + ", automatically reloading");
                    getEntityModel().load(ConcurrencyChecking.NO_CHECK);
                }
                super.validate();
            }
            
            @Override
            protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                Session.get().getFeedbackMessages().clear();
                getForm().clearInput();
                getForm().visitFormComponentsPostOrder(new IVisitor<FormComponent<?>, Void>() {

                    @Override
                    public void component(FormComponent<?> formComponent, IVisit<Void> visit) {
                        if (formComponent instanceof CancelHintRequired) {
                            final CancelHintRequired cancelHintRequired = (CancelHintRequired) formComponent;
                            cancelHintRequired.onCancel();
                        }
                    }
                });
                
                try {
                    getEntityModel().resetPropertyModels();
                } catch(RuntimeException ex) {
                    throw ex;
                }
                toViewMode(target);
            }

            @Override
            protected void onError(final AjaxRequestTarget target, final Form<?> form) {
                toViewMode(target);
            }
        };
        add(cancelButton);

        editButton.setOutputMarkupPlaceholderTag(true);
        cancelButton.setOutputMarkupPlaceholderTag(true);
        
        editButton.add(new JGrowlBehaviour());
        cancelButton.add(new JGrowlBehaviour());
    }

    private String recognizeExceptionAndNotify(RuntimeException ex, Component feedbackComponentIfAny) {
        
        // see if the exception is recognized as being a non-serious error
        
        String recognizedErrorMessageIfAny = recognizeException(ex);
        feedbackOrNotifyAnyRecognizedError(recognizedErrorMessageIfAny, feedbackComponentIfAny);

        return recognizedErrorMessageIfAny;
    }

    private void feedbackOrNotifyAnyRecognizedError(String recognizedErrorMessageIfAny, Component feedbackComponentIfAny) {
        if(recognizedErrorMessageIfAny == null) {
            return;
        }
        
        if(feedbackComponentIfAny != null) {
            feedbackComponentIfAny.error(recognizedErrorMessageIfAny);
        }
        getMessageBroker().addWarning(recognizedErrorMessageIfAny);

        // we clear the abort cause because we've handled rendering the exception
        getTransactionManager().getTransaction().clearAbortCause();
    }

    private String recognizeException(RuntimeException ex) {
        
        // REVIEW: this code is similar to stuff in EntityPropertiesForm, perhaps move up to superclass?
        // REVIEW: similar code also in WebRequestCycleForIsis; combine?
        
        final List<ExceptionRecognizer> exceptionRecognizers = getServicesInjector().lookupServices(ExceptionRecognizer.class);
        final String message = new ExceptionRecognizerComposite(exceptionRecognizers).recognize(ex);
        return message;
    }

    private void requestRepaintPanel(final AjaxRequestTarget target) {
        if (target != null) {
            target.add(owningPanel);
            // TODO: is it necessary to add these too?
            target.add(editButton, okButton, cancelButton, feedback);
        }
    }

    private void addValidator() {

        // no longer used, instead using the PreValidate stuff.
        
//        add(new AbstractFormValidator() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public FormComponent<?>[] getDependentFormComponents() {
//                return new FormComponent<?>[0];
//            }
//
//            @Override
//            public void validate(final Form<?> form) {
//            }
//        });
    }

    private EntityModel getEntityModel() {
        return (EntityModel) getModel();
    }

    void toViewMode(final AjaxRequestTarget target) {
        getEntityModel().toViewMode();
        editButton.setVisible(isAnythingEditable());
        okButton.setVisible(false);
        cancelButton.setVisible(false);
        requestRepaintPanel(target);
    }

    private boolean isAnythingEditable() {
        final EntityModel entityModel = (EntityModel) getModel();
        final ObjectAdapter adapter = entityModel.getObject();

        return !enabledAssociations(adapter, adapter.getSpecification()).isEmpty();
    }
    
    private List<ObjectAssociation> enabledAssociations(final ObjectAdapter adapter, final ObjectSpecification objSpec) {
        return objSpec.getAssociations(enabledAssociationFilter(adapter));
    }

    @SuppressWarnings("unchecked")
    private Filter<ObjectAssociation> enabledAssociationFilter(final ObjectAdapter adapter) {
        return Filters.and(ObjectAssociationFilters.PROPERTIES, ObjectAssociationFilters.enabled(getAuthenticationSession(), adapter, Where.OBJECT_FORMS));
    }

    private void toEditMode(final AjaxRequestTarget target) {
        getEntityModel().toEditMode();
        editButton.setVisible(false);
        okButton.setVisible(true);
        cancelButton.setVisible(true);
        requestRepaintPanel(target);
    }


    private void addFeedbackGui() {
        final FeedbackPanel feedback = addOrReplaceFeedback();
        feedback.setEscapeModelStrings(false);

        final ObjectAdapter adapter = getEntityModel().getObject();
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
    
    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////
    
    protected IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }

    protected ServicesInjector getServicesInjector() {
        return IsisContext.getPersistenceSession().getServicesInjector();
    }

    protected MessageBroker getMessageBroker() {
        return getAuthenticationSession().getMessageBroker();
    }

}