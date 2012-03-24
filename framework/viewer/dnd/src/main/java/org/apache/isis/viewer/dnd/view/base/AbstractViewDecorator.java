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

    @Override
    public void addView(final View view) {
        wrappedView.addView(view);
    }

    @Override
    public Consent canChangeValue() {
        return wrappedView.canChangeValue();
    }

    @Override
    public boolean canFocus() {
        return wrappedView.canFocus();
    }

    @Override
    public boolean contains(final View view) {
        return wrappedView.contains(view);
    }

    @Override
    public boolean containsFocus() {
        return wrappedView.containsFocus();
    }

    @Override
    public void contentMenuOptions(final UserActionSet menuOptions) {
        wrappedView.contentMenuOptions(menuOptions);
    }

    @Override
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

    @Override
    public void debugStructure(final DebugBuilder debug) {
        wrappedView.debugStructure(debug);
    }

    @Override
    public void dispose() {
        wrappedView.dispose();
    }

    @Override
    public void drag(final ContentDrag contentDrag) {
        wrappedView.drag(contentDrag);
    }

    @Override
    public void drag(final InternalDrag drag) {
        wrappedView.drag(drag);
    }

    @Override
    public void drag(final ViewDrag drag) {
        wrappedView.drag(drag);
    }

    @Override
    public void dragCancel(final InternalDrag drag) {
        wrappedView.dragCancel(drag);
    }

    @Override
    public View dragFrom(final Location location) {
        return wrappedView.dragFrom(location);
    }

    @Override
    public void dragIn(final ContentDrag drag) {
        wrappedView.dragIn(drag);
    }

    @Override
    public void dragOut(final ContentDrag drag) {
        wrappedView.dragOut(drag);
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        return wrappedView.dragStart(drag);
    }

    @Override
    public void dragTo(final InternalDrag drag) {
        wrappedView.dragTo(drag);
    }

    @Override
    public void draw(final Canvas canvas) {
        wrappedView.draw(canvas);
    }

    @Override
    public void drop(final ContentDrag drag) {
        wrappedView.drop(drag);
    }

    @Override
    public void drop(final ViewDrag drag) {
        wrappedView.drop(drag);
    }

    @Override
    public void editComplete(final boolean moveFocus, final boolean toNextField) {
        wrappedView.editComplete(moveFocus, toNextField);
    }

    @Override
    public void entered() {
        wrappedView.entered();
    }

    @Override
    public void exited() {
        wrappedView.exited();
    }

    @Override
    public void firstClick(final Click click) {
        wrappedView.firstClick(click);
    }

    @Override
    public void focusLost() {
        wrappedView.focusLost();
    }

    @Override
    public void focusReceived() {
        wrappedView.focusReceived();
    }

    @Override
    public Location getAbsoluteLocation() {
        return wrappedView.getAbsoluteLocation();
    }

    @Override
    public int getBaseline() {
        return wrappedView.getBaseline();
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(getLocation(), getSize());
    }

    @Override
    public Content getContent() {
        return wrappedView.getContent();
    }

    @Override
    public FocusManager getFocusManager() {
        return wrappedView.getFocusManager();
    }

    @Override
    public int getId() {
        return wrappedView.getId();
    }

    @Override
    public Location getLocation() {
        return wrappedView.getLocation();
    }

    @Override
    public Padding getPadding() {
        return wrappedView.getPadding();
    }

    @Override
    public View getParent() {
        return wrappedView.getParent();
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        return wrappedView.getRequiredSize(availableSpace);
    }

    @Override
    public Size getSize() {
        return wrappedView.getSize();
    }

    @Override
    public ViewSpecification getSpecification() {
        return wrappedView.getSpecification();
    }

    @Override
    public ViewState getState() {
        return wrappedView.getState();
    }

    @Override
    public View[] getSubviews() {
        return wrappedView.getSubviews();
    }

    @Override
    public View getView() {
        return wrappedView.getView();
    }

    @Override
    public Axes getViewAxes() {
        return wrappedView.getViewAxes();
    }

    @Override
    public Viewer getViewManager() {
        return wrappedView.getViewManager();
    }

    @Override
    public Feedback getFeedbackManager() {
        return wrappedView.getFeedbackManager();
    }

    @Override
    public Workspace getWorkspace() {
        return wrappedView.getWorkspace();
    }

    @Override
    public boolean hasFocus() {
        return wrappedView.hasFocus();
    }

    @Override
    public View identify(final Location mouseLocation) {
        return wrappedView.identify(mouseLocation);
    }

    @Override
    public void invalidateContent() {
        wrappedView.invalidateContent();
    }

    @Override
    public void invalidateLayout() {
        wrappedView.invalidateLayout();
    }

    @Override
    public void keyPressed(final KeyboardAction key) {
        wrappedView.keyPressed(key);
    }

    @Override
    public void keyReleased(final KeyboardAction action) {
        wrappedView.keyReleased(action);
    }

    @Override
    public void keyTyped(final KeyboardAction action) {
        wrappedView.keyTyped(action);
    }

    @Override
    public void layout() {
        wrappedView.layout();
    }

    @Override
    public void limitBoundsWithin(final Size size) {
        wrappedView.limitBoundsWithin(size);
    }

    @Override
    public void markDamaged() {
        wrappedView.markDamaged();
    }

    @Override
    public void markDamaged(final Bounds bounds) {
        wrappedView.markDamaged(bounds);
    }

    @Override
    public void mouseDown(final Click click) {
        wrappedView.mouseDown(click);
    }

    @Override
    public void mouseMoved(final Location at) {
        wrappedView.mouseMoved(at);
    }

    @Override
    public void mouseUp(final Click click) {
        wrappedView.mouseUp(click);
    }

    @Override
    public void objectActionResult(final ObjectAdapter result, final Placement placement) {
        wrappedView.objectActionResult(result, placement);
    }

    @Override
    public View pickupContent(final Location location) {
        return wrappedView.pickupContent(location);
    }

    @Override
    public View pickupView(final Location location) {
        return wrappedView.pickupView(location);
    }

    @Override
    public void print(final Canvas canvas) {
        wrappedView.print(canvas);
    }

    @Override
    public void refresh() {
        wrappedView.refresh();
    }

    @Override
    public void removeView(final View view) {
        wrappedView.removeView(view);
    }

    @Override
    public void replaceView(final View toReplace, final View replacement) {
        wrappedView.replaceView(toReplace, replacement);
    }

    protected void replaceWrappedView(final View withReplacement) {
        final View root = getView();
        final View parent = getParent();
        parent.markDamaged();
        getViewManager().removeFromNotificationList(root);
        for (final View view : root.getSubviews()) {
            view.dispose();
        }
        wrappedView = withReplacement;
        wrappedView.setView(root);
        wrappedView.setParent(parent);
        getViewManager().addToNotificationList(withReplacement);

        wrappedView.invalidateContent();
    }

    @Override
    public void secondClick(final Click click) {
        wrappedView.secondClick(click);
    }

    @Override
    public void setBounds(final Bounds bounds) {
        wrappedView.setBounds(bounds);
    }

    @Override
    public void setFocusManager(final FocusManager focusManager) {
        wrappedView.setFocusManager(focusManager);
    }

    @Override
    public void setLocation(final Location point) {
        wrappedView.setLocation(point);
    }

    @Override
    public void setParent(final View view) {
        wrappedView.setParent(view);
    }

    @Override
    public void setSize(final Size size) {
        wrappedView.setSize(size);
    }

    @Override
    public void setView(final View view) {
        wrappedView.setView(view);
    }

    @Override
    public View subviewFor(final Location location) {
        return wrappedView.subviewFor(location);
    }

    @Override
    public void thirdClick(final Click click) {
        wrappedView.thirdClick(click);
    }

    @Override
    public String toString() {
        final String name = getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1) + "/" + wrappedView;
    }

    @Override
    public void update(final ObjectAdapter object) {
        wrappedView.update(object);
    }

    @Override
    public void updateView() {
        wrappedView.updateView();
    }

    @Override
    public ViewAreaType viewAreaType(final Location mouseLocation) {
        return wrappedView.viewAreaType(mouseLocation);
    }

    @Override
    public void viewMenuOptions(final UserActionSet menuOptions) {
        wrappedView.viewMenuOptions(menuOptions);
    }

    @Override
    public void loadOptions(final Options viewOptions) {
        wrappedView.loadOptions(viewOptions);
    }

    @Override
    public void saveOptions(final Options viewOptions) {
        wrappedView.saveOptions(viewOptions);
    }
}
