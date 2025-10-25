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

import java.util.List;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.ActionLayoutDataOwner;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutDataOwner;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.FieldSetOwner;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;

public record BSElementOwnerResolvingWalker(BSRowOwner root) {

    public void walk() {
        traverseRows(root);
    }

    private void traverseRows(final BSRowOwner rowOwner) {
        for (BSRow bsRow : rowOwner.getRows()) {
            traverseCols(bsRow);
        }
    }

    private void traverseCols(final BSRow bsRow) {
        for (BSRowContent rowContent : bsRow.getRowContents()) {
            rowContent.setOwner(bsRow);
            if(rowContent instanceof BSCol bsCol) {
                traverseDomainObject(bsCol);
                traverseTabGroups(bsCol);
                traverseActions(bsCol);
                traverseFieldSets(bsCol);
                traverseCollections(bsCol);
                traverseRows(bsCol);
            }
        }
    }

    private void traverseDomainObject(final BSCol bsCol) {
        var domainObject = bsCol.getDomainObject();
        if(domainObject == null) return;
        domainObject.setOwner(bsCol);
    }

    private void traverseTabGroups(final BSTabGroupOwner bsTabGroupOwner) {
        for (BSTabGroup bsTabGroup : bsTabGroupOwner.getTabGroups()) {
            bsTabGroup.setOwner(bsTabGroupOwner);
            traverseTabs(bsTabGroup);
        }
    }

    private void traverseTabs(final BSTabOwner bsTabOwner) {
        for (BSTab tab : bsTabOwner.getTabs()) {
            tab.setOwner(bsTabOwner);
            traverseRows(tab);
        }
    }

    private void traverseActions(final ActionLayoutDataOwner actionLayoutDataOwner) {
        if(actionLayoutDataOwner.getActions() == null) return;
        for (final ActionLayoutData actionLayoutData : actionLayoutDataOwner.getActions()) {
            actionLayoutData.setOwner(actionLayoutDataOwner);
        }
    }

    private void traverseFieldSets(final FieldSetOwner fieldSetOwner) {
        final List<FieldSet> fieldSets = fieldSetOwner.getFieldSets();
        for (FieldSet fieldSet : fieldSets) {
            fieldSet.setOwner(fieldSetOwner);
            traverseActions(fieldSet);
            final List<PropertyLayoutData> properties = fieldSet.getProperties();
            for (final PropertyLayoutData property : properties) {
                property.setOwner(fieldSet);
                traverseActions(property);
            }
        }
    }

    private void traverseCollections(
        final CollectionLayoutDataOwner owner) {
        final List<CollectionLayoutData> collections = owner.getCollections();
        for (CollectionLayoutData collection : collections) {
            collection.setOwner(owner);
            traverseActions(collection);
        }
    }

}
