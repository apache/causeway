package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.viewer.skylark.CollectionContent;
import org.nakedobjects.viewer.skylark.CollectionElement;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.OneToManyField;
import org.nakedobjects.viewer.skylark.OneToManyFieldElement;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.core.AbstractViewBuilder;
import org.nakedobjects.viewer.skylark.core.CompositeView;

import java.util.Enumeration;

import org.apache.log4j.Logger;

public class CollectionElementBuilder extends AbstractViewBuilder {
    private static final Logger LOG = Logger.getLogger(CollectionElementBuilder.class);
	private boolean canDragView = true;
	private SubviewSpec subviewDesign;
    private boolean showAll;

    public CollectionElementBuilder(SubviewSpec subviewDesign, boolean showAll) {
    	this.subviewDesign = subviewDesign;
    	this.showAll = showAll;
	}
	
    public void build(View view) {
		Assert.assertEquals(view.getView(), view);

		Content content = view.getContent();
		OneToManyAssociation field = content instanceof OneToManyField ? ((OneToManyField) content).getOneToManyAssociation() : null;

        LOG.debug("rebuild view " + view + " for " + content);

        CollectionContent collectionContent = ((CollectionContent) content);
         Enumeration elements;
        if(showAll) {
            elements = collectionContent.allElements();
        } else {
             elements = collectionContent.allElements();
        }
         
        /*
         * remove all subviews from the view and then work through the elements of the
         * collection adding in a view for each element.  Where a subview for the that 
         * element already exists it should be reused. 
         */
        View[] subviews = view.getSubviews();
        Naked[] existingElements = new NakedObject[subviews.length];
        for (int i = 0; i < subviews.length; i++) {
            view.removeView(subviews[i]);
			existingElements[i] = subviews[i].getContent().getNaked();
		}

        while (elements.hasMoreElements()) {
            NakedObject element = (NakedObject) elements.nextElement();
            View elementView = null;
        	for (int i = 0; i < subviews.length; i++) {
				if(existingElements[i] == element) {
					elementView = subviews[i];
					break;
				}
			}
        	if(elementView == null) {
        		Content elementContent ;
        		if(field == null) {
        			elementContent = new CollectionElement(element);
        		} else {
        		    Naked obj = view.getParent().getContent().getNaked();
        		    NakedObject parent = (NakedObject) obj;
        			elementContent = new OneToManyFieldElement(parent, element, field);
        		}
        		elementView = subviewDesign.createSubview(elementContent, view.getViewAxis());
        	}
        	if(elementView != null) {
        	    view.addView(elementView);
        	}
        }
    }

	public View createCompositeView(Content content, CompositeViewSpecification specification, ViewAxis axis) {
    	CompositeView view = new CompositeView(content, specification, axis);
    	view.setCanDragView(canDragView);
		return view;
    }

	public void setCanDragView(boolean canDragView) {
		this.canDragView = canDragView;
	}
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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