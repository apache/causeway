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

package org.apache.isis.core.metamodel.facets;

import junit.framework.TestCase;

public class EnumerationAbstractTest extends TestCase {

    public static class ConcreteEnumeration extends EnumerationAbstract {

        protected ConcreteEnumeration(final int num, final String nameInCode, final String friendlyName) {
            super(nameInCode, friendlyName);
        }
    }

    public static class OtherConcreteEnumeration extends EnumerationAbstract {

        protected OtherConcreteEnumeration(final int num, final String nameInCode, final String friendlyName) {
            super(nameInCode, friendlyName);
        }
    }

    private EnumerationAbstract foo1, foo2, bar1, fooInterloper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        foo1 = new ConcreteEnumeration(1, "foo", "Foo");
        foo2 = new ConcreteEnumeration(1, "foo", "Foo");
        bar1 = new ConcreteEnumeration(2, "bar", "Bar");
        fooInterloper = new OtherConcreteEnumeration(1, "foo", "Foo");
    }

    @Override
    protected void tearDown() throws Exception {
        foo1 = foo2 = bar1 = null;
        super.tearDown();
    }

    public void testGetNameInCode() {
        assertEquals("foo", foo1.getNameInCode());
    }

    public void testGetFriendlyName() {
        assertEquals("Foo", foo1.getFriendlyName());
    }

    public void testEqualsAbstractEnumerationForSelf() {
        assertTrue(foo1.equals(foo1));
    }

    public void testEqualsAbstractEnumerationForEqual() {
        assertTrue(foo1.equals(foo2));
    }

    public void testEqualsAbstractEnumerationForDifferent() {
        assertFalse(foo1.equals(bar1));
    }

    public void testEqualsAbstractEnumerationForSameCardinalButDifferentType() {
        assertFalse(foo1.equals(fooInterloper));
    }

    public void testHashCodeForEqual() {
        assertEquals(foo1.hashCode(), foo2.hashCode());
    }

    public void testHashCodeForDifferent() {
        assertFalse(foo1.hashCode() == bar1.hashCode());
    }

}
