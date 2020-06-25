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
package org.apache.isis.viewer.common.model.gridlayout;

import java.util.Optional;

import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3ClearFix;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Col;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Row;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Tab;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3TabGroup;
import org.apache.isis.core.commons.internal.base._Lazy;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "bind")
public class UiGridLayout {

    @RequiredArgsConstructor
    public static abstract class Visitor<C, T> {
        private final C rootContainer;
        protected abstract C newActionPanel(C container);
        protected abstract C newRow(C container, BS3Row rowData);
        protected abstract C newCol(C container, BS3Col colData);
        protected abstract T newTabGroup(C container, BS3TabGroup tabGroupData);
        protected abstract C newTab(T tabGroup, BS3Tab tabData);
        protected abstract C newFieldSet(C container, FieldSet fieldSetData);
        protected abstract void onObjectTitle(C container, DomainObjectLayoutData domainObjectData);
        protected abstract void onClearfix(C container, BS3ClearFix clearFixData);
        protected abstract void onAction(C container, ActionLayoutData actionData);
        protected abstract void onProperty(C container, PropertyLayoutData propertyData);
        protected abstract void onCollection(C container, CollectionLayoutData collectionData);
        
    }
    
    @NonNull private final ManagedObject managedObject;
    private _Lazy<Optional<BS3Grid>> gridData = _Lazy.threadSafe(this::initGridData);
    
    public <C, T> void visit(Visitor<C, T> visitor) {
        
        // recursively visit the grid
        gridData.get()
        .ifPresent(bs3Grid->{
            for(val bs3Row: bs3Grid.getRows()) {
                visitRow(bs3Row, visitor.rootContainer, visitor);         
            }
        });
        
    }

    private Optional<BS3Grid> initGridData() {
        return Optional.ofNullable(
                managedObject.getSpecification().getFacet(GridFacet.class))
        .map(gridFacet->gridFacet.getGrid(managedObject))
        .filter(grid->grid instanceof BS3Grid)
        .map(BS3Grid.class::cast);
    }

    private <C, T> void visitRow(BS3Row bs3Row, C container, Visitor<C, T> visitor) {
        
        val uiRow = visitor.newRow(container, bs3Row);
        
        for(val bs3RowContent: bs3Row.getCols()) {
            if(bs3RowContent instanceof BS3Col) {
                
                visitCol((BS3Col) bs3RowContent, uiRow, visitor);
                
            } else if (bs3RowContent instanceof BS3ClearFix) {
                visitor.onClearfix(uiRow, (BS3ClearFix) bs3RowContent);
            } else {
                throw new IllegalStateException("Unrecognized implementation of BS3RowContent");
            }
        }
    }
    
    private <C, T> void visitCol(BS3Col bS3Col, C container, Visitor<C, T> visitor) {
        val uiCol = visitor.newCol(container, bS3Col);
        
        val hasDomainObject = bS3Col.getDomainObject()!=null; 
        val hasActions = _NullSafe.size(bS3Col.getActions())>0;
        val hasRows = _NullSafe.size(bS3Col.getRows())>0;
        
        if(hasDomainObject || hasActions) {
            val uiActionPanel = visitor.newActionPanel(uiCol);
            if(hasDomainObject) {
                visitor.onObjectTitle(uiActionPanel, bS3Col.getDomainObject());    
            }
            if(hasActions) {
                for(val action : bS3Col.getActions()) {
                    visitor.onAction(uiActionPanel, action);
                }    
            }
        }
        
        for(val fieldSet : bS3Col.getFieldSets()) {
            visitFieldSet(fieldSet, uiCol, visitor);
        }
        
        for(val tabGroup : bS3Col.getTabGroups()) {
            visitTabGroup(tabGroup, uiCol, visitor);
        }
        
        if(hasRows) {
            for(val bs3Row: bS3Col.getRows()) { 
                visitRow(bs3Row, uiCol, visitor);         
            }    
        }
        
        for(val collectionData : bS3Col.getCollections()) {
            visitor.onCollection(uiCol, collectionData);    
        }
        
    }
    
    private <C, T> void visitTabGroup(BS3TabGroup bS3ColTabGroup, C container, Visitor<C, T> visitor) {
        val uiTabGroup = visitor.newTabGroup(container, bS3ColTabGroup);
        for(val bs3Tab: bS3ColTabGroup.getTabs()) { 
            visitTab(bs3Tab, uiTabGroup, visitor);
        }
    }
    
    private <C, T> void visitTab(BS3Tab bS3Tab, T container, Visitor<C, T> visitor) {
        val uiTab = visitor.newTab(container, bS3Tab);
        for(val bs3Row: bS3Tab.getRows()) { 
            visitRow(bs3Row, uiTab, visitor);         
        }
    }
    
    private <C, T> void visitFieldSet(FieldSet cptFieldSet, C container, Visitor<C, T> visitor) {
        val uiFieldSet = visitor.newFieldSet(container, cptFieldSet);
        for(val propertyData: cptFieldSet.getProperties()) { 
            visitor.onProperty(uiFieldSet, propertyData);         
        }
    }
    

}
