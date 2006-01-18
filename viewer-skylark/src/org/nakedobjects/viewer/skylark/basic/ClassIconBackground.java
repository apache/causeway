package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.UserActionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.core.AbstractViewDecorator;

public class ClassIconBackground extends AbstractViewDecorator {

	protected ClassIconBackground(View wrappedView) {
		super(wrappedView);
	}
	
	public void draw(Canvas canvas) {
		int height = getSize().getHeight();
		canvas.drawSolidOval(0, 0, height * 13 / 10, height, Style.PRIMARY3);
		super.draw(canvas);
	}
	
	public void secondClick(Click click) {
		NakedObject object = ((ObjectContent) getContent()).getObject();
		Action action = object.getSpecification().getObjectAction(Action.USER, "Instances");
		NakedCollection instances = (NakedCollection) object.execute(action, null);
		Workspace workspace = getWorkspace();
        View view = workspace.createSubviewFor(instances, false);
		view.setLocation(click.getLocation());
		workspace.addView(view);
	}
	
	public void contentMenuOptions(UserActionSet options) {
    	NakedObject object = ((ObjectContent) getContent()).getObject();
		NakedClass cls = (NakedClass) object.getObject();
		OptionFactory.addClassMenuOptions(cls.forObjectType(), options);
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