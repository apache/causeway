/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    The authors can be contacted via www.nakedobjects.org (the
    registered address of Naked Objects Group is Kingsway House, 123 Goldworth
    Road, Woking GU21 1NR, UK).
*/
package org.nakedobjects.viewer.lightweight.view;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.viewer.lightweight.AbstractCompositeView;
import org.nakedobjects.viewer.lightweight.AbstractObjectView;
import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.DragSource;
import org.nakedobjects.viewer.lightweight.DragTarget;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.RootView;
import org.nakedobjects.viewer.lightweight.Style;


public class Browser extends AbstractCompositeView implements RootView, DragSource, DragTarget {
	private BrowserTree treePrototype = new BrowserTree();
    private AbstractObjectView selected;
    private BrowserFilter browserFilter;
    private InternalView objectView;

    public Browser() {
        setLayout(new BrowserLayout());
        setBorder(new RootBorder());

        browserFilter = new BrowserFilter() {
                    public boolean isInTree(Field field) {
                        return ! field.isValue();  // all excpet value fields
                        //return field.isAggregate(); // InternalCollections Only
                    }
                };
    }

    public String getName() {
        return "Tree Browser";
    }

    protected boolean includeTitle() {
    	return false;
	}
    
    protected boolean includeIcon() {
		return false;
	}
    
    public void setSelected(AbstractObjectView element) {
        if (!(element.getObject() instanceof InternalCollection)) {
            selected = element;
            show(selected.getObject());
        }
    }

    public boolean isSelected(AbstractObjectView element) {
        return selected == element;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        // divider
        if (getComponents().length > 1) {
            int y1 = getPadding().getTop() - 1;
            int y2 = getSize().getHeight() - getPadding().getBottom();

            int x = getComponents()[1].getLocation().getX();
            canvas.drawLine(x, y1, x, y2, Style.FEINT);
            canvas.drawLine(x + 1, y1, x + 1, y2, Style.FEINT);
            canvas.drawLine(x + 2, y1, x + 2, y2, Style.FEINT);
        }
    }

    protected void init(NakedObject object) {
		addView((InternalView) treePrototype.makeView(object, null));	
    }
    
    protected BrowserFilter getBrowserFilter() {
        return browserFilter;
    }

    private void show(NakedObject object) {
        BrowserForm form = new BrowserForm(browserFilter);
        form.init(object);

	//    	InternalView form = ViewFactory.getViewFactory().createInternalView(object, null, false);
    	
        if (objectView == null) {
            addView(form);
        } else {
            replaceView(objectView, form);
        }

        objectView = form;
    }
}
