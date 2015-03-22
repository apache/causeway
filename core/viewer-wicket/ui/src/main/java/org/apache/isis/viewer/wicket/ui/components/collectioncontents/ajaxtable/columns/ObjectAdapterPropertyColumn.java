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

package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel.RenderingHint;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanel;

/**
 * A {@link ColumnAbstract column} within a
 * {@link CollectionContentsAsAjaxTablePanel} representing a single property of the
 * provided {@link ObjectAdapter}.
 * 
 * <p>
 * Looks up the {@link ComponentFactory} to render the property from the
 * {@link ComponentFactoryRegistry}.
 */
public final class ObjectAdapterPropertyColumn extends ColumnAbstract<ObjectAdapter> {

    private static final long serialVersionUID = 1L;

    private final String propertyExpression;
    private final boolean escaped;

    public ObjectAdapterPropertyColumn(
            final IModel<String> columnNameModel,
            final String sortProperty,
            final String propertyName,
            final boolean escaped) {
        super(columnNameModel, sortProperty);
        this.propertyExpression = propertyName;
        this.escaped = escaped;
    }

    public Component getHeader(final String componentId)
    {
        final Label label = new Label(componentId, getDisplayModel());
        label.setEscapeModelStrings(escaped);
        return label;
    }


    @Override
    public void populateItem(final Item<ICellPopulator<ObjectAdapter>> cellItem, final String componentId, final IModel<ObjectAdapter> rowModel) {
        final Component component = createComponent(componentId, rowModel);
        cellItem.add(component);
    }

    private Component createComponent(final String id, final IModel<ObjectAdapter> rowModel) {

        final ObjectAdapter adapter = rowModel.getObject();
        final EntityModel model = new EntityModel(adapter);
        final OneToOneAssociation property = (OneToOneAssociation) adapter.getSpecification().getAssociation(propertyExpression);
        final PropertyMemento pm = new PropertyMemento(property);
        final ScalarModel scalarModel = model.getPropertyModel(pm);

        scalarModel.setRenderingHint(RenderingHint.PROPERTY_COLUMN);
        scalarModel.toViewMode();

        final ComponentFactory componentFactory = findComponentFactory(ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);
        final Component component = componentFactory.createComponent(id, scalarModel);
        
        return component;
    }

}