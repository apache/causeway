package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractCompositeViewSpecification;
import org.nakedobjects.viewer.skylark.special.CollectionElementBuilder;
import org.nakedobjects.viewer.skylark.special.StackLayout;
import org.nakedobjects.viewer.skylark.special.SubviewSpec;
import org.nakedobjects.viewer.skylark.util.ViewFactory;

public class ListSpecification extends AbstractCompositeViewSpecification implements SubviewSpec {

	public ListSpecification() {
		builder = new StackLayout(new CollectionElementBuilder(this, true));
	}
	
    public View createView(Content content, ViewAxis axis) {
        return new WindowBorder(super.createView(content, axis), true);
    }
	
    public View createSubview(Content content, ViewAxis axis) {
		ViewSpecification specification = ViewFactory.getViewFactory().getIconizedSubViewSpecification((ObjectContent) content);
		return specification.createView(content, axis);
	}

	public String getName() {
		return "List";
	}
	
	public boolean canDisplay(Content content) {
		return content.isCollection();
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