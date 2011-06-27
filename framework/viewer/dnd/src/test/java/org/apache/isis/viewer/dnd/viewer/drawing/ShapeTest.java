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

import org.apache.isis.viewer.dnd.drawing.Shape;

public class ShapeTest extends TestCase {

    private Shape shape;

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(ShapeTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        shape = new Shape();
    }

    public void testNew() {
        assertEquals(0, shape.count());
        assertEquals(0, shape.getX().length);
        assertEquals(0, shape.getY().length);
    }

    public void testAddPoint() {
        shape.addPoint(10, 12);
        assertEquals(1, shape.count());
        assertEquals(10, shape.getX()[0]);
        assertEquals(12, shape.getY()[0]);
    }

    public void testAddThreePoints() {
        shape.addPoint(10, 12);
        shape.addPoint(8, 5);
        shape.addPoint(0, 2);
        assertEquals(3, shape.count());
        assertEquals(10, shape.getX()[0]);
        assertEquals(12, shape.getY()[0]);
        assertEquals(8, shape.getX()[1]);
        assertEquals(5, shape.getY()[1]);
        assertEquals(0, shape.getX()[2]);
        assertEquals(2, shape.getY()[2]);
    }

    public void testCreateCopy() {
        shape.addPoint(10, 12);
        shape.addPoint(8, 5);
        shape.addPoint(0, 2);

        final Shape copy = new Shape(shape);

        assertEquals(3, copy.count());
        assertEquals(10, copy.getX()[0]);
        assertEquals(12, copy.getY()[0]);
        assertEquals(8, copy.getX()[1]);
        assertEquals(5, copy.getY()[1]);
        assertEquals(0, copy.getX()[2]);
        assertEquals(2, copy.getY()[2]);
    }

    public void testTransform() {
        shape.addPoint(10, 12);
        shape.addPoint(8, 5);
        shape.addPoint(0, 2);
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
