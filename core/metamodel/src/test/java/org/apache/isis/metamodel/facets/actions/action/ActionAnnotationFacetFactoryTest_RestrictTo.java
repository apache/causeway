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
package org.apache.isis.metamodel.facets.actions.action;

import static org.junit.Assert.assertNull;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.metamodel.facets.actions.prototype.PrototypeFacet;
import org.junit.Assert;
import org.junit.Test;

public class ActionAnnotationFacetFactoryTest_RestrictTo extends ActionAnnotationFacetFactoryTest {

    @Test
    public void whenRestrictedToPrototyping() {

        class Customer {
            @Action(restrictTo = org.apache.isis.applib.annotation.RestrictTo.PROTOTYPING)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(
        		cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processRestrictTo(processMethodContext);

        // then
        final PrototypeFacet facet = facetedMethod.getFacet(PrototypeFacet.class);
        Assert.assertNotNull(facet);
    }

    @Test
    public void whenRestrictedToNoRestriction() {

        class Customer {
            @Action(restrictTo = org.apache.isis.applib.annotation.RestrictTo.NO_RESTRICTIONS)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(
        		cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processRestrictTo(processMethodContext);

        // then
        final PrototypeFacet facet = facetedMethod.getFacet(PrototypeFacet.class);
        assertNull(facet);
    }

    @Test
    public void whenNotPresent() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(
        		cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processRestrictTo(processMethodContext);

        // then
        final PrototypeFacet facet = facetedMethod.getFacet(PrototypeFacet.class);
        assertNull(facet);
    }

}