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

package org.apache.isis.viewer.dnd.table;

import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.base.Layout;
import org.apache.isis.viewer.dnd.view.composite.CompositeViewSpecification;

public class TableRowSpecification extends CompositeViewSpecification {
    public TableRowSpecification() {
        builder = new TableCellBuilder();
    }

    @Override
    public Layout createLayout(final Content content, final Axes axes) {
        return new TableRowLayout(axes.getAxis(TableAxis.class));
    }

    @Override
    public boolean canDisplay(final ViewRequirement requirement) {
        return requirement.isObject();
    }

    @Override
    public String getName() {
        return "Table Row";
    }

    @Override
    public boolean isReplaceable() {
        return false;
    }

    @Override
    public boolean isSubView() {
        return true;
    }
}
