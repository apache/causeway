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
package org.apache.isis.core.metamodel.interactions;

import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderAbstract;

import lombok.val;

import junit.framework.TestCase;

public class InteractionUtils_isA_Test extends TestCase {

    public class FooSuperFacet extends FacetAbstract {
        public FooSuperFacet(final Class<? extends Facet> facetType, final FacetHolder holder) {
            super(facetType, holder);
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
            super(BarFacet.class, holder);
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

        facetHolder = FacetHolderAbstract.forTesting(MetaModelContext_forTesting.buildDefault());
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
        val predicate = _Predicates.instanceOf(FooFacet.class);
        TestCase.assertTrue(predicate.test(fooFacet));
    }

    public void testIsAWhenIsNot() {
        val predicate = _Predicates.instanceOf(FooFacet.class);
        TestCase.assertFalse(predicate.test(barFacet));
    }

    public void testIsAWhenIsSubclass() {
        val predicate = _Predicates.instanceOf(FooFacet.class);
        TestCase.assertTrue(predicate.test(fooSubFacet));
    }

    public void testIsAWhenIsNotBecauseASuperclass() {
        val predicate = _Predicates.instanceOf(FooFacet.class);
        TestCase.assertFalse(predicate.test(fooSuperFacet));
    }

}
