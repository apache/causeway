package org.nakedobjects.viewer.skylark;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import junit.framework.TestCase;


public class BoundsTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(BoundsTest.class);
    }

    private Bounds b;

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        b = new Bounds(5, 10, 10, 20);
    }

    public void testContains() {
        assertTrue(b.contains(new Location(8, 15)));
        assertTrue(b.contains(new Location(5, 10)));
        assertFalse(b.contains(new Location(4, 10)));
        assertFalse(b.contains(new Location(15, 10)));
        assertTrue(b.contains(new Location(10, 29)));
    }

    public void testContracSize() {
        b.contract(new Size(5, 12));
        assertEquals(5, b.getWidth());
        assertEquals(8, b.getHeight());
    }

    public void testContractHeight() {
        b.contractHeight(12);
        assertEquals(8, b.getHeight());
    }

    public void testContractPadding() {
        b.contract(new Padding(2, 4, 1, 3));
        assertEquals(3, b.getWidth());
        assertEquals(17, b.getHeight());
        assertEquals(9, b.getX());
        assertEquals(12, b.getY());
    }

    public void testContracWidth() {
        b.contractWidth(5);
        assertEquals(5, b.getWidth());
    }

    public void testDefaultBounds() {
        Bounds b = new Bounds();
        assertEquals(0, b.getX());
        assertEquals(0, b.getY());
        assertEquals(0, b.getWidth());
        assertEquals(0, b.getHeight());
    }

    public void testDownLeftIntersects() {
        Bounds c = new Bounds(b);
        c.translate(-5, -5);
        assertTrue(b.intersects(c));

        c.translate(-b.getWidth(), 0);
        assertFalse(b.intersects(c));
    }

    public void testEnclosingUnion() {
        Bounds c = new Bounds(10, 20, 5, 5);
        Bounds u = b.union(c);
        assertEquals(b, u);
    }

    public void testExplicitBounds() {
        assertEquals(5, b.getX());
        assertEquals(10, b.getY());
        assertEquals(10, b.getWidth());
        assertEquals(20, b.getHeight());

        Bounds b1 = new Bounds(b);
        assertEquals(5, b1.getX());
        assertEquals(10, b1.getY());
        assertEquals(10, b1.getWidth());
        assertEquals(20, b1.getHeight());

        Bounds b2 = new Bounds(new Location(10, 20), new Size(8, 16));
        assertEquals(10, b2.getX());
        assertEquals(20, b2.getY());
        assertEquals(8, b2.getWidth());
        assertEquals(16, b2.getHeight());

        Bounds b3 = new Bounds(new Size(5, 10));
        assertEquals(0, b3.getX());
        assertEquals(0, b3.getY());
        assertEquals(5, b3.getWidth());
        assertEquals(10, b3.getHeight());
    }

    public void testFarPoint() {
        assertEquals(5, b.getX());
        assertEquals(14, b.getX2());
        assertEquals(10, b.getY());
        assertEquals(29, b.getY2());
    }

    public void testgrow() {
        b.extend(10, 5);
        assertEquals(5, b.getX());
        assertEquals(10, b.getY());
        assertEquals(20, b.getWidth());
        assertEquals(25, b.getHeight());
    }

    public void testLimitBoundsWhenTooTall() {
        Bounds b2 = new Bounds(10, 0, 4, 30);
        assertTrue(b.limitBounds(b2));
        assertEquals(new Bounds(10, 10, 4, 20), b2);
    }

    public void testLimitBoundsWhenTooWide() {
        Bounds b2 = new Bounds(0, 12, 20, 5);
        assertTrue(b.limitBounds(b2));
        assertEquals(new Bounds(5, 12, 10, 5), b2);
    }

    public void testLimitBoundsWithHorizontalOverlap() {
        Bounds b2 = new Bounds(10, 12, 10, 5);
        assertTrue(b.limitBounds(b2));
        assertEquals(new Bounds(5, 12, 10, 5), b2);
    }

    public void testLimitBoundsWithNoOverlap() {
        Bounds b2 = new Bounds(7, 12, 5, 5);
        assertFalse(b.limitBounds(b2));
        assertEquals(new Bounds(7, 12, 5, 5), b2);
    }

    public void testLimitBoundsWithVerticalOverlap() {
        Bounds b2 = new Bounds(5, 20, 5, 20);
        assertTrue(b.limitBounds(b2));
        assertEquals(new Bounds(5, 10, 5, 20), b2);
    }

    public void testNonOverlappingUnion() {
        Bounds c = new Bounds(20, 40, 10, 20);
        Bounds u = b.union(c);
        assertEquals(new Bounds(5, 10, 25, 50), u);
    }

    public void testOverlappingIntersects() {
        Bounds c = new Bounds(b);
        c.translate(-5, -5);
        c.extend(10, 10);
        assertTrue(b.intersects(c));

        c = new Bounds(b);
        c.translate(5, 5);
        c.extend(-10, -10);
        assertTrue(b.intersects(c));
    }

    public void testOverlappingUnion() {
        Bounds c = new Bounds(3, 5, 10, 10);
        Bounds u = b.union(c);
        assertEquals(new Bounds(3, 5, 12, 25), u);
    }

    public void testTranslate() {
        b.translate(10, 5);
        assertEquals(15, b.getX());
        assertEquals(15, b.getY());
        assertEquals(10, b.getWidth());
        assertEquals(20, b.getHeight());
    }

    public void testUpRightIntersects() {
        Bounds c = new Bounds(b);
        c.translate(5, 5);
        assertTrue(b.intersects(c));

        c.translate(b.getWidth(), 0);
        assertFalse(b.intersects(c));
    }

    public void testXNoOverlapToLeft() {
        Bounds c = new Bounds(1, 15, 4, 0);
        assertFalse(b.intersects(c));
    }

    public void testXNoOverlapToRight() {
        Bounds c = new Bounds(15, 15, 5, 0);
        assertFalse(b.intersects(c));
    }

    public void testXOverlapInCenter() {
        Bounds c = new Bounds(6, 15, 2, 0);
        assertTrue(b.intersects(c));
    }

    public void testXOverlapToLeft() {
        Bounds c = new Bounds(1, 15, 6, 0);
        assertTrue(b.intersects(c));
    }

    public void testXOverlapToRight() {
        Bounds c = new Bounds(14, 15, 5, 0);
        assertTrue(b.intersects(c));
    }

    public void testYOverlapToTop() {
        Bounds c = new Bounds(10, 29, 0, 5);
        assertTrue(b.intersects(c));
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
