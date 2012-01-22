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
import junit.textui.TestRunner;

public class ViewManagerTest extends TestCase {
    // private final static Component COMPONENT = new Component() {
    // };

    public static void main(final String[] args) {
        TestRunner.run(ViewManagerTest.class);
    }

    public void testNone() {
    }

    /*
     * public void testMouseClick() { MockWorkspace workspace = new
     * MockWorkspace(null); MockView view = new MockView();
     * workspace.setupIdentifyView(view);
     * 
     * ViewManager manager = new ViewManager(workspace, null);
     * 
     * view.setupGetAbsoluteLocation(new Location(10, 10));
     * view.setupIndicatesForView(true); view.setExpectedFirstClickCalls(1);
     * 
     * manager.mouseClicked(createMouseEvent(10, 20, 1,
     * MouseEvent.BUTTON1_MASK));
     * 
     * view.verify(); }
     * 
     * public void testPopupMouseClick() { MockWorkspace workspace = new
     * MockWorkspace(null); MockView view = new MockView();
     * workspace.setupIdentifyView(view);
     * 
     * MockPopupMenu popup = new MockPopupMenu(); ViewManager manager = new
     * ViewManager(workspace, popup);
     * 
     * view.setupGetAbsoluteLocation(new Location(10, 10));
     * view.setupIndicatesForView(true);
     * 
     * workspace.addExpectedSetIdentifiedViewValues(view);
     * 
     * popup.addExpectedInitValues(view, true, true);
     * view.setupGetAbsoluteLocation(new Location(10, 10));
     * view.setupIndicatesForView(true); // popup.setupGetParent(null); //
     * popup.setupIndicatesForView(true);
     * 
     * manager.mouseClicked(createMouseEvent(10, 20, 1,
     * MouseEvent.BUTTON3_MASK));
     * 
     * popup.verify(); view.verify();
     * 
     * popup.setExpectedFirstClickCalls(1); }
     * 
     * private MouseEvent createMouseEvent(final int x, final int y, final int
     * count, final int button) { return new MouseEvent(COMPONENT, 0, 0, button,
     * x, y, count, false); }
     */
}
