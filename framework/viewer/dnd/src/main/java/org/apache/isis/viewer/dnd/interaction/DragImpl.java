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

package org.apache.isis.viewer.dnd.interaction;

import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Viewer;

public abstract class DragImpl extends PointerEvent implements DragEvent {
    protected DragImpl() {
        super(0);
    }

    /**
     * Indicates the drag has been cancelled; no action should be taken.
     */
    @Override
    public abstract void cancel(final Viewer viewer);

    /**
     * Indicates that the drag state has changed.
     */
    @Override
    public abstract void drag(final View target, final Location location, final int mods);

    /**
     * Indicates the drag has properly ended (the mouse button has been
     * released)
     * 
     */
    @Override
    public abstract void end(final Viewer viewer);

    @Override
    public abstract View getOverlay();
}
