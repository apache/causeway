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

package org.apache.isis.viewer.dnd;

import junit.framework.TestCase;

import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.interaction.ContentDragImpl;
import org.apache.isis.viewer.dnd.view.ContentDrag;

public class ContentDragTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testLocation() {
        final DummyView sourceView = new DummyView();
        sourceView.setParent(new DummyWorkspaceView());
        sourceView.setupLocation(new Location(1000, 1000));

        final ContentDrag drag = new ContentDragImpl(sourceView, new Location(10, 10), new DummyView());
        assertEquals(new Location(10, 10), drag.getOffset());

        final DummyView targetView = new DummyView();
        targetView.setupAbsoluteLocation(new Location(100, 100));

        // drag.drag(targetView, new Location(120, 120), 0);
        // assertEquals(new Location(20, 20), drag.getTargetLocation());
    }
}
