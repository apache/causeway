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

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.ActionModel.ColumnActionModifier;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataRowWkt;
import org.apache.causeway.viewer.wicket.ui.components.actionlinks.entityactions.ActionLinksPanel;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.NonNull;

public final class ActionColumn
extends GenericColumnAbstract {

    private static final long serialVersionUID = 1L;

    public static Optional<ActionColumn> create(
            @NonNull final Identifier featureId,
            @NonNull final ObjectSpecification elementType) {
        var actions = elementType.streamActionsForColumnRendering(featureId)
                .collect(Can.toCan());
        if(actions.isEmpty()) return Optional.empty();

        var wktConfig = elementType.getMetaModelContext().getConfiguration().getViewer().getWicket();
        return wktConfig.isActionColumnEnabled()
                ? Optional.of(new ActionColumn(elementType, actions))
                : Optional.empty();
    }

    private final Can<String> actionIds;
    private transient Can<ObjectAction> actions;

    private ActionColumn(
            final ObjectSpecification elementType,
            final Can<ObjectAction> actionsForColumnRendering) {
        super(elementType, "Actions");
        this.actions = actionsForColumnRendering;
        this.actionIds = actions.map(ObjectAction::getId);
    }

    @Override
    protected Component createCellComponent(
            final String componentId, final DataRowWkt dataRowWkt) {
        var dataRow = dataRowWkt.getObject();
        var rowElement = dataRow.getRowElement();

        var entityModel = UiObjectWkt.ofAdapter(rowElement);
        var elementType = elementType();

        var actionModels = actions().stream()
            .map(act->ActionModel.forEntityFromActionColumn(act, entityModel,
                    determineColumnActionModifier(act, elementType)))
            .collect(Can.toCan());

        return ActionLinksPanel.actionLinks(componentId, actionModels, ActionLinksPanel.Style.DROPDOWN)
                .map(Component.class::cast)
                .orElseGet(()->Wkt.label(componentId, ""));
    }

    // -- HELPER

    private ColumnActionModifier determineColumnActionModifier(
            final ObjectAction action,
            final ObjectSpecification collectionElementType) {
        return action.getElementType().isVoid()
                || action.getElementType().isOfType(collectionElementType)
                ? ColumnActionModifier.FORCE_STAY_ON_PAGE
                : ColumnActionModifier.FORCE_NEW_BROWSER_WINDOW;
    }

    private Can<ObjectAction> actions() {
        synchronized(this) {
            if(actions==null) {
                var elementType = elementType();
                this.actions = actionIds.map(elementType::getActionElseFail);
            }
        }
        return actions;
    }

}
