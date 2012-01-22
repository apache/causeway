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

package org.apache.isis.viewer.dnd.view.composite;

import org.apache.isis.core.runtime.userprofile.Options;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

public class GridLayoutControlBorder extends AbstractBorder {

    public static final class Factory implements CompositeViewDecorator {
        @Override
        public View decorate(final View view, final Axes axes) {
            return new GridLayoutControlBorder(view);
        }
    }

    protected GridLayoutControlBorder(final View view) {
        super(view);
    }

    @Override
    public void viewMenuOptions(final UserActionSet menuOptions) {
        super.viewMenuOptions(menuOptions);

        final GridLayout layout = getViewAxes().getAxis(GridLayout.class);

        final boolean columnOrientation = layout.getOrientation() == GridLayout.COLUMNS;

        final UserActionSet submenu = menuOptions.addNewActionSet("Grid");

        submenu.add(new UserActionAbstract("Add " + (columnOrientation ? "Column" : "Row")) {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                layout.setSize(layout.getSize() + 1);
                invalidateLayout();
            }
        });

        if (layout.getSize() > 1) {
            submenu.add(new UserActionAbstract("Remove " + (columnOrientation ? "Column" : "Row")) {
                @Override
                public void execute(final Workspace workspace, final View view, final Location at) {
                    layout.setSize(layout.getSize() - 1);
                    invalidateLayout();
                }
            });
        }

        submenu.add(new UserActionAbstract(columnOrientation ? "In Rows" : "In Columns") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                layout.setOrientation(columnOrientation ? GridLayout.ROWS : GridLayout.COLUMNS);
                invalidateLayout();
            }
        });
    }

    @Override
    public void saveOptions(final Options viewOptions) {
        super.saveOptions(viewOptions);

        final GridLayout layout = getViewAxes().getAxis(GridLayout.class);
        viewOptions.addOption("orientation", layout.getOrientation() == GridLayout.COLUMNS ? "columns" : "rows");
        viewOptions.addOption("size", layout.getSize() + "");
    }

    @Override
    public void loadOptions(final Options viewOptions) {
        super.loadOptions(viewOptions);

        final GridLayout layout = getViewAxes().getAxis(GridLayout.class);
        layout.setOrientation(viewOptions.getString("orientation", "columns").equals("columns") ? GridLayout.COLUMNS : GridLayout.ROWS);
        layout.setSize(viewOptions.getInteger("size", 1));
    }
}
