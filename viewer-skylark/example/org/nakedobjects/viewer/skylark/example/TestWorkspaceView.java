
package org.nakedobjects.viewer.skylark.example;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.core.AbstractView;

import java.util.Vector;


public class TestWorkspaceView extends AbstractView implements Workspace {
    private Vector views = new Vector();

    public View[] getSubviews() {
        View[] array = new View[views.size()];
        views.copyInto(array);
        return array;
    }

    protected TestWorkspaceView(Content content) {
        super(content, new TestWorkspaceSpecification(), null);
    }

    public void draw(Canvas canvas) {
        Bounds bounds = getBounds();
        canvas.drawRectangle(0, 0, bounds.getWidth(), bounds.getHeight(), Color.BLUE);
        canvas.drawText("Test Workspace", 10, 20, Color.BLUE, Style.TITLE);

        for (int i = 0; i < views.size(); i++) {
            View view = (View) views.elementAt(i);
	        Canvas subcanvas = canvas.createSubcanvas(view.getBounds());
	        view.draw(subcanvas);
        }
    }
    
    public View subviewFor(Location location) {
        for (int i = 0; i < views.size(); i++) {
            View view = (View) views.elementAt(i);
            if(view.getBounds().contains(location)) {
                return view;
            }
        }
        
        
        return null;
    }

    public Size getRequiredSize() {
        return new Size(600, 400);
    }

    public View addIconFor(Naked naked, Location at) {
        return null;
    }

    public View addOpenViewFor(Naked object, Location at) {
        return null;
    }

    public View createSubviewFor(Naked object, boolean asIcon) {
        return null;
    }

    public void lower(View view) {}

    public void raise(View view) {}

    public void removeViewsFor(NakedObject object) {}

    public void addView(View view) {
        views.addElement(view);
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