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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.field.DatePickerControl;
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
    private static final Logger LOG = LoggerFactory.getLogger(CalendarView.class);

    protected CalendarView(final Content content, final ViewSpecification specification) {
        super(content, specification);
    }

    @Override
    public void doLayout(final Size maximumSize) {
        LOG.debug("doLayout() " + maximumSize + "  " + getSize());
        final View toolbar = getSubviews()[0];
        maximumSize.contract(getPadding());
        final Size toolbarSize = toolbar.getRequiredSize(maximumSize);
        LOG.debug("   toolbar " + toolbarSize);
        Bounds bounds = new Bounds(toolbarSize);
        toolbar.setBounds(bounds);

        final View grid = getSubviews()[1];
        final Size gridSize = getRequiredSize(Size.createMax());
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
    public Size requiredSize(final Size availableSpace) {
        final Size workspace = getWorkspace().getSize();
        return new Size((int) (workspace.getWidth() * 0.8), (int) (workspace.getHeight() * 0.8));
    }

    @Override
    protected void buildView() {
        if (subviews().length == 0) {
            final CalendarGrid grid = new CalendarGrid(getContent());
            final ToolbarView toolbar = createToolbar(grid);
            addView(toolbar);
            addView(grid);
        } else {
            // TODO update grid view
        }
    }

    private ToolbarView createToolbar(final CalendarGrid calendar) {
        final ToolbarView toolbarView = new ToolbarView(getContent(), null);

        toolbarView.addView(new Button(new AbstractButtonAction("+Row") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                calendar.addRow();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("-Row") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                calendar.removeRow();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Across") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                calendar.acrossFirst();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Down") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                calendar.downFirst();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Next") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                calendar.nextPeriod();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Previous") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                calendar.previousePeriod();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Day") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                calendar.showSingleDay();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Days") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                calendar.showDays();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Weeks") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                calendar.showWeeks();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Months") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                calendar.showMonths();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Years") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                calendar.showYears();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Today") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                calendar.today();
            }
        }, this));

        toolbarView.addView(new Button(new AbstractButtonAction("Date") {
            @Override
            public void execute(final Workspace workspace, View view, final Location at) {
                final Content content = new NullContent() {
                };
                view = DatePickerControl.getPicker(content);
                calendar.today();
                getViewManager().setOverlayView(view);
            }
        }, this));

        return toolbarView;
    }

    /*
     * public void invalidateLayout() { // super.invalidateLayout(); View parent
     * = getParent(); if (parent != null) { // parent.invalidateLayout(); }
     * isInvalid = true; View toolbar = getSubviews()[0];
     * toolbar.invalidateLayout(); // View grid = getSubviews()[1]; //
     * grid.invalidateLayout(); } protected boolean isLayoutInvalid() { return
     * isInvalid; }
     */

}
