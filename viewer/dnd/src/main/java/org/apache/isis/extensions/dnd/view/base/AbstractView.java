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


package org.apache.isis.extensions.dnd.view.base;

import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.ConsentAbstract;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.runtime.userprofile.Options;
import org.apache.isis.extensions.dnd.drawing.Bounds;
import org.apache.isis.extensions.dnd.drawing.Canvas;
import org.apache.isis.extensions.dnd.drawing.Color;
import org.apache.isis.extensions.dnd.drawing.ColorsAndFonts;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.drawing.Padding;
import org.apache.isis.extensions.dnd.drawing.Size;
import org.apache.isis.extensions.dnd.util.Properties;
import org.apache.isis.extensions.dnd.util.ViewerException;
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
import org.apache.isis.extensions.dnd.view.ObjectContent;
import org.apache.isis.extensions.dnd.view.Placement;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.UndoStack;
import org.apache.isis.extensions.dnd.view.UserActionSet;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewAreaType;
import org.apache.isis.extensions.dnd.view.ViewAxis;
import org.apache.isis.extensions.dnd.view.ViewDrag;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.ViewSpecification;
import org.apache.isis.extensions.dnd.view.ViewState;
import org.apache.isis.extensions.dnd.view.Viewer;
import org.apache.isis.extensions.dnd.view.Workspace;
import org.apache.isis.extensions.dnd.view.collection.CollectionContent;
import org.apache.isis.extensions.dnd.view.collection.RootCollection;
import org.apache.isis.extensions.dnd.view.content.FieldContent;
import org.apache.isis.extensions.dnd.view.content.RootObject;
import org.apache.isis.extensions.dnd.view.option.OpenViewOption;
import org.apache.isis.extensions.dnd.view.option.UserActionAbstract;


public abstract class AbstractView implements View {
    private static final Logger LOG = Logger.getLogger(AbstractView.class);

    private static int nextId = 0;
    private int id = 0;
    private View parent;
    private View viewRoot;
    private ViewSpecification specification;
    private Content content;
    private final ViewState state;
    private int x;
    private int y;
    private int height;
    private int width;
    
    protected AbstractView(final Content content) {
        this(content, null);
    }

    protected AbstractView(final Content content, final ViewSpecification specification) {
        if (content == null) {
            throw new IllegalArgumentException("Content not specified");
        }
        assignId();
        this.content = content;
        this.specification = specification;
        state = new ViewState();
        viewRoot = this;
    }

    public void addView(final View view) {
        throw new IsisException("Can't add views to " + this);
    }

    protected void assignId() {
        id = nextId++;
    }

    public Consent canChangeValue() {
        return Veto.DEFAULT;
    }

    public boolean canFocus() {
        return true;
    }

    public boolean contains(final View view) {
        final View[] subviews = getSubviews();
        for (int i = 0; i < subviews.length; i++) {
            if (subviews[i] == view || (subviews[i] != null && subviews[i].contains(view))) {
                return true;
            }
        }
        return false;
    }

    public boolean containsFocus() {
        if (hasFocus()) {
            return true;
        }

        final View[] subviews = getSubviews();
        for (int i = 0; i < subviews.length; i++) {
            if (subviews[i] != null && subviews[i].containsFocus()) {
                return true;
            }
        }
        return false;
    }

    public void contentMenuOptions(final UserActionSet options) {
        options.setColor(Toolkit.getColor(ColorsAndFonts.COLOR_MENU_CONTENT));

        final Content content = getContent();
        if (content != null) {
            content.contentMenuOptions(options);
        }
    }

    /**
     * Returns debug details about this view.
     */
    public void debug(final DebugString debug) {
        final String name = getClass().getName();
        debug.appendln("Root: " + name.substring(name.lastIndexOf('.') + 1) + getId());
        debug.indent();
        debug.appendln("required size", getRequiredSize(Size.createMax()));
        debug.appendln("given size", getSize());
        debug.appendln("padding", getPadding());
        debug.appendln("base line", getBaseline());
        debug.unindent();
        debug.appendln();

        debug.appendTitle("Specification");
        if (specification == null) {
            debug.append("\none");
        } else {
            debug.appendln(specification.getName());
            debug.appendln("  " + specification.getClass().getName());
            debug.appendln("  " + (specification.isOpen() ? "open" : "closed"));
            debug.appendln("  " + (specification.isReplaceable() ? "replaceable" : "non-replaceable"));
            debug.appendln("  " + (specification.isSubView() ? "subview" : "main view"));
        }

        debug.appendln();
        debug.appendTitle("View");

        debug.appendln("Self", getView());
        debug.appendln("Parent's size", getParent() == null ? new Size() : getParent().getSize());
        debug.appendln("Size w/in parent", getView().getRequiredSize(getParent() == null ? new Size() : getParent().getSize()));
        debug.appendln("Location w/in parent", getView().getLocation());
        debug.appendln("Changable", canChangeValue());
        debug.appendln("Focus", (canFocus() ? "focusable" : "non-focusable"));
        debug.appendln("Has focus", hasFocus());
        debug.appendln("Contains focus", containsFocus());
        debug.appendln("Focus manager", getFocusManager());
        debug.appendln("State", getState()); 
        debug.appendln("Axes", getViewAxes());
        appendDebug(debug);

        debug.appendln("Workspace", getWorkspace());

        View p = getParent();
        debug.appendln("Parent hierarchy:" + (p == null ? "none" : ""));
        debug.indent();
        while (p != null) {
            debug.appendln(p.toString());
            p = p.getParent();
        }
        debug.unindent();

        debug.appendln();
        debug.appendln();
        debug.appendln();

        debug.appendTitle("View structure");
        // b.appendln("Built", (buildInvalid ? "no" : "yes") + ", " + buildCount + " builds");
        // b.appendln("Laid out", (layoutInvalid ? "no" : "yes") + ", " + layoutCount + " layouts");

        debug.appendln(getSpecification().getName().toUpperCase());
        debugStructure(debug);
    }
    
    protected void appendDebug(final DebugString debug) {}

    public void debugStructure(final DebugString b) {
        b.appendln("Content", getContent() == null ? "none" : getContent());
        b.appendln("Required size ", getRequiredSize(Size.createMax()));
        b.appendln("Bounds", getBounds());
        b.appendln("Baseline", getBaseline());
        b.appendln("Location", getAbsoluteLocation());
        final View views[] = getSubviews();
        b.indent();
        for (int i = 0; i < views.length; i++) {
            final View subview = views[i];
            b.appendln();
            ViewSpecification spec = subview.getSpecification();
            b.appendln(spec == null ? "none" : spec.getName().toUpperCase());
            b.appendln("View", subview);
            subview.debugStructure(b);
        }
        b.unindent();
    }

    public void dispose() {
        View parent = getParent();
        if (parent != null) {
            parent.removeView(getView());
        }
    }

    public void drag(final InternalDrag drag) {}

    public void drag(final ContentDrag contentDrag) {}

    public void drag(ViewDrag drag) {
        getViewManager().getSpy().addTrace(this, "view drag", drag);
    }
    
    public void dragCancel(final InternalDrag drag) {
        getFeedbackManager().showDefaultCursor();
    }

    public View dragFrom(final Location location) {
        final View subview = subviewFor(location);
        if (subview != null) {
            location.subtract(subview.getLocation());
            return subview.dragFrom(location);
        } else {
            return null;
        }
    }

    public void dragIn(final ContentDrag drag) {}

    public void dragOut(final ContentDrag drag) {}

    public DragEvent dragStart(final DragStart drag) {
        final View subview = subviewFor(drag.getLocation());
        if (subview != null) {
            drag.subtract(subview.getLocation());
            return subview.dragStart(drag);
        } else {
            return null;
        }
    }

    public void dragTo(final InternalDrag drag) {}

    /**
     * Clears the background of this view to the given color (call from the {@link #draw(Canvas)} method.
     */
    protected void clearBackground(Canvas canvas, Color color) {
        final Bounds bounds = getBounds();
        canvas.drawSolidRectangle(0, 0, bounds.getWidth(), bounds.getHeight(), color);
    }

    public void draw(final Canvas canvas) {
        if (Toolkit.debug) {
            canvas.drawDebugOutline(new Bounds(getSize()), getBaseline(), Toolkit
                    .getColor(ColorsAndFonts.COLOR_DEBUG_BOUNDS_VIEW));
        }
    }

    public void drop(final ContentDrag drag) {}

    /**
     * No default behaviour, views can only be dropped on workspace
     */
    public void drop(final ViewDrag drag) {
        getParent().drop(drag);
    }

    public void editComplete(boolean moveFocus, boolean toNextField) {}

    public void entered() {
        final Content cont = getContent();
        if (cont != null) {
            final String description = cont.getDescription();
            if (description != null && !"".equals(description)) {
                getFeedbackManager().setViewDetail(description);
            }
        }
    }

    public void exited() {}

    public void firstClick(final Click click) {
        final View subview = subviewFor(click.getLocation());
        if (subview != null) {
            click.subtract(subview.getLocation());
            subview.firstClick(click);
        }
    }

    public void focusLost() {}

    public void focusReceived() {}

    public Location getAbsoluteLocation() {
        View parent = getParent();
        if (parent == null) {
            return getLocation();
        } else {
            final Location location = parent.getAbsoluteLocation();
            getViewManager().getSpy().addTrace(this, "parent location", location);
            location.add(x, y);
            getViewManager().getSpy().addTrace(this, "plus view's location", location);
            final Padding pad = parent.getPadding();
            location.add(pad.getLeft(), pad.getTop());
            getViewManager().getSpy().addTrace(this, "plus view's padding", location);
            return location;
        }
    }

    public int getBaseline() {
        return 0;
    }

    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }

    public Content getContent() {
        return content;
    }

    public FocusManager getFocusManager() {
        return getParent() == null ? null : getParent().getFocusManager();
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return new Location(x, y);
    }

    public Padding getPadding() {
        return new Padding(0, 0, 0, 0);
    }

    public final View getParent() {
        // Assert.assertEquals(parent == null ? null : parent.getView(), parent);
        // return parent;

        return parent == null ? null : parent.getView();
    }

    public Size getRequiredSize(final Size maximumSize) {
        return new Size(maximumSize);
    }

    public Size getSize() {
        return new Size(width, height);
    }

    public ViewSpecification getSpecification() {
        if (specification == null) {
            specification = new NonBuildingSpecification(this);
        }
        return specification;
    }

    public ViewState getState() {
        return state;
    }

    public View[] getSubviews() {
        return new View[0];
    }

    public final View getView() {
        return viewRoot;
    }
    
    public Axes getViewAxes() {
        return new Axes();
    }
    
    public Viewer getViewManager() {
        return Toolkit.getViewer();
    }

    public Feedback getFeedbackManager() {
        return Toolkit.getFeedbackManager();
    }

    public Workspace getWorkspace() {
        return getParent() == null ? null : getParent().getWorkspace();
    }

    public boolean hasFocus() {
        return getViewManager().hasFocus(getView());
    }

    public View identify(final Location location) {
        final View subview = subviewFor(location);
        if (subview == null) {
            getViewManager().getSpy().addTrace(this, "mouse location within node view", location);
            getViewManager().getSpy().addTrace("----");
            return getView();
        } else {
            location.subtract(subview.getLocation());
            return subview.identify(location);
        }
    }

    public void invalidateContent() {}

    public void invalidateLayout() {
        View parent = getParent();
        if (parent != null) {
            parent.invalidateLayout();
        }
    }

    public void keyPressed(final KeyboardAction key) {}

    public void keyReleased(KeyboardAction action) {}

    public void keyTyped(KeyboardAction action) {}

    public void layout() {}

    /**
     * Limits the bounds of the this view (when being moved or dropped) so it never extends outside the
     * specified bounds e.g. outside of a parent view
     */
    public void limitBoundsWithin(final Bounds containerBounds) {
        final Bounds contentBounds = getView().getBounds();
        if (containerBounds.limitBounds(contentBounds)) {
            getView().setBounds(contentBounds);
        }
    }

    public void limitBoundsWithin(final Size size) {
        final int w = getView().getSize().getWidth();
        final int h = getView().getSize().getHeight();

        int x = getView().getLocation().getX();
        int y = getView().getLocation().getY();

        if (x + w > size.getWidth()) {
            x = size.getWidth() - w;
        }
        if (x < 0) {
            x = 0;
        }

        if (y + h > size.getHeight()) {
            y = size.getHeight() - h;
        }
        if (y < 0) {
            y = 0;
        }

        getView().setLocation(new Location(x, y));
    }

    public void markDamaged() {
        markDamaged(getView().getBounds());
    }

    public void markDamaged(final Bounds bounds) {
        View parent = getParent();
        if (parent == null) {
            getViewManager().markDamaged(bounds);
        } else {
            final Location pos = parent.getLocation();
            bounds.translate(pos.getX(), pos.getY());
            final Padding pad = parent.getPadding();
            bounds.translate(pad.getLeft(), pad.getTop());
            parent.markDamaged(bounds);
        }
    }

    public void mouseDown(final Click click) {
        final View subview = subviewFor(click.getLocation());
        if (subview != null) {
            click.subtract(subview.getLocation());
            subview.mouseDown(click);
        }
    }

    public void mouseMoved(final Location location) {
        final View subview = subviewFor(location);
        if (subview != null) {
            location.subtract(subview.getLocation());
            subview.mouseMoved(location);
        }
    }

    public void mouseUp(final Click click) {
        final View subview = subviewFor(click.getLocation());
        if (subview != null) {
            click.subtract(subview.getLocation());
            subview.mouseUp(click);
        }
    }

    public void objectActionResult(final ObjectAdapter result, final Placement placement) {
        if (result != null) {
            final CollectionFacet facet = result.getSpecification().getFacet(CollectionFacet.class);
            ObjectAdapter objectToDisplay = result;
            if (facet != null) {
                if (facet.size(result) == 1) {
                    objectToDisplay = facet.firstElement(result);
                }
            }
            getWorkspace().addWindowFor(objectToDisplay, placement);
        }
    }

    public View pickupContent(final Location location) {
        final View subview = subviewFor(location);
        if (subview != null) {
            location.subtract(subview.getLocation());
            return subview.pickupView(location);
        } else {
            return Toolkit.getViewFactory().createDragViewOutline(getView());
        }
    }

    public View pickupView(final Location location) {
        final View subview = subviewFor(location);
        if (subview != null) {
            location.subtract(subview.getLocation());
            return subview.pickupView(location);
        } else {
            return null;
        }
    }

    /**
     * Delegates all printing the the draw method.
     * 
     * @see #draw(Canvas)
     */
    public void print(final Canvas canvas) {
        draw(canvas);
    }

    public void refresh() {}

    public void removeView(final View view) {
        throw new IsisException();
    }

    public void replaceView(final View toReplace, final View replacement) {
        throw new IsisException();
    }

    public void secondClick(final Click click) {
        final View subview = subviewFor(click.getLocation());
        if (subview != null) {
            click.subtract(subview.getLocation());
            subview.secondClick(click);
        }
    }

    /**
     * Sets the location and size view the {@link #setLocation(Location)) and {@link #setSize(Size)) methods.
     */
    public void setBounds(final Bounds bounds) {
        setLocation(bounds.getLocation());
        setSize(bounds.getSize());
    }

    public void setFocusManager(final FocusManager focusManager) {
    }

    protected void setContent(final Content content) {
        this.content = content;
    }

    public void setLocation(final Location location) {
        x = location.getX();
        y = location.getY();
    }

    public final void setParent(final View parentView) {
        LOG.debug("set parent " + parentView + " for " + this);
        parent = parentView.getView();
    }

    public void setMaximumSize(final Size size) {}

    public void setSize(final Size size) {
        width = size.getWidth();
        height = size.getHeight();
    }

    protected void setSpecification(final ViewSpecification specification) {
        this.specification = specification;
    }

    public final void setView(final View view) {
        this.viewRoot = view;
    }

    @Deprecated
    protected void setViewAxis(final ViewAxis viewAxis) {
       // this.viewAxis = viewAxis;
    }

    public View subviewFor(final Location location) {
        return null;
    }

    public void thirdClick(final Click click) {
        final View subview = subviewFor(click.getLocation());
        if (subview != null) {
            click.subtract(subview.getLocation());
            subview.thirdClick(click);
        }
    }

    @Override   
    public String toString() {
        final String name = getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1) + getId() + ":" + getState() + ":" + getContent();
    }

    public void update(final ObjectAdapter object) {}

    public void updateView() {}

    public ViewAreaType viewAreaType(final Location location) {
        final View subview = subviewFor(location);
        if (subview != null) {
            location.subtract(subview.getLocation());
            return subview.viewAreaType(location);
        } else {
            return ViewAreaType.CONTENT;
        }
    }

    public void viewMenuOptions(final UserActionSet options) {
        options.setColor(Toolkit.getColor(ColorsAndFonts.COLOR_MENU_VIEW));

        final Content content = getContent();
        addContentMenuItems(options, content);
        addNewViewMenuItems(options, content);

        // TODO ask the viewer for the print option - provided by the underlying system
        // options.add(new PrintOption());

        addViewDebugMenuItems(options);

        final UndoStack undoStack = getViewManager().getUndoStack();
        if (!undoStack.isEmpty()) {
            options.add(new UserActionAbstract("Undo " + undoStack.getNameOfUndo()) {

                @Override
                public Consent disabled(final View component) {
                    return new ConsentAbstract("", undoStack.descriptionOfUndo()) {
                        private static final long serialVersionUID = 1L;
                    };
                }

                public void execute(final Workspace workspace, final View view, final Location at) {
                    undoStack.undoLastCommand();
                }
            });
        }
    }

    private void addViewDebugMenuItems(final UserActionSet options) {
        options.add(new UserActionAbstract("Refresh view", ObjectActionType.DEBUG) {
            public void execute(final Workspace workspace, final View view, final Location at) {
                refresh();
            }
        });

        options.add(new UserActionAbstract("Invalidate content", ObjectActionType.DEBUG) {
            public void execute(final Workspace workspace, final View view, final Location at) {
                invalidateContent();
            }
        });

        options.add(new UserActionAbstract("Invalidate layout", ObjectActionType.DEBUG) {
            public void execute(final Workspace workspace, final View view, final Location at) {
                invalidateLayout();
            }
        });
    }

    private void addNewViewMenuItems(final UserActionSet options, final Content content) {
        if (getContent() instanceof ObjectContent) {
            options.add(new UserActionAbstract("Use as default view for objects", ObjectActionType.USER) {
                public void execute(final Workspace workspace, final View view, final Location at) {
                    Properties.setStringOption("view.object-default", getSpecification().getName());
                    /*
                    Options viewOptions = Properties.getViewConfigurationOptions(getSpecification());
                    getView().saveOptions(viewOptions);
                    */
                }
            });
        }
        if (getContent() instanceof CollectionContent) {
            options.add(new UserActionAbstract("Use as default view for collection", ObjectActionType.USER) {
                public void execute(final Workspace workspace, final View view, final Location at) {
                    Properties.setStringOption("view.collection-default", getSpecification().getName());
                    /*
                    Options viewOptions = Properties.getViewConfigurationOptions(getSpecification());
                    getView().saveOptions(viewOptions);
                    */
                }
            });
        }
        if (getContent() instanceof ObjectContent && !getSpecification().isOpen()) {
            options.add(new UserActionAbstract("Use as default view for icon", ObjectActionType.USER) {
                public void execute(final Workspace workspace, final View view, final Location at) {
                    Properties.setStringOption("view.icon-default", getSpecification().getName());
                    /*
                    Options viewOptions = Properties.getViewConfigurationOptions(getSpecification());
                    getView().saveOptions(viewOptions);
                    */
                }
            });
        }

        if (getContent() instanceof RootObject || getContent() instanceof RootCollection) {
            options.add(new UserActionAbstract("Use as default view for " + getContent().getSpecification().getSingularName(), ObjectActionType.USER) {
                public void execute(final Workspace workspace, final View view, final Location at) {
                    Options viewOptions = Properties.getViewConfigurationOptions(getSpecification());
                    getView().saveOptions(viewOptions);
                    
                    // Options viewOptions = 
                    ObjectSpecification specification = content.getSpecification();
                    Options settingsOptions = Properties.getDefaultViewOptions(specification);
                    settingsOptions.addOption("spec", getSpecification().getName());
                }
            });
        }
        /*
        options.add(new UserActionAbstract("Create new specification", UserAction.USER) {
            // TODO probably needs to be a replace with new view specification
            public void execute(final Workspace workspace, final View view, final Location at) {
                UserViewSpecification newSpec = new UserViewSpecification(getView());
                Options viewOptions = Properties.getViewConfigurationOptions(newSpec);
                getView().saveOptions(viewOptions);
                
                viewOptions = Properties.getUserViewSpecificationOptions(newSpec);
                viewOptions.addOption("wrapped-specification", getSpecification().getClass().getName());
                
                Toolkit.getViewFactory().addSpecification(newSpec);
            }
        });
        */
        options.add(new UserActionAbstract("Save specification", ObjectActionType.USER) {
            public void execute(final Workspace workspace, final View view, final Location at) {
                Options viewOptions = Properties.getViewConfigurationOptions(getSpecification());
                getView().saveOptions(viewOptions);
                
                Toolkit.getViewFactory().addSpecification(getSpecification());
            }
        });
        createOpenAsSubmenu(options, content);

        createCreateViewSubmenu(options, content);
    }

    private void createOpenAsSubmenu(final UserActionSet options, final Content content) {
        ViewRequirement requirements = new ViewRequirement(content, ViewRequirement.OPEN | ViewRequirement.EXPANDABLE);
        final Enumeration possibleViews = Toolkit.getViewFactory().availableViews(requirements);
        if (possibleViews.hasMoreElements()) {
            UserActionSet submenu = options.addNewActionSet("Open as");
            while (possibleViews.hasMoreElements()) {
                final ViewSpecification specification = (ViewSpecification) possibleViews.nextElement();
                final UserActionAbstract viewAs = new OpenViewOption(specification);
                submenu.add(viewAs);
            }
        }
    }

    private void createCreateViewSubmenu(final UserActionSet options, final Content content) {
        ViewRequirement requirements = new ViewRequirement(content, ViewRequirement.OPEN);
        final Enumeration possibleViews = Toolkit.getViewFactory().availableDesigns(requirements);
        if (possibleViews.hasMoreElements()) {
            UserActionSet submenu = options.addNewActionSet("Create view from");
            while (possibleViews.hasMoreElements()) {
                final ViewSpecification specification = (ViewSpecification) possibleViews.nextElement();
                final UserActionAbstract viewAs = new UserActionAbstract(specification.getName(), ObjectActionType.USER) {
                    public void execute(final Workspace workspace, final View view, final Location at) {
                        ViewSpecification newSpec;
                        try {
                            newSpec = specification.getClass().newInstance();
                        } catch (InstantiationException e) {
                            throw new ViewerException(e);
                        } catch (IllegalAccessException e) {
                            throw new ViewerException(e);
                        }

                        Content content = view.getContent();
                        if (!(content instanceof FieldContent)) {
                            content = Toolkit.getContentFactory().createRootContent(content.getAdapter());
                        }
                        final View newView = newSpec.createView(content, view.getViewAxes(), -1);
                        LOG.debug("open view " + newView);
                        workspace.addWindow(newView, new Placement(view));
                        workspace.markDamaged();
                        
                        Options viewOptions = Properties.getViewConfigurationOptions(newSpec);
                        newView.saveOptions(viewOptions);
                        viewOptions = Properties.getUserViewSpecificationOptions(newSpec.getName());
                        viewOptions.addOption("design", specification.getClass().getName());
                        
                        Toolkit.getViewFactory().addSpecification(newSpec);
                    }
                };
                
                submenu.add(viewAs);
            }
        }
    }

    private void addContentMenuItems(final UserActionSet options, final Content content) {
        if (content != null) {
            content.viewMenuOptions(options);
        }
    }

    public void loadOptions(Options viewOptions) {}

    public void saveOptions(Options viewOptions) {
//        viewOptions.addOption("spec", getSpecification().getClass().getName());
    }
}
