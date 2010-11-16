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


package org.apache.isis.viewer.dnd.calendar;

import org.apache.log4j.Logger;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.field.DatePicker;
import org.apache.isis.viewer.dnd.toolbar.ToolbarView;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.FocusManager;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.composite.CompositeView;
import org.apache.isis.viewer.dnd.view.content.NullContent;
import org.apache.isis.viewer.dnd.view.control.AbstractButtonAction;
import org.apache.isis.viewer.dnd.view.control.Button;


public class CalendarView extends CompositeView {
    private static final Logger LOG = Logger.getLogger(CalendarView.class);

    protected CalendarView(Content content, ViewSpecification specification) {
        super(content, specification);
    }

    public void doLayout(Size maximumSize) {
        LOG.debug("doLayout() " + maximumSize + "  " + getSize());
        View toolbar = getSubviews()[0];
        maximumSize.contract(getPadding());
        Size toolbarSize = toolbar.getRequiredSize(maximumSize);
        LOG.debug("   toolbar " + toolbarSize);
        Bounds bounds = new Bounds(toolbarSize);
        toolbar.setBounds(bounds);

        
        View grid = getSubviews()[1];
        Size gridSize = getRequiredSize(Size.createMax());
        gridSize.contract(getPadding());
        gridSize.contractHeight(toolbarSize.getHeight());
        bounds = new Bounds(new Location(0, toolbarSize.getHeight()), gridSize);
        grid.setBounds(bounds);
        LOG.debug("   grid " + toolbarSize);
         
    }

    @Override
    public void setFocusManager(final FocusManager focusManager) {
    // this.focusManager = focusManager;
    }

    @Override
    public Size requiredSize(Size availableSpace) {
        Size workspace = getWorkspace().getSize();
        return new Size((int) (workspace.getWidth() * 0.8), (int) (workspace.getHeight() * 0.8));
    }

    protected void buildView() {
        if (subviews().length == 0) {
            CalendarGrid grid = new CalendarGrid(getContent());
            ToolbarView toolbar = createToolbar(grid);
            addView(toolbar);
          addView(grid);
        } else {
            // TODO update grid view
        }
    }

    private ToolbarView createToolbar(final CalendarGrid calendar) {
        ToolbarView toolbarView = new ToolbarView(getContent(), null);
        
        toolbarView.addView(new Button(new AbstractButtonAction("+Row") {
            public void execute(Workspace workspace, View view, Location at) {
                calendar.addRow();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("-Row") {
            public void execute(Workspace workspace, View view, Location at) {
                calendar.removeRow();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Across") {
            public void execute(Workspace workspace, View view, Location at) {
                calendar.acrossFirst();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Down") {
            public void execute(Workspace workspace, View view, Location at) {
                calendar.downFirst();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Next") {
            public void execute(Workspace workspace, View view, Location at) {
                calendar.nextPeriod();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Previous") {
            public void execute(Workspace workspace, View view, Location at) {
                calendar.previousePeriod();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Day") {
            public void execute(Workspace workspace, View view, Location at) {
                calendar.showSingleDay();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Days") {
            public void execute(Workspace workspace, View view, Location at) {
                calendar.showDays();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Weeks") {
            public void execute(Workspace workspace, View view, Location at) {
                calendar.showWeeks();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Months") {
            public void execute(Workspace workspace, View view, Location at) {
                calendar.showMonths();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Years") {
            public void execute(Workspace workspace, View view, Location at) {
                calendar.showYears();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Today") {
            public void execute(Workspace workspace, View view, Location at) {
                calendar.today();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Date") {
            public void execute(Workspace workspace, View view, Location at) {
                Content content = new NullContent() {};
                view = new DatePicker(content);
                calendar.today();
                getViewManager().setOverlayView(view);
            }
        }, this));

        return toolbarView;
    }

    /*
     * public void invalidateLayout() { // super.invalidateLayout(); View parent = getParent(); if (parent !=
     * null) { // parent.invalidateLayout(); } isInvalid = true; View toolbar = getSubviews()[0];
     * toolbar.invalidateLayout(); // View grid = getSubviews()[1]; // grid.invalidateLayout(); }
    protected boolean isLayoutInvalid() {
        return isInvalid;
    }
     */

}
