package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.OneToManyField;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;
import org.nakedobjects.viewer.skylark.metal.InternalCollectionIconGraphic;

public class InternalCollectionBorder extends AbstractBorder {
	private IconGraphic icon;

	protected InternalCollectionBorder(View wrappedView) {
		super(wrappedView);
		
		icon = new InternalCollectionIconGraphic(this, Style.NORMAL);
		left = icon.getSize().getWidth();
	}
	
	protected void debugDetails(StringBuffer b) {
		b.append("InternalCollectionBorder ");
    }

	public Size getRequiredSize() {
		Size size = super.getRequiredSize();
		size.ensureWidth(left + 45 + right);
		size.ensureHeight(24);
		return size;
	}
	
	public void draw(Canvas canvas) {
		icon.draw(canvas, 0, getBaseline());
		
		ObjectContent content = (ObjectContent) getContent();
		NakedCollection collection = (NakedCollection) content.getObject();
		if(collection.size() == 0) {
			canvas.drawText("empty", left, getBaseline(), Style.SECONDARY2, Style.NORMAL);
		} else {
		    int x = icon.getSize().getWidth() / 2;
		    int x2 = x + 4;
		    int y = icon.getSize().getHeight() + 1;
		    int y2 = getSize().getHeight() - 5;
		    canvas.drawLine(x, y, x, y2, Style.SECONDARY2);
		    canvas.drawLine(x, y2, x2, y2, Style.SECONDARY2);
		}
		super.draw(canvas);
	}
	
	
	public void menuOptions(MenuOptionSet options) {
        super.menuOptions(options);
        
        NakedObjectSpecification nakedClass = ((OneToManyField) getContent()).getSpecification();
        /*
        InternalCollection collection = (InternalCollection) ((OneToManyField) getContent()).getCollection();
        NakedObjectSpecification nakedClass = NakedObjectSpecificationLoader.getInstance().loadSpecification(collection.getElementSpecification().getFullName());
        */
        ClassOption.menuOptions(nakedClass, options);
    }
	
    public void objectActionResult(Naked result, Location at) {
        // same as in TreeNodeBorder
        OneToManyField internalCollectionContent = (OneToManyField) getContent();
        OneToManyAssociation field = (OneToManyAssociation) internalCollectionContent.getField();
        NakedObject target = ((ObjectContent) getParent().getContent()).getObject();
        
        Hint about = target.getHint(ClientSession.getSession(), field, (NakedObject) result);
        if(about.canUse().isAllowed()) {
//        if(field.canAssociate(target, (NakedObject) result)) {
        	target.setAssociation(field, (NakedObject) result);
        }
        super.objectActionResult(result, at);
    }

	public String toString() {
		return "InternalCollectionBorder/" + wrappedView ;
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