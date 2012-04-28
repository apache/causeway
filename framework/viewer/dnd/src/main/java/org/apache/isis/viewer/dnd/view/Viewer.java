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

package org.apache.isis.viewer.dnd.view;

import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.viewer.dnd.drawing.Background;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;

public interface Viewer {

    void markDamaged(final Bounds bounds);

    boolean hasFocus(final View view);

    UndoStack getUndoStack();

    Size getOverlaySize();

    void saveCurrentFieldEntry();

    void setKeyboardFocus(final View view);

    boolean isRunningAsExploration();

    boolean isRunningAsPrototype();

    void clearAction();

    /**
     * Force a repaint of the damaged area of the viewer.
     */
    void scheduleRepaint();

    void addToNotificationList(final View view);

    void removeFromNotificationList(final View view);

    void setBackground(Background background);

    InteractionSpy getSpy();

    void clearOverlayView();

    void clearOverlayView(final View view);

    void setOverlayView(final View view);

    void showDebugFrame(DebuggableWithTitle[] info, Location at);

    void showInOverlay(Content content, Location location);

    // TODO should this be an extension?
    String selectFilePath(final String title, final String directory);

    void setClipboard(String clip, Class<?> class1);

    Object getClipboard(Class<?> class1);

    /**
     * Removes views for objects that no longer exist, ie have been deleted.
     */
    void disposeUnneededViews();

    void saveOpenObjects();
}
