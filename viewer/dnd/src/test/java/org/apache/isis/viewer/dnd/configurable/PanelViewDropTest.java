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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.testsystem.TestProxyConfiguration;
import org.apache.isis.viewer.dnd.DummyContent;
import org.apache.isis.viewer.dnd.DummyView;
import org.apache.isis.viewer.dnd.DummyViewSpecification;
import org.apache.isis.viewer.dnd.TestToolkit;
import org.apache.isis.viewer.dnd.configurable.PanelView;
import org.apache.isis.viewer.dnd.configurable.PanelView.Position;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewDrag;
import org.apache.isis.viewer.dnd.view.ViewSpecification;

import static org.junit.Assert.assertEquals;


public class PanelViewDropTest {

    private PanelView view;
    private DummyContent content;
    private Position position;
    private DummyView sourceView = new DummyView();

    @Before
    public void setup() {
        Logger.getRootLogger().setLevel(Level.OFF);
        TestToolkit.createInstance();
        IsisContext.setConfiguration(new TestProxyConfiguration());


        content = new DummyContent();

        view = new PanelView(content, new DummyViewSpecification()) {
            public void addView(Content content, Position position) {
                PanelViewDropTest.this.position = position;
            }
            
            public synchronized View[] getSubviews() {
                return new View[] {sourceView};
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
        Mockery context = new Mockery();
        final ViewDrag drag = context.mock(ViewDrag.class);
        context.checking(new Expectations() {
            {
                exactly(3).of(drag).getSourceView();
                will(returnValue(sourceView));
                
                atLeast(1).of(drag).getLocation();
                will(returnValue(location));
            }
        });
        return drag;
    }

}

