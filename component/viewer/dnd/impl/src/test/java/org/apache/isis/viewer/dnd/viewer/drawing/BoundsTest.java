/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.viewer.dnd.viewer.drawing;

import junit.framework.TestCase;

import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Padding;
import org.apache.isis.viewer.dnd.drawing.Size;

public class BoundsTest extends TestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(BoundsTest.class);
    }

    private Bounds b;

    @Override
    protected void setUp() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
        b = new Bounds(5, 10, 10, 20);
    }

    public void testContains() {
        assertTrue(b.contains(new Location(8, 15)));
        assertTrue(b.contains(new Location(5, 10)));
        assertFalse(b.contains(new Location(4, 10)));
        assertFalse(b.contains(new Location(15, 10)));
        assertTrue(b.contains(new Location(10, 29)));
    }

    public void testNotEquals() {
        Bounds c = new Bounds(0, 10, 10, 20);
        assertFalse(c.equals(b));

        c = new Bounds(5, 0, 10, 20);
        assertFalse(c.equals(b));

        c = new Bounds(5, 10, 0, 20);
        assertFalse(c.equals(b));

        c = new Bounds(5, 10, 10, 0);
        assertFalse(c.equals(b));
    }

    public void testEquals() {
        final Bounds c = new Bounds(5, 10, 10, 20);
        assertTrue(c.equals(b));
        assertTrue(b.equals(c));
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

    public void testCopyBounds() {
        final Bounds c = new Bounds();
        c.setBounds(b);

        assertEquals(5, b.getX());
        assertEquals(10, b.getY());
        assertEquals(10, b.getWidth());
        assertEquals(20, b.getHeight());
    }

    public void testDefaultBounds() {
        final Bounds b = new Bounds();
        assertEquals(0, b.getX());
        assertEquals(0, b.getY());
        assertEquals(0, b.getWidth());
        assertEquals(0, b.getHeight());
    }

    public void testDownLeftIntersects() {
        final Bounds c = new Bounds(b);
        c.translate(-5, -5);
        assertTrue(b.intersects(c));

        c.translate(-b.getWidth(), 0);
        assertFalse(b.intersects(c));
    }

    public void testEnclosingUnion() {
        final Bounds c = new Bounds(10, 20, 5, 5);
        final Bounds u = new Bounds(b);
        u.union(c);
        assertEquals(b, u);
    }

    public void testExplicitBounds() {
        assertEquals(5, b.getX());
        assertEquals(10, b.getY());
        assertEquals(10, b.getWidth());
        assertEquals(20, b.getHeight());

        final Bounds b1 = new Bounds(b);
        assertEquals(5, b1.getX());
        assertEquals(10, b1.getY());
        assertEquals(10, b1.getWidth());
        assertEquals(20, b1.getHeight());

        final Bounds b2 = new Bounds(new Location(10, 20), new Size(8, 16));
        assertEquals(10, b2.getX());
        assertEquals(20, b2.getY());
        assertEquals(8, b2.getWidth());
        assertEquals(16, b2.getHeight());

        final Bounds b3 = new Bounds(new Size(5, 10));
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
        final Bounds b2 = new Bounds(10, 0, 4, 30);
        assertTrue(b.limitBounds(b2));
        assertEquals(new Bounds(10, 10, 4, 20), b2);
    }

    public void testLimitBoundsWhenTooWide() {
        final Bounds b2 = new Bounds(0, 12, 20, 5);
        assertTrue(b.limitBounds(b2));
        assertEquals(new Bounds(5, 12, 10, 5), b2);
    }

    public void testLimitBoundsWithHorizontalOverlap() {
        final Bounds b2 = new Bounds(10, 12, 10, 5);
        assertTrue(b.limitBounds(b2));
        assertEquals(new Bounds(5, 12, 10, 5), b2);
    }

    public void testLimitBoundsWithNoOverlap() {
        final Bounds b2 = new Bounds(7, 12, 5, 5);
        assertFalse(b.limitBounds(b2));
        assertEquals(new Bounds(7, 12, 5, 5), b2);
    }

    public void testLimitBoundsWithVerticalOverlap() {
        final Bounds b2 = new Bounds(5, 20, 5, 20);
        assertTrue(b.limitBounds(b2));
        assertEquals(new Bounds(5, 10, 5, 20), b2);
    }

    public void testNonOverlappingUnion() {
        final Bounds c = new Bounds(20, 40, 10, 20);
        final Bounds u = new Bounds(b);
        u.union(c);
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
        final Bounds c = new Bounds(3, 5, 10, 10);
        final Bounds u = new Bounds(b);
        u.union(c);
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
        final Bounds c = new Bounds(b);
        c.translate(5, 5);
        assertTrue(b.intersects(c));

        c.translate(b.getWidth(), 0);
        assertFalse(b.intersects(c));
    }

    public void testXNoOverlapToLeft() {
        final Bounds c = new Bounds(1, 15, 4, 0);
        assertFalse(b.intersects(c));
    }

    public void testXNoOverlapToRight() {
        final Bounds c = new Bounds(15, 15, 5, 0);
        assertFalse(b.intersects(c));
    }

    public void testXOverlapInCenter() {
        final Bounds c = new Bounds(6, 15, 2, 0);
        assertTrue(b.intersects(c));
    }

    public void testXOverlapToLeft() {
        final Bounds c = new Bounds(1, 15, 6, 0);
        assertTrue(b.intersects(c));
    }

    public void testXOverlapToRight() {
        final Bounds c = new Bounds(14, 15, 5, 0);
        assertTrue(b.intersects(c));
    }

    public void testYOverlapToTop() {
        final Bounds c = new Bounds(10, 29, 0, 5);
        assertTrue(b.intersects(c));
    }

}
