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
package org.apache.isis.extensions.jpa.metamodel;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.object.entity.JpaEntityAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.prop.id.JpaIdAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.specloader.progmodelfacets.JpaProgrammingModelFacets;

@RunWith(Parameterized.class)
public class GivenJpaProgrammingModelWhenInitializedThenFacetsContentsTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { JpaEntityAnnotationFacetFactory.class },
                { JpaIdAnnotationFacetFactory.class },
                });
    }


    private final Class<?> facetFactoryClass;

    public GivenJpaProgrammingModelWhenInitializedThenFacetsContentsTest(
            final Class<?> facetFactoryClass) {
        this.facetFactoryClass = facetFactoryClass;
    }


    @Test
    public void shouldContainSpecifiedFacetFactory() throws Exception {
        final JpaProgrammingModelFacets jpaProgrammingModelFacets = new JpaProgrammingModelFacets();
        jpaProgrammingModelFacets.init();
        final List<FacetFactory> list = jpaProgrammingModelFacets.getList();
        assertThat(list, IsisMatchers
                .containsElementThat(is(instanceOf(facetFactoryClass))));
    }
}
