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
package org.apache.isis.viewer.wicket.ui.components.entity.assocgroup;

import java.util.List;
import java.util.Map;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract2;
import org.apache.isis.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;

/**
 * This isn't actually used, was originally written for mixin/view models, but we now simply use BS3Grid.
 * Still, this is a reasonable fallback... main difference is that it doesn't recognise fieldsets.
 *
 * Might end up deleting it...
 */
public class AssociationGroup extends PanelAbstract<EntityModel> implements HasDynamicallyVisibleContent {

    private static final String ID_ASSOCIATION_GROUP = "associationGroup";

    private static final String ID_ASSOCIATIONS = "associations";
    private static final String ID_ASSOCIATION = "association";

    private final Grid grid;

    private final List<ScalarPanelAbstract2> childScalarPanelAbstract2s;
    private final List<Component> childComponents;

    public AssociationGroup(final String id, final EntityModel model, final Grid grid) {
        super(id, model);
        this.grid = grid;

        // the UI is only ever built once.
        childComponents = buildGui();
        childScalarPanelAbstract2s = FluentIterable.from(childComponents).filter(ScalarPanelAbstract2.class).toList();
    }

    public EntityModel getModel() {
        return (EntityModel) getDefaultModel();
    }


    private List<Component> buildGui() {

        final List<Component> childComponents = Lists.newArrayList();

        setOutputMarkupPlaceholderTag(true);
        setOutputMarkupId(true);

        final WebMarkupContainer div = new WebMarkupContainer(ID_ASSOCIATION_GROUP);

        final List<ObjectAssociation> associations = getObjectAssociations();

        final RepeatingView propertyRv = new RepeatingView(ID_ASSOCIATIONS);
        div.addOrReplace(propertyRv);

        final EntityModel entityModel = getModel();

        final GridFacet gridFacet = entityModel.getTypeOfSpecification().getFacet(GridFacet.class);
        final Grid grid = gridFacet.getGrid(entityModel.getObject());
        final Map<String, CollectionLayoutData> collectionLayoutDataById = grid.getAllCollectionsById();

        for (final ObjectAssociation association : associations) {

            final WebMarkupContainer associationRvContainer = new WebMarkupContainer(propertyRv.newChildId());
            propertyRv.addOrReplace(associationRvContainer);

            if(association.isOneToOneAssociation()) {
                final OneToOneAssociation otoa = (OneToOneAssociation) association;

                final PropertyMemento pm = new PropertyMemento(otoa, entityModel.getIsisSessionFactory());

                final ScalarModel scalarModel =
                        entityModel.getPropertyModel(pm, EntityModel.Mode.VIEW, EntityModel.RenderingHint.REGULAR);

                final Component component = getComponentFactoryRegistry()
                        .addOrReplaceComponent(associationRvContainer, ID_ASSOCIATION, ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);

                childComponents.add(component);
            } else {
                final String associationId = association.getId();

                final CollectionLayoutData collectionLayoutData = collectionLayoutDataById.get(associationId);

                // successively trample all over; not a problem
                entityModel.setCollectionLayoutData(collectionLayoutData);

                final Component component = getComponentFactoryRegistry()
                        .addOrReplaceComponent(associationRvContainer, ID_ASSOCIATION,
                                ComponentType.ENTITY_COLLECTION, entityModel);

                childComponents.add(component);
            }
        }


        // either add the built content, or hide entire
        if(associations.isEmpty()) {
            Components.permanentlyHide(this, div.getId());
        } else {
            this.addOrReplace(div);
        }

        return childComponents;
    }

    private List<ObjectAssociation> getObjectAssociations() {
        return this.getModel().getObject().getSpecification().getAssociations(Contributed.INCLUDED);
    }

    @Override
    public void onConfigure() {
        for (final ScalarPanelAbstract2 childComponent : childScalarPanelAbstract2s) {
            childComponent.configure();
        }
        super.onConfigure();
    }

    @Override
    public boolean isVisible() {

        // HACK: there are some components that are not ScalarPanelAbstract2's, eg the pdfjsviewer.
        // In this case, don't ever hide.

        // TODO: should remove this hack.  We need some sort of SPI for ScalarPanelAbstract2's and any other component,
        // (eg PdfJsViewer) that can implement.  It's "probably" just a matter of having PdfJsViewer do its work in the
        // correct Wicket callback (probably onConfigure).
        if(childComponents.size() > childScalarPanelAbstract2s.size()) {
            return true;
        }
        // HACK:END

        for (final ScalarPanelAbstract2 childComponent : childScalarPanelAbstract2s) {
            if(childComponent.isVisibilityAllowed()) {
                return true;
            }
        }
        return false;
    }

}
