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

package org.apache.isis.viewer.dnd.viewer.view;

import junit.framework.TestCase;

import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Padding;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.base.AbstractView;
import org.apache.isis.viewer.dnd.view.content.NullContent;

public class AbstractViewTest extends TestCase {
    private AbstractView av;

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(AbstractViewTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        av = new AbstractView(new NullContent()) {
        };
        super.setUp();
    }

    public void testBounds() {
        assertEquals(new Location(), av.getLocation());
        assertEquals(new Size(), av.getSize());
        assertEquals(new Bounds(), av.getBounds());

        av.setLocation(new Location(10, 20));
        assertEquals(new Location(10, 20), av.getLocation());
        assertEquals(new Size(), av.getSize());
        assertEquals(new Bounds(10, 20, 0, 0), av.getBounds());

        av.setSize(new Size(30, 40));
        assertEquals(new Location(10, 20), av.getLocation());
        assertEquals(new Size(30, 40), av.getSize());
        assertEquals(new Bounds(10, 20, 30, 40), av.getBounds());

        av.setBounds(new Bounds(new Location(50, 60), new Size(70, 80)));
        assertEquals(new Location(50, 60), av.getLocation());
        assertEquals(new Size(70, 80), av.getSize());
        assertEquals(new Bounds(50, 60, 70, 80), av.getBounds());
    }

    public void testPadding() {
        assertEquals(new Padding(0, 0, 0, 0), av.getPadding());
    }

    public void testViewAreaType() {
        final Location loc = new Location(10, 10);
        assertEquals(ViewAreaType.CONTENT, av.viewAreaType(loc));
    }

    public void testBoundsSetSizeAndLocation() throws Exception {
        final Location l = new Location();
        final Size z = new Size();
        final View view = new AbstractView(new NullContent()) {
            @Override
            public void setLocation(final Location location) {
                l.translate(location);
            }

            @Override
            public void setSize(final Size size) {
                z.extend(size);
            }
        };

        view.setBounds(new Bounds(20, 30, 40, 50));
        assertEquals(new Location(20, 30), l);
        assertEquals(new Size(40, 50), z);
    }

    public void testRequiredSizeIsLimitedToTheMaximumSize() throws Exception {
        assertEquals(Size.createMax(), av.getRequiredSize(Size.createMax()));
        assertEquals(new Size(100, 50), av.getRequiredSize(new Size(100, 50)));
    }
}
