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

import org.apache.log4j.Logger;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.viewer.lightweight.AbstractCompositeView;
import org.nakedobjects.viewer.lightweight.AbstractObjectView;
import org.nakedobjects.viewer.lightweight.AbstractValueView;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.MenuOptionSet;
import org.nakedobjects.viewer.lightweight.options.RemoveAssociationOption;


public abstract class Form extends AbstractCompositeView {
	private static final Logger LOG = Logger.getLogger(Form.class);
	
    protected void init(NakedObject object) {
		objectUpdate(object);
    }
    
	public void objectMenuOptions(MenuOptionSet options) {
		super.objectMenuOptions(options);
		if(getFieldOf() != null) {
			options.add(MenuOptionSet.OBJECT, new RemoveAssociationOption());
		}
	 }  	
        	
    public void objectUpdate(NakedObject object) {
    	LOG.debug("Form update " + object);
    	
		NakedClass cls = getObject().getNakedClass();
		Field[] flds = cls.getVisibleFields(object);

		InternalView[] views = getComponents();
		
		if(flds.length == views.length) {
			// replace fields that have new references 
			for (int f = 0; f < flds.length; f++) {
				if(views[f] instanceof AbstractValueView) {
					LOG.debug("  Value refresh " + flds[f]);
					((AbstractValueView)views[f]).refresh();
					continue;
					
				} else if(views[f] instanceof CollectionView) {
					LOG.debug("  Association (one to many) refresh " + flds[f]);
					((CollectionView) views[f]).refresh();
					
				} else {
					Naked currentReference = flds[f].get(getObject());
					Naked existingReference = ((AbstractObjectView)views[f]).getObject();
					if(currentReference != existingReference) {
						InternalView newView = createFieldElement(flds[f].get(getObject()), flds[f]);
						LOG.debug("  Association changed " + flds[f]);
						replaceView(views[f], newView);
					}
				}
			}

		}
		else {
			// new view - add all fields
			for (int f = 0; f < flds.length; f++) {
				Field field = flds[f];
				addView(createFieldElement(field.get(getObject()), field));
			}
		}
		
		invalidateLayout();
		layout();
//		repaintAll();
		redraw();
    }

	public abstract InternalView createFieldElement(Naked naked, Field field);
}
