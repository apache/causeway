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
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.commons.internal.base._NullSafe;

@FunctionalInterface
public interface BSGridTransformer extends UnaryOperator<BSGrid> {

    /**
     * Removes empty tabs from tab groups.
     */
    record EmptyTabRemover() implements BSGridTransformer {

        static final class Flag {
            boolean keep = false;
        }

        @Override
        public BSGrid apply(final BSGrid bsGrid) {
            var emptyTabs = new ArrayList<BSTab>();

            bsGrid.visit(new BSElement.Visitor() {

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

                @Override public void visit(final BSTab bsTab) {
                    stack.push(new Flag());
                }
                @Override public void postVisit(final BSTab bsTab) {
                    var flag = stack.pop();
                    if(!flag.keep) {
                        // collecting into list, so we don't risk a ConcurrentModificationException,
                        // when racing with the underlying iterator
                        emptyTabs.add(bsTab);
                    }
                }
                private void keep() {
                    if(stack.isEmpty()) return;
                    stack.peek().keep = true;
                }
            });

            emptyTabs.forEach(tab->{
                    var tabGroup = (BSTabGroup)tab.getOwner();
                    tab.setOwner(null); //fully detach
                    if(tabGroup==null) return;
                    tabGroup.getTabs().remove(tab);
                    // remove empty tab-groups as well
                    if(tabGroup.getTabs().isEmpty()) {
                        var tabGroupOwner = tabGroup.getOwner();
                        tabGroupOwner.getTabGroups().remove(tabGroup);
                    }
                });

            return bsGrid;
        }

    }

    /**
     * Conditionally collapses tab groups.
     *
     * <p> honors {@code <bs:tabGroup collapseIfOne="true">}
     */
    record CollapseIfOneTab() implements BSGridTransformer {

        @Override
        public BSGrid apply(final BSGrid bsGrid) {
            bsGrid.visit(new BSElement.Visitor() {
                @Override
                public void visit(final BSTabGroup bsTabGroup) {
                    if(bsTabGroup.getTabs().size()!=1) return; // when has no tabs is also a no-op

                    // opt-out semantics: absence of the attribute results in participation
                    if(!bsTabGroup.isCollapseIfOne(true)) return;

                    var parent = (BSCol) bsTabGroup.getOwner();
                    parent.getTabGroups().remove(bsTabGroup);
                    // relocate rows from tab to owning col
                    bsTabGroup.getTabs().get(0).getRows()
                        .forEach(row->{
                            parent.getRows().add(row);
                        });
                }
            });
            return bsGrid;
        }
    }

}
