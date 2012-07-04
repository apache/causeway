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
package org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.object.entity.JpaEntityAnnotationFacetFactory;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.specloader.progmodelfacets.JpaProgrammingModelFacets;

public class GivenJpaProgrammingModelWhenInitializingTest {

    private JpaProgrammingModelFacets jpaProgrammingModelFacets;


    @Before
    public void setUp() throws Exception {
        jpaProgrammingModelFacets = new JpaProgrammingModelFacets();
    }


    @Test
    public void hasNoFacetFactoriesRegisteredIfNotInitialized()
            throws Exception {
        final List<FacetFactory> list = jpaProgrammingModelFacets.getList();
        assertThat(list.size(), is(0));
    }

    @Test
    public void onceInitializedShouldContainEntityAnnotationFacetFactory()
            throws Exception {
        jpaProgrammingModelFacets.init();
        final List<FacetFactory> list = jpaProgrammingModelFacets.getList();
        assertThat(
                list,
                IsisMatchers.containsElementThat(is(instanceOf(JpaEntityAnnotationFacetFactory.class))));
    }
}
