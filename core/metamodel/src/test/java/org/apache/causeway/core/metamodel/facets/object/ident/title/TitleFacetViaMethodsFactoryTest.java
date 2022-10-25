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
package org.apache.causeway.core.metamodel.facets.object.ident.title;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.ObjectSupportMethod;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.causeway.core.metamodel.facets.object.support.ObjectSupportFacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.object.title.TitleFacet;
import org.apache.causeway.core.metamodel.facets.object.title.methods.TitleFacetFromToStringMethod;

class TitleFacetViaMethodsFactoryTest
extends ObjectSupportFacetFactoryTestAbstract {

    public void testTitleMethodPickedUpOnClassAndMethodRemoved() {
        class Customer {
            @SuppressWarnings("unused")
            public String title() {
                return "Some title";
            }
        }
        assertPicksUp(1, facetFactory, Customer.class, ObjectSupportMethod.TITLE, TitleFacet.class);
    }

    public void testToStringMethodPickedUpOnClassAndMethodRemoved() {
        class Customer {
            @Override
            public String toString() {
                return "Some title via toString";
            }
        }
        final Method toStringMethod = findMethod(Customer.class, "toString");

        facetFactory.process(ProcessClassContext
                .forTesting(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(TitleFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof TitleFacetFromToStringMethod);
        final TitleFacetFromToStringMethod titleFacetViaTitleMethod = (TitleFacetFromToStringMethod) facet;
        assertEquals(toStringMethod, titleFacetViaTitleMethod.getMethods().getFirstOrFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(toStringMethod));
    }

    public void testTitleFacetOnJavaObjectToStringIsIgnored() throws NoSuchMethodException, SecurityException {

        final Method sampleMethod = Object.class
                .getMethod("toString", _Constants.emptyClasses);
        assertFalse(TitleFacetFromToStringMethod
                    .create(sampleMethod, facetedMethod)
                    .isPresent());
    }

    public void testNoExplicitTitleOrToStringMethod() {
        class Customer {
        }

        facetFactory.process(ProcessClassContext
                .forTesting(Customer.class, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(TitleFacet.class));
        assertFalse(methodRemover.getRemovedMethodMethodCalls().isEmpty());
    }

}
