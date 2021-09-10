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
package org.apache.isis.core.metamodel.commons;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TypesTest_filteredList {

    private List<Object> shapes;

    abstract static class Shape {}
    static class Square extends Shape {}
    static class Circle extends Shape {}
    static class Rectangle extends Shape {}

    @Before
    public void setUp() throws Exception {
        shapes = Arrays.<Object>asList(new Square(), new Circle(), new Square());
    }

    @Test
    public void empty() throws ClassNotFoundException {
        final Collection<Object> filtered = ListExtensions.filtered(Collections.emptyList(), Object.class);
        assertThat(filtered.isEmpty(), is(true));
    }

    @Test
    public void subtype() throws ClassNotFoundException {
        final Collection<Square> filtered = ListExtensions.filtered(shapes, Square.class);
        assertThat(filtered.size(), is(2));
    }

    @Test
    public void supertype() throws ClassNotFoundException {
        final Collection<Shape> filtered = ListExtensions.filtered(shapes, Shape.class);
        assertThat(filtered.size(), is(3));
    }

    @Test
    public void subtype_whenNonMatching() throws ClassNotFoundException {
        final Collection<Rectangle> filtered = ListExtensions.filtered(shapes, Rectangle.class);
        assertThat(filtered.size(), is(0));
    }

}
