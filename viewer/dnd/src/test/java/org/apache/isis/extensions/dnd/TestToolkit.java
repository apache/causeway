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


package org.apache.isis.extensions.dnd;

import java.util.Enumeration;

import org.apache.isis.core.commons.debug.DebugInfo;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.extensions.dnd.drawing.Background;
import org.apache.isis.extensions.dnd.drawing.Bounds;
import org.apache.isis.extensions.dnd.drawing.Color;
import org.apache.isis.extensions.dnd.drawing.ColorsAndFonts;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.drawing.Size;
import org.apache.isis.extensions.dnd.drawing.Text;
import org.apache.isis.extensions.dnd.view.BackgroundTask;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.DragEvent;
import org.apache.isis.extensions.dnd.view.Feedback;
import org.apache.isis.extensions.dnd.view.GlobalViewFactory;
import org.apache.isis.extensions.dnd.view.InteractionSpy;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.UndoStack;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.ViewSpecification;
import org.apache.isis.extensions.dnd.view.Viewer;
import org.apache.isis.extensions.dnd.viewer.basic.NullColor;
import org.apache.isis.extensions.dnd.viewer.drawing.DummyText;


public class TestToolkit extends Toolkit {

    public static void createInstance() {
        if (getInstance() == null) {
            new TestToolkit();
        }
    }

    private TestToolkit() {}

    @Override
    protected void init() {
        colorsAndFonts = new ColorsAndFonts() {

            public int defaultBaseline() {
                return 0;
            }

            public int defaultFieldHeight() {
                return 0;
            }

            public Color getColor(final int rgbColor) {
                return null;
            }

            public Color getColor(final String name) {
                return new NullColor();
            }

            public Text getText(final String name) {
                return new DummyText();
            }

            public void init() {}
        };

        viewer = new Viewer() {

            public void addToNotificationList(final View view) {}

            public void clearAction() {}

            public void clearOverlayView() {}

            public void clearOverlayView(final View view) {}

            public void disposeUnneededViews() {}

            public Object getClipboard(final Class<?> class1) {
                return null;
            }

            public InteractionSpy getSpy() {
                return null;
            }

            UndoStack undoStack = new UndoStack();

            public UndoStack getUndoStack() {
                return undoStack;
            }

            public boolean hasFocus(final View view) {
                return false;
            }

            public boolean isRunningAsExploration() {
                return false;
            }

            public boolean isRunningAsPrototype() {
                return false;
            }

            public void markDamaged(final Bounds bounds) {}

            public void removeFromNotificationList(final View view) {}

            public void scheduleRepaint() {}

            public void saveCurrentFieldEntry() {}

            public String selectFilePath(final String title, final String directory) {
                return null;
            }

            public void setBackground(final Background background) {}

            public void setClipboard(final String clip, final Class<?> class1) {}

            public void setKeyboardFocus(final View view) {}

            public void setOverlayView(final View view) {}

            public void showInOverlay(Content content, Location location) {}

            public void showDebugFrame(DebugInfo[] info, Location at) {}
            
            public Size getOverlaySize() {
                return null;
            }

            public void saveOpenObjects() {}

        };

        feedbackManager = new Feedback() {

            public void showArrowCursor() {}

            public void showCrosshairCursor() {}

            public void showDefaultCursor() {}

            public void showException(final Throwable e) {}

            public void showHandCursor() {}

            public void showMoveCursor() {}

            public void showResizeDownCursor() {}

            public void showResizeDownLeftCursor() {}

            public void showResizeDownRightCursor() {}

            public void showResizeLeftCursor() {}

            public void showResizeRightCursor() {}

            public void showResizeUpCursor() {}

            public void showResizeUpLeftCursor() {}

            public void showResizeUpRightCursor() {}

            public void showTextCursor() {}

            public void addMessage(final String string) {}

            public void clearAction() {}

            public void clearBusy(final View view) {}

            public void clearError() {}

            public String getStatusBarOutput() {
                return null;
            }

            public boolean isBusy(final View view) {
                return false;
            }

            public void setAction(final String string) {}

            public void setBusy(final View view, final BackgroundTask task) {}

            public void setError(final String string) {}

            public void setViewDetail(final String string) {}

            public void showBusyState(final View view) {}

            public void showMessagesAndWarnings() {}
        };
        
        viewFactory = new GlobalViewFactory() {

            public Enumeration<ViewSpecification> availableViews(ViewRequirement viewRequirement) {
                throw new UnexpectedCallException();
            }

            public View createDialog(Content content) {
                throw new UnexpectedCallException();
            }

            public DragEvent createDragContentOutline(View view, Location location) {
                throw new UnexpectedCallException();
            }

            public View createDragViewOutline(View view) {
                throw new UnexpectedCallException();
            }

            public View createMinimizedView(View view) {
                throw new UnexpectedCallException();
            }

            public View createView(ViewRequirement requirement) {
                return new DummyView();
            }

            public void debugData(DebugString debug) {}

            public String debugTitle() {
                throw new UnexpectedCallException();
            }

            public void addSpecification(ViewSpecification spec) {}

            public Enumeration<ViewSpecification> availableDesigns(ViewRequirement viewRequirement) {
                return null;
            }
            
        };
    }
}

