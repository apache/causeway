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

package org.apache.isis.viewer.dnd.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Offset;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.interaction.ViewDragImpl;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.DragStart;
import org.apache.isis.viewer.dnd.view.ObjectContent;
import org.apache.isis.viewer.dnd.view.Placement;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;
import org.apache.isis.viewer.dnd.view.base.DragViewOutline;
import org.apache.isis.viewer.dnd.view.base.IconGraphic;
import org.apache.isis.viewer.dnd.view.collection.CollectionContent;
import org.apache.isis.viewer.dnd.view.collection.CollectionElement;
import org.apache.isis.viewer.dnd.view.content.FieldContent;
import org.apache.isis.viewer.dnd.view.field.OneToManyField;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;
import org.apache.isis.viewer.dnd.view.text.ObjectTitleText;
import org.apache.isis.viewer.dnd.view.text.TitleText;

// TODO use ObjectBorder to provide the basic border functionality
public class TreeNodeBorder extends AbstractBorder {
    private static final int BORDER = 13;
    private static final int BOX_PADDING = 2;
    private static final int BOX_SIZE = 9;
    private static final int BOX_X_OFFSET = 5;
    private final static Text LABEL_STYLE = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL);
    private static final Logger LOG = LoggerFactory.getLogger(TreeNodeBorder.class);
    private final int baseline;
    private final IconGraphic icon;
    private final ViewSpecification replaceWithSpecification;
    private final TitleText text;

    public TreeNodeBorder(final View wrappedView, final ViewSpecification replaceWith) {
        super(wrappedView);

        replaceWithSpecification = replaceWith;

        icon = new IconGraphic(this, LABEL_STYLE);
        text = new ObjectTitleText(this, LABEL_STYLE);
        final int height = icon.getSize().getHeight();

        baseline = icon.getBaseline() + 1;

        left = 22;
        right = 0 + BORDER;
        top = height + 2;
        bottom = 0;
    }

    private int canOpen() {
        return ((NodeSpecification) getSpecification()).canOpen(getContent());
    }

    @Override
    protected Bounds contentArea() {
        return new Bounds(getLeft(), getTop(), wrappedView.getSize().getWidth(), wrappedView.getSize().getHeight());
    }

    @Override
    public void debugDetails(final DebugBuilder debug) {
        debug.append("TreeNodeBorder " + left + " pixels\n");
        debug.append("           titlebar " + (top) + " pixels\n");
        debug.append("           replace with  " + replaceWithSpecification);
        debug.append("           text " + text);
        debug.append("           icon " + icon);
        super.debugDetails(debug);

    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        if (drag.getLocation().getX() > getSize().getWidth() - right) {
            final View dragOverlay = new DragViewOutline(getView());
            return new ViewDragImpl(this, new Offset(drag.getLocation()), dragOverlay);
        } else if (overBorder(drag.getLocation())) {
            return Toolkit.getViewFactory().createDragContentOutline(this, drag.getLocation());
        } else {
            return super.dragStart(drag);
        }
    }

    @Override
    public void draw(final Canvas canvas) {
        final Color secondary1 = Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1);
        final Color secondary2 = Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2);
        final Color secondary3 = Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3);
        /*
         * REVIEW if (getViewAxis(TableAxis.class) != null) { if
         * (((SelectableViewAxis)
         * getViewAxis(SelectableViewAxis.class)).isSelected(getView())) {
         * canvas.drawSolidRectangle(left, 0, getSize().getWidth() - left, top,
         * Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY2)); secondary2 =
         * secondary1; } }
         */
        if (getState().isObjectIdentified()) {
            canvas.drawRectangle(left, 0, getSize().getWidth() - left, top, secondary2);

            final int xExtent = getSize().getWidth();
            canvas.drawSolidRectangle(xExtent - BORDER + 1, 1, BORDER - 2, top - 2, secondary3);
            canvas.drawLine(xExtent - BORDER, 0, xExtent - BORDER, top - 2, secondary2);
        }

        // lines
        int x = 0;
        final int y = top / 2;
        canvas.drawLine(x, y, x + left, y, secondary2);

        final boolean isOpen = getSpecification().isOpen();
        final int canOpen = canOpen();
        final boolean addBox = isOpen || canOpen != NodeSpecification.CANT_OPEN;
        if (addBox) {
            x += BOX_X_OFFSET;
            canvas.drawLine(x, y, x + BOX_SIZE - 1, y, secondary3);
            canvas.drawSolidRectangle(x, y - BOX_SIZE / 2, BOX_SIZE, BOX_SIZE, Toolkit.getColor(ColorsAndFonts.COLOR_WHITE));
            canvas.drawRectangle(x, y - BOX_SIZE / 2, BOX_SIZE, BOX_SIZE, secondary1);

            if (canOpen == NodeSpecification.UNKNOWN) {

            } else {
                final Color black = Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);
                canvas.drawLine(x + BOX_PADDING, y, x + BOX_SIZE - 1 - BOX_PADDING, y, black);
                if (!isOpen) {
                    x += BOX_SIZE / 2;
                    canvas.drawLine(x, y - BOX_SIZE / 2 + BOX_PADDING, x, y + BOX_SIZE / 2 - BOX_PADDING, black);
                }
            }
        }

        final View[] nodes = getSubviews();
        if (nodes.length > 0) {
            final int y1 = top / 2;
            final View node = nodes[nodes.length - 1];
            final int y2 = top + node.getLocation().getY() + top / 2;
            canvas.drawLine(left - 1, y1, left - 1, y2, secondary2);
        }

        // icon & title
        x = left + 1;
        icon.draw(canvas, x, baseline);
        x += icon.getSize().getWidth();
        final int maxWith = getSize().getWidth() - x;
        text.draw(canvas, x, baseline, maxWith);

        if (Toolkit.debug) {
            canvas.drawRectangleAround(getBounds(), Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BASELINE));
        }

        // draw components
        super.draw(canvas);
    }

    @Override
    public void entered() {
        getState().setContentIdentified();
        getState().setViewIdentified();
        wrappedView.entered();
        markDamaged();
    }

    @Override
    public void exited() {
        getState().clearObjectIdentified();
        getState().clearViewIdentified();
        wrappedView.exited();
        markDamaged();
    }

    @Override
    public void firstClick(final Click click) {
        final int x = click.getLocation().getX();
        final int y = click.getLocation().getY();

        if (withinBox(x, y)) {
            if (canOpen() == NodeSpecification.UNKNOWN) {
                resolveContent();
                markDamaged();
            }
            LOG.debug((getSpecification().isOpen() ? "close" : "open") + " node " + getContent().getAdapter());
            if (canOpen() == NodeSpecification.CAN_OPEN) {
                final View newView = replaceWithSpecification.createView(getContent(), getViewAxes(), -1);
                getParent().replaceView(getView(), newView);
            }
            /*
             * } else if (y < top && x > left && click.button1()) { if
             * (canOpen() == NodeSpecification.UNKNOWN) { resolveContent();
             * markDamaged(); } selectNode();
             */
        } else {
            super.firstClick(click);
        }
    }

    @Override
    public int getBaseline() {
        return wrappedView.getBaseline() + baseline;
    }

    @Override
    public Size getRequiredSize(final Size maximumSize) {
        final Size size = super.getRequiredSize(maximumSize);
        // size.extendHeight(2 * VPADDING);
        size.ensureWidth(left + ViewConstants.HPADDING + icon.getSize().getWidth() + text.getSize().getWidth() + ViewConstants.HPADDING + right);
        return size;
    }

    @Override
    public void objectActionResult(final ObjectAdapter result, final Placement placement) {
        if (getContent() instanceof OneToManyField && result instanceof ObjectAdapter) {
            // same as InternalCollectionBorder
            final OneToManyField internalCollectionContent = (OneToManyField) getContent();
            final OneToManyAssociation field = internalCollectionContent.getOneToManyAssociation();
            final ObjectAdapter target = ((ObjectContent) getParent().getContent()).getObject();

            final Consent about = field.isValidToAdd(target, result);
            if (about.isAllowed()) {
                field.addElement(target, result);
            }
        }
        super.objectActionResult(result, placement);
    }

    private void resolveContent() {
        ObjectAdapter parent = getParent().getContent().getAdapter();
        if (!(parent instanceof ObjectAdapter)) {
            parent = getParent().getParent().getContent().getAdapter();
        }

        if (getContent() instanceof FieldContent) {
            final ObjectAssociation field = ((FieldContent) getContent()).getField();
            IsisContext.getPersistenceSession().resolveField(parent, field);
        } else if (getContent() instanceof CollectionContent) {
            IsisContext.getPersistenceSession().resolveImmediately(parent);
        } else if (getContent() instanceof CollectionElement) {
            IsisContext.getPersistenceSession().resolveImmediately(getContent().getAdapter());
        }
    }

    @Override
    public void secondClick(final Click click) {
        final int x = click.getLocation().getX();
        final int y = click.getLocation().getY();
        if (y < top && x > left) {
            if (canOpen() == NodeSpecification.UNKNOWN) {
                resolveContent();
                markDamaged();
            }
            final Location location = getAbsoluteLocation();
            location.translate(click.getLocation());
            getWorkspace().addWindowFor(getContent().getAdapter(), new Placement(this));
        } else {
            super.secondClick(click);
        }
    }

    // TODO remove
    private void selectNode() {
        /*
         * if (getViewAxis(SelectableViewAxis.class) != null) {
         * ((SelectableViewAxis)
         * getViewAxis(SelectableViewAxis.class)).selected(getView()); }
         */
    }

    @Override
    public String toString() {
        return wrappedView.toString() + "/TreeNodeBorder";
    }

    @Override
    public ViewAreaType viewAreaType(final Location mouseLocation) {
        final Bounds bounds = new Bounds(left + 1, 0, getSize().getWidth() - left - BORDER, top);
        if (bounds.contains(mouseLocation)) {
            return ViewAreaType.CONTENT;
        } else {
            return super.viewAreaType(mouseLocation);
        }
    }

    @Override
    public void viewMenuOptions(final UserActionSet options) {
        super.viewMenuOptions(options);
        TreeDisplayRules.menuOptions(options);

        options.add(new UserActionAbstract("Select node") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                selectNode();
            }

            @Override
            public String getDescription(final View view) {
                return "Show this node in the right-hand pane";
            }
        });

        final ObjectAdapter adapter = getView().getContent().getAdapter();
        if (adapter instanceof ObjectAdapter && (adapter.isGhost() /*|| adapter.getResolveState().isPartlyResolved() */)) {
            options.add(new UserActionAbstract("Load object") {
                @Override
                public void execute(final Workspace workspace, final View view, final Location at) {
                    resolveContent();
                }
            });
        }
    }

    private boolean withinBox(final int x, final int y) {
        return x >= BOX_X_OFFSET && x <= BOX_X_OFFSET + BOX_SIZE && y >= (top - BOX_SIZE) / 2 && y <= (top + BOX_SIZE) / 2;
    }
}
