package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.CollectionContent;
import org.nakedobjects.viewer.skylark.CollectionElement;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.FieldContent;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.OneToManyField;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.basic.IconGraphic;
import org.nakedobjects.viewer.skylark.basic.ObjectTitleText;
import org.nakedobjects.viewer.skylark.basic.TitleText;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;
import org.nakedobjects.viewer.skylark.core.AbstractView;


public class TreeNodeBorder extends AbstractBorder {
    private static final int BOX_PADDING = 2;
    private static final int BOX_X_OFFSET = 5;
    private static final int BOX_SIZE = 8;
    private final static Text LABEL_STYLE = Style.NORMAL;
    private int baseline;
    private ViewSpecification replaceWithSpecification;
    private IconGraphic icon;
    private TitleText text;

    public TreeNodeBorder(View wrappedView, ViewSpecification replaceWith) {
        super(wrappedView);

        replaceWithSpecification = replaceWith;

        if (getContent() instanceof OneToManyField) {
            String type = ((OneToManyField) getContent()).getSpecification().getFullName();
            final NakedObjectSpecification nc = NakedObjects.getSpecificationLoader().loadSpecification(type);
            icon = new IconGraphic(this, LABEL_STYLE) {
                protected String iconName(NakedObject object) {
                    return nc.getShortName();
                }
            };
            text = new TitleText(this, LABEL_STYLE) {
                protected String title() {
                    return nc.getPluralName();
                }
            };

        } else {
            icon = new IconGraphic(this, LABEL_STYLE);
            text = new ObjectTitleText(this, LABEL_STYLE);
        }
        int height = icon.getSize().getHeight();

        baseline = icon.getBaseline();

        left = 22;
        right = 0;
        top = height;
        bottom = 0;
    }

    public void debugDetails(StringBuffer b) {
        b.append("TreeNodeBorder " + left + " pixels\n");
        b.append("           titlebar " + (top) + " pixels\n");
        b.append("           replace with  " + replaceWithSpecification);
    }

    public void draw(Canvas canvas) {
        boolean isOpen = getSpecification().isOpen();
        boolean canOpen = isOpen || canClick();

        // blank background
   //     canvas.drawSolidRectangle(0, 0, getSize().getWidth() - 1, getSize().getHeight() - 1, Style.background(getSpecification()));

        if (((TreeBrowserFrame) getViewAxis()).getSelectedNode() == getView()) {
            canvas.drawSolidRectangle(left, 0, getSize().getWidth() - left - 1, top - 1, Style.PRIMARY2);
        }
        if (getState().isObjectIdentified()) {
            canvas.drawRectangle(left, 0, getSize().getWidth() - left - 1, top - 1, Style.SECONDARY2);
        }

        // lines
        int x = 0;
        int y = top / 2;
        canvas.drawLine(x, y, x + left, y, Style.SECONDARY2);
        if (canOpen) {
            x += BOX_X_OFFSET;
            canvas.drawLine(x, y, x + BOX_SIZE, y, Style.SECONDARY3);
            canvas.drawRectangle(x, y - BOX_SIZE / 2, BOX_SIZE, BOX_SIZE, Style.SECONDARY1);
            canvas.drawLine(x + BOX_PADDING, y, x + BOX_SIZE - BOX_PADDING, y, Style.BLACK);
            if (!isOpen) {
                x += BOX_SIZE / 2;
                canvas.drawLine(x, y - BOX_SIZE / 2 + BOX_PADDING, x, y + BOX_SIZE / 2 - BOX_PADDING, Style.BLACK);
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
        x = left;
        icon.draw(canvas, x, baseline);
        x += icon.getSize().getWidth();
        text.draw(canvas, x, baseline);

        if (AbstractView.debug) {
            canvas.drawRectangleAround(this, Color.DEBUG_BASELINE);
        }

        // draw components
        super.draw(canvas);
    }

    public void firstClick(Click click) {
        int x = click.getLocation().getX();
        int y = click.getLocation().getY();
        
        if (withinBox(x, y) && canClick()) {
            resolveContent();
            View newView = replaceWithSpecification.createView(getContent(), getViewAxis());
            getParent().replaceView(getView(), newView);
        } else if (y < top && x > left) {
            ((TreeBrowserFrame) getViewAxis()).setSelectedNode(getView());
        } else {
            super.firstClick(click);
        }
    }

    private void resolveContent() {
        Naked parent = getParent().getContent().getNaked();
        if(!(parent instanceof NakedObject)) {
            parent = getParent().getParent().getContent().getNaked();
        }
        
        if(getContent() instanceof FieldContent) {
            NakedObjectField field = ((FieldContent) getContent()).getFieldReflector();
            NakedObjects.getObjectManager().resolveEagerly((NakedObject) parent, field);
        } else if(getContent() instanceof CollectionContent) {
            NakedObjects.getObjectManager().resolveImmediately((NakedObject) parent);
        } else if(getContent() instanceof CollectionElement) {
            NakedObjects.getObjectManager().resolveImmediately((NakedObject) getContent().getNaked());
        }
    }

    private boolean canClick() {
    	return ((TreeNodeSpecification) getSpecification()).canOpen(getContent());
    //    return true;
    }

    private boolean withinBox(int x, int y) {
        return x >= BOX_X_OFFSET
                && x <= BOX_X_OFFSET + BOX_SIZE && y >= (top - BOX_SIZE) / 2 && y <= (top + BOX_SIZE) / 2;
    }

    public int getBaseline() {
        return wrappedView.getBaseline() + baseline;
    }

    public Size getRequiredSize() {
        Size size = super.getRequiredSize();

        size.ensureWidth(left + icon.getSize().getWidth() + text.getSize().getWidth() + right);
        return size;
    }

    public String toString() {
        return wrappedView.toString() + "/TreeNodeBorder";
    }

 /*    public ViewAreaType viewAreaType(Location location) {
        View subview = subviewFor(location);
        if(subview == null) {
            Size size = wrappedView.getSize();
            Bounds bounds = new Bounds(getLeft(), getTop(), size.getWidth(), size.getHeight());
            if (bounds.contains(location)) {
                return ViewAreaType.CONTENT;
            } else {
                return ViewAreaType.VIEW;
            }
        } else {
            location.subtract(subview.getLocation());
            return subview.viewAreaType(location);
        }
        /*
        Size size = wrappedView.getSize();
        Bounds bounds = new Bounds(getLeft(), getTop(), size.getWidth(), size.getHeight());

/*        int iconWidth = icon.getSize().getWidth();
        int textWidth = text.getSize().getWidth();

        Bounds bounds = new Bounds(0, 0, iconWidth + textWidth, top);
* /
        if (bounds.contains(location)) {
            return ViewAreaType.CONTENT;
        } else {
            return super.viewAreaType(location);

        }
        */
 //   }
    
    
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
    }
    
    public void objectActionResult(Naked result, Location at) {
        if (getContent() instanceof OneToManyField) {
            // same as InternalCollectionBorder
            OneToManyField internalCollectionContent = (OneToManyField) getContent();
            OneToManyAssociation field = internalCollectionContent.getOneToManyAssociation();
            NakedObject target = ((ObjectContent) getParent().getContent()).getObject();

            Hint about = target.getHint(field, result);
            if (about.canUse().isAllowed()) {
                //	          if(field.canAssociate(target, (NakedObject) result)) {
                target.setAssociation(field, (NakedObject) result);
            }
        }
        super.objectActionResult(result, at);
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

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */