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

import java.util.Optional;

import org.apache.causeway.applib.layout.grid.bootstrap.BSCol;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTabGroup;

/**
 * Conditionally collapses tab groups.
 *
 * <p> honors {@code <bs:tabGroup collapseIfOne="true">}
 */
record CollapseIfOneTabProcessor(BSGrid bsGrid) {

    public void run() {
        bsGrid.visit(new BSGrid.VisitorAdapter() {
            @Override
            public void visit(BSTabGroup bsTabGroup) {
                var isCollapseIfOne = Optional.ofNullable(bsTabGroup.isCollapseIfOne())
                    .map(boolean.class::cast)
                    .orElse(true); // opt-out semantics: absence of the attribute results in participation

                if(!isCollapseIfOne
                        || bsTabGroup.getTabs().size()>1) {
                    return;
                }

                if(!isCollapseIfOne) return;

                var parent = (BSCol) bsTabGroup.getOwner();
                parent.getTabGroups().remove(bsTabGroup);
                // relocate rows from tab to owning col
                bsTabGroup.getTabs().get(0).getRows()
                    .forEach(row->{
                        parent.getRows().add(row);
                        row.setOwner(parent);
                    });
            }
        });
    }
}
