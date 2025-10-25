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
package org.apache.causeway.applib.layout.grid.bootstrap;

import java.util.ArrayList;
import java.util.List;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.ActionLayoutDataOwner;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutDataOwner;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.FieldSetOwner;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.Grid;

public record BSWalker(BSRowOwner root) {

    public void walk(final Grid.Visitor visitor) {
        final BSElement.Visitor bsVisitor = asBsVisitor(visitor);
        if(root instanceof BSGrid bsGrid) {
            bsVisitor.preVisit(bsGrid);
            bsVisitor.visit(bsGrid);
            traverseRows(root, bsVisitor);
            bsVisitor.postVisit(bsGrid);
        } else {
            traverseRows(root, bsVisitor);
        }
    }

    private void traverseRows(final BSRowOwner rowOwner, final BSElement.Visitor bsVisitor) {
        final List<BSRow> rows = rowOwner.getRows();
        for (BSRow bsRow : new ArrayList<>(rows)) {
            bsVisitor.preVisit(bsRow);
            bsVisitor.visit(bsRow);
            traverseCols(bsRow, bsVisitor);
            bsVisitor.postVisit(bsRow);
        }
    }

    private void traverseCols(final BSRow bsRow, final BSElement.Visitor bsVisitor) {
        final List<BSRowContent> cols = bsRow.getRowContents();
        for (BSRowContent rowContent : new ArrayList<>(cols)) {
            if(rowContent instanceof BSCol bsCol) {
                bsVisitor.preVisit(bsCol);
                bsVisitor.visit(bsCol);
                traverseDomainObject(bsCol, bsVisitor);
                traverseTabGroups(bsCol, bsVisitor);
                traverseActions(bsCol, bsVisitor);
                traverseFieldSets(bsCol, bsVisitor);
                traverseCollections(bsCol, bsVisitor);
                traverseRows(bsCol, bsVisitor);
                bsVisitor.postVisit(bsCol);
            } else if (rowContent instanceof BSClearFix bsClearFix) {
                bsVisitor.visit(bsClearFix);
            } else {
                throw new IllegalStateException(
                        "Unrecognized implementation of BSRowContent, " + rowContent);
            }
        }
    }

    private void traverseDomainObject(final BSCol bsCol, final BSElement.Visitor bsVisitor) {
        final DomainObjectLayoutData domainObject = bsCol.getDomainObject();
        if(domainObject == null) return;
        bsVisitor.visit(domainObject);
    }

    private void traverseTabGroups(final BSTabGroupOwner bsTabGroupOwner, final BSElement.Visitor bsVisitor) {
        final List<BSTabGroup> tabGroups = bsTabGroupOwner.getTabGroups();
        for (BSTabGroup bsTabGroup : new ArrayList<>(tabGroups)) {
            bsVisitor.preVisit(bsTabGroup);
            bsVisitor.visit(bsTabGroup);
            traverseTabs(bsTabGroup, bsVisitor);
            bsVisitor.postVisit(bsTabGroup);
        }
    }

    private void traverseTabs(final BSTabOwner bsTabOwner, final BSElement.Visitor bsVisitor) {
        final List<BSTab> tabs = bsTabOwner.getTabs();
        for (BSTab tab : new ArrayList<>(tabs)) {
            bsVisitor.preVisit(tab);
            bsVisitor.visit(tab);
            traverseRows(tab, bsVisitor);
            bsVisitor.postVisit(tab);
        }
    }

    private void traverseActions(final ActionLayoutDataOwner actionLayoutDataOwner, final BSElement.Visitor bsVisitor) {
        final List<ActionLayoutData> actionLayoutDatas = actionLayoutDataOwner.getActions();
        if(actionLayoutDatas == null) return;

        for (final ActionLayoutData actionLayoutData : new ArrayList<>(actionLayoutDatas)) {
            bsVisitor.visit(actionLayoutData);
        }
    }

    private void traverseFieldSets(final FieldSetOwner fieldSetOwner, final BSElement.Visitor bsVisitor) {
        final List<FieldSet> fieldSets = fieldSetOwner.getFieldSets();
        for (FieldSet fieldSet : new ArrayList<>(fieldSets)) {
            bsVisitor.visit(fieldSet);
            traverseActions(fieldSet, bsVisitor);
            final List<PropertyLayoutData> properties = fieldSet.getProperties();
            for (final PropertyLayoutData property : new ArrayList<>(properties)) {
                bsVisitor.visit(property);
                traverseActions(property, bsVisitor);
            }
        }
    }

    private void traverseCollections(
            final CollectionLayoutDataOwner owner, final BSElement.Visitor bsVisitor) {
        final List<CollectionLayoutData> collections = owner.getCollections();
        for (CollectionLayoutData collection : new ArrayList<>(collections)) {
            bsVisitor.visit(collection);
            traverseActions(collection, bsVisitor);
        }
    }

    private BSElement.Visitor asBsVisitor(final Grid.Visitor visitor) {
        return visitor instanceof BSElement.Visitor bsGridVisistor
            ? bsGridVisistor
            : new BSElement.Visitor() {
                @Override public void visit(final DomainObjectLayoutData domainObjectLayoutData) {
                    visitor.visit(domainObjectLayoutData);
                }
                @Override public void visit(final ActionLayoutData actionLayoutData) {
                    visitor.visit(actionLayoutData);
                }
                @Override public void visit(final PropertyLayoutData propertyLayoutData) {
                    visitor.visit(propertyLayoutData);
                }
                @Override public void visit(final CollectionLayoutData collectionLayoutData) {
                    visitor.visit(collectionLayoutData);
                }
                @Override public void visit(final FieldSet fieldSet) {
                    visitor.visit(fieldSet);
                }
            };
    }

}
