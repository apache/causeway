/*
	Naked Objects - a framework that exposes behaviourally complete
	business objects directly to the user.
	Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

package org.nakedobjects.viewer.skylark;

import junit.framework.TestCase;


public class LocationTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(LocationTest.class);
	}

	public void testCopy() {
		Location l = new Location(10, 20);
		Location m = new Location(l);
		assertTrue(l != m);
		assertEquals(l, m);
	}
	
	public void testTranslate() {
		Location l = new Location(10,20);
		l.move(5, 10);
		assertEquals(new Location(15, 30), l);
		l.move(-10, -5);
		assertEquals(new Location(5, 25), l);
	}
}
