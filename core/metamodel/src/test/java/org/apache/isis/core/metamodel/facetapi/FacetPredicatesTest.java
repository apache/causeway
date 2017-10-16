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

package org.apache.isis.core.metamodel.facetapi;

import com.google.common.base.Predicate;

import junit.framework.TestCase;

public class FacetPredicatesTest extends TestCase {

    public class FooSuperFacet extends FacetAbstract {
        public FooSuperFacet(final Class<? extends Facet> facetType, final FacetHolder holder) {
            super(facetType, holder, Derivation.NOT_DERIVED);
        }
    }

    public class FooFacet extends FooSuperFacet {
        public FooFacet(final FacetHolder holder) {
            super(FooFacet.class, holder);
        }
    }

    public class FooSubFacet extends FooFacet {
        public FooSubFacet(final FacetHolder holder) {
            super(holder);
        }
    }

    public class BarFacet extends FacetAbstract {
        public BarFacet(final FacetHolder holder) {
            super(BarFacet.class, holder, Derivation.NOT_DERIVED);
        }
    }

    private FacetHolder facetHolder;
    private FooFacet fooFacet;
    private FooSubFacet fooSubFacet;
    private FooSuperFacet fooSuperFacet;
    private BarFacet barFacet;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        facetHolder = new FacetHolderImpl();
        fooSuperFacet = new FooSuperFacet(FooSuperFacet.class, facetHolder);
        fooFacet = new FooFacet(facetHolder);
        fooSubFacet = new FooSubFacet(facetHolder);
        barFacet = new BarFacet(facetHolder);
    }

    @Override
    protected void tearDown() throws Exception {
        facetHolder = null;
        fooSuperFacet = null;
        fooSubFacet = null;
        fooFacet = null;
        barFacet = null;
        super.tearDown();
    }

    public void testIsAWhenIs() {
        final Predicate<Facet> predicate = Facet.Predicates.isA(FooFacet.class);
        assertTrue(predicate.apply(fooFacet));
    }

    public void testIsAWhenIsNot() {
        final Predicate<Facet> predicate = Facet.Predicates.isA(FooFacet.class);
        assertFalse(predicate.apply(barFacet));
    }

    public void testIsAWhenIsSubclass() {
        final Predicate<Facet> predicate = Facet.Predicates.isA(FooFacet.class);
        assertTrue(predicate.apply(fooSubFacet));
    }

    public void testIsAWhenIsNotBecauseASuperclass() {
        final Predicate<Facet> predicate = Facet.Predicates.isA(FooFacet.class);
        assertFalse(predicate.apply(fooSuperFacet));
    }

    public void testAndTrueTrue() {
        final Predicate<Facet> and = com.google.common.base.Predicates
                .and(Facet.Predicates.ANY, Facet.Predicates.ANY );
        assertTrue(and.apply(fooFacet));
    }

    public void testAndTrueFalse() {
        final Predicate<Facet> and = com.google.common.base.Predicates
                .and(Facet.Predicates.ANY, Facet.Predicates.NONE );
        assertFalse(and.apply(fooFacet));
    }

    public void testAndFalseTrue() {
        final Predicate<Facet> and = com.google.common.base.Predicates
                .and(Facet.Predicates.NONE, Facet.Predicates.ANY );
        assertFalse(and.apply(fooFacet));
    }

    public void testAndFalseFalse() {
        final Predicate<Facet> and = com.google.common.base.Predicates
                .and(Facet.Predicates.NONE, Facet.Predicates.NONE );
        assertFalse(and.apply(fooFacet));
    }

    public void testOrTrueTrue() {
        final Predicate<Facet> or = com.google.common.base.Predicates
                .or(Facet.Predicates.ANY, Facet.Predicates.ANY );
        assertTrue(or.apply(fooFacet));
    }

    public void testOrTrueFalse() {
        final Predicate<Facet> or = com.google.common.base.Predicates
                .or(Facet.Predicates.ANY, Facet.Predicates.NONE );
        assertTrue(or.apply(fooFacet));
    }

    public void testorFalseTrue() {
        final Predicate<Facet> or = com.google.common.base.Predicates
                .or(Facet.Predicates.NONE, Facet.Predicates.ANY );
        assertTrue(or.apply(fooFacet));
    }

    public void testOrFalseFalse() {
        final Predicate<Facet> or = com.google.common.base.Predicates
                .and(Facet.Predicates.NONE, Facet.Predicates.NONE );
        assertFalse(or.apply(fooFacet));
    }

    public void testNotTrue() {
        final Predicate<Facet> not = com.google.common.base.Predicates.not(Facet.Predicates.ANY);
        assertFalse(not.apply(fooFacet));
    }

    public void testNotFalse() {
        final Predicate<Facet> not = com.google.common.base.Predicates.not(Facet.Predicates.NONE);
        assertTrue(not.apply(fooFacet));
    }

    public void testAny() {
        final Predicate<Facet> any = com.google.common.base.Predicates.alwaysTrue();
        assertTrue(any.apply(fooFacet));
    }

    public void testNone() {
        final Predicate<Facet> none = com.google.common.base.Predicates.alwaysFalse();
        assertFalse(none.apply(fooFacet));
    }

}
