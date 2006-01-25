package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.utility.DebugString;
import org.nakedobjects.viewer.skylark.AbstractUserAction;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Offset;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.UserActionSet;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewDrag;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.basic.IconGraphic;
import org.nakedobjects.viewer.skylark.basic.ObjectTitleText;


public class MinimizedView extends AbstractView {

    private final View viewToMinimize;
    private IconGraphic icon;
    private ObjectTitleText text;

    public MinimizedView(View viewToMinimize) {
        super(viewToMinimize.getContent(), null, null);
        this.viewToMinimize = viewToMinimize;
        icon = new IconGraphic(this, Style.NORMAL);
        text = new ObjectTitleText(this, Style.NORMAL);
    }

    public String debugDetails() {
        DebugString b = new DebugString();
        b.append(super.debugDetails());
        b.appendln(0, "minimized view:  " + viewToMinimize);
        return b.toString();
    }
    
    public void dispose() {
        super.dispose();
        viewToMinimize.dispose();
    }

    public Drag dragStart(DragStart drag) {
        View dragOverlay = new DragViewOutline(getView());
        return new ViewDrag(this, new Offset(drag.getLocation()), dragOverlay);
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        
        Size size = getSize();
        int width = size.getWidth();
        int height = size.getHeight();
        
        boolean hasFocus = containsFocus();
        Color darkColor =hasFocus ? Style.SECONDARY1 : Style.SECONDARY2;
        Color lightColor = hasFocus ? Style.SECONDARY2:  Style.SECONDARY3;

        canvas.clearBackground(this, Style.WINDOW_BACKGROUND);
        canvas.drawRectangle(0, 0, width, height, darkColor);
        canvas.drawSolidRectangle(1, 1, width - 2, 4, lightColor);
        canvas.drawLine(1, 4, width - 2, 4, Style.PRIMARY1);
            
        int baseline = icon.getBaseline() + 7;
        icon.draw(canvas, 3, baseline);
        text.draw(canvas, 3 + icon.getSize().getWidth(), baseline);
    }

    public Size getRequiredSize() {
        Size size = new Size();
        
        size.extendWidth(3);
        size.extendWidth(icon.getSize().getWidth());
        size.extendWidth(text.getSize().getWidth());
        size.extendWidth(20);
        
        size.extendHeight(5);
        size.extendHeight(icon.getSize().getHeight());
        size.extendHeight(15);

        return size;
    }

    private void restore() {
        Workspace workspace = getWorkspace();
        
        
        View[] views = workspace.getSubviews();
        for (int i = 0; i < views.length; i++) {
            if (views[i] == this) {
                viewToMinimize.setParent(workspace);
                viewToMinimize.setLocation(getLocation());
                workspace.removeView(this);
                workspace.addView(viewToMinimize);
                workspace.invalidateLayout();
                return;
            }
        }
    }

    public void secondClick(Click click) {
        restore();
    }

    public ViewAreaType viewAreaType(Location location) {
        return ViewAreaType.VIEW;
    }

    public void viewMenuOptions(UserActionSet options) {
        options.add(new AbstractUserAction("Restore") {

            public void execute(Workspace workspace, View view, Location at) {
                restore();
            }
        });
        super.viewMenuOptions(options);
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