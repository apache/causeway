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

import java.util.Vector;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
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
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.ViewDrag;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.ViewState;
import org.apache.isis.viewer.dnd.view.Viewer;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.Layout;

public class DummyView implements View {
    private Size requiredSize = new Size(100, 10);
    private Size size;
    private View parent;
    private View view;
    private Location location = new Location(0, 0);
    private Location absoluteLocation;
    private Content content;
    private ViewSpecification specification;
    public int invlidateLayout;
    public int invalidateContent;
    private final ViewState state = new ViewState();
    private final Vector<View> subviews = new Vector<View>();
    private boolean allowSubviewsToBeAdded;
    private final Axes axes = new Axes();

    public DummyView() {
        setView(this);
    }

    public DummyView(final int width, final int height) {
        this();
        setupRequiredSize(new Size(width, height));
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        return new Size(requiredSize);
    }

    @Override
    public Consent canChangeValue() {
        return Veto.DEFAULT;
    }

    @Override
    public boolean canFocus() {
        return false;
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
        throw new NotYetImplementedException();
    }

    @Override
    public void debug(final DebugBuilder debug) {
        throw new NotYetImplementedException();
    }

    @Override
    public void debugStructure(final DebugBuilder b) {
        throw new NotYetImplementedException();
    }

    @Override
    public void dispose() {
        final Workspace workspace = getWorkspace();
        if (workspace != null) {
            workspace.removeView(this);
        }
    }

    @Override
    public void drag(final InternalDrag drag) {
        throw new NotYetImplementedException();
    }

    @Override
    public void drag(final ViewDrag drag) {
        throw new NotYetImplementedException();
    }

    @Override
    public void dragCancel(final InternalDrag drag) {
        throw new NotYetImplementedException();
    }

    @Override
    public View dragFrom(final Location location) {
        throw new NotYetImplementedException();
    }

    @Override
    public void drag(final ContentDrag contentDrag) {
        throw new NotYetImplementedException();
    }

    @Override
    public void dragIn(final ContentDrag drag) {
        throw new NotYetImplementedException();
    }

    @Override
    public void dragOut(final ContentDrag drag) {
        throw new NotYetImplementedException();
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        throw new NotYetImplementedException();
    }

    @Override
    public void dragTo(final InternalDrag drag) {
        throw new NotYetImplementedException();
    }

    @Override
    public void draw(final Canvas canvas) {
        throw new NotYetImplementedException();
    }

    @Override
    public void drop(final ContentDrag drag) {
        throw new NotYetImplementedException();
    }

    @Override
    public void drop(final ViewDrag drag) {
        throw new NotYetImplementedException();
    }

    @Override
    public void editComplete(final boolean moveFocus, final boolean toNextField) {
        throw new NotYetImplementedException();
    }

    @Override
    public void entered() {
        throw new NotYetImplementedException();
    }

    @Override
    public void exited() {
        throw new NotYetImplementedException();
    }

    @Override
    public void firstClick(final Click click) {
        throw new NotYetImplementedException();
    }

    @Override
    public void focusLost() {
        throw new NotYetImplementedException();
    }

    @Override
    public void focusReceived() {
        throw new NotYetImplementedException();
    }

    @Override
    public Location getAbsoluteLocation() {
        return absoluteLocation;
    }

    @Override
    public int getBaseline() {
        return 0;
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(location, size);
    }

    @Override
    public Content getContent() {
        return content;
    }

    @Override
    public FocusManager getFocusManager() {
        throw new NotYetImplementedException();
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Padding getPadding() {
        return new Padding();
    }

    @Override
    public View getParent() {
        return parent;
    }

    @Override
    public Size getSize() {
        return size;
    }

    @Override
    public ViewSpecification getSpecification() {
        return specification;
    }

    @Override
    public ViewState getState() {
        return state;
    }

    @Override
    public void addView(final View view) {
        if (allowSubviewsToBeAdded) {
            subviews.add(view);
        } else {
            throw new IsisException("Can't add view. Do you need to set the allowSubviewsToBeAdded flag?");
        }
    }

    @Override
    public void removeView(final View view) {
        if (allowSubviewsToBeAdded) {
            subviews.remove(view);
        } else {
            throw new IsisException("Can't remove view. Do you need to set the allowSubviewsToBeAdded flag?");
        }
    }

    @Override
    public View[] getSubviews() {
        return subviews.toArray(new View[subviews.size()]);
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public Viewer getViewManager() {
        throw new NotYetImplementedException();
    }

    @Override
    public Feedback getFeedbackManager() {
        throw new NotYetImplementedException();
    }

    @Override
    public Workspace getWorkspace() {
        return getParent() == null ? null : getParent().getWorkspace();
    }

    @Override
    public boolean hasFocus() {
        return false;
    }

    @Override
    public View identify(final Location mouseLocation) {
        throw new NotYetImplementedException();
    }

    @Override
    public void invalidateContent() {
        invalidateContent++;
    }

    @Override
    public void invalidateLayout() {
        invlidateLayout++;
    }

    @Override
    public void keyPressed(final KeyboardAction key) {
        throw new NotYetImplementedException();
    }

    @Override
    public void keyReleased(final KeyboardAction action) {
        throw new NotYetImplementedException();
    }

    @Override
    public void keyTyped(final KeyboardAction action) {
        throw new NotYetImplementedException();
    }

    @Override
    public void layout() {
    }

    @Override
    public void limitBoundsWithin(final Size size) {
        throw new NotYetImplementedException();
    }

    @Override
    public void markDamaged() {
        throw new NotYetImplementedException();
    }

    @Override
    public void markDamaged(final Bounds bounds) {
    }

    @Override
    public void mouseDown(final Click click) {
        throw new NotYetImplementedException();
    }

    @Override
    public void mouseMoved(final Location location) {
        throw new NotYetImplementedException();
    }

    @Override
    public void objectActionResult(final ObjectAdapter result, final Placement placement) {
        throw new NotYetImplementedException();
    }

    @Override
    public View pickupContent(final Location location) {
        return null;
    }

    @Override
    public View pickupView(final Location location) {
        throw new NotYetImplementedException();
    }

    @Override
    public void print(final Canvas canvas) {
        throw new NotYetImplementedException();
    }

    @Override
    public void refresh() {
        throw new NotYetImplementedException();
    }

    @Override
    public void replaceView(final View toReplace, final View replacement) {
        throw new NotYetImplementedException();
    }

    @Override
    public void secondClick(final Click click) {
        throw new NotYetImplementedException();
    }

    @Override
    public void setBounds(final Bounds bounds) {
        throw new NotYetImplementedException();
    }

    @Override
    public void setFocusManager(final FocusManager focusManager) {
        throw new NotYetImplementedException();
    }

    public void setLayout(final Layout layout) {
        throw new NotYetImplementedException();
    }

    @Override
    public void setLocation(final Location location) {
        this.location = location;
    }

    @Override
    public void setParent(final View view) {
        parent = view.getView();
    }

    @Override
    public void setSize(final Size size) {
        this.size = size;
    }

    @Override
    public void setView(final View view) {
        this.view = view;
    }

    @Override
    public View subviewFor(final Location location) {
        for (final View view : getSubviews()) {
            if (view.getBounds().contains(location)) {
                return view;
            }
        }
        return null;
    }

    @Override
    public void thirdClick(final Click click) {
        throw new NotYetImplementedException();
    }

    @Override
    public void update(final ObjectAdapter object) {
        throw new NotYetImplementedException();
    }

    @Override
    public void updateView() {
        throw new NotYetImplementedException();
    }

    @Override
    public ViewAreaType viewAreaType(final Location mouseLocation) {
        throw new NotYetImplementedException();
    }

    @Override
    public void viewMenuOptions(final UserActionSet menuOptions) {
        throw new NotYetImplementedException();
    }

    public void setupLocation(final Location location) {
        this.location = location;
    }

    public void setupAbsoluteLocation(final Location location) {
        this.absoluteLocation = location;
    }

    public void setupContent(final Content content) {
        this.content = content;
    }

    public void setupRequiredSize(final Size size) {
        this.requiredSize = size;
    }

    public void setupSpecification(final ViewSpecification specification) {
        this.specification = specification;
    }

    public void setupSubviews(final View[] views) {
        for (final View view : views) {
            subviews.add(view);
        }
    }

    @Override
    public void mouseUp(final Click click) {
        throw new NotYetImplementedException();
    }

    @Override
    public Axes getViewAxes() {
        return axes;
    }

    public void setupAllowSubviewsToBeAdded(final boolean allowSubviewsToBeAdded) {
        this.allowSubviewsToBeAdded = allowSubviewsToBeAdded;
    }

    @Override
    public void loadOptions(final Options viewOptions) {
    }

    @Override
    public void saveOptions(final Options viewOptions) {
    }
}
