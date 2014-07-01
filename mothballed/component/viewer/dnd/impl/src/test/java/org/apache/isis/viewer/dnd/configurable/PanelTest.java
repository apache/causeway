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

package org.apache.isis.viewer.dnd.configurable;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.viewer.dnd.DummyView;
import org.apache.isis.viewer.dnd.TestToolkit;
import org.apache.isis.viewer.dnd.configurable.PanelView.Position;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;

public class PanelTest {

    private Panel panel;
    private DummyView view2;
    private DummyView view1;
    private DummyView view3;

    @Before
    public void setup() {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
        TestToolkit.createInstance();

        view1 = new DummyView();
        view1.setupRequiredSize(new Size(200, 100));
        view2 = new DummyView();
        view2.setupRequiredSize(new Size(150, 120));
        view3 = new DummyView();
        view3.setupRequiredSize(new Size(150, 120));

        panel = new Panel();
        panel.addView(view1, null);
        panel.addView(view2, Position.East);

    }

    @Test
    public void firstPanelInTopLeft() throws Exception {
        panel.layout(Size.createMax());
        assertEquals(new Location(0, 0), view1.getLocation());
    }

    @Test
    public void firstPanelSizeSetToRequiredSize() throws Exception {
        assertNull(view1.getSize());
        panel.layout(Size.createMax());
        assertEquals(new Size(200, 100), view1.getSize());
    }

    @Test
    public void totalSizeIsWidthPlusMaxHeigth() throws Exception {
        assertEquals(new Size(350, 120), panel.getRequiredSize(Size.createMax()));
    }

    @Test
    public void addingToSouth() throws Exception {
        panel = new Panel();
        panel.addView(view1, Position.East);
        panel.addView(view2, Position.South);
        panel.addView(view3, Position.South);

        assertEquals(new Size(200, 340), panel.getRequiredSize(Size.createMax()));
    }

    @Test
    public void addingToEast() throws Exception {
        panel.addView(view3, Position.East);

        assertEquals(new Size(500, 120), panel.getRequiredSize(Size.createMax()));
    }

    @Test
    public void secondPanelLocatedToRightOfFirst() throws Exception {
        panel.layout(Size.createMax());
        assertEquals(new Location(0, 0), view1.getLocation());
        assertEquals(new Location(200, 0), view2.getLocation());
    }

    @Test
    public void addingThirdPanelToLeftMovesFirstTwoPanels() throws Exception {
        panel.addView(view3, Position.West);
        panel.layout(Size.createMax());
        assertEquals(new Location(150, 0), view1.getLocation());
        assertEquals(new Location(350, 0), view2.getLocation());
        assertEquals(new Location(0, 0), view3.getLocation());
    }

    @Test
    public void nullPostionAddInSameOrientation() throws Exception {
        panel.addView(view3, null);
        assertEquals(new Size(500, 120), panel.getRequiredSize(Size.createMax()));
    }

    @Test
    public void addingThirdPanelToBottomIncreasesHeight() throws Exception {
        panel.addView(view3, Position.South);
        assertEquals(new Size(350, 240), panel.getRequiredSize(Size.createMax()));
    }

    @Test
    public void addingFouthPanelToLeftIncreasesWidth() throws Exception {
        final DummyView view4 = new DummyView();
        view4.setupRequiredSize(new Size(50, 100));

        panel.addView(view3, Position.South);
        panel.addView(view4, Position.East);
        assertEquals(new Size(400, 240), panel.getRequiredSize(Size.createMax()));
    }

    @Test
    public void addingThirdPanelToNorthIncreasesHeight() throws Exception {
        panel.addView(view3, Position.North);
        assertEquals(new Size(350, 240), panel.getRequiredSize(Size.createMax()));
    }

    @Test
    public void addingThirdPanelToTopPlacesItAboveTheOtherTwo() throws Exception {
        panel.addView(view3, Position.North);
        panel.layout(Size.createMax());
        assertEquals(new Location(0, 120), view1.getLocation());
        assertEquals(new Location(200, 120), view2.getLocation());
        assertEquals(new Location(0, 0), view3.getLocation());
    }

    @Test
    public void addingThirdPanelToBottomPlacesItBelowTheOtherTwo() throws Exception {
        panel.addView(view3, Position.South);
        panel.layout(Size.createMax());
        assertEquals(new Location(0, 0), view1.getLocation());
        assertEquals(new Location(200, 0), view2.getLocation());
        assertEquals(new Location(0, 120), view3.getLocation());
    }
}
