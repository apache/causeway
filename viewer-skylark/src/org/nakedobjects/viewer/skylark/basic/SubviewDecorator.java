package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.viewer.skylark.CompositeViewBuilder;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.core.AbstractBuilderDecorator;

public class SubviewDecorator extends AbstractBuilderDecorator {
	private boolean isReplaceable;

	public SubviewDecorator(CompositeViewBuilder design) {
		this(design, true);
	}
	
	public SubviewDecorator(CompositeViewBuilder design, boolean isReplaceable) {
		super(design);
		this.isReplaceable = isReplaceable;
	}
	
	public View createCompositeView(Content content, CompositeViewSpecification specification, ViewAxis axis) {
		return new Identifier(new SimpleBorder(wrappedBuilder.createCompositeView(content,specification, axis)));
	}
	
	public boolean isOpen() {
		return true;
	}

	public boolean isSubView() {
		return true;
	}
	
	public boolean isReplaceable() {
		return isReplaceable;
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