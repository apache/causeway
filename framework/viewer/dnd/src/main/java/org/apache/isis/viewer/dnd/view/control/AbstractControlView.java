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

package org.apache.isis.viewer.dnd.view.control;

import java.awt.event.KeyEvent;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.runtime.userprofile.Options;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Padding;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ContentDrag;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.DragStart;
import org.apache.isis.viewer.dnd.view.Feedback;
import org.apache.isis.viewer.dnd.view.FocusManager;
import org.apache.isis.viewer.dnd.view.InternalDrag;
import org.apache.isis.viewer.dnd.view.KeyboardAction;
import org.apache.isis.viewer.dnd.view.Placement;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserAction;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.ViewDrag;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.ViewState;
import org.apache.isis.viewer.dnd.view.Viewer;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.Layout;

public abstract class AbstractControlView implements View {
    protected final UserAction action;
    private final View parent;
    private int width;
    private int x;
    private int y;
    private int height;
    private boolean isOver;
    private boolean isPressed;

    public AbstractControlView(final UserAction action, final View target) {
        this.action = action;
        this.parent = target;
    }

    @Override
    public Axes getViewAxes() {
        return new Axes();
    }

    @Override
    public void addView(final View view) {
    }

    @Override
    public Consent canChangeValue() {
        return Veto.DEFAULT;
    }

    @Override
    public boolean canFocus() {
        return action.disabled(parent).isAllowed();
    }

    @Override
    public boolean contains(final View view) {
        return false;
    }

    @Override
    public boolean containsFocus() {
        return false;
    }

    @Override
    public void contentMenuOptions(final UserActionSet menuOptions) {
    }

    @Override
    public void debug(final DebugBuilder debug) {
    }

    @Override
    public void debugStructure(final DebugBuilder b) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void drag(final ContentDrag contentDrag) {
    }

    @Override
    public void drag(final InternalDrag drag) {
    }

    @Override
    public void drag(final ViewDrag drag) {
    }

    @Override
    public void dragCancel(final InternalDrag drag) {
    }

    @Override
    public View dragFrom(final Location location) {
        return null;
    }

    @Override
    public void dragIn(final ContentDrag drag) {
    }

    @Override
    public void dragOut(final ContentDrag drag) {
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        return null;
    }

    @Override
    public void dragTo(final InternalDrag drag) {
    }

    @Override
    public void draw(final Canvas canvas) {
    }

    @Override
    public void drop(final ContentDrag drag) {
    }

    @Override
    public void drop(final ViewDrag drag) {
        getParent().drop(drag);
    }

    @Override
    public void editComplete(final boolean moveFocus, final boolean toNextField) {
    }

    @Override
    public void entered() {
        final View target = getParent();
        final Consent consent = action.disabled(target);
        final String actionText = action.getName(target) + " - " + (consent.isVetoed() ? consent.getReason() : action.getDescription(target));
        getFeedbackManager().setAction(actionText);

        isOver = true;
        isPressed = false;
        markDamaged();
    }

    @Override
    public void exited() {
        getFeedbackManager().clearAction();
        isOver = false;
        isPressed = false;
        markDamaged();
    }

    public void exitedSubview() {
    }

    @Override
    public void firstClick(final Click click) {
        executeAction();
    }

    private void executeAction() {
        final View target = getParent().getView();
        if (action.disabled(target).isAllowed()) {
            markDamaged();
            getViewManager().saveCurrentFieldEntry();
            action.execute(target.getWorkspace(), target, getLocation());
        }
    }

    @Override
    public void focusLost() {
    }

    @Override
    public void focusReceived() {
    }

    @Override
    public Location getAbsoluteLocation() {
        final Location location = parent.getAbsoluteLocation();
        getViewManager().getSpy().addTrace(this, "parent location", location);
        location.add(x, y);
        getViewManager().getSpy().addTrace(this, "plus view's location", location);
        final Padding pad = parent.getPadding();
        location.add(pad.getLeft(), pad.getTop());
        getViewManager().getSpy().addTrace(this, "plus view's padding", location);
        return location;
    }

    public boolean isOver() {
        return isOver;
    }

    public boolean isPressed() {
        return isPressed;
    }

    @Override
    public int getBaseline() {
        return 0;
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }

    @Override
    public Content getContent() {
        return null;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public FocusManager getFocusManager() {
        return getParent() == null ? null : getParent().getFocusManager();
    }

    @Override
    public Location getLocation() {
        return new Location(x, y);
    }

    @Override
    public Padding getPadding() {
        return null;
    }

    @Override
    public View getParent() {
        return parent;
    }

    @Override
    public Size getSize() {
        return new Size(width, height);
    }

    @Override
    public ViewSpecification getSpecification() {
        return null;
    }

    @Override
    public ViewState getState() {
        return null;
    }

    @Override
    public View[] getSubviews() {
        return new View[0];
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public Viewer getViewManager() {
        return Toolkit.getViewer();
    }

    @Override
    public Feedback getFeedbackManager() {
        return Toolkit.getFeedbackManager();
    }

    @Override
    public Workspace getWorkspace() {
        return null;
    }

    @Override
    public boolean hasFocus() {
        return getViewManager().hasFocus(getView());
    }

    @Override
    public View identify(final Location location) {
        return this;
    }

    @Override
    public void invalidateContent() {
    }

    @Override
    public void invalidateLayout() {
    }

    @Override
    public void keyPressed(final KeyboardAction key) {
        if (key.getKeyCode() == KeyEvent.VK_ENTER) {
            executeAction();
        }
    }

    @Override
    public void keyReleased(final KeyboardAction action) {
    }

    @Override
    public void keyTyped(final KeyboardAction action) {
    }

    @Override
    public void layout() {
    }

    @Override
    public void limitBoundsWithin(final Size size) {
    }

    @Override
    public void markDamaged() {
        markDamaged(getView().getBounds());
    }

    @Override
    public void markDamaged(final Bounds bounds) {
        if (parent == null) {
            getViewManager().markDamaged(bounds);
        } else {
            final Location pos = parent.getLocation();
            bounds.translate(pos.getX(), pos.getY());
            parent.markDamaged(bounds);
        }
    }

    @Override
    public void mouseDown(final Click click) {
        final View target = getParent().getView();
        if (action.disabled(target).isAllowed()) {
            markDamaged();
            getViewManager().saveCurrentFieldEntry();
            // action.execute(target.getWorkspace(), target, getLocation());
        }
        final boolean vetoed = action.disabled(target).isVetoed();
        if (!vetoed) {
            isPressed = true;
            markDamaged();
        }
    }

    @Override
    public void mouseUp(final Click click) {
        final View target = getParent().getView();
        final boolean vetoed = action.disabled(target).isVetoed();
        if (!vetoed) {
            isPressed = false;
            markDamaged();
        }
    }

    @Override
    public void mouseMoved(final Location location) {
    }

    @Override
    public void objectActionResult(final ObjectAdapter result, final Placement placement) {
    }

    @Override
    public View pickupContent(final Location location) {
        return null;
    }

    @Override
    public View pickupView(final Location location) {
        return null;
    }

    @Override
    public void print(final Canvas canvas) {
    }

    @Override
    public void refresh() {
    }

    @Override
    public void removeView(final View view) {
    }

    @Override
    public void replaceView(final View toReplace, final View replacement) {
    }

    @Override
    public void secondClick(final Click click) {
    }

    @Override
    public void setBounds(final Bounds bounds) {
    }

    @Override
    public void setFocusManager(final FocusManager focusManager) {
    }

    public void setLayout(final Layout layout) {
    }

    @Override
    public void setLocation(final Location point) {
        x = point.getX();
        y = point.getY();
    }

    @Override
    public void setParent(final View view) {
    }

    public void setMaximumSize(final Size size) {
    }

    @Override
    public void setSize(final Size size) {
        width = size.getWidth();
        height = size.getHeight();
    }

    @Override
    public void setView(final View view) {
    }

    @Override
    public View subviewFor(final Location location) {
        return null;
    }

    @Override
    public void thirdClick(final Click click) {
    }

    @Override
    public void update(final ObjectAdapter object) {
    }

    @Override
    public void updateView() {
    }

    @Override
    public ViewAreaType viewAreaType(final Location mouseLocation) {
        return null;
    }

    @Override
    public void viewMenuOptions(final UserActionSet menuOptions) {
    }

    @Override
    public void loadOptions(final Options viewOptions) {
    }

    @Override
    public void saveOptions(final Options viewOptions) {
    }

}
