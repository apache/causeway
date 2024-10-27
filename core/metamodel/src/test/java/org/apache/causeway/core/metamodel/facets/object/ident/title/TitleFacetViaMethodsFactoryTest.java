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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.ObjectSupportMethod;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.object.support.ObjectSupportFacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.object.title.TitleFacet;
import org.apache.causeway.core.metamodel.facets.object.title.methods.TitleFacetFromToStringMethod;

class TitleFacetViaMethodsFactoryTest
extends ObjectSupportFacetFactoryTestAbstract {

    @Test
    void titleMethodPickedUpOnClassAndMethodRemoved() {
        @SuppressWarnings("unused")
        class Customer {
            public String title() { return "Some title"; }
        }
        assertPicksUp(1, facetFactory, Customer.class, ObjectSupportMethod.TITLE, TitleFacet.class);
    }

    @Test
    void toStringMethodPickedUpOnClassAndMethodRemoved() {
        class Customer {
            @Override
            public String toString() { return "Some title via toString"; }
        }

        var toStringMethod = findMethodExactOrFail(Customer.class, "toString");

        objectScenario(Customer.class, (processClassContext, facetHolder) -> {
            //when
            facetFactory.process(processClassContext);
            //then
            final Facet facet = facetHolder.getFacet(TitleFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof TitleFacetFromToStringMethod);
            var titleFacetViaTitleMethod = (TitleFacetFromToStringMethod) facet;
            assertMethodEqualsFirstIn(toStringMethod, titleFacetViaTitleMethod);
            assertMethodWasRemoved(toStringMethod);
        });
    }

    @Test
    void titleFacetOnJavaObjectToStringIsIgnored() throws NoSuchMethodException, SecurityException {
        var sampleMethod = findMethodExactOrFail(Object.class, "toString");
        var facetedMethod = Mockito.mock(FacetedMethod.class);

        assertFalse(TitleFacetFromToStringMethod
                    .create(sampleMethod, facetedMethod)
                    .isPresent());
    }

    @Test
    void noExplicitTitleOrToStringMethod() {
        class Customer {
        }

        objectScenario(Customer.class, (processClassContext, facetHolder) -> {
            //when
            facetFactory.process(processClassContext);
            //then
            assertNull(facetHolder.getFacet(TitleFacet.class));
        });

    }

}
