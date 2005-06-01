package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.Offset;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewDrag;
import org.nakedobjects.viewer.skylark.ViewState;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.basic.RootIconSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;
import org.nakedobjects.viewer.skylark.core.DragViewOutline;
import org.nakedobjects.viewer.skylark.special.ScrollBorder;
import org.nakedobjects.viewer.skylark.util.ImageFactory;


public class WindowBorder extends AbstractBorder {
    private final static int BUTTON_HEIGHT = 13;
    private final static int BUTTON_WIDTH = BUTTON_HEIGHT + 2;
    private final static int LINE_THICKNESS = 5;
    private final static Text TITLE_STYLE = Style.TITLE;
    private final int baseline;
    private final int padding = 2;
    private final int titlebarHeight;

    public WindowBorder(View wrappedView, boolean scrollable) {
        super(addTransientBorderIfNeccessary(scrollable ? new ScrollBorder(wrappedView) : wrappedView));

        titlebarHeight = Math.max(BUTTON_HEIGHT + 2 * VPADDING + TITLE_STYLE.getDescent(), TITLE_STYLE.getTextHeight());
        baseline = LINE_THICKNESS + padding + BUTTON_HEIGHT;

        left = LINE_THICKNESS;
        right = LINE_THICKNESS;
        top = LINE_THICKNESS + titlebarHeight;
        bottom = LINE_THICKNESS;
    }
    
    private static View addTransientBorderIfNeccessary(View view) {
        Content content = view.getContent();
        if(content.isPersistable() && content.isTransient()) {
            return new SaveTransientObjectBorder(view);
        } else {
            return view;
        }
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

    public void draw(Canvas canvas) {
        Size s = getSize();
        int x = left;
        int y = titlebarHeight;
        int width = s.getWidth();
        int height = s.getHeight();

        // blank background
        canvas.clearBackground(this, Style.background(getSpecification()));

        // slightly rounded grey border
        canvas.drawRectangle(1, 0, width - 3, height - 1, Style.SECONDARY1);
        canvas.drawRectangle(0, 1, width - 1, height - 3, Style.SECONDARY1);

        for (int i = 2; i < left; i++) {
            canvas.drawRectangle(i, i, width - 2 * i - 1, height - 2 * i - 1, Style.SECONDARY1);
        }

		ViewState state = getState();
        if(state.isActive()) {
            int i = left;
            canvas.drawRectangle(i, top, width - 2 * i - 1, height - 2 * i - 1 - top, Style.ACTIVE);
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

        canvas.drawSolidRectangle(left, LINE_THICKNESS, width - left - right, titlebarHeight, Style.SECONDARY2);
        canvas.drawLine(x, top - 1, width - right - 1, top - 1, Style.SECONDARY1);

        canvas.drawText(getContent().windowTitle(), x + HPADDING, baseline, Style.BLACK, TITLE_STYLE);

        x = width - right - 4 * (BUTTON_WIDTH + padding) - 1;
        y = LINE_THICKNESS + padding;
        int w = BUTTON_WIDTH - 1;
        int h = BUTTON_HEIGHT - 1;

        // transient marker
        if (isTransient()) {
            Image icon = ImageFactory.getInstance().createIcon("transient", BUTTON_HEIGHT, null);
            if (icon == null) {
                canvas.drawText("*", x, y, Style.BLACK, Style.NORMAL);
            } else {
                canvas.drawIcon(icon, x, y, BUTTON_HEIGHT, BUTTON_HEIGHT);
            }
            //canvas.drawRectangle(x, y, w, h, Style.WHITE);
        }
        
        if(!isTransient()) {
	        // window buttons
	        x += BUTTON_WIDTH + padding;
	        canvas.drawRectangle(x + 1, y + 1, w, h, Style.WHITE);
	        canvas.drawRectangle(x, y, w, h, Style.BLACK);
	        canvas.drawLine(x + 3, y + 9, x + 8, y + 9, Style.BLACK);
	        canvas.drawLine(x + 3, y + 10, x + 8, y + 10, Style.BLACK);
	
	        x += BUTTON_WIDTH + padding;
	        canvas.drawRectangle(x + 1, y + 1, w, h, Style.WHITE);
	        canvas.drawRectangle(x, y, w, h, Style.SECONDARY1);
	        canvas.drawRectangle(x + 3, y + 2, 8, 8, Style.SECONDARY2);
	        canvas.drawLine(x + 3, y + 3, x + 10, y + 3, Style.SECONDARY2);
	
	        // close button
	        x += BUTTON_WIDTH + padding;
	        canvas.drawRectangle(x + 1, y + 1, w, h, Style.WHITE);
	        canvas.drawRectangle(x, y, w, h, Style.SECONDARY1);
	        Color crossColor = isTransient() ? Style.SECONDARY1 : Style.BLACK;
	        canvas.drawLine(x + 4, y + 3, x + 10, y + 9, crossColor);
	        canvas.drawLine(x + 5, y + 3, x + 11, y + 9, crossColor);
	        canvas.drawLine(x + 10, y + 3, x + 4, y + 9, crossColor);
	        canvas.drawLine(x + 11, y + 3, x + 5, y + 9, crossColor);
        }
        
        // components
        super.draw(canvas);
    }

    private boolean isTransient() {
        Content content = getContent();
        return content.isPersistable() && content.isTransient();
    }

    public void firstClick(Click click) {
        if (!isTransient()) {
            Bounds buttonBounds = new Bounds(getSize().getWidth() - right - 3 * (BUTTON_WIDTH + padding) - 1, LINE_THICKNESS + padding,
                    BUTTON_WIDTH, BUTTON_WIDTH);
            if (buttonBounds.contains(click.getLocation())) {
                View iconView = new RootIconSpecification().createView(getContent(), null);
                iconView.setLocation(getView().getLocation());
                getWorkspace().removeView(getView());
                getWorkspace().addView(iconView);
                return;
            }

            buttonBounds.translate(BUTTON_WIDTH + padding, 0);
            buttonBounds.translate(BUTTON_WIDTH + padding, 0);
            if (buttonBounds.contains(click.getLocation())) {
                dispose();
                return;
            }
        }

        if (overBorder(click.getLocation())) {
            Workspace workspace = getWorkspace();
            if (workspace != null) {
                if (click.button2()) {
                    workspace.lower(getView());
                } else if (click.button1()) {
                    workspace.raise(getView());
                }
            }
        }

        super.firstClick(click);
    }

    public int getBaseline() {
        return wrappedView.getBaseline() + baseline + titlebarHeight;
    }

    public Size getRequiredSize() {
        Size size = super.getRequiredSize();

        size.ensureWidth(left + TITLE_STYLE.stringWidth(getContent().windowTitle()) + padding + 4 * (padding + BUTTON_WIDTH)
                + right);
        return size;
    }

    private void iconize(Workspace workspace, View view) {
        workspace.removeView(view);
        workspace.addIconFor(getContent().getNaked(), getLocation());
    }

    public void viewMenuOptions(MenuOptionSet menuOptions) {
        super.viewMenuOptions(menuOptions);

        menuOptions.add(MenuOptionSet.VIEW, new MenuOption("Iconize") {
            public void execute(Workspace workspace, View view, Location at) {
                iconize(workspace, view);
            }

        });
    }

    public void secondClick(Click click) {

        if(overBorder(click.getLocation())) {
            iconize(getWorkspace(), getView());
        } else {
            super.secondClick(click);
        }
    }

    public String toString() {
        return wrappedView.toString() + "/WindowBorder [" + getSpecification() + "]";
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