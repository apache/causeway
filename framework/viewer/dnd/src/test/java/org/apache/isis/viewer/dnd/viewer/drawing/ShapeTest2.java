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

public class ShapeTest2 extends TestCase {

    private Shape shape;

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(ShapeTest2.class);
    }

    @Override
    protected void setUp() throws Exception {
        shape = new Shape(10, 20);
    }

    public void testNew() {
        assertEquals(1, shape.count());
        assertEquals(10, shape.getX()[0]);
        assertEquals(20, shape.getY()[0]);
    }

    public void testAddLine() {
        shape.addVector(5, 10);
        assertEquals(2, shape.count());
        assertEquals(15, shape.getX()[1]);
        assertEquals(30, shape.getY()[1]);
    }

    public void testAddTwoLines() {
        shape.addVector(5, 10);
        shape.addVector(-8, -5);
        assertEquals(3, shape.count());
        assertEquals(7, shape.getX()[2]);
        assertEquals(25, shape.getY()[2]);
    }

}
