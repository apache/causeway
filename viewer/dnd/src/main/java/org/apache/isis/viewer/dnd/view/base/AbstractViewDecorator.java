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


package org.apache.isis.viewer.dnd.view.base;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.runtimes.dflt.runtime.userprofile.Options;
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
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.ViewDrag;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.ViewState;
import org.apache.isis.viewer.dnd.view.Viewer;
import org.apache.isis.viewer.dnd.view.Workspace;


public abstract class AbstractViewDecorator implements View {
    protected View wrappedView;

    protected AbstractViewDecorator(final View wrappedView) {
        this.wrappedView = wrappedView;
        wrappedView.setView(this);
    }

    public void addView(final View view) {
        wrappedView.addView(view);
    }
    
    public Consent canChangeValue() {
        return wrappedView.canChangeValue();
    }

    public boolean canFocus() {
        return wrappedView.canFocus();
    }

    public boolean contains(final View view) {
        return wrappedView.contains(view);
    }

    public boolean containsFocus() {
        return wrappedView.containsFocus();
    }

    public void contentMenuOptions(final UserActionSet menuOptions) {
        wrappedView.contentMenuOptions(menuOptions);
    }

    public final void debug(final DebugBuilder debug) {
        debug.append("Decorator: ");
        debug.indent();
        debugDetails(debug);
        debug.appendln("required size", getRequiredSize(Size.createMax()));
        debug.appendln("given size", getSize());
        debug.appendln("padding", getPadding());
        debug.appendln("baseline", getBaseline());

        debug.appendln();
        debug.unindent();
        wrappedView.debug(debug);
    }

    protected void debugDetails(final DebugBuilder debug) {
        final String name = getClass().getName();
        debug.appendln(name.substring(name.lastIndexOf('.') + 1));
    }

    public void debugStructure(final DebugBuilder debug) {
        wrappedView.debugStructure(debug);
    }

    public void dispose() {
        wrappedView.dispose();
    }

    public void drag(final ContentDrag contentDrag) {
        wrappedView.drag(contentDrag);
    }

    public void drag(final InternalDrag drag) {
        wrappedView.drag(drag);
    }
    
    public void drag(ViewDrag drag) {
        wrappedView.drag(drag);
    }

    public void dragCancel(final InternalDrag drag) {
        wrappedView.dragCancel(drag);
    }

    public View dragFrom(final Location location) {
        return wrappedView.dragFrom(location);
    }

    public void dragIn(final ContentDrag drag) {
        wrappedView.dragIn(drag);
    }

    public void dragOut(final ContentDrag drag) {
        wrappedView.dragOut(drag);
    }

    public DragEvent dragStart(final DragStart drag) {
        return wrappedView.dragStart(drag);
    }

    public void dragTo(final InternalDrag drag) {
        wrappedView.dragTo(drag);
    }

    public void draw(final Canvas canvas) {
        wrappedView.draw(canvas);
    }

    public void drop(final ContentDrag drag) {
        wrappedView.drop(drag);
    }

    public void drop(final ViewDrag drag) {
        wrappedView.drop(drag);
    }

    public void editComplete(boolean moveFocus, boolean toNextField) {
        wrappedView.editComplete(moveFocus, toNextField);
    }

    public void entered() {
        wrappedView.entered();
    }

    public void exited() {
        wrappedView.exited();
    }

    public void firstClick(final Click click) {
        wrappedView.firstClick(click);
    }

    public void focusLost() {
        wrappedView.focusLost();
    }

    public void focusReceived() {
        wrappedView.focusReceived();
    }

    public Location getAbsoluteLocation() {
        return wrappedView.getAbsoluteLocation();
    }

    public int getBaseline() {
        return wrappedView.getBaseline();
    }

    public Bounds getBounds() {
        return new Bounds(getLocation(), getSize());
    }

    public Content getContent() {
        return wrappedView.getContent();
    }

    public FocusManager getFocusManager() {
        return wrappedView.getFocusManager();
    }

    public int getId() {
        return wrappedView.getId();
    }

    public Location getLocation() {
        return wrappedView.getLocation();
    }

    public Padding getPadding() {
        return wrappedView.getPadding();
    }

    public View getParent() {
        return wrappedView.getParent();
    }

    public Size getRequiredSize(Size availableSpace) {
        return wrappedView.getRequiredSize(availableSpace);
    }

    public Size getSize() {
        return wrappedView.getSize();
    }

    public ViewSpecification getSpecification() {
        return wrappedView.getSpecification();
    }

    public ViewState getState() {
        return wrappedView.getState();
    }

    public View[] getSubviews() {
        return wrappedView.getSubviews();
    }

    public View getView() {
        return wrappedView.getView();
    }

    public Axes getViewAxes() {
        return wrappedView.getViewAxes();
    }

    public Viewer getViewManager() {
        return wrappedView.getViewManager();
    }

    public Feedback getFeedbackManager() {
        return wrappedView.getFeedbackManager();
    }

    public Workspace getWorkspace() {
        return wrappedView.getWorkspace();
    }

    public boolean hasFocus() {
        return wrappedView.hasFocus();
    }

    public View identify(final Location mouseLocation) {
        return wrappedView.identify(mouseLocation);
    }

    public void invalidateContent() {
        wrappedView.invalidateContent();
    }

    public void invalidateLayout() {
        wrappedView.invalidateLayout();
    }

    public void keyPressed(final KeyboardAction key) {
        wrappedView.keyPressed(key);
    }

    public void keyReleased(KeyboardAction action) {
        wrappedView.keyReleased(action);
    }

    public void keyTyped(KeyboardAction action) {
        wrappedView.keyTyped(action);
    }

    public void layout() {
        wrappedView.layout();
    }

    public void limitBoundsWithin(final Size size) {
        wrappedView.limitBoundsWithin(size);
    }

    public void markDamaged() {
        wrappedView.markDamaged();
    }

    public void markDamaged(final Bounds bounds) {
        wrappedView.markDamaged(bounds);
    }

    public void mouseDown(final Click click) {
        wrappedView.mouseDown(click);
    }

    public void mouseMoved(final Location at) {
        wrappedView.mouseMoved(at);
    }

    public void mouseUp(final Click click) {
        wrappedView.mouseUp(click);
    }

    public void objectActionResult(final ObjectAdapter result, final Placement placement) {
        wrappedView.objectActionResult(result, placement);
    }

    public View pickupContent(final Location location) {
        return wrappedView.pickupContent(location);
    }

    public View pickupView(final Location location) {
        return wrappedView.pickupView(location);
    }

    public void print(final Canvas canvas) {
        wrappedView.print(canvas);
    }

    public void refresh() {
        wrappedView.refresh();
    }

    public void removeView(final View view) {
        wrappedView.removeView(view);
    }

    public void replaceView(final View toReplace, final View replacement) {
        wrappedView.replaceView(toReplace, replacement);
    }
    
    protected void replaceWrappedView(final View withReplacement) {
        View root = getView();
        View parent = getParent();
        parent.markDamaged();
        getViewManager().removeFromNotificationList(root);
        for (View view : root.getSubviews()) {
            view.dispose();
        } 
        wrappedView = withReplacement;
        wrappedView.setView(root);
        wrappedView.setParent(parent);
        getViewManager().addToNotificationList(withReplacement);
    
        wrappedView.invalidateContent();
    }
    
    public void secondClick(final Click click) {
        wrappedView.secondClick(click);
    }

    public void setBounds(final Bounds bounds) {
        wrappedView.setBounds(bounds);
    }

    public void setFocusManager(final FocusManager focusManager) {
        wrappedView.setFocusManager(focusManager);
    }
    
    public void setLocation(final Location point) {
        wrappedView.setLocation(point);
    }

    public void setParent(final View view) {
        wrappedView.setParent(view);
    }

    public void setSize(final Size size) {
        wrappedView.setSize(size);
    }

    public void setView(final View view) {
        wrappedView.setView(view);
    }

    public View subviewFor(final Location location) {
        return wrappedView.subviewFor(location);
    }

    public void thirdClick(final Click click) {
        wrappedView.thirdClick(click);
    }

    @Override
    public String toString() {
        final String name = getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1) + "/" + wrappedView;
    }

    public void update(final ObjectAdapter object) {
        wrappedView.update(object);
    }

    public void updateView() {
        wrappedView.updateView();
    }

    public ViewAreaType viewAreaType(final Location mouseLocation) {
        return wrappedView.viewAreaType(mouseLocation);
    }

    public void viewMenuOptions(final UserActionSet menuOptions) {
        wrappedView.viewMenuOptions(menuOptions);
    }
    
    public void loadOptions(Options viewOptions) {
        wrappedView.loadOptions(viewOptions);
    }

    public void saveOptions(Options viewOptions) {
        wrappedView.saveOptions(viewOptions);
    }
}
