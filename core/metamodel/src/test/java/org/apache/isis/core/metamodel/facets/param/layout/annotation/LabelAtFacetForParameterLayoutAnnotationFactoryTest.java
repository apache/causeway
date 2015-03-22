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

package org.apache.isis.core.metamodel.facets.param.layout.annotation;

import java.lang.reflect.Method;
import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.param.layout.LabelAtFacetForParameterLayoutAnnotation;
import org.apache.isis.core.metamodel.facets.param.layout.ParameterLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class LabelAtFacetForParameterLayoutAnnotationFactoryTest extends AbstractFacetFactoryTest {

    public void testParameterLayoutAnnotationPickedUp() {
        final ParameterLayoutFacetFactory facetFactory = new ParameterLayoutFacetFactory();

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@ParameterLayout(labelPosition = LabelPosition.LEFT) final String foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[] { String.class });

        facetFactory.processParams(new FacetFactory.ProcessParameterContext(Customer.class, method, 0, null, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(LabelAtFacet.class);
        assertThat(facet, is(notNullValue()));
        assertThat(facet, is(instanceOf(LabelAtFacetForParameterLayoutAnnotation.class)));
        final LabelAtFacetForParameterLayoutAnnotation layoutAnnotation = (LabelAtFacetForParameterLayoutAnnotation) facet;
        assertThat(layoutAnnotation.label(), is(LabelPosition.LEFT));
    }
}
