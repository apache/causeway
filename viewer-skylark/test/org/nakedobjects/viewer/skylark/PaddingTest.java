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


public class PaddingTest extends TestCase {

	private Padding p;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(PaddingTest.class);
	}

	protected void setUp() throws Exception {
		p = new Padding(2, 3, 4, 5);
	}
	
	public void testCopy() {
		Padding q = new Padding(p);
		assertTrue(p!=q);
		assertEquals(p, q);
	}
	
	public void testValues() {
		assertEquals(2, p.getTop());
		assertEquals(3, p.getLeft());
		assertEquals(4, p.getBottom());
		assertEquals(5, p.getRight());
	}
	
	public void testExtend() {
		p.extendTop(10);
		assertEquals(new Padding(12, 3, 4, 5), p);
		
		p.extendLeft(10);
		assertEquals(new Padding(12, 13, 4, 5), p);
		
		p.extendBottom(10);
		assertEquals(new Padding(12, 13, 14, 5), p);

		p.extendRight(10);
		assertEquals(new Padding(12, 13, 14, 15), p);
	}
}
