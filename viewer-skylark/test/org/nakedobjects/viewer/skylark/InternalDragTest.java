package org.nakedobjects.viewer.skylark;

import junit.framework.TestCase;

import org.nakedobjects.viewer.skylark.core.AbstractView;

public class InternalDragTest extends TestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(InternalDrag.class);
	}
	
	public void testDragStart() {
		MockView view = new MockView(null);
		InternalDrag id = InternalDrag.create(view, new Location(100, 110), new Location(20, 25), 0);
		assertEquals(id, view.dragFromCall);
		
		assertEquals(new Location(20, 25), id.getRelativeLocation());
		
		id.updateLocationWithinViewer(new Location(110, 120), view, new Location(20, 35));
		
		assertEquals(new Location(20, 35), id.getRelativeLocation());
	}
	
	private static class MockView extends AbstractView {
		protected MockView(Content content) {
			super(content, null, null);
		}

		InternalDrag dragFromCall;
		
		public View dragFrom(InternalDrag drag) {
			dragFromCall = drag;
			return this;
		}
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