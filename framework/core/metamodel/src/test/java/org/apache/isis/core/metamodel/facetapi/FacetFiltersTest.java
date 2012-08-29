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

import junit.framework.TestCase;

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;

public class FacetFiltersTest extends TestCase {

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
        final Filter<Facet> filter = FacetFilters.isA(FooFacet.class);
        assertTrue(filter.accept(fooFacet));
    }

    public void testIsAWhenIsNot() {
        final Filter<Facet> filter = FacetFilters.isA(FooFacet.class);
        assertFalse(filter.accept(barFacet));
    }

    public void testIsAWhenIsSubclass() {
        final Filter<Facet> filter = FacetFilters.isA(FooFacet.class);
        assertTrue(filter.accept(fooSubFacet));
    }

    public void testIsAWhenIsNotBecauseASuperclass() {
        final Filter<Facet> filter = FacetFilters.isA(FooFacet.class);
        assertFalse(filter.accept(fooSuperFacet));
    }

    public void testAndTrueTrue() {
        final Filter<Facet> and = Filters.and(FacetFilters.ANY, FacetFilters.ANY);
        assertTrue(and.accept(fooFacet));
    }

    public void testAndTrueFalse() {
        final Filter<Facet> and = Filters.and(FacetFilters.ANY, FacetFilters.NONE);
        assertFalse(and.accept(fooFacet));
    }

    public void testAndFalseTrue() {
        final Filter<Facet> and = Filters.and(FacetFilters.NONE, FacetFilters.ANY);
        assertFalse(and.accept(fooFacet));
    }

    public void testAndFalseFalse() {
        final Filter<Facet> and = Filters.and(FacetFilters.NONE, FacetFilters.NONE);
        assertFalse(and.accept(fooFacet));
    }

    public void testOrTrueTrue() {
        final Filter<Facet> or = Filters.or(FacetFilters.ANY, FacetFilters.ANY);
        assertTrue(or.accept(fooFacet));
    }

    public void testOrTrueFalse() {
        final Filter<Facet> or = Filters.or(FacetFilters.ANY, FacetFilters.NONE);
        assertTrue(or.accept(fooFacet));
    }

    public void testorFalseTrue() {
        final Filter<Facet> or = Filters.or(FacetFilters.NONE, FacetFilters.ANY);
        assertTrue(or.accept(fooFacet));
    }

    public void testOrFalseFalse() {
        final Filter<Facet> or = Filters.and(FacetFilters.NONE, FacetFilters.NONE);
        assertFalse(or.accept(fooFacet));
    }

    public void testNotTrue() {
        final Filter<Facet> not = Filters.not(FacetFilters.ANY);
        assertFalse(not.accept(fooFacet));
    }

    public void testNotFalse() {
        final Filter<Facet> not = Filters.not(FacetFilters.NONE);
        assertTrue(not.accept(fooFacet));
    }

    public void testAny() {
        final Filter<Facet> any = Filters.any();
        assertTrue(any.accept(fooFacet));
    }

    public void testNone() {
        final Filter<Facet> none = Filters.none();
        assertFalse(none.accept(fooFacet));
    }

}
