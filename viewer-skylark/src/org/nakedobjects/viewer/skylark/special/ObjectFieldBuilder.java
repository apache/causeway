package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.FieldContent;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.ObjectField;
import org.nakedobjects.viewer.skylark.OneToManyField;
import org.nakedobjects.viewer.skylark.OneToOneField;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.ValueField;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.core.AbstractViewBuilder;
import org.nakedobjects.viewer.skylark.core.CompositeObjectView;

import org.apache.log4j.Logger;


public class ObjectFieldBuilder extends AbstractViewBuilder {
    private static final Logger LOG = Logger.getLogger(ObjectFieldBuilder.class);
	private SubviewSpec subviewDesign;

    public ObjectFieldBuilder(SubviewSpec subviewDesign) {
    	this.subviewDesign = subviewDesign;
	}

    public void build(View view) {
		Assert.assertEquals(view.getView(), view);

        NakedObject object = ((ObjectContent) view.getContent()).getObject();

        LOG.debug("rebuild view " + view + " for " + object);

        NakedClass cls = object.getNakedClass();
        Field[] flds = cls.getVisibleFields(object);
       
	    if(view.getSubviews().length == 0) {
	    	newBuild(view, object, flds);
	    } else {
	    	updateBuild(view, object, flds);
	    }
    }

    public View createCompositeView(Content content, CompositeViewSpecification specification, ViewAxis axis) {
		return new CompositeObjectView(content, specification, axis);
    }

    private void newBuild(View view, NakedObject object, Field[] flds) {
        LOG.debug("build new view " + view + " for " + object);
        for (int f = 0; f < flds.length; f++) {
            Field field = flds[f];
			Naked value = field.get(object);
			ObjectField content = createContent(object, value, field);
			View fieldView = subviewDesign.createSubview(content, view.getViewAxis());
			if(fieldView != null) {
				view.addView(decorateSubview(fieldView));
			}
       }
    }

    private void updateBuild(View view, NakedObject object, Field[] flds) {
        LOG.debug("rebuild view " + view + " for " + object);

        View[] subviews = view.getSubviews();
    	
    	int fld = 0;
    	
    	for (int i = 0; i < subviews.length; i++) {
            View subview = subviews[i];
            while(((FieldContent) subview.getContent()).getField() != flds[fld]) {
                fld++;
            }
            Assert.assertTrue(fld < flds.length);
            
            Field field = flds[fld];
            Naked value = field.get(object);
            if(value instanceof NakedValue) {
    		    NakedValue existing = ((ValueContent) subview.getContent()).getValue();
    			if(value != existing) {
    			    ((ValueField) subview.getContent()).updateDerivedValue((NakedValue) value);
    			}
    		    subview.refresh();
    		} else if (value instanceof NakedCollection) {
    		    subview.update((NakedObject) value);
    		} else {
    		    NakedObject existing = ((ObjectContent) subviews[i].getContent()).getObject();
    			boolean changeToNull = value == null && existing != null;
				boolean changedFromNull = value != null && existing == null;
				if(changeToNull || changedFromNull) {
					View fieldView =	subviewDesign.createSubview(createContent(object, value, field), view.getViewAxis());
					if(fieldView != null) {
						view.replaceView(subview, decorateSubview(fieldView));
					}
    			} 
    		}
    		fld++;
        }
    }

	private ObjectField createContent(NakedObject parent, Naked object, Field field) {
		if(field == null) {
			throw new NullPointerException();
		}
		
		ObjectField content;
		if(object instanceof InternalCollection) {
			content = new OneToManyField(parent, (InternalCollection) object, (OneToManyAssociation) field);
	    } else if(object instanceof NakedObject || object == null) {
			content = new OneToOneField(parent, (NakedObject) object, (OneToOneAssociation) field);
	    } else if(object instanceof NakedValue) { 
			content = new ValueField(parent, (NakedValue) object, (Value) field);  
	    } else {
	        throw new NakedObjectRuntimeException();
	    }
	
		return content;
	}

    public View decorateSubview(View subview) {
    	return subviewDesign.decorateSubview(subview);
	}
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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
