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

import java.util.Enumeration;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.collection.TypedCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.viewer.lightweight.AbstractCompositeView;
import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.Click;
import org.nakedobjects.viewer.lightweight.CompositeView;
import org.nakedobjects.viewer.lightweight.DragSource;
import org.nakedobjects.viewer.lightweight.DragTarget;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.util.StackLayout;


public class BrowserTree extends AbstractCompositeView implements InternalView, DragSource,
    DragTarget {
	
	private static final BrowserNode nodePrototype = new BrowserNode();

    public BrowserTree() {
        setLayout(new StackLayout(true));
		setBorder(new BrowserBorder());
    }
    
    protected void init(NakedObject object) {
        if (object instanceof TypedCollection) {
            Enumeration e = ((NakedCollection) object).elements();

            while (e.hasMoreElements()) {
                NakedObject obj = (NakedObject) e.nextElement();
                obj.resolve();
                addView((InternalView) nodePrototype.makeView(obj, null));
            }
        } else {
            NakedClass cls = object.getNakedClass();
            Field[] flds = cls.getFields();

            for (int f = 0; f < flds.length; f++) {
                Field field = flds[f];

                if ( getBrowser().getBrowserFilter().isInTree(field)) {
                    NakedObject fieldObject = (NakedObject) field.get(object);

                    if (fieldObject != null) {
                        addView((InternalView) nodePrototype.makeView(fieldObject, field));
                    }
                }
            }
        }
    }

    /**
     * Trees cannot be replaced by windows
     */
    public boolean isReplaceable() {
        return false;
    }

    public Size getRequiredSize() {
        Size size = super.getRequiredSize();
        size.ensureWidth(titleSize().getWidth());

        return size;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
   }

    public void firstClick(Click click) {
        getBrowser().setSelected(this);
    }

    public void secondClick(Click click) {
        BrowserNode replacement = (BrowserNode) nodePrototype.makeView(getObject(), getFieldOf());
        ((CompositeView) getParent()).replaceView(this, replacement);
        getBrowser().setSelected(replacement);
    }
    
	private Browser getBrowser() {
		return ((Browser) getRoot());
	}
}

