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
package org.apache.isis.viewer.wicket.ui.components.entity.propsandcolls;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import org.apache.isis.applib.annotation.MemberGroupLayout.ColumnSpans;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.layout.fixedcols.ColumnMetadata;
import org.apache.isis.applib.layout.fixedcols.ColumnMetadata.Hint;
import org.apache.isis.applib.layout.fixedcols.TabMetadata;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.memento.Memento;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.entity.PropUtil;
import org.apache.isis.viewer.wicket.ui.components.entity.column.EntityColumn;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.CancelHintRequired;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlBehaviour;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.panels.FormAbstract;
import org.apache.isis.viewer.wicket.ui.panels.IFormSubmitterWithPreValidateHook;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import de.agilecoders.wicket.core.util.Attributes;

public class EntityPropsAndCollsForm extends FormAbstract<ObjectAdapter> implements ActionPromptProvider {

    private static final long serialVersionUID = 1L;

    private static final String ID_LEFT_COLUMN = "leftColumn";
    private static final String ID_MIDDLE_COLUMN = "middleColumn";
    private static final String ID_RIGHT_COLUMN = "rightColumn";

    private static final String ID_ENTITY_COLUMN = "entityColumn";
    private static final String ID_ENTITY_COLLECTIONS = "entityCollections";
    private static final String ID_ENTITY_COLLECTIONS_OVERFLOW = "entityCollectionsOverflow";

    private static final String ID_EDIT_BUTTON = "edit";
    private static final String ID_OK_BUTTON = "ok";
    private static final String ID_CANCEL_BUTTON = "cancel";

    private static final String ID_FEEDBACK = "feedback";

    private final Component owningPanel;

    private Button editButton;
    private Button okButton;
    private Button cancelButton;

    private NotificationPanel feedback;
    

    public EntityPropsAndCollsForm(
            final String id,
            final EntityModel entityModel,
            final Component owningPanel) {

        super(id, entityModel);
        this.owningPanel = owningPanel; // for repainting

        buildGui();
        
        // add any concurrency exception that might have been propagated into the entity model 
        // as a result of a previous action invocation
        final String concurrencyExceptionIfAny = entityModel.getAndClearConcurrencyExceptionIfAny();
        if(concurrencyExceptionIfAny != null) {
            error(concurrencyExceptionIfAny);
        }
    }

    private void buildGui() {

        final EntityModel entityModel = (EntityModel) getModel();
        final TabMetadata tabMetaDataIfAny = entityModel.getTabMetadata();

        final ColumnSpans columnSpans;
        if(tabMetaDataIfAny != null) {
            final ColumnMetadata middle = tabMetaDataIfAny.getMiddle();
            final ColumnMetadata right = tabMetaDataIfAny.getRight();
            columnSpans = ColumnSpans.asSpans(
                    tabMetaDataIfAny.getLeft().getSpan(),
                    middle != null? middle.getSpan(): 0,
                    right != null? right.getSpan(): 0);
        } else {
            final MemberGroupLayoutFacet memberGroupLayoutFacet =
                    entityModel.getObject().getSpecification().getFacet(MemberGroupLayoutFacet.class);
            columnSpans = memberGroupLayoutFacet.getColumnSpans();
        }

        // left column
        // (unlike middle and right columns, the left column is always added to hold the edit buttons and feedback)
        MarkupContainer leftColumn = new WebMarkupContainer(ID_LEFT_COLUMN);
        add(leftColumn);

        if(columnSpans.getLeft() > 0) {
            addPropertiesAndCollections(leftColumn, entityModel, tabMetaDataIfAny, Hint.LEFT);
        } else {
            Components.permanentlyHide(this, ID_LEFT_COLUMN);
        }

        // middle column
        MarkupContainer middleColumn;
        if(columnSpans.getMiddle() > 0) {
            middleColumn = new WebMarkupContainer(ID_MIDDLE_COLUMN);
            add(middleColumn);
            addPropertiesAndCollections(middleColumn, entityModel, tabMetaDataIfAny, Hint.MIDDLE);
        } else {
            middleColumn = null;
            Components.permanentlyHide(this, ID_MIDDLE_COLUMN);
        }

        // right column
        MarkupContainer rightColumn;
        if(columnSpans.getRight() > 0) {
            rightColumn = new WebMarkupContainer(ID_RIGHT_COLUMN);
            add(rightColumn);
            addPropertiesAndCollections(rightColumn, entityModel, tabMetaDataIfAny, Hint.RIGHT);
        } else {
            rightColumn = null;
            Components.permanentlyHide(this, ID_RIGHT_COLUMN);
        }

        // column spans
        if(columnSpans.getLeft() > 0) {
            addClassForSpan(leftColumn, Hint.LEFT.from(columnSpans));
        }
        if(columnSpans.getMiddle() > 0) {
            addClassForSpan(middleColumn, Hint.MIDDLE.from(columnSpans));
        }
        if(columnSpans.getRight() > 0) {
            addClassForSpan(rightColumn, Hint.RIGHT.from(columnSpans));
        }

        // edit buttons and feedback (not supported on tabbed view)
        final Hint leftHint = Hint.LEFT;
        final ColumnMetadata leftColumnMetaDataIfAny = leftHint.from(tabMetaDataIfAny);
        final boolean hasProperties = leftColumnMetaDataIfAny == null && !PropUtil
                .propertyGroupNames(entityModel, leftHint, leftColumnMetaDataIfAny).isEmpty();
        if (hasProperties) {
            addButtons(leftColumn);
            addFeedbackGui(leftColumn);

        } else {
            Components.permanentlyHide(leftColumn,
                    ID_EDIT_BUTTON, ID_OK_BUTTON, ID_CANCEL_BUTTON,
                    ID_FEEDBACK);
        }


        // collections (only if not being added to a tab)
        if(tabMetaDataIfAny == null && columnSpans.getCollections() > 0) {
            final String idCollectionsToShow;
            final String idCollectionsToHide;
            int collectionSpan;
            if (columnSpans.exceedsRow())  {
                idCollectionsToShow = ID_ENTITY_COLLECTIONS_OVERFLOW;
                idCollectionsToHide = ID_ENTITY_COLLECTIONS;
                collectionSpan = 12;
            } else {
                idCollectionsToShow = ID_ENTITY_COLLECTIONS;
                idCollectionsToHide = ID_ENTITY_COLLECTIONS_OVERFLOW;
                collectionSpan = columnSpans.getCollections();
            }

            final Component collectionsColumn =
                    getComponentFactoryRegistry().addOrReplaceComponent(
                            this, idCollectionsToShow, ComponentType.ENTITY_COLLECTIONS, entityModel);
            addClassForSpan(collectionsColumn, collectionSpan);

            Components.permanentlyHide(this, idCollectionsToHide);
        } else {
            Components.permanentlyHide(this, ID_ENTITY_COLLECTIONS);
            Components.permanentlyHide(this, ID_ENTITY_COLLECTIONS_OVERFLOW);
        }
    }

    private void addPropertiesAndCollections(
            final MarkupContainer markupContainer,
            final EntityModel entityModel,
            final TabMetadata tabMetaDataIfAny,
            final Hint hint) {
        final ColumnMetadata columnMetaDataIfAny = hint.from(tabMetaDataIfAny);

        final EntityModel entityModelWithHints = entityModel.cloneWithColumnMetadata(columnMetaDataIfAny, hint);

        final EntityColumn columnMembers =
                new EntityColumn(ID_ENTITY_COLUMN, entityModelWithHints);
        markupContainer.add(columnMembers);
    }


    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);

        Attributes.addClass(tag, "form-horizontal");
    }

    @Override
    public ActionPrompt getActionPrompt() {
        return ActionPromptProvider.Util.getFrom(this).getActionPrompt();
    }

    abstract class AjaxButtonWithOnError extends AjaxButton {

        public AjaxButtonWithOnError(String id, IModel<String> model) {
            super(id, model);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form) {
            super.onError(target, form);
            toEditMode(target);
        }

        /**
         * Render the 'type' attribute even for invisible buttons to avoid
         * <a href="https://github.com/twbs/bootlint/wiki/W007">Bootlint W007</a>
         *
         * @param tag The component tag to render
         * @param response The response to write to
         */
        // TODO mgrigorov Move this to Wicket Bootstrap project
        @Override
        protected void renderPlaceholderTag(ComponentTag tag, Response response) {
            String ns = Strings.isEmpty(tag.getNamespace()) ? null : tag.getNamespace() + ':';

            response.write("<");
            if (ns != null)
            {
                response.write(ns);
            }
            response.write(tag.getName());
            response.write(" id=\"");
            response.write(getAjaxRegionMarkupId());

            String type = tag.getAttribute("type");
            if (!Strings.isEmpty(type)) {
                response.write("\" type=\""+type);
            }

            response.write("\" style=\"display:none\"></");
            if (ns != null)
            {
                response.write(ns);
            }
            response.write(tag.getName());
            response.write(">");
        }
    }

    public class AjaxButtonForValidate extends AjaxButtonWithOnError implements IFormSubmitterWithPreValidateHook {
        private static final long serialVersionUID = 1L;
        public AjaxButtonForValidate(String id, IModel<String> model) {
            super(id, model);
        }

        @Override
        public String preValidate() {
            // attempt to load with concurrency checking, catching recognized exceptions
            try {
                getEntityModel().load(ConcurrencyChecking.CHECK); // could have also just called #getObject(), since CHECK is the default

            } catch(ConcurrencyException ex){
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
            EntityPropsAndCollsForm form = EntityPropsAndCollsForm.this;
            String preValidationErrorIfAny = form.getPreValidationErrorIfAny();

            if(preValidationErrorIfAny != null) {
                feedbackOrNotifyAnyRecognizedError(preValidationErrorIfAny, form);
                // skip validation, because would relate to old values

                final EntityPage entityPage = new EntityPage(EntityPropsAndCollsForm.this.getModelObject(), null);
                EntityPropsAndCollsForm.this.setResponsePage(entityPage);
            } else {
                // run Wicket's validation
                super.validate();
            }
        }

        @Override
        protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

            if (getForm().hasError()) {
                // stay in edit mode
                return;
            }

            doPreApply();
            if (applyFormChangesElse(target)) return;
            final Object redirectIfAny = doPostApply();

            if (flushChangesElse(target)) return;


            getEntityModel().resetPropertyModels();

            toViewMode(null);

            // "redirect-after-post"
            //
            // RequestCycle.get().getActiveRequestHandler() indicates this is handled by the ListenerInterfaceRequestHandler
            // which renders page at end.
            //
            // it's necessary to zap the page parameters (so mapping is to just wicket/page?nn)
            // otherwise (what I think happens) is that the httpServletResponse.sendRedirect ends up being to the same URL,
            // and this is rejected as invalid either by the browser or by the servlet container (perhaps only if running remotely).
            //

            final ObjectAdapter objectAdapter;
            if(redirectIfAny != null) {
                objectAdapter = getPersistenceSession().adapterFor(redirectIfAny);
            } else {
                // we obtain the adapter from the entity model because (if a view model) then the entity model may contain
                // a different adapter (the cloned view model) to the one with which we started with.
                objectAdapter = getEntityModel().getObjectAdapterMemento().getObjectAdapter(ConcurrencyChecking.NO_CHECK);
            }

            final EntityPage entityPage = new EntityPage(objectAdapter, null);
            EntityPropsAndCollsForm.this.setResponsePage(entityPage);
        }

        /**
         * Optional hook to override.
         *
         * <p>
         * If a non-null value is returned, then transition to it (ie eg the finish() transition for a wizard).
         * </p>
         */
        protected void doPreApply() {
        }

        /**
         * Optional hook to override.
         *
         * <p>
         * If a non-null value is returned, then transition to it (ie eg the finish() transition for a wizard).
         * </p>
         */
        protected Object doPostApply() {
            return null;
        }

    }

    abstract class AjaxButtonForCancel extends AjaxButtonWithOnError {

        public AjaxButtonForCancel(String id, IModel<String> model) {
            super(id, model);
            setDefaultFormProcessing(false);
        }
    }


    private void addButtons(MarkupContainer markupContainer) {

        // edit button
        editButton = new AjaxButtonWithOnError(ID_EDIT_BUTTON, new ResourceModel("editLabel")) {
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
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.getAjaxCallListeners().add(new org.apache.wicket.ajax.attributes.AjaxCallListener(){

                    private static final long serialVersionUID = 1L;

                    @Override
                    public CharSequence getSuccessHandler(Component component) {
                        // scroll to the top of the entity panel
                        return "$('html, body').animate({"
                               + "        scrollTop: $('.entityIconAndTitlePanel').offset().top"
                               + "    }, 1000);";
                    }
                });
            }
        };
        editButton.add(new Label("editLabel", editButton.getModel()));
        markupContainer.add(editButton);


        // ok button
        okButton = new AjaxButtonForValidate(ID_OK_BUTTON, new ResourceModel("okLabel"));
        markupContainer.add(okButton);


        // cancel button
        cancelButton = new AjaxButtonForCancel(ID_CANCEL_BUTTON, new ResourceModel("cancelLabel")) {
            private static final long serialVersionUID = 1L;
            
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
        };

        markupContainer.add(cancelButton);

        okButton.setOutputMarkupPlaceholderTag(true);
        editButton.setOutputMarkupPlaceholderTag(true);
        cancelButton.setOutputMarkupPlaceholderTag(true);
        
        // flush any JGrowl messages (typically concurrency exceptions) if they are added.
        okButton.add(new JGrowlBehaviour());
        editButton.add(new JGrowlBehaviour());
        cancelButton.add(new JGrowlBehaviour());
    }

    // to perform object-level validation, we must apply the changes first
    // contrast this with ActionPanel (for validating actionarguments) where
    // we do the validation prior to the execution of the action
    private boolean applyFormChangesElse(AjaxRequestTarget target) {
        final ObjectAdapter adapter = getEntityModel().getObject();
        final Memento snapshotToRollbackToIfInvalid = new Memento(adapter);

        getEntityModel().apply();
        final String invalidReasonIfAny = getEntityModel().getReasonInvalidIfAny();
        if (invalidReasonIfAny != null) {
            error(invalidReasonIfAny);
            snapshotToRollbackToIfInvalid.recreateObject();
            toEditMode(target);

            // abort otherwise the object will have been dirtied and JDO will end up committing,
            // possibly bumping the version and resulting in a subsequent concurrency exception.
            IsisContext.getTransactionManager().abortTransaction();
            return true;
        }
        return false;
    }

    private boolean flushChangesElse(AjaxRequestTarget target) {
        try {
            this.getTransactionManager().flushTransaction();
        } catch(RuntimeException ex) {

            // There's no need to abort the transaction here, as it will have already been done
            // (in IsisTransactionManager#executeWithinTransaction(...)).

            String message = recognizeExceptionAndNotify(ex, this);
            if(message == null) {
                throw ex;
            }

            toEditMode(target);
            return true;
        }
        return false;
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

    private EntityModel getEntityModel() {
        return (EntityModel) getModel();
    }

    void toViewMode(final AjaxRequestTarget target) {

        getEntityModel().toViewMode();

        setVisible(editButton, isAnythingEditable());
        setVisible(okButton, false);
        setVisible(cancelButton, false);

        requestRepaintPanel(target);
    }

    private void setVisible(Button b, boolean editable) {
        if(b != null) {
            b.setVisible(editable);
        }
    }

    private boolean isAnythingEditable() {
        final EntityModel entityModel = (EntityModel) getModel();
        final ObjectAdapter adapter = entityModel.getObject();

        return !enabledAssociations(adapter, adapter.getSpecification()).isEmpty();
    }
    
    private List<ObjectAssociation> enabledAssociations(final ObjectAdapter adapter, final ObjectSpecification objSpec) {
        return objSpec.getAssociations(Contributed.EXCLUDED, enabledAssociationFilter(adapter));
    }

    @SuppressWarnings("unchecked")
    private Filter<ObjectAssociation> enabledAssociationFilter(final ObjectAdapter adapter) {
        return Filters.and(ObjectAssociation.Filters.PROPERTIES, ObjectAssociation.Filters.enabled(adapter,
                InteractionInitiatedBy.USER, Where.OBJECT_FORMS
        ));
    }

    private void toEditMode(final AjaxRequestTarget target) {
        getEntityModel().toEditMode();

        editButton.setVisible(false);
        okButton.setVisible(true);
        cancelButton.setVisible(true);

        requestRepaintPanel(target);
    }

    private void addFeedbackGui(final MarkupContainer markupContainer) {
        feedback = new NotificationPanel(ID_FEEDBACK, this, new ComponentFeedbackMessageFilter(this));
        feedback.setOutputMarkupPlaceholderTag(true);
        markupContainer.addOrReplace(feedback);

        // to avoid potential XSS attacks, no longer escape model strings
        // (risk is low but could just happen: error message being rendered might accidentally or deliberately contain rogue Javascript)
        // feedback.setEscapeModelStrings(false);

        final ObjectAdapter adapter = getEntityModel().getObject();
        if (adapter == null) {
            feedback.error("cannot locate object:" + getEntityModel().getObjectAdapterMemento().toString());
        }
    }

    
    static void addClassForSpan(final Component component, final int numGridCols) {
        component.add(new CssClassAppender("col-xs-"+numGridCols));
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
