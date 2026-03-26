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
import java.util.Stack;
import java.util.function.UnaryOperator;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.HasHidden;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.bootstrap.BSElement.BSElementVisitor;
import org.apache.causeway.commons.internal.base._NullSafe;

@FunctionalInterface
public interface BSGridTransformer extends UnaryOperator<BSGrid> {

    /**
     * Removes empty tabs from tab groups. And then also empty tab groups.
     */
    final static class EmptyTabRemover implements BSGridTransformer {

        static final class Flag {
            boolean keep = false;
        }

        @Override
        public BSGrid apply(final BSGrid bsGrid) {
            var emptyTabs = new ArrayList<BSTab>();

            // first phase: collect all empty tabs for removal
            bsGrid.visit(new BSElementVisitor() {

                final Stack<Flag> stack = new Stack<>();

                @Override public void visit(final ActionLayoutData actionLayoutData) {
                    if(_NullSafe.isEmpty(actionLayoutData.getMetadataError())
                            && !isAlwaysHidden(actionLayoutData)) {
                        keep();
                    }
                }
                @Override public void visit(final DomainObjectLayoutData domainObjectLayoutData) {
                    if(_NullSafe.isEmpty(domainObjectLayoutData.getMetadataError())) {
                        keep();
                    }
                }
                @Override public void visit(final PropertyLayoutData propertyLayoutData) {
                    if(_NullSafe.isEmpty(propertyLayoutData.getMetadataError())
                            && !isAlwaysHidden(propertyLayoutData)) {
                        keep();
                    }
                }
                @Override public void visit(final CollectionLayoutData collectionLayoutData) {
                    if(_NullSafe.isEmpty(collectionLayoutData.getMetadataError())
                            && !isAlwaysHidden(collectionLayoutData)) {
                        keep();
                    }
                }

                @Override public void enter(final BSTab bsTab) {
                    stack.push(new Flag());
                }
                @Override public void exit(final BSTab bsTab) {
                    var flag = stack.pop();
                    if(!flag.keep) {
                        // collecting empty tabs
                        emptyTabs.add(bsTab);
                    }
                }
                private boolean isAlwaysHidden(final HasHidden hasHidden) {
                    if(hasHidden==null || hasHidden.getHidden()==null) return false;
                    return hasHidden.getHidden().isAlways()
                            || hasHidden.getHidden().isObjectForms();
                }
                private void keep() {
                    stack.stream().forEach(row->row.keep=true);
                }
            });

            // second phase: removal of tabs not to keep
            emptyTabs.forEach(tab->
                BSUtil.remove(tab)
                    .map(BSTabGroup.class::cast)
                    .filter(tabGroup->tabGroup.getTabs().isEmpty())
                    .ifPresent(BSUtil::remove));

            return bsGrid;
        }

    }

    /**
     * Removes empty rows from their row-owners.
     */
    final static class EmptyRowRemover implements BSGridTransformer {

        static final class Flag {
            boolean keep = false;
        }

        @Override
        public BSGrid apply(final BSGrid bsGrid) {
            var emptyRows = new ArrayList<BSRow>();

            bsGrid.visit(new BSElementVisitor() {

                final Stack<Flag> stack = new Stack<Flag>();

                @Override public void visit(final ActionLayoutData actionLayoutData) {
                    if(_NullSafe.isEmpty(actionLayoutData.getMetadataError())) keep();
                }
                @Override public void visit(final DomainObjectLayoutData domainObjectLayoutData) {
                    if(_NullSafe.isEmpty(domainObjectLayoutData.getMetadataError())) keep();
                }
                @Override public void visit(final PropertyLayoutData propertyLayoutData) {
                    if(_NullSafe.isEmpty(propertyLayoutData.getMetadataError())) keep();
                }
                @Override public void visit(final CollectionLayoutData collectionLayoutData) {
                    if(_NullSafe.isEmpty(collectionLayoutData.getMetadataError())) keep();
                }

                @Override public void enter(final BSRow bsRow) {
                    stack.push(new Flag());
                }
                @Override public void exit(final BSRow bsRow) {
                    var flag = stack.pop();
                    if(!flag.keep) {
                        // collecting into list, so we don't risk a ConcurrentModificationException,
                        // when racing with the underlying iterator
                        emptyRows.add(bsRow);
                    }
                }
                private void keep() {
                    stack.stream().forEach(row->row.keep=true);
                }
            });

            emptyRows.forEach(row->{
                BSUtil.remove(row);
            });

            return bsGrid;
        }
    }

    /**
     * Conditionally collapses tab groups.
     *
     * <p> honors {@code <bs:tabGroup collapseIfOne="true">}
     */
    final static class CollapseIfOneTab implements BSGridTransformer {

        @Override
        public BSGrid apply(final BSGrid bsGrid) {
            bsGrid.visit(new BSElementVisitor() {
                @Override
                public void enter(final BSTabGroup bsTabGroup) {
                    if(bsTabGroup.getTabs().size()!=1) return; // when has no tabs is also a no-op

                    // opt-out semantics: absence of the attribute results in participation
                    if(!bsTabGroup.isCollapseIfOne()==Boolean.TRUE) return;

                    var col = (BSCol) bsTabGroup.getOwner();
                    col.getTabGroups().remove(bsTabGroup);
                    // relocate rows from tab to owning col
                    bsTabGroup.getTabs().get(0).getRows()
                        .forEach(row->{
                            col.getRows().add(row);
                        });
                }
            });
            return bsGrid;
        }
    }

}
