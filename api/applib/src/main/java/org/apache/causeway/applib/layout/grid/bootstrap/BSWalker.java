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
import org.apache.causeway.applib.layout.grid.bootstrap.BSElement.BSElementVisitor;

public record BSWalker(BSRowOwner root) {

    public void walk(final BSElementVisitor visitor) {
        if(root instanceof BSGrid bsGrid) {
            visitor.preVisit(bsGrid);
            visitor.visit(bsGrid);
            traverseRows(root, visitor);
            visitor.postVisit(bsGrid);
        } else {
            traverseRows(root, visitor);
        }
    }

    private void traverseRows(final BSRowOwner rowOwner, final BSElementVisitor visitor) {
        final List<BSRow> rows = rowOwner.getRows();
        for (BSRow bsRow : new ArrayList<>(rows)) {
            visitor.preVisit(bsRow);
            visitor.visit(bsRow);
            traverseCols(bsRow, visitor);
            visitor.postVisit(bsRow);
        }
    }

    private void traverseCols(final BSRow bsRow, final BSElementVisitor visitor) {
        final List<BSRowContent> cols = bsRow.getRowContents();
        for (BSRowContent rowContent : new ArrayList<>(cols)) {
            if(rowContent instanceof BSCol bsCol) {
                visitor.preVisit(bsCol);
                visitor.visit(bsCol);
                traverseDomainObject(bsCol, visitor);
                traverseTabGroups(bsCol, visitor);
                traverseActions(bsCol, visitor);
                traverseFieldSets(bsCol, visitor);
                traverseCollections(bsCol, visitor);
                traverseRows(bsCol, visitor);
                visitor.postVisit(bsCol);
            } else if (rowContent instanceof BSClearFix bsClearFix) {
                visitor.visit(bsClearFix);
            } else {
                throw new IllegalStateException(
                        "Unrecognized implementation of BSRowContent, " + rowContent);
            }
        }
    }

    private void traverseDomainObject(final BSCol bsCol, final BSElementVisitor visitor) {
        final DomainObjectLayoutData domainObject = bsCol.getDomainObject();
        if(domainObject == null) return;
        visitor.visit(domainObject);
    }

    private void traverseTabGroups(final BSTabGroupOwner bsTabGroupOwner, final BSElementVisitor visitor) {
        final List<BSTabGroup> tabGroups = bsTabGroupOwner.getTabGroups();
        for (BSTabGroup bsTabGroup : new ArrayList<>(tabGroups)) {
            visitor.preVisit(bsTabGroup);
            visitor.visit(bsTabGroup);
            traverseTabs(bsTabGroup, visitor);
            visitor.postVisit(bsTabGroup);
        }
    }

    private void traverseTabs(final BSTabOwner bsTabOwner, final BSElementVisitor visitor) {
        final List<BSTab> tabs = bsTabOwner.getTabs();
        for (BSTab tab : new ArrayList<>(tabs)) {
            visitor.preVisit(tab);
            visitor.visit(tab);
            traverseRows(tab, visitor);
            visitor.postVisit(tab);
        }
    }

    private void traverseActions(final ActionLayoutDataOwner actionLayoutDataOwner, final BSElementVisitor visitor) {
        final List<ActionLayoutData> actionLayoutDatas = actionLayoutDataOwner.getActions();
        if(actionLayoutDatas == null) return;

        for (final ActionLayoutData actionLayoutData : new ArrayList<>(actionLayoutDatas)) {
            visitor.visit(actionLayoutData);
        }
    }

    private void traverseFieldSets(final FieldSetOwner fieldSetOwner, final BSElementVisitor visitor) {
        final List<FieldSet> fieldSets = fieldSetOwner.getFieldSets();
        for (FieldSet fieldSet : new ArrayList<>(fieldSets)) {
            visitor.visit(fieldSet);
            traverseActions(fieldSet, visitor);
            final List<PropertyLayoutData> properties = fieldSet.getProperties();
            for (final PropertyLayoutData property : new ArrayList<>(properties)) {
                visitor.visit(property);
                traverseActions(property, visitor);
            }
        }
    }

    private void traverseCollections(
            final CollectionLayoutDataOwner owner, final BSElementVisitor visitor) {
        final List<CollectionLayoutData> collections = owner.getCollections();
        for (CollectionLayoutData collection : new ArrayList<>(collections)) {
            visitor.visit(collection);
            traverseActions(collection, visitor);
        }
    }

}
