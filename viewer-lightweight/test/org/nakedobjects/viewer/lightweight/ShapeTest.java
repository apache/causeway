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

package org.nakedobjects.viewer.lightweight;

import junit.framework.TestCase;


public class ShapeTest extends TestCase {

	private Shape shape;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(ShapeTest.class);
	}

	protected void setUp() throws Exception {
		shape = new Shape();
	}
	
	public void testNew() {
		assertEquals(0, shape.count());
		assertEquals(0, shape.getX().length);
		assertEquals(0, shape.getY().length);
	}
	
	public void testAddPoint() {
		shape.addVertex(10, 12);
		assertEquals(1, shape.count());
		assertEquals(10, shape.getX()[0]);
		assertEquals(12, shape.getY()[0]);
	}
	
	public void testAddThreePoints() {
		shape.addVertex(10, 12);
		shape.addVertex(8, 5);
		shape.addVertex(0, 2);
		assertEquals(3, shape.count());
		assertEquals(10, shape.getX()[0]);
		assertEquals(12, shape.getY()[0]);
		assertEquals(8, shape.getX()[1]);
		assertEquals(5, shape.getY()[1]);
		assertEquals(0, shape.getX()[2]);
		assertEquals(2, shape.getY()[2]);
	}
	
	public void testTransform() {
		shape.addVertex(10, 12);
		shape.addVertex(8, 5);
		shape.addVertex(0, 2);
		shape.translate(10, 20);
		assertEquals(3, shape.count());
		assertEquals(20, shape.getX()[0]);
		assertEquals(32, shape.getY()[0]);
		assertEquals(18, shape.getX()[1]);
		assertEquals(25, shape.getY()[1]);
		assertEquals(10, shape.getX()[2]);
		assertEquals(22, shape.getY()[2]);
	}
	
}
