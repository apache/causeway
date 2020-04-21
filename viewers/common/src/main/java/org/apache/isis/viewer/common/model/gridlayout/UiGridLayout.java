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
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "bind")
public class UiGridLayout {

    @RequiredArgsConstructor
    public static abstract class Visitor<T> {
        private final T rootContainer;
        protected abstract T newActionPanel(T container);
        protected abstract T newRow(T container, BS3Row rowData);
        protected abstract T newCol(T container, BS3Col colData);
        protected abstract T newTabGroup(T container, BS3TabGroup tabGroupData);
        protected abstract T newTab(T container, BS3Tab tabData);
        protected abstract T newFieldSet(T container, FieldSet fieldSetData);
        protected abstract void onObjectTitle(T container, DomainObjectLayoutData domainObjectData);
        protected abstract void onClearfix(T container, BS3ClearFix clearFixData);
        protected abstract void onAction(T container, ActionLayoutData actionData);
        protected abstract void onProperty(T container, PropertyLayoutData propertyData);
        protected abstract void onCollection(T container, CollectionLayoutData collectionData);
        
    }
    
    @NonNull private final ManagedObject managedObject;
    private Optional<BS3Grid> gridData;
    
    public <T> void visit(Visitor<T> visitor) {
        
        if(gridData==null) {
            gridData = Optional.ofNullable(
                    managedObject.getSpecification().getFacet(GridFacet.class))
            .map(gridFacet->gridFacet.getGrid(managedObject))
            .filter(grid->grid instanceof BS3Grid)
            .map(BS3Grid.class::cast);
        }
        
        // recursively visit the grid
        gridData
        .ifPresent(bs3Grid->{
            for(val bs3Row: bs3Grid.getRows()) {
                visitRow(bs3Row, visitor.rootContainer, visitor);         
            }
        });
        
    }

    private <T> void visitRow(BS3Row bs3Row, T container, Visitor<T> visitor) {
        
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
    
    private <T> void visitCol(BS3Col bS3Col, T container, Visitor<T> visitor) {
        val uiCol = visitor.newCol(container, bS3Col);
        
        val hasDomainObject = bS3Col.getDomainObject()!=null; 
        val hasActions = bS3Col.getActions().size()>0;
        
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
        
        for(val collectionData : bS3Col.getCollections()) {
            visitor.onCollection(uiCol, collectionData);    
        }
        
        for(val fieldSet : bS3Col.getFieldSets()) {
            visitFieldSet(fieldSet, uiCol, visitor);
        }
        
        for(val tabGroup : bS3Col.getTabGroups()) {
            visitTabGroup(tabGroup, uiCol, visitor);
        }
        
        // columns having rows seems not permitted by XML schema
        for(val bs3Row: bS3Col.getRows()) { 
            throw _Exceptions.unsupportedOperation();
            //visitRow(bs3Row, uiCol, visitor);         
        }
        
    }
    
    private <T> void visitTabGroup(BS3TabGroup bS3ColTabGroup, T container, Visitor<T> visitor) {
        val uiTabGroup = visitor.newTabGroup(container, bS3ColTabGroup);
        for(val bs3Tab: bS3ColTabGroup.getTabs()) { 
            visitTab(bs3Tab, uiTabGroup, visitor);
        }
    }
    
    private <T> void visitTab(BS3Tab bS3Tab, T container, Visitor<T> visitor) {
        val uiTab = visitor.newTab(container, bS3Tab);
        for(val bs3Row: bS3Tab.getRows()) { 
            visitRow(bs3Row, uiTab, visitor);         
        }
    }
    
    private <T> void visitFieldSet(FieldSet cptFieldSet, T container, Visitor<T> visitor) {
        val uiFieldSet = visitor.newFieldSet(container, cptFieldSet);
        for(val propertyData: cptFieldSet.getProperties()) { 
            visitor.onProperty(uiFieldSet, propertyData);         
        }
    }
    

}
