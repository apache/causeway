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
package org.apache.causeway.core.metamodel.interactions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

class InteractionUtils_isA_Test {

    class FooSuperFacet extends FacetAbstract {
        public FooSuperFacet(final Class<? extends Facet> facetType, final FacetHolder holder) {
            super(facetType, holder);
        }
    }

    class FooFacet extends FooSuperFacet {
        public FooFacet(final FacetHolder holder) {
            super(FooFacet.class, holder);
        }
    }

    class FooSubFacet extends FooFacet {
        public FooSubFacet(final FacetHolder holder) {
            super(holder);
        }
    }

    class BarFacet extends FacetAbstract {
        public BarFacet(final FacetHolder holder) {
            super(BarFacet.class, holder);
        }
    }

    private FacetHolder facetHolder;

    @BeforeEach
    protected void setUp() throws Exception {
        facetHolder = FacetHolder.forTesting(MetaModelContext_forTesting.buildDefault());
    }

    @AfterEach
    protected void tearDown() throws Exception {
        facetHolder = null;
    }

    @Test
    void isAWhenIs() {
        var predicate = _Predicates.instanceOf(FooFacet.class);
        var fooFacet = new FooFacet(facetHolder);
        assertTrue(predicate.test(fooFacet));
    }

    @Test
    void isAWhenIsNot() {
        var predicate = _Predicates.instanceOf(FooFacet.class);
        var barFacet = new BarFacet(facetHolder);
        assertFalse(predicate.test(barFacet));
    }

    @Test
    void isAWhenIsSubclass() {
        var predicate = _Predicates.instanceOf(FooFacet.class);
        var fooSubFacet = new FooSubFacet(facetHolder);
        assertTrue(predicate.test(fooSubFacet));
    }

    @Test
    void isAWhenIsNotBecauseASuperclass() {
        var predicate = _Predicates.instanceOf(FooFacet.class);
        var fooSuperFacet = new FooSuperFacet(FooSuperFacet.class, facetHolder);
        assertFalse(predicate.test(fooSuperFacet));
    }

}
