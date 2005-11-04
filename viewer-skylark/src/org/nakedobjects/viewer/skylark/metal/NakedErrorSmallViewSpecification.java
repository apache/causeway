package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.object.ConcurrencyException;
import org.nakedobjects.object.NakedObjectApplicationException;
import org.nakedobjects.object.defaults.PojoAdapter;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.special.ScrollBorder;
import org.nakedobjects.viewer.skylark.util.ImageFactory;


public class NakedErrorSmallViewSpecification implements ViewSpecification {

    public boolean canDisplay(Content content) {
        return content.isObject() && content.getNaked() instanceof PojoAdapter
                && ((PojoAdapter) content.getNaked()).getObject() instanceof Exception;
    }

    public String getName() {
        return "Compact Naked Error";
    }

    public View createView(Content content, ViewAxis axis) {
        // TODO extract the 'close window' action
        ButtonAction actions[] = new ButtonAction[] { new CloseAction() };
        return new ExceptionDialogBorder(new ButtonBorder(actions, new SmallErrorView(content, this, null)), false);
    }

    public boolean isOpen() {
        return true;
    }

    public boolean isReplaceable() {
        return false;
    }

    public boolean isSubView() {
        return false;
    }

    public static class CloseAction extends AbstractButtonAction {
        public CloseAction() {
            super("Close");
        }

        public void execute(Workspace workspace, View view, Location at) {
            view.dispose();
        }
    }
}

class SmallErrorView extends AbstractErrorView {
    private static final int left = 20;
    private static final int top = 15;
    private static final int PADDING = 10;

    private static Image errorIcon;
    {
        errorIcon = ImageFactory.getInstance().createIcon("error", 32, null);
        if (errorIcon == null) {
            errorIcon = ImageFactory.getInstance().createFallbackIcon(32, null);
        }
    }

    protected SmallErrorView(Content content, ViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis);
    }

    public Size getRequiredSize() {
        Size size = new Size();

        size.extendWidth(errorIcon.getWidth());
        size.extendWidth(PADDING);
        size.extendWidth(Style.NORMAL.stringWidth(message));

        size.ensureHeight(errorIcon.getHeight());
        size.ensureHeight(Style.NORMAL.getLineHeight());

        size.extend(left * 2, top * 2);
        return size;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.clearBackground(this, Style.WHITE);

        canvas.drawIcon(errorIcon, left, top);

        int x = left + errorIcon.getWidth() + PADDING;
        int y = top + Style.NORMAL.getAscent();
        canvas.drawText(message, x, y, Color.BLACK, Style.NORMAL);
    }

    public ViewAreaType viewAreaType(Location mouseLocation) {
        return ViewAreaType.VIEW;
    }

}

class ExceptionDialogBorder extends AbstractWindowBorder {

    public ExceptionDialogBorder(View wrappedView, boolean scrollable) {
        super(scrollable ? new ScrollBorder(wrappedView) : wrappedView);
        setControls(new WindowControl[] { new CloseWindowControl(this) });
    }

    protected String title() {
        Object exception = getContent().getNaked().getObject();
        if (exception instanceof NakedObjectApplicationException) {
            return "Application Exception";
        } else if (exception instanceof ConcurrencyException) {
            return "Concurrency Exception";
        } else {
            return "System Error";
        }

    }

    public String toString() {
        return wrappedView.toString() + "/ExceptionDialogBorder [" + getSpecification() + "]";
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */