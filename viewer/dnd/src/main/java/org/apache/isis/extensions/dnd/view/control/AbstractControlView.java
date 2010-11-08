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


package org.apache.isis.extensions.dnd.view.control;

import java.awt.event.KeyEvent;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.Veto;
import org.apache.isis.extensions.dnd.drawing.Bounds;
import org.apache.isis.extensions.dnd.drawing.Canvas;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.drawing.Padding;
import org.apache.isis.extensions.dnd.drawing.Size;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Click;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.ContentDrag;
import org.apache.isis.extensions.dnd.view.DragEvent;
import org.apache.isis.extensions.dnd.view.DragStart;
import org.apache.isis.extensions.dnd.view.Feedback;
import org.apache.isis.extensions.dnd.view.FocusManager;
import org.apache.isis.extensions.dnd.view.InternalDrag;
import org.apache.isis.extensions.dnd.view.KeyboardAction;
import org.apache.isis.extensions.dnd.view.Placement;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.UserAction;
import org.apache.isis.extensions.dnd.view.UserActionSet;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewAreaType;
import org.apache.isis.extensions.dnd.view.ViewDrag;
import org.apache.isis.extensions.dnd.view.ViewSpecification;
import org.apache.isis.extensions.dnd.view.ViewState;
import org.apache.isis.extensions.dnd.view.Viewer;
import org.apache.isis.extensions.dnd.view.Workspace;
import org.apache.isis.extensions.dnd.view.base.Layout;
import org.apache.isis.runtime.userprofile.Options;


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
     
    public Axes getViewAxes() {
        return new Axes();
    }
    
    public void addView(final View view) {}

    public Consent canChangeValue() {
        return Veto.DEFAULT;
    }

    public boolean canFocus() {
        return action.disabled(parent).isAllowed();
    }

    public boolean contains(final View view) {
        return false;
    }

    public boolean containsFocus() {
        return false;
    }

    public void contentMenuOptions(final UserActionSet menuOptions) {}

    public void debug(final DebugString debug) {}

    public void debugStructure(final DebugString b) {}

    public void dispose() {}

    public void drag(final ContentDrag contentDrag) {}

    public void drag(final InternalDrag drag) {}
    
    public void drag(ViewDrag drag) {}

    public void dragCancel(final InternalDrag drag) {}

    public View dragFrom(final Location location) {
        return null;
    }

    public void dragIn(final ContentDrag drag) {}

    public void dragOut(final ContentDrag drag) {}

    public DragEvent dragStart(final DragStart drag) {
        return null;
    }

    public void dragTo(final InternalDrag drag) {}

    public void draw(final Canvas canvas) {}

    public void drop(final ContentDrag drag) {}

    public void drop(final ViewDrag drag) {
        getParent().drop(drag);
    }

    public void editComplete(boolean moveFocus, boolean toNextField) {}

    public void entered() {
        final View target = getParent();
        final Consent consent = action.disabled(target);
        String actionText = action.getName(target) + " - "
                + (consent.isVetoed() ? consent.getReason() : action.getDescription(target));
        getFeedbackManager().setAction(actionText);

        isOver = true;
        isPressed = false;
        markDamaged();
    }

    public void exited() {
        getFeedbackManager().clearAction();
        isOver = false;
        isPressed = false;
        markDamaged();
    }

    public void exitedSubview() {}

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

    public void focusLost() {}

    public void focusReceived() {}

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

    public int getBaseline() {
        return 0;
    }

    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }

    public Content getContent() {
        return null;
    }

    public int getId() {
        return 0;
    }

    public FocusManager getFocusManager() {
        return getParent() == null ? null : getParent().getFocusManager();
    }

    public Location getLocation() {
        return new Location(x, y);
    }

    public Padding getPadding() {
        return null;
    }

    public View getParent() {
        return parent;
    }

    public Size getSize() {
        return new Size(width, height);
    }

    public ViewSpecification getSpecification() {
        return null;
    }

    public ViewState getState() {
        return null;
    }

    public View[] getSubviews() {
        return new View[0];
    }

    public View getView() {
        return this;
    }

    public Viewer getViewManager() {
        return Toolkit.getViewer();
    }

    public Feedback getFeedbackManager() {
        return Toolkit.getFeedbackManager();
    }

    public Workspace getWorkspace() {
        return null;
    }

    public boolean hasFocus() {
        return getViewManager().hasFocus(getView());
    }

    public View identify(final Location location) {
        return this;
    }

    public void invalidateContent() {}

    public void invalidateLayout() {}

    public void keyPressed(final KeyboardAction key) {
        if (key.getKeyCode() == KeyEvent.VK_ENTER) {
            executeAction();
        }
    }

    public void keyReleased(KeyboardAction action) {}

    public void keyTyped(KeyboardAction action) {}

    public void layout() {}

    public void limitBoundsWithin(final Size size) {}

    public void markDamaged() {
        markDamaged(getView().getBounds());
    }

    public void markDamaged(final Bounds bounds) {
        if (parent == null) {
            getViewManager().markDamaged(bounds);
        } else {
            final Location pos = parent.getLocation();
            bounds.translate(pos.getX(), pos.getY());
            parent.markDamaged(bounds);
        }
    }

    public void mouseDown(final Click click) {
        final View target = getParent().getView();
        if (action.disabled(target).isAllowed()) {
            markDamaged();
            getViewManager().saveCurrentFieldEntry();
            //action.execute(target.getWorkspace(), target, getLocation());
        }
        boolean vetoed = action.disabled(target).isVetoed();
        if (!vetoed) {
            isPressed = true;
            markDamaged();
        }
    }

    public void mouseUp(final Click click) {
        final View target = getParent().getView();
        boolean vetoed = action.disabled(target).isVetoed();
        if (!vetoed) {
            isPressed = false;
            markDamaged();
        }
    }

    public void mouseMoved(final Location location) {}

    public void objectActionResult(final ObjectAdapter result, final Placement placement) {}

    public View pickupContent(final Location location) {
        return null;
    }

    public View pickupView(final Location location) {
        return null;
    }

    public void print(final Canvas canvas) {}

    public void refresh() {}

    public void removeView(final View view) {}

    public void replaceView(final View toReplace, final View replacement) {}

    public void secondClick(final Click click) {}

    public void setBounds(final Bounds bounds) {}

    public void setFocusManager(final FocusManager focusManager) {}
    
    public void setLayout(Layout layout) {}

    public void setLocation(final Location point) {
        x = point.getX();
        y = point.getY();
    }

    public void setParent(final View view) {}

    public void setMaximumSize(final Size size) {}

    public void setSize(final Size size) {
        width = size.getWidth();
        height = size.getHeight();
    }

    public void setView(final View view) {}

    public View subviewFor(final Location location) {
        return null;
    }

    public void thirdClick(final Click click) {}

    public void update(final ObjectAdapter object) {}

    public void updateView() {}

    public ViewAreaType viewAreaType(final Location mouseLocation) {
        return null;
    }

    public void viewMenuOptions(final UserActionSet menuOptions) {}
    
    public void loadOptions(Options viewOptions) {}
    
    public void saveOptions(Options viewOptions) {}

}
