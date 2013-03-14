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

package org.apache.isis.viewer.dnd.viewer.basic;

import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.dnd.DummyView;
import org.apache.isis.viewer.dnd.DummyWorkspaceView;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.PlacementStrategyImpl;

public class PlacementStrategyImplTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private IsisConfiguration mockConfiguration;
    
    private static final int ORIGINAL_Y = 70;
    private static final int ORIGINAL_X = 50;
    private static final int PADDING = 10;
    private static final int NEW_VIEW_HEIGHT = 150;
    private static final int NEW_VIEW_WIDTH = 100;
    private static final int ROOT_VIEW_HEIGHT = 190;
    private static final int ROOT_VIEW_WIDTH = 120;
    private static final int WORKSPACE_HEIGHT = 800;
    private static final int WORKSPACE_WIDTH = 1000;
    
    private DummyWorkspaceView workspace;
    private DummyView existingView;
    private DummyView newView;
    private PlacementStrategyImpl strategy;
    

    @Before
    public void setUp() throws Exception {
        IsisContext.setConfiguration(mockConfiguration);

        workspace = new DummyWorkspaceView();
        workspace.setSize(new Size(WORKSPACE_WIDTH, WORKSPACE_HEIGHT));

        existingView = new DummyView();
        existingView.setSize(new Size(ROOT_VIEW_WIDTH, ROOT_VIEW_HEIGHT));
        existingView.setLocation(new Location(ORIGINAL_X, ORIGINAL_Y));
        existingView.setParent(workspace);
        workspace.addView(existingView);

        newView = new DummyView();
        newView.setupRequiredSize(new Size(NEW_VIEW_WIDTH, NEW_VIEW_HEIGHT));

        strategy = new PlacementStrategyImpl();
    }

    @Test
    public void defaultWhenNoRelativeView() throws Exception {
        final Location location = strategy.determinePlacement(workspace, null, newView);
        Assert.assertEquals(new Location(), location);
    }

    @Test
    public void placeToRight() throws Exception {
        final Location location = strategy.determinePlacement(workspace, existingView, newView);
        Assert.assertEquals(new Location(ORIGINAL_X + ROOT_VIEW_WIDTH + PADDING, 70), location);
    }

    @Test
    public void adjustWhenOnTopOfExistingField() throws Exception {
        final DummyView anotherView = new DummyView();
        anotherView.setLocation(new Location(ORIGINAL_X + ROOT_VIEW_WIDTH + PADDING, ORIGINAL_Y));
        anotherView.setSize(new Size(50, 50));
        workspace.addView(anotherView);

        newView.setupRequiredSize(new Size(NEW_VIEW_WIDTH, NEW_VIEW_HEIGHT));

        final Location location = strategy.determinePlacement(workspace, existingView, newView);
        Assert.assertEquals(new Location(ORIGINAL_X + ROOT_VIEW_WIDTH + PADDING + PADDING * 4, ORIGINAL_Y + PADDING * 4), location);
    }

    @Test
    public void placeBelow() throws Exception {
        existingView.setLocation(new Location(WORKSPACE_WIDTH - 200, 100));

        final Location location = strategy.determinePlacement(workspace, existingView, newView);
        Assert.assertEquals(new Location(WORKSPACE_WIDTH - 200, 100 + +ROOT_VIEW_HEIGHT + PADDING), location);
    }

    @Test
    public void placeToLeft() throws Exception {
        existingView.setLocation(new Location(WORKSPACE_WIDTH - 200, 500));

        final Location location = strategy.determinePlacement(workspace, existingView, newView);
        Assert.assertEquals(new Location(WORKSPACE_WIDTH - 200 - PADDING - NEW_VIEW_WIDTH, 500), location);
    }

    @Test
    public void placeAbove() throws Exception {
        existingView.setLocation(new Location(100, 700));
        existingView.setSize(new Size(900, 100));

        final Location location = strategy.determinePlacement(workspace, existingView, newView);
        Assert.assertEquals(new Location(100, 700 - NEW_VIEW_HEIGHT - PADDING), location);
    }

    @Test
    public void viewLargerThanWorkspace() throws Exception {
        existingView.setLocation(new Location(100, 100));
        newView.setupRequiredSize(new Size(1100, 900));

        final Location location = strategy.determinePlacement(workspace, existingView, newView);
        Assert.assertEquals("should be placed on top of original, but slightly offset", new Location(0, 0), location);
    }

    @Test
    public void viewLargerThanWorkspaceAndExisitingViewInCorner() throws Exception {
        existingView.setLocation(new Location(200, 300));
        existingView.setLocation(new Location(0, 0));
        newView.setupRequiredSize(new Size(1100, 900));

        final Location location = strategy.determinePlacement(workspace, existingView, newView);
        Assert.assertEquals("should be placed on top of original, but slightly offset", new Location(0, 0), location);
    }

    @Test
    public void notEnoughFreeSpaceInAnyDirection() throws Exception {
        existingView.setLocation(new Location(100, 100));
        existingView.setSize(new Size(800, 600));

        final Location location = strategy.determinePlacement(workspace, existingView, newView);
        Assert.assertEquals("should be placed on top of original, but slightly offset", new Location(100 + PADDING * 6, 100 + PADDING * 6), location);
    }

    @Test
    public void wideComponentShiftsToLeft() throws Exception {
        newView.setupRequiredSize(new Size(1200, NEW_VIEW_HEIGHT));

        final Location location = strategy.determinePlacement(workspace, existingView, newView);
        Assert.assertEquals(new Location(0, ORIGINAL_Y + ROOT_VIEW_HEIGHT + PADDING), location);
    }

    @Test
    public void tallComponentShiftsUp() throws Exception {
        newView.setupRequiredSize(new Size(NEW_VIEW_WIDTH, 1000));

        final Location location = strategy.determinePlacement(workspace, existingView, newView);
        Assert.assertEquals(new Location(ORIGINAL_X + ROOT_VIEW_WIDTH + PADDING, 0), location);
    }

    @Test
    public void wideComponentsDontCompletelyOverlap() throws Exception {
        final DummyView anotherView = new DummyView();
        anotherView.setLocation(new Location(0, 70 + ROOT_VIEW_HEIGHT + PADDING));
        anotherView.setSize(new Size(WORKSPACE_WIDTH, 100));
        workspace.addView(anotherView);

        newView.setupRequiredSize(new Size(WORKSPACE_WIDTH, NEW_VIEW_HEIGHT));

        final Location location = strategy.determinePlacement(workspace, existingView, newView);
        Assert.assertEquals(new Location(0, 70 + ROOT_VIEW_HEIGHT + PADDING + PADDING * 4), location);
    }

    @Test
    public void tallComponentsDontCompletelyOverlap() throws Exception {
        final DummyView anotherView = new DummyView();
        anotherView.setLocation(new Location(ORIGINAL_X + ROOT_VIEW_WIDTH + PADDING, 0));
        anotherView.setSize(new Size(100, WORKSPACE_HEIGHT));
        workspace.addView(anotherView);

        newView.setupRequiredSize(new Size(NEW_VIEW_WIDTH, WORKSPACE_HEIGHT));

        final Location location = strategy.determinePlacement(workspace, existingView, newView);
        Assert.assertEquals(new Location(ORIGINAL_X + ROOT_VIEW_WIDTH + PADDING + PADDING * 4, 0), location);
    }

    @Test
    public void noSpaceToMoveNewView() throws Exception {
        newView.setupRequiredSize(new Size(WORKSPACE_WIDTH - ORIGINAL_X, WORKSPACE_HEIGHT - ORIGINAL_Y));

        final Location location = strategy.determinePlacement(workspace, existingView, newView);
        Assert.assertEquals(new Location(ORIGINAL_X, ORIGINAL_Y), location);
    }

}
