package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.utility.UnexpectedCallException;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ContentDrag;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.special.LookupBorder;
import org.nakedobjects.viewer.skylark.util.ImageFactory;


public class EmptyField extends AbstractView {

    public static class Specification implements ViewSpecification {
        public boolean canDisplay(Content content) {
            return content == null || content.getNaked() == null;
        }

        public View createView(Content content, ViewAxis axis) {
            EmptyField emptyField = new EmptyField(content, this, axis);
            NakedObjectSpecification contentType = content.getSpecification();
            if (contentType.isLookup()) {
                //            if (((ObjectContent) content).getObject() instanceof Lookup)
                // {
                return new ObjectBorder(new LookupBorder(emptyField));
            } else {
                return new ObjectBorder(emptyField);
            }
        }

        public String getName() {
            return "empty field";
        }

        public boolean isOpen() {
            return false;
        }

        public boolean isReplaceable() {
            return true;
        }

        public boolean isSubView() {
            return true;
        }
    }

    private static Text style = Style.NORMAL;

    public EmptyField(Content content, ViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis);
        if (((ObjectContent) content).getObject() != null) {
            throw new IllegalArgumentException("Content for EmptyField must be null: " + content);
        }
        NakedObject object = ((ObjectContent) getContent()).getObject();
        if (object != null) {
            throw new IllegalArgumentException("Content for EmptyField must be null: " + object);
        }
    }

    private Consent canDrop(NakedObject dragSource) {
        ObjectContent content = (ObjectContent) getContent();
        return content.canSet(dragSource);
    }

    public void dragIn(ContentDrag drag) {
        NakedObject source = ((ObjectContent) drag.getSourceContent()).getObject();
        Consent perm = canDrop(source);
        if (perm.getReason() != null) {
            getViewManager().setStatus(perm.getReason());
        }

        if (perm.isAllowed()) {
            getState().setCanDrop();
        } else {
            getState().setCantDrop();
        }

        markDamaged();
    }

    public void dragOut(ContentDrag drag) {
        getState().clearObjectIdentified();
        markDamaged();
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        Color color;

        if (getState().canDrop()) {
            color = Style.VALID;
        } else if (getState().cantDrop()) {
            color = Style.INVALID;
        } else if (getState().isViewIdentified()) {
            color = Style.PRIMARY1;
        } else {
            color = Style.SECONDARY1;
        }

        int iconHeight = (style.getHeight() * 120) / 100;
        int iconWidth = (iconHeight * 80) / 100;
        int containerHeight = getSize().getHeight();
        int iconCentre = containerHeight / 2;

        int xt = iconWidth + (HPADDING * 2);
        int yt = getBaseline();

        int xi = getPadding().getLeft() + HPADDING;
        int yi = iconCentre - (iconHeight / 2);

        Image icon = ImageFactory.getInstance().createIcon("emptyField", iconHeight, null);
        if (icon == null) {
            canvas.drawSolidOval(xi, yi, iconWidth, iconHeight, color);
        } else {
            canvas.drawIcon(icon, xi, yi, iconWidth, iconHeight);
        }

        canvas.drawText(name(), xt, yt, color, style);

        if (AbstractView.DEBUG) {
            Size size = getSize();
            canvas.drawRectangle(0, 0, size.getWidth() - 1, size.getHeight() - 1, Color.DEBUG_VIEW_BOUNDS);
            canvas.drawLine(0, size.getHeight() / 2, size.getWidth() - 1, size.getHeight() / 2, Color.DEBUG_VIEW_BOUNDS);
            canvas.drawLine(0, getBaseline(), size.getWidth() - 1, getBaseline(), Color.DEBUG_BASELINE);
        }
    }

    public void drop(ContentDrag drag) {
        NakedObject target = ((ObjectContent) getParent().getContent()).getObject();
        NakedObject source = ((ObjectContent) drag.getSourceContent()).getObject();

        setField(target, source);
    }

    /**
     * @see View#getBaseline()
     */
    public int getBaseline() {
        return style.getAscent() + VPADDING;
    }

    public Size getRequiredSize() {
        Size size = new Size(0, 0);
        int iconHeight = (style.getHeight() * 120) / 100;
        size.extendWidth((iconHeight * 80) / 100);
        size.extendWidth(HPADDING * 3);
        size.extendWidth(style.stringWidth(name()));

        size.setHeight(iconHeight);
        size.extendHeight(VPADDING * 2);
        return size;
    }

    public void contentMenuOptions(MenuOptionSet options) {
        getContent().menuOptions(options);
        options.setColor(Style.CONTENT_MENU);
    }

    private String name() {
        return ((ObjectContent) getContent()).getSpecification().getSingularName();
    }

    /**
     * Objects returned by menus are used to set this field before passing the
     * call on to the parent.
     */
    public void objectActionResult(Naked result, Location at) {
        NakedObject target = ((ObjectContent) getParent().getContent()).getObject();
        setField(target, (NakedObject) result);
        super.objectActionResult(result, at);
    }

    private void setField(NakedObject parent, NakedObject object) {
        // TODO deal with the dropping of a NakedClass
        if (object instanceof NakedClass) {
            throw new UnexpectedCallException("Not ready for NakedClasses");
        }

        if (canDrop(object).isAllowed()) {
            ((ObjectContent) getContent()).setObject(object);

            getParent().invalidateContent();
            /*
             * boolean isNotPersistent = parent.getOid() == null; if
             * (isNotPersistent) { getParent().invalidateContent(); }
             */
        }
    }

    protected String title() {
        return name();
    }

    public String toString() {
        return "EmptyField" + getId();
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
