package org.nakedobjects.viewer.skylark;

import junit.framework.TestCase;

import org.nakedobjects.viewer.skylark.core.AbstractView;

public class InternalDragTest extends TestCase {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(InternalDrag.class);
	}
	
	public void testDragStart() {
		MockView view = new MockView(null);
		InternalDrag id = InternalDrag.create(view, new Location(100, 110), 0);
		assertEquals(id, view.dragFromCall);		
		assertEquals(new Location(70, 50), id.getMouseLocationRelativeToView());
		
		id.update(new Location(110, 130), view);
		assertEquals(new Location(80, 70), id.getMouseLocationRelativeToView());
		
		assertEquals(new Offset(10, 20), id.getOffset());
	}
	
	private static class MockView extends AbstractView {
		protected MockView(Content content) {
			super(content, null, null);
		}

		InternalDrag dragFromCall;
		
		public Location getAbsoluteLocation() {
            return new Location(30, 60);
        }
		
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