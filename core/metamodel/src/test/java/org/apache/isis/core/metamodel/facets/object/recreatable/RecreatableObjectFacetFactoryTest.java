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

package org.apache.isis.core.metamodel.facets.object.recreatable;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.methods.MethodByClassMap;

public class RecreatableObjectFacetFactoryTest extends AbstractFacetFactoryTest {

    private RecreatableObjectFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new RecreatableObjectFacetFactory(new MethodByClassMap());
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testViewModelInterfacePickedUpOnClassAndDefaultsToAlways() {
        class Customer implements ViewModel {

            @Override
            public String viewModelMemento() {
                return null;
            }

            @Override
            public void viewModelInit(String memento) {
            }

        }

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ViewModelFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof RecreatableObjectFacetForRecreatableObjectInterface);

        assertNoMethodsRemoved();
    }

}
