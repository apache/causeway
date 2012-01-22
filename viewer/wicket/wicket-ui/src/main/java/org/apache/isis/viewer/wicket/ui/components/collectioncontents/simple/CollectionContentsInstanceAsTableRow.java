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

package org.apache.isis.viewer.wicket.ui.components.collectioncontents.simple;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * Renders a single entity instance within the HTML table.
 * 
 * <p>
 * TODO: it ought to be possible to in-line this into
 * {@link CollectionContentsAsSimpleTable}.
 */
class CollectionContentsInstanceAsTableRow extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    public CollectionContentsInstanceAsTableRow(final String id, final EntityModel model) {
        super(id, model);

        addTableRow(model);
    }

    private void addTableRow(final EntityModel model) {

        final ObjectAdapter adapter = model.getObject();
        final ObjectSpecification typeOfSpec = model.getTypeOfSpecification();
        final List<? extends ObjectAssociation> propertyList = typeOfSpec.getAssociations(ObjectAssociationFilters.PROPERTIES);

        add(new Label("title", adapter.titleString()));

        final RepeatingView propertyValues = new RepeatingView("propertyValue");
        add(propertyValues);

        for (final ObjectAssociation property : propertyList) {
            final ObjectAdapter propertyValueAdapter = property.get(adapter);
            Component component;
            if (propertyValueAdapter == null) {
                component = new Label(property.getId(), "(null)");
            } else {
                if (propertyValueAdapter.getSpecification().getFacet(ValueFacet.class) == null) {
                    // TODO: make more sophisticated, eg with Links if an object
                    component = new Label(property.getId(), propertyValueAdapter.titleString());
                } else {
                    component = new Label(property.getId(), propertyValueAdapter.titleString());
                }
            }
            propertyValues.add(component);
        }
    }

}
