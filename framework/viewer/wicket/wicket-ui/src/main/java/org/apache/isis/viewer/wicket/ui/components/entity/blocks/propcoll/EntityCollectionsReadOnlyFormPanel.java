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
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.progmodel.facets.object.validate.ValidateObjectFacet;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.collection.CollectionPanel;
import org.apache.isis.viewer.wicket.ui.panels.FormAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.EvenOrOddCssClassAppenderFactory;

/**
 * {@link PanelAbstract Panel} representing the properties of an entity, as per
 * the provided {@link EntityModel}.
 */
public class EntityCollectionsReadOnlyFormPanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_PROPERTIES_AND_OR_COLLECTIONS = "entityPropertiesAndOrCollections";

    private PropCollForm form;

    public EntityCollectionsReadOnlyFormPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);
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
            form = new PropCollForm(ID_ENTITY_PROPERTIES_AND_OR_COLLECTIONS, model, this);
            addOrReplace(form);
        } else {
            permanentlyHide(ID_ENTITY_PROPERTIES_AND_OR_COLLECTIONS);
        }
    }

    static class PropCollForm extends FormAbstract<ObjectAdapter> {

        private static final long serialVersionUID = 1L;

        private static final String ID_PROPERTIES_AND_OR_COLLECTIONS = "propertiesAndOrCollections";
        private static final String ID_PROPERTY_OR_COLLECTION = "propertyOrCollection";

        private final Component owningPanel;

        public PropCollForm(final String id, final EntityModel entityModel, final Component owningPanel) {
            super(id, entityModel);
            this.owningPanel = owningPanel; // for repainting

            buildGui();
        }

        private void buildGui() {
            addPropertiesAndOrCollections();

            addValidator();
        }

        private void addPropertiesAndOrCollections() {
            final EntityModel entityModel = (EntityModel) getModel();
            final ObjectAdapter adapter = entityModel.getObject();
            final ObjectSpecification noSpec = adapter.getSpecification();

            final List<ObjectAssociation> associations = visibleAssociations(adapter, noSpec);

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
			@SuppressWarnings("unused")
			Component component;
			final OneToOneAssociation otoa = (OneToOneAssociation) association;
			final PropertyMemento pm = new PropertyMemento(otoa);

			final ScalarModel scalarModel = entityModel.getPropertyModel(pm);
			component = getComponentFactoryRegistry().addOrReplaceComponent(container, ID_PROPERTY_OR_COLLECTION, ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);
		}

		private void addCollectionToForm(final EntityModel entityModel,
				final ObjectAssociation association,
				final WebMarkupContainer container) {
			@SuppressWarnings("unused")
			Component component;
			final OneToManyAssociation otma = (OneToManyAssociation) association;

			final EntityCollectionModel entityCollectionModel = EntityCollectionModel.createParented(entityModel, otma);
			final CollectionPanel collectionPanel = new CollectionPanel(ID_PROPERTY_OR_COLLECTION, entityCollectionModel);
			container.addOrReplace(collectionPanel);

			component = getComponentFactoryRegistry().addOrReplaceComponent(container, ID_PROPERTY_OR_COLLECTION, ComponentType.COLLECTION_NAME_AND_CONTENTS, entityCollectionModel);
		}

        private List<ObjectAssociation> visibleAssociations(final ObjectAdapter adapter, final ObjectSpecification noSpec) {
            return noSpec.getAssociations(visibleAssociationFilter(adapter));
        }

        @SuppressWarnings("unchecked")
		private Filter<ObjectAssociation> visibleAssociationFilter(final ObjectAdapter adapter) {
            return Filters.and(ObjectAssociationFilters.COLLECTIONS, ObjectAssociationFilters.dynamicallyVisible(getAuthenticationSession(), adapter, Where.PARENTED_TABLES));
        }


        private void requestRepaintPanel(final AjaxRequestTarget target) {
            if (target != null) {
                //target.addComponent(owningPanel);
                target.add(owningPanel);
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
                    final ObjectAdapter adapter = entityModel.getObject();
                    final ValidateObjectFacet facet = adapter.getSpecification().getFacet(ValidateObjectFacet.class);
                    if (facet == null) {
                        return;
                    }
                    final String invalidReasonIfAny = facet.invalidReason(adapter);
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
    }
}
