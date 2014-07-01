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

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.dnd.DummyContent;
import org.apache.isis.viewer.dnd.DummyView;
import org.apache.isis.viewer.dnd.DummyViewSpecification;
import org.apache.isis.viewer.dnd.TestToolkit;
import org.apache.isis.viewer.dnd.configurable.PanelView.Position;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewDrag;

public class PanelViewDropTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);

    @Mock
    private IsisConfiguration mockConfiguration;
    @Mock
    private ViewDrag viewDrag;
    
    private PanelView view;
    private DummyContent content;
    private Position position;
    private final DummyView sourceView = new DummyView();


    @Before
    public void setup() {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
        
        TestToolkit.createInstance();
        IsisContext.setConfiguration(mockConfiguration);

        content = new DummyContent();

        view = new PanelView(content, new DummyViewSpecification()) {
            @Override
            public void addView(final Content content, final Position position) {
                PanelViewDropTest.this.position = position;
            }

            @Override
            public synchronized View[] getSubviews() {
                return new View[] { sourceView };
            }
        };
        view.setSize(new Size(200, 100));
        view.setLocation(new Location(400, 200));
    }

    @Test
    public void dropOnLeft() throws Exception {
        final ViewDrag drag = dragTo(new Location(405, 250));
        view.drop(drag);
        assertEquals(Position.West, position);
    }

    @Test
    public void dropOnRight() throws Exception {
        final ViewDrag drag = dragTo(new Location(595, 250));
        view.drop(drag);
        assertEquals(Position.East, position);
    }

    @Test
    public void dropOnTop() throws Exception {
        final ViewDrag drag = dragTo(new Location(500, 205));
        view.drop(drag);
        assertEquals(Position.North, position);
    }

    @Test
    public void dropOnBottom() throws Exception {
        final ViewDrag drag = dragTo(new Location(500, 295));
        view.drop(drag);
        assertEquals(Position.South, position);
    }

    @Test
    public void dropOnTopLeft() throws Exception {
        final ViewDrag drag = dragTo(new Location(405, 205));
        view.drop(drag);
        assertEquals(Position.North, position);
    }

    @Test
    public void dropOnTopRight() throws Exception {
        final ViewDrag drag = dragTo(new Location(595, 205));
        view.drop(drag);
        assertEquals(Position.North, position);
    }

    @Test
    public void dropOnBottomLeft() throws Exception {
        final ViewDrag drag = dragTo(new Location(405, 295));
        view.drop(drag);
        assertEquals(Position.South, position);
    }

    @Test
    public void dropOnBottomRight() throws Exception {
        final ViewDrag drag = dragTo(new Location(595, 295));
        view.drop(drag);
        assertEquals(Position.South, position);
    }

    private ViewDrag dragTo(final Location location) {
        context.checking(new Expectations() {
            {
                exactly(3).of(viewDrag).getSourceView();
                will(returnValue(sourceView));

                atLeast(1).of(viewDrag).getLocation();
                will(returnValue(location));
            }
        });
        return viewDrag;
    }

}
