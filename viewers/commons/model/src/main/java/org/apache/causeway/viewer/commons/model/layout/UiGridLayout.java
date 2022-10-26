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
package org.apache.causeway.viewer.commons.model.layout;

import java.util.Optional;
import java.util.Set;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.bootstrap.BSClearFix;
import org.apache.causeway.applib.layout.grid.bootstrap.BSCol;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSRow;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTab;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTabGroup;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.UiModel;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "bind")
public class UiGridLayout implements UiModel {

    @RequiredArgsConstructor
    public static abstract class Visitor<C, T> {
        private final C rootContainer;
        protected abstract C newActionPanel(C container);
        protected abstract C newRow(C container, BSRow rowData);
        protected abstract C newCol(C container, BSCol colData);
        protected abstract T newTabGroup(C container, BSTabGroup tabGroupData);
        protected abstract C newTab(T tabGroup, BSTab tabData);
        protected abstract C newFieldSet(C container, FieldSet fieldSetData);
        protected abstract void onObjectTitle(C container, DomainObjectLayoutData domainObjectData);
        protected abstract void onClearfix(C container, BSClearFix clearFixData);
        protected abstract void onAction(C container, ActionLayoutData actionData);
        protected abstract void onProperty(C container, PropertyLayoutData propertyData);
        protected abstract void onCollection(C container, CollectionLayoutData collectionData);

    }

    @NonNull private final ManagedObject managedObject;
    private _Lazy<Optional<BSGrid>> gridData = _Lazy.threadSafe(this::initGridData);

    public <C, T> void visit(final Visitor<C, T> visitor) {

        // recursively visit the grid
        gridData.get()
        .ifPresent(bsGrid->{
            for(val bsRow: bsGrid.getRows()) {
                visitRow(bsRow, visitor.rootContainer, visitor);
            }
        });

    }

    private Optional<BSGrid> initGridData() {
        return Facets.bootstrapGrid(managedObject.getSpecification(), managedObject)
        .map(this::attachAssociatedActions);
    }

    //TODO[refactor] this should not be necessary here, the GridFacet should already have done that for us
    private BSGrid attachAssociatedActions(final BSGrid bSGrid) {

        val primedActions = bSGrid.getAllActionsById();
        final Set<String> actionIdsAlreadyAdded = _Sets.newHashSet(primedActions.keySet());

        managedObject.getSpecification().streamProperties(MixedIn.INCLUDED)
        .forEach(property->{
            Optional.ofNullable(
                    bSGrid.getAllPropertiesById().get(property.getId()))
            .ifPresent(pl->{

                ObjectAction.Util.findForAssociation(managedObject, property)
                .map(action->action.getId())
                .filter(id->!actionIdsAlreadyAdded.contains(id))
                .peek(actionIdsAlreadyAdded::add)
                .map(ActionLayoutData::new)
                .forEach(pl.getActions()::add);

            });


        });
        return bSGrid;
    }

    private <C, T> void visitRow(final BSRow bsRow, final C container, final Visitor<C, T> visitor) {

        val uiRow = visitor.newRow(container, bsRow);

        for(val bsRowContent: bsRow.getCols()) {
            if(bsRowContent instanceof BSCol) {

                visitCol((BSCol) bsRowContent, uiRow, visitor);

            } else if (bsRowContent instanceof BSClearFix) {
                visitor.onClearfix(uiRow, (BSClearFix) bsRowContent);
            } else {
                throw new IllegalStateException("Unrecognized implementation of BSRowContent");
            }
        }
    }

    private <C, T> void visitCol(final BSCol bSCol, final C container, final Visitor<C, T> visitor) {
        val uiCol = visitor.newCol(container, bSCol);

        val hasDomainObject = bSCol.getDomainObject()!=null;
        val hasActions = _NullSafe.size(bSCol.getActions())>0;
        val hasRows = _NullSafe.size(bSCol.getRows())>0;

        if(hasDomainObject || hasActions) {
            val uiActionPanel = visitor.newActionPanel(uiCol);
            if(hasDomainObject) {
                visitor.onObjectTitle(uiActionPanel, bSCol.getDomainObject());
            }
            if(hasActions) {
                for(val action : bSCol.getActions()) {
                    visitor.onAction(uiActionPanel, action);
                }
            }
        }

        for(val fieldSet : bSCol.getFieldSets()) {
            visitFieldSet(fieldSet, uiCol, visitor);
        }

        for(val tabGroup : bSCol.getTabGroups()) {
            visitTabGroup(tabGroup, uiCol, visitor);
        }

        if(hasRows) {
            for(val bsRow: bSCol.getRows()) {
                visitRow(bsRow, uiCol, visitor);
            }
        }

        for(val collectionData : bSCol.getCollections()) {
            visitor.onCollection(uiCol, collectionData);
        }

    }

    private <C, T> void visitTabGroup(final BSTabGroup BSColTabGroup, final C container, final Visitor<C, T> visitor) {
        val uiTabGroup = visitor.newTabGroup(container, BSColTabGroup);
        for(val bsTab: BSColTabGroup.getTabs()) {
            visitTab(bsTab, uiTabGroup, visitor);
        }
    }

    private <C, T> void visitTab(final BSTab bSTab, final T container, final Visitor<C, T> visitor) {
        val uiTab = visitor.newTab(container, bSTab);
        for(val bsRow: bSTab.getRows()) {
            visitRow(bsRow, uiTab, visitor);
        }
    }

    private <C, T> void visitFieldSet(final FieldSet cptFieldSet, final C container, final Visitor<C, T> visitor) {
        val uiFieldSet = visitor.newFieldSet(container, cptFieldSet);
        for(val propertyData: cptFieldSet.getProperties()) {
            visitor.onProperty(uiFieldSet, propertyData);
        }
    }


}
