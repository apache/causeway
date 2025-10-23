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
package org.apache.causeway.core.metamodel.services.grid.bootstrap;

import java.util.ArrayList;
import java.util.Stack;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTab;

/**
 * Removes empty tabs from tab groups.
 */
record EmptyTabRemovalProcessor(BSGrid bsGrid) {

    static final class Flag {
        boolean keep = false;
    }

    public void run() {

        var emptyTabs = new ArrayList<BSTab>();

        bsGrid.visit(new BSGrid.VisitorAdapter() {

            final Stack<Flag> stack = new Stack<Flag>();

            @Override public void visit(ActionLayoutData actionLayoutData) { keep(); }
            @Override public void visit(DomainObjectLayoutData domainObjectLayoutData) { keep(); }
            @Override public void visit(PropertyLayoutData propertyLayoutData) { keep(); }
            @Override public void visit(CollectionLayoutData collectionLayoutData) { keep(); }

            @Override public void visit(BSTab bsTab) {
                stack.push(new Flag());
            }
            @Override public void postVisit(BSTab bsTab) {
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

        emptyTabs.forEach(tab->tab.remove());

    }
}
