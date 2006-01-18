package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.AbstractUserAction;
import org.nakedobjects.viewer.skylark.UserActionSet;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.special.ScrollBorder;
import org.nakedobjects.viewer.skylark.util.ImageFactory;


public class WindowBorder extends AbstractWindowBorder {

    public WindowBorder(View wrappedView, boolean scrollable) {
        super(addTransientBorderIfNeccessary(scrollable ? new ScrollBorder(wrappedView) : wrappedView));

        if (isTransient()) {
            setControls(new WindowControl[] { new CloseWindowControl(this) });
        } else {
            setControls(new WindowControl[] { new IconizeWindowControl(this), new ResizeWindowControl(this),
                    new CloseWindowControl(this) });
        }
    }

    private static View addTransientBorderIfNeccessary(View view) {
        Content content = view.getContent();
        if (content.isPersistable() && content.isTransient()) {
            return new SaveTransientObjectBorder(view);
        } else {
            return view;
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (isTransient()) {
            int height = top - LINE_THICKNESS - 2;
            int x = getSize().getWidth() - 50;
            Image icon = ImageFactory.getInstance().createIcon("transient", height, null);
            if (icon == null) {
                canvas.drawText("*", x, getBaseline(), Style.BLACK, Style.NORMAL);
            } else {
                canvas.drawIcon(icon, x, LINE_THICKNESS + 1, height, height);
              //  canvas.drawRectangle(x, LINE_THICKNESS + 1, height, height, Color.RED);
            }
        }
    }

    private boolean isTransient() {
        Content content = getContent();
        return content.isPersistable() && content.isTransient();
    }

    private void iconize(Workspace workspace, View view) {
        view.dispose();
        workspace.addIconFor(getContent().getNaked(), getLocation());
    }

    public void viewMenuOptions(UserActionSet menuOptions) {
        super.viewMenuOptions(menuOptions);

        menuOptions.add(new AbstractUserAction("Iconize") {
            public void execute(Workspace workspace, View view, Location at) {
                iconize(workspace, view);
            }
        });
    }

    public void secondClick(Click click) {
        if (overBorder(click.getLocation())) {
            iconize(getWorkspace(), getView());
        } else {
            super.secondClick(click);
        }
    }

    protected String title() {
        return getContent().windowTitle();
    }

    public String toString() {
        return wrappedView.toString() + "/WindowBorder [" + getSpecification() + "]";
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