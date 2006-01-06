package org.nakedobjects.viewer.skylark.tree;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedReference;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.CollectionContent;
import org.nakedobjects.viewer.skylark.CollectionElement;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.ContentDrag;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.FieldContent;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.OneToManyField;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.basic.DragContentIcon;
import org.nakedobjects.viewer.skylark.basic.IconGraphic;
import org.nakedobjects.viewer.skylark.basic.ObjectTitleText;
import org.nakedobjects.viewer.skylark.basic.TitleText;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;
import org.nakedobjects.viewer.skylark.core.AbstractView;

import org.apache.log4j.Logger;


public class TreeNodeBorder extends AbstractBorder {
    private static final int BOX_PADDING = 2;
    private static final int BOX_SIZE = 9;
    private static final int BOX_X_OFFSET = 5;
    private final static Text LABEL_STYLE = Style.NORMAL;
    private static final Logger LOG = Logger.getLogger(TreeNodeBorder.class);
    private int baseline;
    private IconGraphic icon;
    private ViewSpecification replaceWithSpecification;
    private TitleText text;

    public TreeNodeBorder(View wrappedView, ViewSpecification replaceWith) {
        super(wrappedView);

        replaceWithSpecification = replaceWith;

        icon = new IconGraphic(this, LABEL_STYLE);
        text = new ObjectTitleText(this, LABEL_STYLE);
        int height = icon.getSize().getHeight();

        baseline = icon.getBaseline() + 1;

        left = 22;
        right = 0;
        top = height + 2;
        bottom = 0;
    }

    private int canOpen() {
        return ((NodeSpecification) getSpecification()).canOpen(getContent());
    }

    protected Bounds contentArea() {
        return new Bounds(getLeft(), getTop(), wrappedView.getSize().getWidth(), wrappedView.getSize().getHeight());
    }

    public void contentMenuOptions(MenuOptionSet menuOptions) {
        super.contentMenuOptions(menuOptions);

        Naked object = getView().getContent().getNaked();
        ResolveState resolveState = ((NakedReference) object).getResolveState();
        if(object instanceof NakedReference && (resolveState.isGhost() || resolveState.isPartlyResolved())) {
            menuOptions.add(MenuOptionSet.VIEW, new MenuOption("Load object") {
                public void execute(Workspace workspace, View view, Location at) {
                    resolveContent();
                }
            });
        }
    }

    public void debugDetails(StringBuffer b) {
        b.append("TreeNodeBorder " + left + " pixels\n");
        b.append("           titlebar " + (top) + " pixels\n");
        b.append("           replace with  " + replaceWithSpecification);
        b.append("           text " + text);
        b.append("           icon " + icon);
        super.debugDetails(b);

    }

    public Drag dragStart(DragStart drag) {
        if (overBorder(drag.getLocation())) {
            View dragOverlay = new DragContentIcon(getContent());
            return new ContentDrag(this, drag.getLocation(), dragOverlay);
        } else {
            return super.dragStart(drag);
        }
    }

    public void draw(Canvas canvas) {
        if (((TreeBrowserFrame) getViewAxis()).getSelectedNode() == getView()) {
            canvas.drawSolidRectangle(left, 0, getSize().getWidth() - left, top, Style.PRIMARY2);
        }
        if (getState().isObjectIdentified()) {
            canvas.drawRectangle(left, 0, getSize().getWidth() - left, top, Style.SECONDARY2);
        }

        // lines
        int x = 0;
        int y = top / 2;
        canvas.drawLine(x, y, x + left, y, Style.SECONDARY2);

        boolean isOpen = getSpecification().isOpen();
        int canOpen = canOpen();
        boolean addBox = isOpen || canOpen != NodeSpecification.CANT_OPEN;
        if (addBox) {
            x += BOX_X_OFFSET;
            canvas.drawLine(x, y, x + BOX_SIZE - 1, y, Style.SECONDARY3);
            canvas.drawSolidRectangle(x, y - BOX_SIZE / 2, BOX_SIZE, BOX_SIZE, Style.WHITE);
            canvas.drawRectangle(x, y - BOX_SIZE / 2, BOX_SIZE, BOX_SIZE, Style.SECONDARY1);

            if (canOpen == NodeSpecification.UNKNOWN) {

            } else {
                canvas.drawLine(x + BOX_PADDING, y, x + BOX_SIZE - 1 - BOX_PADDING, y, Style.BLACK);
                if (!isOpen) {
                    x += BOX_SIZE / 2;
                    canvas.drawLine(x, y - BOX_SIZE / 2 + BOX_PADDING, x, y + BOX_SIZE / 2 - BOX_PADDING, Style.BLACK);
                }
            }
        }

        View[] nodes = getSubviews();
        if (nodes.length > 0) {
            int y1 = top / 2;
            View node = nodes[nodes.length - 1];
            int y2 = top + node.getLocation().getY() + top / 2;
            canvas.drawLine(left - 1, y1, left - 1, y2, Style.SECONDARY2);
        }

        // icon & title
        x = left + 1;
        icon.draw(canvas, x, baseline);
        x += icon.getSize().getWidth();
        text.draw(canvas, x, baseline);

        if (AbstractView.debug) {
            canvas.drawRectangleAround(this, Color.DEBUG_BASELINE);
        }

        // draw components
        super.draw(canvas);
    }

    public void entered() {
        super.entered();
        getState().setObjectIdentified();
        markDamaged();
    }

    public void exited() {
        super.exited();
        getState().clearObjectIdentified();
        markDamaged();
    }

    public void firstClick(Click click) {
        int x = click.getLocation().getX();
        int y = click.getLocation().getY();

        if (withinBox(x, y)) {
            if (canOpen() == NodeSpecification.UNKNOWN) {
                resolveContent();
                markDamaged();
            }
            LOG.debug((getSpecification().isOpen() ? "close" : "open") + " node " + getContent().getNaked());
            if (canOpen() == NodeSpecification.CAN_OPEN) {
                View newView = replaceWithSpecification.createView(getContent(), getViewAxis());
                getParent().replaceView(getView(), newView);
            }
        } else if (y < top && x > left) {
            selectNode();
        } else {
            super.firstClick(click);
        }
    }

    public int getBaseline() {
        return wrappedView.getBaseline() + baseline;
    }

    public Size getRequiredSize() {
        Size size = super.getRequiredSize();
//        size.extendHeight(2 * VPADDING);
        size.ensureWidth(left + HPADDING + icon.getSize().getWidth() + text.getSize().getWidth() + HPADDING + right);
        return size;
    }

    public void objectActionResult(Naked result, Location at) {
        if (getContent() instanceof OneToManyField) {
            // same as InternalCollectionBorder
            OneToManyField internalCollectionContent = (OneToManyField) getContent();
            OneToManyAssociation field = internalCollectionContent.getOneToManyAssociation();
            NakedObject target = ((ObjectContent) getParent().getContent()).getObject();

            Consent about = target.canAdd(field, (NakedObject) result);
            if (about.isAllowed()) {
                target.setAssociation(field, (NakedObject) result);
            }
        }
        super.objectActionResult(result, at);
    }

    private void resolveContent() {
        Naked parent = getParent().getContent().getNaked();
        if (!(parent instanceof NakedObject)) {
            parent = getParent().getParent().getContent().getNaked();
        }

        if (getContent() instanceof FieldContent) {
            NakedObjectField field = ((FieldContent) getContent()).getField();
            NakedObjects.getObjectPersistor().resolveField((NakedObject) parent, field);
        } else if (getContent() instanceof CollectionContent) {
            NakedObjects.getObjectPersistor().resolveImmediately((NakedObject) parent);
        } else if (getContent() instanceof CollectionElement) {
            NakedObjects.getObjectPersistor().resolveImmediately((NakedObject) getContent().getNaked());
        }
    }

    private void selectNode() {
        LOG.debug("node selected " + getContent().getNaked());
        ((TreeBrowserFrame) getViewAxis()).setSelectedNode(getView());
    }

    public String toString() {
        return wrappedView.toString() + "/TreeNodeBorder";
    }

    public ViewAreaType viewAreaType(Location mouseLocation) {
        int iconWidth = icon.getSize().getWidth();
        int textWidth = text.getSize().getWidth();

        Bounds bounds = new Bounds(0, 0, iconWidth + textWidth, top);
        if (bounds.contains(mouseLocation)) {
            return ViewAreaType.CONTENT;
        } else {
            return super.viewAreaType(mouseLocation);
        }
    }

    public void viewMenuOptions(MenuOptionSet options) {
        super.viewMenuOptions(options);
        TreeDisplayRules.menuOptions(options);
        
        options.add(MenuOptionSet.VIEW, new MenuOption("Select node") {
            public void execute(Workspace workspace, View view, Location at) {
                selectNode();
            }

            public String getDescription(View view) {
                return "Show this node in the right-hand pane";
            }
        });
    }

    private boolean withinBox(int x, int y) {
        return x >= BOX_X_OFFSET && x <= BOX_X_OFFSET + BOX_SIZE && y >= (top - BOX_SIZE) / 2 && y <= (top + BOX_SIZE) / 2;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */