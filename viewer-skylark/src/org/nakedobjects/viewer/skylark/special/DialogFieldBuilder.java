package org.nakedobjects.viewer.skylark.special;

import org.apache.log4j.Logger;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.basic.ActionContent;
import org.nakedobjects.viewer.skylark.core.AbstractViewBuilder;
import org.nakedobjects.viewer.skylark.core.CompositeObjectView;


public class DialogFieldBuilder extends AbstractViewBuilder {
    private static final Logger LOG = Logger.getLogger(DialogFieldBuilder.class);
	private SubviewSpec subviewDesign;

    public DialogFieldBuilder(SubviewSpec subviewDesign) {
    	this.subviewDesign = subviewDesign;
	}

    public void build(View view) {
		Assert.assertEquals(view.getView(), view);

        Action action = ((ActionContent) view.getContent()).getAction();

        LOG.debug("rebuild view " + view + " for " + action);

        NakedClass[] flds = action.parameters();
        for (int f = 0; f < flds.length; f++) {
            NakedClass field = flds[f];
			
			View fieldView = subviewDesign.createSubview(new ParameterContent(), view.getViewAxis());
			if(fieldView != null) {
				view.addView(decorateSubview(fieldView));
			}
       }
    }

    public View createCompositeView(Content content, CompositeViewSpecification specification, ViewAxis axis) {
		return new CompositeObjectView(content, specification, axis);
    }
/*
    private void newBuild(View view, NakedObject object, Field[] flds) {
       
    }

    private void updateBuild(View view, NakedObject object, Field[] flds) {
    	View[] subviews = view.getSubviews();
     	for (int f = 0; f < flds.length; f++) {
    		Field field = flds[f];
    		Naked value = field.get(object);
    		
    		if(value instanceof NakedObject) {
    			
    			
    			NakedObject existing = ((ObjectContent) subviews[f].getContent()).getObject();
    			boolean changeToNull = value == null && existing != null;
				boolean changedFromNull = value != null && existing == null;
				if(changeToNull || changedFromNull) {
					View fieldView =	subviewDesign.createSubview(createContent(value, field), view.getViewAxis());
					if(fieldView != null) {
						view.replaceView(subviews[f], decorateSubview(fieldView));
					}
    			} else {
    				continue;
    			}
    		}
    	}
    }

	private Content createContent(Naked object, Field field) {
		if(field == null) {
			throw new NullPointerException();
		}
		
		Content content;
		if(object instanceof InternalCollection) {
			content = new InternalCollectionContent((InternalCollection) object, (OneToManyAssociation) field);
	    } else if(object instanceof NakedObject) {
			content = new OneToOneContent((NakedObject) object, (OneToOneAssociation) field);
	    } else if(object instanceof NakedValue) { 
			content = new ValueContent((NakedValue) object, (Value) field);  
	    } else {
			content = new OneToOneContent(null, (OneToOneAssociation) field);
	    }
	
		return content;
	}
*/
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
