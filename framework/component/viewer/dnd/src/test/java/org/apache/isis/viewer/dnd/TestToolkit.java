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

import java.util.Enumeration;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.viewer.dnd.drawing.Background;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.view.BackgroundTask;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.Feedback;
import org.apache.isis.viewer.dnd.view.GlobalViewFactory;
import org.apache.isis.viewer.dnd.view.InteractionSpy;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UndoStack;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.Viewer;
import org.apache.isis.viewer.dnd.viewer.basic.NullColor;
import org.apache.isis.viewer.dnd.viewer.drawing.DummyText;

public class TestToolkit extends Toolkit {

    public static void createInstance() {
        if (getInstance() == null) {
            new TestToolkit();
        }
    }

    private TestToolkit() {
    }

    @Override
    protected void init() {
        colorsAndFonts = new ColorsAndFonts() {

            @Override
            public int defaultBaseline() {
                return 0;
            }

            @Override
            public int defaultFieldHeight() {
                return 0;
            }

            @Override
            public Color getColor(final int rgbColor) {
                return null;
            }

            @Override
            public Color getColor(final String name) {
                return new NullColor();
            }

            @Override
            public Text getText(final String name) {
                return new DummyText();
            }

            @Override
            public void init() {
            }
        };

        viewer = new Viewer() {

            @Override
            public void addToNotificationList(final View view) {
            }

            @Override
            public void clearAction() {
            }

            @Override
            public void clearOverlayView() {
            }

            @Override
            public void clearOverlayView(final View view) {
            }

            @Override
            public void disposeUnneededViews() {
            }

            @Override
            public Object getClipboard(final Class<?> class1) {
                return null;
            }

            @Override
            public InteractionSpy getSpy() {
                return null;
            }

            UndoStack undoStack = new UndoStack();

            @Override
            public UndoStack getUndoStack() {
                return undoStack;
            }

            @Override
            public boolean hasFocus(final View view) {
                return false;
            }

            @Override
            public boolean isRunningAsExploration() {
                return false;
            }

            @Override
            public boolean isRunningAsPrototype() {
                return false;
            }

            @Override
            public void markDamaged(final Bounds bounds) {
            }

            @Override
            public void removeFromNotificationList(final View view) {
            }

            @Override
            public void scheduleRepaint() {
            }

            @Override
            public void saveCurrentFieldEntry() {
            }

            @Override
            public String selectFilePath(final String title, final String directory) {
                return null;
            }

            @Override
            public void setBackground(final Background background) {
            }

            @Override
            public void setClipboard(final String clip, final Class<?> class1) {
            }

            @Override
            public void setKeyboardFocus(final View view) {
            }

            @Override
            public void setOverlayView(final View view) {
            }

            @Override
            public void showInOverlay(final Content content, final Location location) {
            }

            @Override
            public void showDebugFrame(final DebuggableWithTitle[] info, final Location at) {
            }

            @Override
            public Size getOverlaySize() {
                return null;
            }

            @Override
            public void saveOpenObjects() {
            }

        };

        feedbackManager = new Feedback() {

            @Override
            public void showArrowCursor() {
            }

            @Override
            public void showCrosshairCursor() {
            }

            @Override
            public void showDefaultCursor() {
            }

            @Override
            public void showException(final Throwable e) {
            }

            @Override
            public void showHandCursor() {
            }

            @Override
            public void showMoveCursor() {
            }

            @Override
            public void showResizeDownCursor() {
            }

            @Override
            public void showResizeDownLeftCursor() {
            }

            @Override
            public void showResizeDownRightCursor() {
            }

            @Override
            public void showResizeLeftCursor() {
            }

            @Override
            public void showResizeRightCursor() {
            }

            @Override
            public void showResizeUpCursor() {
            }

            @Override
            public void showResizeUpLeftCursor() {
            }

            @Override
            public void showResizeUpRightCursor() {
            }

            @Override
            public void showTextCursor() {
            }

            @Override
            public void addMessage(final String string) {
            }

            @Override
            public void clearAction() {
            }

            @Override
            public void clearBusy(final View view) {
            }

            @Override
            public void clearError() {
            }

            @Override
            public String getStatusBarOutput() {
                return null;
            }

            @Override
            public boolean isBusy(final View view) {
                return false;
            }

            @Override
            public void setAction(final String string) {
            }

            @Override
            public void setBusy(final View view, final BackgroundTask task) {
            }

            @Override
            public void setError(final String string) {
            }

            @Override
            public void setViewDetail(final String string) {
            }

            @Override
            public void showBusyState(final View view) {
            }

            @Override
            public void showMessagesAndWarnings() {
            }
        };

        viewFactory = new GlobalViewFactory() {

            @Override
            public Enumeration<ViewSpecification> availableViews(final ViewRequirement viewRequirement) {
                throw new UnexpectedCallException();
            }

            @Override
            public View createDialog(final Content content) {
                throw new UnexpectedCallException();
            }

            @Override
            public DragEvent createDragContentOutline(final View view, final Location location) {
                throw new UnexpectedCallException();
            }

            @Override
            public View createDragViewOutline(final View view) {
                throw new UnexpectedCallException();
            }

            @Override
            public View createMinimizedView(final View view) {
                throw new UnexpectedCallException();
            }

            @Override
            public View createView(final ViewRequirement requirement) {
                return new DummyView();
            }

            @Override
            public void debugData(final DebugBuilder debug) {
            }

            @Override
            public String debugTitle() {
                throw new UnexpectedCallException();
            }

            @Override
            public void addSpecification(final ViewSpecification spec) {
            }

            @Override
            public Enumeration<ViewSpecification> availableDesigns(final ViewRequirement viewRequirement) {
                return null;
            }

        };
    }
}
