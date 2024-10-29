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
package org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.core.metamodel.commons.ViewOrEditMode;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataRowWkt;

public final class SingularColumn
extends AssociationColumnAbstract {

    private static final long serialVersionUID = 1L;

    public SingularColumn(
            final ObjectSpecification elementType,
            final EntityCollectionModel.Variant collectionVariant,
            final IModel<String> columnNameModel,
            final Optional<String> sortProperty,
            final String propertyId,
            final String parentTypeName,
            final Optional<String> describedAs) {
        super(elementType, collectionVariant, columnNameModel, sortProperty, propertyId, parentTypeName, describedAs);
    }

    @Override
    protected Component createCellComponent(final String componentId, final DataRowWkt dataRowWkt) {
        var dataRow = dataRowWkt.getObject();
        var rowElement = dataRow.getRowElement();
        var rowElementModel = UiObjectWkt.ofAdapter(rowElement);
        var property = rowElement.getSpecification().getPropertyElseFail(memberId);

        var scalarModel = rowElementModel
                .getPropertyModel(
                        property,
                        ViewOrEditMode.VIEWING,
                        collectionVariant.getColumnRenderingHint());

        return findComponentFactory(UiComponentType.SCALAR_NAME_AND_VALUE, scalarModel)
                .createComponent(componentId, scalarModel);
    }

}
