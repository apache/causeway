package org.nakedobjects.viewer.skylark.metal;

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
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.special.ScrollBorder;
import org.nakedobjects.viewer.skylark.util.ImageFactory;


public class MessageDialogSpecification implements ViewSpecification {

    public boolean canDisplay(Content content) {
        return content instanceof MessageContent;
    }

    public String getName() {
        return "Message Dialog";
    }

    public View createView(Content content, ViewAxis axis) {
        ButtonAction actions[] = new ButtonAction[] { new CloseViewAction() };
        return new ExceptionDialogBorder(new ButtonBorder(actions, new MessageDialogView((MessageContent) content, this, null)), false);
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

    public static class CloseViewAction extends AbstractButtonAction {
        public CloseViewAction() {
            super("Close");
        }

        public void execute(Workspace workspace, View view, Location at) {
            view.dispose();
        }
    }
}

class MessageDialogView extends AbstractView {
    private static final int left = 20;
    private static final int top = 15;
    private static final int PADDING = 10;
    private Image errorIcon;

    protected MessageDialogView(MessageContent content, ViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis);
        String iconName = ((MessageContent) getContent()).getIconName();
           errorIcon = ImageFactory.getInstance().createIcon(iconName, 32, null);
        if (errorIcon == null) {
            errorIcon = ImageFactory.getInstance().createFallbackIcon(32, null);
        }
    }

    public Size getRequiredSize() {
        Size size = new Size();

        String message = ((MessageContent) getContent()).getMessage();
        
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

        String message = ((MessageContent) getContent()).getMessage();
        String heading = ((MessageContent) getContent()).title();

        canvas.clearBackground(this, Style.WHITE);

        canvas.drawIcon(errorIcon, left, top);

        int x = left + errorIcon.getWidth() + PADDING;
        int y = top + 3 + Style.NORMAL.getAscent();
        if(!heading.equals("")) {
            canvas.drawText(heading, x, y, Color.BLACK, Style.TITLE);
            y += Style.TITLE.getLineHeight();
        }
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
        return getContent().windowTitle();
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