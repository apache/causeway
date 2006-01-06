package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Offset;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewDrag;
import org.nakedobjects.viewer.skylark.ViewState;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;
import org.nakedobjects.viewer.skylark.core.DragViewOutline;


public abstract class AbstractWindowBorder extends AbstractBorder {
    final protected static int LINE_THICKNESS = 5;
    private final static Text TITLE_STYLE = Style.TITLE;
    private final int baseline;
    private final int padding = 2;
    private final int titlebarHeight;
    private WindowControl controls[];

    public AbstractWindowBorder(View enclosedView) {
        super(enclosedView);

        titlebarHeight = Math.max(WindowControl.HEIGHT + 2 * VPADDING + TITLE_STYLE.getDescent(), TITLE_STYLE.getTextHeight());
        baseline = LINE_THICKNESS + padding + WindowControl.HEIGHT;

        left = LINE_THICKNESS;
        right = LINE_THICKNESS;
        top = LINE_THICKNESS + titlebarHeight;
        bottom = LINE_THICKNESS;
    }

    public void debugDetails(StringBuffer b) {
        b.append("WindowBorder " + left + " pixels\n");
        b.append("           titlebar " + (top - titlebarHeight) + " pixels");
        super.debugDetails(b);
    }

    public Drag dragStart(DragStart drag) {
        if (overBorder(drag.getLocation())) {
            Location location = drag.getLocation();
            DragViewOutline dragOverlay = new DragViewOutline(getView());
            return new ViewDrag(this, new Offset(location.getX(), location.getY()), dragOverlay);
        } else {
            return super.dragStart(drag);
        }
    }

    protected void setControls(WindowControl[] controls) {
        this.controls = controls;
    }

    public void setSize(Size size) {
        super.setSize(size);
        layoutControls(size.getWidth());
    }

    public void setBounds(Bounds bounds) {
        super.setBounds(bounds);
        layoutControls(bounds.getWidth());
    }

    private void layoutControls(int width) {
        width -= getPadding().getRight();
        int x = width - (WindowControl.WIDTH + HPADDING) * controls.length;
        int y = LINE_THICKNESS + VPADDING;

        for (int i = 0; i < controls.length; i++) {
            controls[i].setSize(controls[i].getRequiredSize());
            controls[i].setLocation(new Location(x, y));
            x += controls[i].getSize().getWidth() + HPADDING;
        }
    }

    public void draw(Canvas canvas) {
        Size s = getSize();
        int x = left;
        int width = s.getWidth();
        int height = s.getHeight();

        // blank background
        Bounds bounds = getBounds();
        canvas.drawSolidRectangle(3, 3, bounds.getWidth() - 6, bounds.getHeight() - 6, Style.background(getSpecification()));

        // slightly rounded grey border
        canvas.drawRectangle(1, 0, width - 2, height, Style.SECONDARY1);
        canvas.drawRectangle(0, 1, width, height - 2, Style.SECONDARY1);

        for (int i = 2; i < left; i++) {
            canvas.drawRectangle(i, i, width - 2 * i, height - 2 * i, Style.SECONDARY1);
        }

        ViewState state = getState();
        if (state.isActive()) {
            int i = left;
            canvas.drawRectangle(i, top, width - 2 * i, height - 2 * i - top, Style.ACTIVE);
        }

        // vertical lines within border
        canvas.drawLine(2, 15, 2, height - 15, Style.BLACK);
        canvas.drawLine(3, 16, 3, height - 14, Style.PRIMARY1);
        canvas.drawLine(width - 3, 15, width - 3, height - 15, Style.BLACK);
        canvas.drawLine(width - 2, 16, width - 2, height - 14, Style.PRIMARY1);

        // horizontal lines within border
        canvas.drawLine(15, 2, width - 15, 2, Style.BLACK);
        canvas.drawLine(16, 3, width - 14, 3, Style.PRIMARY1);
        canvas.drawLine(15, height - 3, width - 15, height - 3, Style.BLACK);
        canvas.drawLine(16, height - 2, width - 14, height - 2, Style.PRIMARY1);

        canvas.drawSolidRectangle(left, LINE_THICKNESS, width - left - right, titlebarHeight,
                getState().isRootViewIdentified() ? Style.PRIMARY2 : Style.SECONDARY2);
        int y = LINE_THICKNESS + titlebarHeight - 1;
        canvas.drawLine(x, y, width - right - 1, y, Style.SECONDARY1);

        canvas.drawText(title(), x + HPADDING, baseline, Style.BLACK, TITLE_STYLE);

        for (int i = 0; controls != null && i < controls.length; i++) {
            Canvas controlCanvas = canvas.createSubcanvas(controls[i].getBounds());
            controls[i].draw(controlCanvas);
        }

        super.draw(canvas);
    }

    protected abstract String title();

    public Size getRequiredSize() {
        Size size = super.getRequiredSize();
        int width = getLeft() + HPADDING + TITLE_STYLE.stringWidth(title()) + HPADDING + controls.length
                * (WindowControl.WIDTH + HPADDING) + HPADDING + getRight();
        size.ensureWidth(width);
        return size;
    }

    public void secondClick(Click click) {
        View button = overControl(click.getLocation());
        if (button == null) {
            super.secondClick(click);
        }
    }

    public void thirdClick(Click click) {
        View button = overControl(click.getLocation());
        if (button == null) {
            super.thirdClick(click);
        }
    }

    public void firstClick(Click click) {
        View button = overControl(click.getLocation());
        if (button == null) {
            if (overBorder(click.getLocation())) {
                Workspace workspace = getWorkspace();
                if (workspace != null) {
                    if (click.button2()) {
                        workspace.lower(getView());
                    } else if (click.button1()) {
                        workspace.raise(getView());
                    }
                }
            } else {
                super.firstClick(click);
            }

        } else {
            button.firstClick(click);
        }

    }

    private View overControl(Location location) {
        for (int i = 0; i < controls.length; i++) {
            WindowControl control = controls[i];
            if (control.getBounds().contains(location)) {
                return control;
            }
        }
        return null;
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