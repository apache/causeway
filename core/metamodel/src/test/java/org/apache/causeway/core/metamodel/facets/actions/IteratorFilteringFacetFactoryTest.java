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
package org.apache.causeway.core.metamodel.facets.actions;

import java.lang.reflect.Method;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.causeway.core.metamodel.facets.object.ignore.javalang.IteratorFilteringFacetFactory;

class IteratorFilteringFacetFactoryTest extends AbstractFacetFactoryTest {

    private IteratorFilteringFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new IteratorFilteringFacetFactory(metaModelContext);
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testRequestsRemoverToRemoveIteratorMethods() {
        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {
            }
        }
        facetFactory.process(ProcessClassContext
                .forTesting(Customer.class, methodRemover, facetedMethod));

        assertEquals(1, methodRemover.getRemoveMethodArgsCalls().size());
    }

    public void testNoIteratorMethodFiltered() {
        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        assertFalse(facetFactory.recognizes(actionMethod));
    }

    public void xxxtestIterableIteratorMethodFiltered() {
        class Customer implements Iterable<Customer> {
            @SuppressWarnings("unused")
            public void someAction() {
            }

            @Override
            public Iterator<Customer> iterator() {
                return null;
            }
        }
        final Method iteratorMethod = findMethod(Customer.class, "iterator");

        assertTrue(facetFactory.recognizes(iteratorMethod));
    }

}
