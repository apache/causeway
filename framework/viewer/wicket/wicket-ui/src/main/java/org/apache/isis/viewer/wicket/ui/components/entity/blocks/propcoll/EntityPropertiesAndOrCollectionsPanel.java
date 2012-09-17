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

package org.apache.isis.viewer.wicket.ui.components.entity.blocks.propcoll;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.progmodel.facets.object.validate.ValidateObjectFacet;
import org.apache.isis.runtimes.dflt.runtime.memento.Memento;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.collection.CollectionPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.CancelHintRequired;
import org.apache.isis.viewer.wicket.ui.panels.AjaxButtonWithPreSubmitHook;
import org.apache.isis.viewer.wicket.ui.panels.ButtonWithPreSubmitHook;
import org.apache.isis.viewer.wicket.ui.panels.FormAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.EvenOrOddCssClassAppenderFactory;

/**
 * {@link PanelAbstract Panel} representing the properties of an entity, as per
 * the provided {@link EntityModel}.
 */
public class EntityPropertiesAndOrCollectionsPanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_PROPERTIES_AND_OR_COLLECTIONS = "entityPropertiesAndOrCollections";

    public enum Render {
        PROPERTIES_ONLY {
            @Override
            public Filter<ObjectAssociation> getFilters() {
                return ObjectAssociationFilters.PROPERTIES;
            }
        },
        COLLECTIONS_ONLY {
            @Override
            public Filter<ObjectAssociation> getFilters() {
                return ObjectAssociationFilters.COLLECTIONS;
            }
        },
        PROPERTIES_AND_COLLECTIONS {
            @SuppressWarnings("unchecked")
            @Override
            public Filter<ObjectAssociation> getFilters() {
                return Filters.or(PROPERTIES_ONLY.getFilters(), COLLECTIONS_ONLY.getFilters());
            }
        };

        public abstract Filter<ObjectAssociation> getFilters();
    }

    private final Render render;
    private PropCollForm form;

    public EntityPropertiesAndOrCollectionsPanel(final String id, final EntityModel entityModel, final Render render) {
        super(id, entityModel);
        this.render = render;
        buildGui();
        form.toViewMode(null);
    }

    private void buildGui() {
        buildEntityPropertiesAndOrCollectionsGui();
        setOutputMarkupId(true); // so can repaint via ajax
    }

    private void buildEntityPropertiesAndOrCollectionsGui() {
        final EntityModel model = getModel();
        final ObjectAdapter adapter = model.getObject();
        if (adapter != null) {
            form = new PropCollForm(ID_ENTITY_PROPERTIES_AND_OR_COLLECTIONS, model, render, this);
            addOrReplace(form);
        } else {
            permanentlyHide(ID_ENTITY_PROPERTIES_AND_OR_COLLECTIONS);
        }
    }

    static class PropCollForm extends FormAbstract<ObjectAdapter> {

        private static final long serialVersionUID = 1L;

        private static final String ID_PROPERTIES_AND_OR_COLLECTIONS = "propertiesAndOrCollections";
        private static final String ID_PROPERTY_OR_COLLECTION = "propertyOrCollection";
        private static final String ID_EDIT_BUTTON = "edit";
        private static final String ID_OK_BUTTON = "ok";
        private static final String ID_CANCEL_BUTTON = "cancel";
        private static final String ID_FEEDBACK = "feedback";

        private final Render render;

        private final Component owningPanel;
        private Button editButton;
        private Button okButton;
        private Button cancelButton;
        private FeedbackPanel feedback;

        public PropCollForm(final String id, final EntityModel entityModel, final Render render, final Component owningPanel) {
            super(id, entityModel);
            this.owningPanel = owningPanel; // for repainting
            this.render = render;

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
            final ObjectSpecification noSpec = adapter.getSpecification();

            final List<ObjectAssociation> associations = visibleAssociations(adapter, noSpec, Where.OBJECT_FORMS);

            final RepeatingView rv = new RepeatingView(ID_PROPERTIES_AND_OR_COLLECTIONS);
            final EvenOrOddCssClassAppenderFactory eo = new EvenOrOddCssClassAppenderFactory();
            add(rv);

            @SuppressWarnings("unused")
            Component component;
            for (final ObjectAssociation association : associations) {
                if (association instanceof OneToOneAssociation) {
                    final WebMarkupContainer container = new WebMarkupContainer(rv.newChildId());
                    rv.add(container);
                    container.add(eo.nextClass());

                    addPropertyToForm(entityModel, association, container);
                } else {
                    final WebMarkupContainer container = new WebMarkupContainer(rv.newChildId());
                    rv.add(container);
                    container.add(eo.nextClass());
                    
                    addCollectionToForm(entityModel, association, container);
                }
            }

            // massive hack: an empty property line to get CSS correct...!
            final WebMarkupContainer container = new WebMarkupContainer(rv.newChildId());
            rv.add(container);
            container.add(new Label(ID_PROPERTY_OR_COLLECTION, Model.of(" ")));
            container.add(eo.nextClass());
        }

		private void addPropertyToForm(final EntityModel entityModel,
				final ObjectAssociation association,
				final WebMarkupContainer container) {
			final OneToOneAssociation otoa = (OneToOneAssociation) association;
			final PropertyMemento pm = new PropertyMemento(otoa);

			final ScalarModel scalarModel = entityModel.getPropertyModel(pm);
			getComponentFactoryRegistry().addOrReplaceComponent(container, ID_PROPERTY_OR_COLLECTION, ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);
		}

		private void addCollectionToForm(final EntityModel entityModel,
				final ObjectAssociation association,
				final WebMarkupContainer container) {
			final OneToManyAssociation otma = (OneToManyAssociation) association;

			final EntityCollectionModel entityCollectionModel = EntityCollectionModel.createParented(entityModel, otma);
			final CollectionPanel collectionPanel = new CollectionPanel(ID_PROPERTY_OR_COLLECTION, entityCollectionModel);
			container.addOrReplace(collectionPanel);

			getComponentFactoryRegistry().addOrReplaceComponent(container, ID_PROPERTY_OR_COLLECTION, ComponentType.COLLECTION_NAME_AND_CONTENTS, entityCollectionModel);
		}

        private List<ObjectAssociation> visibleAssociations(final ObjectAdapter adapter, final ObjectSpecification objSpec, Where where) {
            return objSpec.getAssociations(visibleAssociationFilter(adapter, where));
        }

        @SuppressWarnings("unchecked")
        private Filter<ObjectAssociation> visibleAssociationFilter(final ObjectAdapter adapter, Where where) {
            return Filters.and(render.getFilters(), ObjectAssociationFilters.dynamicallyVisible(getAuthenticationSession(), adapter, where));
        }

        private void addButtons() {
            editButton = new AjaxButtonWithPreSubmitHook(ID_EDIT_BUTTON, Model.of("Edit")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void preSubmit() {
                    getEntityModel().getObjectAdapterMemento().getObjectAdapter(ConcurrencyChecking.NO_CHECK);
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

            okButton = new ButtonWithPreSubmitHook(ID_OK_BUTTON, Model.of("OK")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void preSubmit() {
                    try {
                        getEntityModel().getObjectAdapterMemento().getObjectAdapter(ConcurrencyChecking.CHECK);
                    } catch(ConcurrencyException ex){
                        Session.get().getFeedbackMessages().add(new FeedbackMessage(EntityPropertiesAndOrCollectionsPanel.PropCollForm.this, ex.getMessage(), FeedbackMessage.ERROR));
                    }
                }

                @Override
                public void onSubmit() {
                    if (!getForm().hasError()) {
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
                        } else {
                            getEntityModel().resetPropertyModels();
                            toViewMode(null);
                        }
                    } else {
                        // stay in edit mode
                    }
                }

            };
            add(okButton);

            cancelButton = new AjaxButtonWithPreSubmitHook(ID_CANCEL_BUTTON, Model.of("Cancel")) {
                private static final long serialVersionUID = 1L;
                
                {
                    setDefaultFormProcessing(false);
                }

                @Override
                public void preSubmit() {
                    getEntityModel().getObjectAdapterMemento().getObjectAdapter(ConcurrencyChecking.NO_CHECK);
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

//                        @Override
//                        public Object formComponent(final IFormVisitorParticipant formComponent) {
//                            if (formComponent instanceof CancelHintRequired) {
//                                final CancelHintRequired cancelHintRequired = (CancelHintRequired) formComponent;
//                                cancelHintRequired.onCancel();
//                            }
//                            return null;
//                        }
                    
                    });
                    getEntityModel().resetPropertyModels();
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
        }

        private void requestRepaintPanel(final AjaxRequestTarget target) {
            if (target != null) {
//                target.addComponent(owningPanel);
//                // TODO: is it necessary to add these too?
//                target.addComponent(editButton);
//                target.addComponent(okButton);
//                target.addComponent(cancelButton);
//                target.addComponent(feedback);
                
                target.add(owningPanel);
                // TODO: is it necessary to add these too?
                target.add(editButton, okButton, cancelButton, feedback);
            }
        }

        private void addValidator() {
            add(new AbstractFormValidator() {

                private static final long serialVersionUID = 1L;

                @Override
                public FormComponent<?>[] getDependentFormComponents() {
                    return new FormComponent<?>[0];
                }

                @Override
                public void validate(final Form<?> form) {
                    final EntityModel entityModel = (EntityModel) getModel();
                    String invalidReasonIfAny;
                    try {
                        final ObjectAdapter adapter = entityModel.getObject();
                        final ValidateObjectFacet facet = adapter.getSpecification().getFacet(ValidateObjectFacet.class);
                        if (facet == null) {
                            return;
                        }
                        invalidReasonIfAny = facet.invalidReason(adapter);
                    } catch(ConcurrencyException ex) {
                        invalidReasonIfAny = ex.getMessage();
                    }
                    if (invalidReasonIfAny != null) {
                        Session.get().getFeedbackMessages().add(new FeedbackMessage(form, invalidReasonIfAny, FeedbackMessage.ERROR));
                    }
                }
            });
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
            return Filters.and(render.getFilters(), ObjectAssociationFilters.enabled(getAuthenticationSession(), adapter, Where.OBJECT_FORMS));
        }


        private void toEditMode(final AjaxRequestTarget target) {
            getEntityModel().toEditMode();
            editButton.setVisible(false);
            okButton.setVisible(true);
            cancelButton.setVisible(true);
            requestRepaintPanel(target);
        }

        
        @Override
        protected void onValidate() {
            // 6.0.0 - no longer required because feedback messages are automatically cleaned up
            // see https://cwiki.apache.org/WICKET/migration-to-wicket-60.html#MigrationtoWicket6.0-FeedbackStorageRefactoring
//            Session.get().getFeedbackMessages().clear(new IFeedbackMessageFilter() {
//
//                private static final long serialVersionUID = 1L;
//
//                @Override
//                public boolean accept(final FeedbackMessage message) {
//                    return message.getReporter() == owningPanel;
//                }
//            });
            super.onValidate();
        }

        private void addFeedbackGui() {
            final FeedbackPanel feedback = addOrReplaceFeedback();

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
    }

}
