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
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.tabular.interactive.DataRow;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;

import lombok.val;

public final class SingularColumn
extends AssociationColumnAbstract {

    private static final long serialVersionUID = 1L;

    public SingularColumn(
            final MetaModelContext commonContext,
            final EntityCollectionModel.Variant collectionVariant,
            final IModel<String> columnNameModel,
            final String sortProperty,
            final String propertyId,
            final String parentTypeName,
            final Optional<String> describedAs) {
        super(commonContext, collectionVariant, columnNameModel, sortProperty, propertyId, parentTypeName, describedAs);
    }

    @Override
    protected Component createCellComponent(
            final String componentId, final DataRow dataRow, final IModel<Boolean> dataRowToggle) {
        val rowElement = dataRow.getRowElement();
        val rowElementModel = UiObjectWkt.ofAdapter(super.getMetaModelContext(), rowElement);
        val property = rowElement.getSpecification().getPropertyElseFail(memberId);

        val scalarModel = rowElementModel
                .getPropertyModel(
                        property,
                        ViewOrEditMode.VIEWING,
                        collectionVariant.getColumnRenderingHint());

        return findComponentFactory(UiComponentType.SCALAR_NAME_AND_VALUE, scalarModel)
                .createComponent(componentId, scalarModel);
    }

}
