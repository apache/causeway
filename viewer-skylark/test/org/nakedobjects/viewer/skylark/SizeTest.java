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

package org.nakedobjects.viewer.skylark;

import junit.framework.TestCase;


public class SizeTest extends TestCase {

	private Size s;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(SizeTest.class);
	}

	protected void setUp() throws Exception {
		s = new Size(10, 20);
	}
	
	public void testCopy() {
		Size m = new Size(s);
		assertTrue(s != m);
		assertEquals(s, m);
	}
	
	public void testEnsure() {
		s.ensureWidth(18);
		assertEquals(new Size(18, 20), s);
		s.ensureWidth(12);
		assertEquals(new Size(18, 20), s);
		
		s.ensureHeight(16);
		assertEquals(new Size(18, 20), s);
		s.ensureHeight(26);
		assertEquals(new Size(18, 26), s);
	}
	
	public void addPadding() {
		s.extend(new Padding(1, 2, 3, 4));
		assertEquals(new Size(14, 26), s);
	}

	public void testExtend() {
		s.extendWidth(8);
		assertEquals(new Size(18, 20), s);
		
		s.extendHeight(6);
		assertEquals(new Size(18, 26), s);

		s.extend(new Size(3, 5));
		assertEquals(new Size(21, 31), s);
		
		s.extend(5, 3);
		assertEquals(new Size(26, 34), s);

	}
	

	
}
