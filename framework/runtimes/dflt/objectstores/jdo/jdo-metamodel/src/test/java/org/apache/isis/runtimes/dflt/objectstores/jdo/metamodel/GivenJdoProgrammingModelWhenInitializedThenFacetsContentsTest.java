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
package org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel;

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
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableAnnotationFacetFactory;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.prop.primarykey.JdoPrimaryKeyAnnotationFacetFactory;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.specloader.progmodelfacets.JdoProgrammingModelFacets;

@RunWith(Parameterized.class)
public class GivenJdoProgrammingModelWhenInitializedThenFacetsContentsTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { JdoPersistenceCapableAnnotationFacetFactory.class },
                { JdoPrimaryKeyAnnotationFacetFactory.class },
                });
    }


    private final Class<?> facetFactoryClass;

    public GivenJdoProgrammingModelWhenInitializedThenFacetsContentsTest(
            final Class<?> facetFactoryClass) {
        this.facetFactoryClass = facetFactoryClass;
    }


    @Test
    public void shouldContainSpecifiedFacetFactory() throws Exception {
        final JdoProgrammingModelFacets jpaProgrammingModelFacets = new JdoProgrammingModelFacets();
        jpaProgrammingModelFacets.init();
        final List<FacetFactory> list = jpaProgrammingModelFacets.getList();
        assertThat(list, IsisMatchers
                .containsElementThat(is(instanceOf(facetFactoryClass))));
    }
}
