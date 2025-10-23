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
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.UiModel;

import lombok.RequiredArgsConstructor;

public record UiGridLayout(
    BSGrid bsGrid
    ) implements UiModel {

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

    public static Optional<UiGridLayout> createGrid(ManagedObject mo) {
        return Facets.bootstrapGrid(mo.objSpec(), mo)
            .map(UiGridLayout::new);
    }

    /**
     * recursively visits the grid
     */
    public <C, T> void visit(final Visitor<C, T> visitor) {
        for(var bsRow: bsGrid.getRows()) {
            visitRow(bsRow, visitor.rootContainer, visitor);
        }
    }

    // -- HELPER

    private <C, T> void visitRow(final BSRow bsRow, final C container, final Visitor<C, T> visitor) {

        var uiRow = visitor.newRow(container, bsRow);

        for(var bsRowContent: bsRow.getCols()) {
            if(bsRowContent instanceof BSCol) {
                visitCol((BSCol) bsRowContent, uiRow, visitor);
            } else if (bsRowContent instanceof BSClearFix) {
                visitor.onClearfix(uiRow, (BSClearFix) bsRowContent);
            } else {
                throw new IllegalStateException("Unrecognized implementation of BSRowContent");
            }
        }
    }

    private <C, T> void visitCol(final BSCol bsCol, final C container, final Visitor<C, T> visitor) {

        if(bsCol.getSpan() == 0) return; // skip

        var uiCol = visitor.newCol(container, bsCol);

        var hasDomainObject = bsCol.getDomainObject()!=null;
        var hasActions = _NullSafe.size(bsCol.getActions())>0;
        var hasRows = _NullSafe.size(bsCol.getRows())>0;

        if(hasDomainObject || hasActions) {
            var uiActionPanel = visitor.newActionPanel(uiCol);
            if(hasDomainObject) {
                visitor.onObjectTitle(uiActionPanel, bsCol.getDomainObject());
            }
            if(hasActions) {
                for(var action : bsCol.getActions()) {
                    visitor.onAction(uiActionPanel, action);
                }
            }
        }

        for(var fieldSet : bsCol.getFieldSets()) {
            if(_NullSafe.isEmpty(fieldSet.getProperties())) continue; // skip empty fieldsets
            visitFieldSet(fieldSet, uiCol, visitor);
        }

        for(var tabGroup : bsCol.getTabGroups()) {
            visitTabGroup(tabGroup, uiCol, visitor);
        }

        if(hasRows) {
            for(var bsRow: bsCol.getRows()) {
                visitRow(bsRow, uiCol, visitor);
            }
        }

        for(var collectionData : bsCol.getCollections()) {
            visitor.onCollection(uiCol, collectionData);
        }

    }

    private <C, T> void visitTabGroup(final BSTabGroup bsTabGroup, final C container, final Visitor<C, T> visitor) {
        var uiTabGroup = visitor.newTabGroup(container, bsTabGroup);
        for(var bsTab: bsTabGroup.getTabs()) {
            visitTab(bsTab, uiTabGroup, visitor);
        }
    }

    private <C, T> void visitTab(final BSTab bsTab, final T container, final Visitor<C, T> visitor) {
        var uiTab = visitor.newTab(container, bsTab);
        for(var bsRow: bsTab.getRows()) {
            visitRow(bsRow, uiTab, visitor);
        }
    }

    private <C, T> void visitFieldSet(final FieldSet cptFieldSet, final C container, final Visitor<C, T> visitor) {
        var uiFieldSet = visitor.newFieldSet(container, cptFieldSet);
        for(var propertyData: cptFieldSet.getProperties()) {
            visitor.onProperty(uiFieldSet, propertyData);
        }
    }

}
