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
package org.apache.isis.core.metamodel.facets.actions.action;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;

import lombok.val;

public class ActionAnnotationFacetFactoryTest_Hidden extends ActionAnnotationFacetFactoryTest {

    private void processHidden(
            final ActionAnnotationFacetFactory facetFactory, final ProcessMethodContext processMethodContext) {

        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processHidden(processMethodContext, actionIfAny);
    }

    @Test
    public void withAnnotation() {

        class Customer {
            @Action(hidden = Where.REFERENCES_PARENT)
            public void someAction() {
            }
        }

        // given
        val cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        val processMethodContext = ProcessMethodContext
                .forTesting(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        processHidden(facetFactory, processMethodContext);

        // then
        val hiddenFacet = facetedMethod.getFacet(HiddenFacet.class);
        Assert.assertNotNull(hiddenFacet);
        assertThat(hiddenFacet.where(), is(Where.REFERENCES_PARENT));

        val hiddenFacetImpl = facetedMethod.getFacet(HiddenFacet.class);
        Assert.assertNotNull(hiddenFacetImpl);
        Assert.assertTrue(hiddenFacet == hiddenFacetImpl);
    }

}