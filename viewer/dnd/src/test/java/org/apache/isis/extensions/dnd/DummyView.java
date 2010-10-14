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

import java.util.Vector;

import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.commons.exceptions.NotYetImplementedException;
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
    private ViewState state = new ViewState();
    private final Vector subviews = new Vector();
    private boolean allowSubviewsToBeAdded;
    private Axes axes = new Axes();

    public DummyView() {
        setView(this);
    }
    
    public DummyView(int width, int height) {
        this();
        setupRequiredSize(new Size(width, height));
    }

    public Size getRequiredSize(Size availableSpace) {
        return new Size(requiredSize);
    }

    public Consent canChangeValue() {
        return Veto.DEFAULT;
    }

    public boolean canFocus() {
        return false;
    }

    public boolean contains(final View view) {
        return false;
    }

    public boolean containsFocus() {
        return false;
    }

    public void contentMenuOptions(final UserActionSet menuOptions) {
        throw new NotYetImplementedException();
    }

    public void debug(final DebugString debug) {
        throw new NotYetImplementedException();
    }

    public void debugStructure(final DebugString b) {
        throw new NotYetImplementedException();
    }

    public void dispose() {
        Workspace workspace = getWorkspace();
        if (workspace != null) {
            workspace.removeView(this);
        }
    }

    public void drag(final InternalDrag drag) {
        throw new NotYetImplementedException();
    }

    public void drag(ViewDrag drag) {
        throw new NotYetImplementedException();
    }
    
    public void dragCancel(final InternalDrag drag) {
        throw new NotYetImplementedException();
    }

    public View dragFrom(final Location location) {
        throw new NotYetImplementedException();
    }

    public void drag(final ContentDrag contentDrag) {
        throw new NotYetImplementedException();
    }

    public void dragIn(final ContentDrag drag) {
        throw new NotYetImplementedException();
    }

    public void dragOut(final ContentDrag drag) {
        throw new NotYetImplementedException();
    }

    public DragEvent dragStart(final DragStart drag) {
        throw new NotYetImplementedException();
    }

    public void dragTo(final InternalDrag drag) {
        throw new NotYetImplementedException();
    }

    public void draw(final Canvas canvas) {
        throw new NotYetImplementedException();
    }

    public void drop(final ContentDrag drag) {
        throw new NotYetImplementedException();
    }

    public void drop(final ViewDrag drag) {
        throw new NotYetImplementedException();
    }

    public void editComplete(boolean moveFocus, boolean toNextField) {
        throw new NotYetImplementedException();
    }

    public void entered() {
        throw new NotYetImplementedException();
    }

    public void exited() {
        throw new NotYetImplementedException();
    }

    public void firstClick(final Click click) {
        throw new NotYetImplementedException();
    }

    public void focusLost() {
        throw new NotYetImplementedException();
    }

    public void focusReceived() {
        throw new NotYetImplementedException();
    }

    public Location getAbsoluteLocation() {
        return absoluteLocation;
    }

    public int getBaseline() {
        return 0;
    }

    public Bounds getBounds() {
        return new Bounds(location, size);
    }

    public Content getContent() {
        return content;
    }

    public FocusManager getFocusManager() {
        throw new NotYetImplementedException();
    }

    public int getId() {
        return 0;
    }

    public Location getLocation() {
        return location;
    }

    public Padding getPadding() {
        return new Padding();
    }

    public View getParent() {
        return parent;
    }

    public Size getSize() {
        return size;
    }

    public ViewSpecification getSpecification() {
        return specification;
    }

    public ViewState getState() {
        return state ;
    }
    
    public void addView(final View view) {
        if(allowSubviewsToBeAdded) {
            subviews.add(view);
        }  else {
            throw new IsisException("Can't add view. Do you need to set the allowSubviewsToBeAdded flag?");
        }
    }

    public void removeView(final View view) {
        if(allowSubviewsToBeAdded) {
            subviews.remove(view);
        }  else {
            throw new IsisException("Can't remove view. Do you need to set the allowSubviewsToBeAdded flag?");
        }
    }

    public View[] getSubviews() {
        return (View[]) subviews.toArray(new View[subviews.size()]);
    }

    public View getView() {
        return view;
    }

    public Viewer getViewManager() {
        throw new NotYetImplementedException();
    }

    public Feedback getFeedbackManager() {
        throw new NotYetImplementedException();
    }

    public Workspace getWorkspace() {
        return getParent() == null ? null : getParent().getWorkspace();
    }

    public boolean hasFocus() {
        return false;
    }

    public View identify(final Location mouseLocation) {
        throw new NotYetImplementedException();
    }

    public void invalidateContent() {
        invalidateContent++;
    }

    public void invalidateLayout() {
        invlidateLayout++;
    }

    public void keyPressed(final KeyboardAction key) {
        throw new NotYetImplementedException();
    }

    public void keyReleased(KeyboardAction action) {
        throw new NotYetImplementedException();
    }

    public void keyTyped(KeyboardAction action) {
        throw new NotYetImplementedException();
    }

    public void layout() {
    }

    public void limitBoundsWithin(final Size size) {
        throw new NotYetImplementedException();
    }

    public void markDamaged() {
        throw new NotYetImplementedException();
    }

    public void markDamaged(final Bounds bounds) {
    }

    public void mouseDown(final Click click) {
        throw new NotYetImplementedException();
    }

    public void mouseMoved(final Location location) {
        throw new NotYetImplementedException();
    }

    public void objectActionResult(final ObjectAdapter result, final Placement placement) {
        throw new NotYetImplementedException();
    }

    public View pickupContent(final Location location) {
        return null;
    }

    public View pickupView(final Location location) {
        throw new NotYetImplementedException();
    }

    public void print(final Canvas canvas) {
        throw new NotYetImplementedException();
    }

    public void refresh() {
        throw new NotYetImplementedException();
    }

    public void replaceView(final View toReplace, final View replacement) {
        throw new NotYetImplementedException();
    }

    public void secondClick(final Click click) {
        throw new NotYetImplementedException();
    }

    public void setBounds(final Bounds bounds) {
        throw new NotYetImplementedException();
    }

    public void setFocusManager(final FocusManager focusManager) {
        throw new NotYetImplementedException();
    }

    public void setLayout(Layout layout) {
        throw new NotYetImplementedException();
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    public void setParent(final View view) {
        parent = view.getView();
    }

    public void setSize(final Size size) {
        this.size = size;
    }

    public void setView(final View view) {
        this.view = view;
    }

    public View subviewFor(final Location location) {
        for (View view : getSubviews()) {
            if (view.getBounds().contains(location)) {
                return view;
            }
        }
        return null;
    }

    public void thirdClick(final Click click) {
        throw new NotYetImplementedException();
    }

    public void update(final ObjectAdapter object) {
        throw new NotYetImplementedException();
    }

    public void updateView() {
        throw new NotYetImplementedException();
    }

    public ViewAreaType viewAreaType(final Location mouseLocation) {
        throw new NotYetImplementedException();
    }

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

    public void setupSpecification(ViewSpecification specification) {
        this.specification = specification;
    }
    
    public void setupSubviews(View[] views) {
        for (View view : views) {
            subviews.add(view);
        }
    }

    public void mouseUp(Click click) {
        throw new NotYetImplementedException();
    }

    public Axes getViewAxes() {
        return axes;
    }

    public void setupAllowSubviewsToBeAdded(boolean allowSubviewsToBeAdded) {
        this.allowSubviewsToBeAdded = allowSubviewsToBeAdded;
    }

    public void loadOptions(Options viewOptions) {}
    
    public void saveOptions(Options viewOptions) {}
}
