package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.defaults.Error;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.basic.LabelAxis;
import org.nakedobjects.viewer.skylark.core.AbstractView;

import java.util.StringTokenizer;


public class NakedErrorViewSpecification implements ViewSpecification {

    public boolean canDisplay(Naked object) {
        return object instanceof NakedError;
    }

    public String getName() {
        return "Naked Error";
    }

    public View createView(Content content, ViewAxis axis) {
        return new ErrorView(content, this, new LabelAxis());
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
}

class ErrorView extends AbstractView {

    protected ErrorView(Content content, ViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis);
    }
    
    public Size getRequiredSize() {
        return new Size(700, 500);
    }
    
    public void draw(Canvas canvas) {
        super.draw(canvas);
        
        int left = 0;
        int top = 0;
        int width = getSize().getWidth();
        int height = getSize().getHeight();
        
        canvas.drawSolidRectangle(left, top, width - 1, height -1, Style.WHITE);
        canvas.drawRectangle(left, top, width - 1, height - 1, Style.BLACK);

        left = 20;
        top += Style.TITLE.getHeight();
        canvas.drawText("ERROR", left, top, Style.INVALID, Style.TITLE);
        
        Error error = (Error) ((ObjectContent) getContent()).getObject();
        top += 30;
        canvas.drawText(error.getError().stringValue() ,left, top, Style.INVALID, Style.NORMAL);

        top += 30;
        canvas.drawText(error.getException().stringValue(),left, top, Style.INVALID, Style.NORMAL);

        top += 30;
        String trace = error.getTrace().stringValue();
        StringTokenizer st = new StringTokenizer(trace, "\n\r");
        while (st.hasMoreTokens()) {
        canvas.drawText(st.nextToken(),left, top, Style.INVALID, Style.NORMAL);
            top += Style.NORMAL.getHeight();
        }
        

    }
    
    public ViewAreaType viewAreaType(Location mouseLocation) {
        return ViewAreaType.VIEW;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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