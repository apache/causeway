package org.nakedobjects.viewer.skylark;

import junit.framework.TestCase;

import org.nakedobjects.viewer.skylark.core.AbstractView;

public class AbstractViewTest extends TestCase {
	private AbstractView av;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(AbstractViewTest.class);
	}
	
	protected void setUp() throws Exception {
		av = new AbstractView(null, null, null) {};
		super.setUp();
	}
	
	public void testBounds() {
		assertEquals(new Location(), av.getLocation());
		assertEquals(new Size(), av.getSize());
		assertEquals(new Bounds(), av.getBounds());

		av.setLocation(new Location(10, 20));
		assertEquals(new Location(10, 20), av.getLocation());
		assertEquals(new Size(), av.getSize());
		assertEquals(new Bounds(10, 20, 0, 0), av.getBounds());

		av.setSize(new Size(30, 40));
		assertEquals(new Location(10, 20), av.getLocation());
		assertEquals(new Size(30,40), av.getSize());
		assertEquals(new Bounds(10, 20, 30, 40), av.getBounds());

		av.setBounds(new Bounds(new Location(50, 60), new Size(70, 80)));
		assertEquals(new Location(50, 60), av.getLocation());
		assertEquals(new Size(70, 80), av.getSize());
		assertEquals(new Bounds(50, 60, 70, 80), av.getBounds());
	}
	
	public void testPadding() {
		assertEquals(new Padding(0,0,0,0), av.getPadding());
	}
	
	public void testViewAreaType() {
		Location loc = new Location(10, 10);
		assertEquals(ViewAreaType.CONTENT, av.viewAreaType(loc));
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