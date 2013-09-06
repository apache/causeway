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

package org.apache.isis.viewer.dnd.view.composite;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Padding;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.FocusManager;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.base.ObjectView;

public abstract class CompositeView extends ObjectView {
    private static final Logger LOG = LoggerFactory.getLogger(CompositeView.class);
    private int buildCount = 0;
    private boolean buildInvalid = true;
    private boolean canDragView = true;
    private int layoutCount = 0;
    private boolean layoutInvalid = true;
    protected Vector<View> views; // TODO make private
    private FocusManager focusManager;// = new SubviewFocusManager(this);

    public CompositeView(final Content content, final ViewSpecification specification) {
        super(content, specification);
        views = new Vector<View>();
    }

    @Override
    public void refresh() {
        final View views[] = getSubviews();
        for (final View view : views) {
            view.refresh();
        }
    }

    @Override
    public void addView(final View view) {
        add(views, view);
    }

    // TODO make private
    protected void add(final Vector<View> views, final View view) {
        LOG.debug("adding " + view + " to " + this);
        views.addElement(view);
        getViewManager().addToNotificationList(view);
        view.setParent(getView());
        invalidateLayout();
    }

    public boolean canDragView() {
        return canDragView;
    }

    @Override
    public void debugStructure(final DebugBuilder b) {
        b.appendln("Built", (buildInvalid ? "invalid, " : "") + buildCount + " builds");
        b.appendln("Laid out", (layoutInvalid ? "invalid, " : "") + layoutCount + " layouts");
        super.debugStructure(b);
    }

    @Override
    public void dispose() {
        disposeContentsOnly();
        super.dispose();
    }

    protected void disposeContentsOnly() {
        final View views[] = getSubviews();
        for (final View view : views) {
            view.dispose();
        }
    }

    @Override
    public void draw(final Canvas canvas) {
        final View views[] = getSubviews();
        for (final View subview : views) {
            final Bounds bounds = subview.getBounds();
            if (Toolkit.debug) {
                LOG.debug("compare: " + bounds + "  " + canvas);
            }
            if (canvas.overlaps(bounds)) {
                // Canvas subCanvas = canvas.createSubcanvas();
                final Canvas subCanvas = canvas.createSubcanvas(bounds.getX(), bounds.getY(), bounds.getWidth() - 0, bounds.getSize().getHeight());
                // subCanvas.offset(subview.getBounds().getX(),
                // subview.getBounds().getY());
                if (Toolkit.debug) {
                    LOG.debug("-- repainting " + subview);
                    LOG.debug("subcanvas " + subCanvas);
                }
                subview.draw(subCanvas);
                if (Toolkit.debug) {
                    canvas.drawRectangle(subview.getBounds().getX(), subview.getBounds().getY(), subview.getBounds().getWidth() - 1, subview.getBounds().getHeight() - 1, Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BOUNDS_BORDER));
                }
            }
        }
    }

    @Override
    public int getBaseline() {
        final View[] e = getSubviews();
        if (e.length == 0) {
            return 14;
        } else {
            final View subview = e[0];
            return subview.getBaseline();
        }
    }

    @Override
    public FocusManager getFocusManager() {
        return focusManager == null ? super.getFocusManager() : focusManager;
    }

    @Override
    public final Size getRequiredSize(final Size availableSpace) {
        ensureBuilt();
        return requiredSize(availableSpace);
    }

    protected abstract Size requiredSize(Size availableSpace);

    /**
     * Gets the set of subviews for this container. If the container is marked
     * as not being built yet (ie {@link #buildInvalid} is <tt>true</tt> ), then
     * a view building process is initiated and the flag is cleared. During this
     * build process the {@link #buildView()} method is called and then each
     * subview is asked for its subview so that the build process can recurse
     * down the tree if it needs to.
     * 
     * This method is synchronised so that two threads do not try to build the
     * same view at the same time
     */
    @Override
    public synchronized View[] getSubviews() {
        ensureBuilt();
        return subviews();
    }

    protected void ensureBuilt() {
        if (buildInvalid) {
            getFeedbackManager().setBusy(this, null);
            buildInvalid = false;
            if (subviews().length == 0) {
                buildNewView();
            } else {
                buildModifiedView();
            }
            for (final View view : subviews()) {
                view.getSubviews();
            }
            getFeedbackManager().clearBusy(this);
            buildCount++;
        }
    }

    // TODO make abstract
    protected void buildModifiedView() {
        buildView();
    }

    // TODO make abstract
    protected void buildNewView() {
        buildView();
    }

    /**
     * @deprecated
     */
    // TODO call two different methods instead: buildNewView and
    // buildModifiedView
    @Deprecated
    protected abstract void buildView();

    protected View[] subviews() {
        final View v[] = new View[views.size()];
        views.copyInto(v);
        return v;
    }

    @Override
    public void invalidateContent() {
        buildInvalid = true;
        invalidateLayout();
    }

    @Override
    public void invalidateLayout() {
        layoutInvalid = true;
        super.invalidateLayout();
    }

    @Override
    public void layout() {
        if (layoutInvalid) {
            getFeedbackManager().setBusy(this, null);
            markDamaged();

            ensureBuilt();
            final Size maximumSize = getSize();
            // maximumSize.contract(getPadding());
            doLayout(maximumSize);
            layoutInvalid = false;
            for (final View view : getSubviews()) {
                view.layout();
            }
            markDamaged();
            getFeedbackManager().clearBusy(this);
            layoutCount++;
        }
    }

    protected abstract void doLayout(Size maximumSize);

    /**
     * When the specified size is different to the current size the the layout
     * of this component is marked as invalid, forcing its components to re-laid
     * out in turn.
     */
    @Override
    public void setSize(final Size size) {
        final Size previousSize = getSize();
        super.setSize(size);
        if (!size.equals(previousSize)) {
            layoutInvalid = true;
        }
    }

    protected boolean isLayoutInvalid() {
        return layoutInvalid;
    }

    @Override
    public View subviewFor(final Location location) {
        final Location l = new Location(location);
        final Padding padding = getPadding();
        l.subtract(padding.getLeft(), padding.getTop());
        final View views[] = getSubviews();
        for (int i = views.length - 1; i >= 0; i--) {
            if (views[i].getBounds().contains(l)) {
                return views[i];
            }
        }
        return null;
    }

    @Override
    public View pickupView(final Location location) {
        return canDragView ? super.pickupView(location) : null;
    }

    @Override
    public void removeView(final View view) {
        LOG.debug("removing " + view + " from " + this);
        if (views.contains(view)) {
            views.removeElement(view);
            getViewManager().removeFromNotificationList(view);
            markDamaged();
            invalidateLayout();
        } else {
            throw new IsisException(view + "\n    not in " + getView());
        }
    }

    @Override
    public void replaceView(final View toReplace, final View replacement) {
        LOG.debug("replacing " + toReplace + " with " + replacement + " within " + this);
        for (int i = 0; i < views.size(); i++) {
            if (views.elementAt(i) == toReplace) {
                replacement.setParent(getView());
                replacement.setLocation(toReplace.getLocation());
                views.insertElementAt(replacement, i);
                invalidateLayout();
                toReplace.dispose();
                getViewManager().addToNotificationList(replacement);
                return;
            }
        }

        throw new IsisException(toReplace + " not found to replace");
    }

    public void setCanDragView(final boolean canDragView) {
        this.canDragView = canDragView;
    }

    @Override
    public void setFocusManager(final FocusManager focusManager) {
        this.focusManager = focusManager;
    }

    @Override
    public String toString() {
        final ToString to = new ToString(this, getId());
        to.append("type", getSpecification().getName());
        return to.toString();
    }

    @Override
    public void update(final ObjectAdapter object) {
        LOG.debug("update notify on " + this);
        invalidateContent();
    }

    @Override
    public ViewAreaType viewAreaType(final Location location) {
        final View subview = subviewFor(location);
        if (subview == null) {
            return ViewAreaType.VIEW;
        } else {
            location.subtract(subview.getLocation());
            return subview.viewAreaType(location);
        }
    }
}
