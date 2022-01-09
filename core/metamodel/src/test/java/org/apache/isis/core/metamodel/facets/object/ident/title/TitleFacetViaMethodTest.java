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
package org.apache.isis.core.metamodel.facets.object.ident.title;

import java.lang.reflect.Method;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.object.title.methods.TitleFacetViaTitleMethod;
import org.apache.isis.core.metamodel.spec.ManagedObject;

public class TitleFacetViaMethodTest
extends AbstractFacetFactoryJUnit4TestCase {

    private TitleFacetViaTitleMethod facet;

    @Mock private ManagedObject mockOwningAdapter;

    private DomainObjectWithProblemInItsTitleMethod pojo;
    private MetaModelContext metaModelContext;

    public static class DomainObjectWithProblemInItsTitleMethod {
        public String title() {
            throw new NullPointerException();
        }
    }

    @Before
    public void setUp() throws Exception {

        metaModelContext = MetaModelContext_forTesting.builder()
                .build();

        pojo = new DomainObjectWithProblemInItsTitleMethod();
        //mockFacetHolder = mockery.mock(FacetHolder.class);
        //mockOwningAdapter = mockery.mock(ManagedObject.class);
        final Method iconNameMethod = DomainObjectWithProblemInItsTitleMethod.class.getMethod("title");
        facet = (TitleFacetViaTitleMethod) TitleFacetViaTitleMethod
                .create(iconNameMethod, mockFacetHolder)
                .orElse(null);


        context.checking(new Expectations() {{

            allowing(mockFacetHolder).getMetaModelContext();
            will(returnValue(metaModelContext));

            allowing(mockOwningAdapter).getPojo();
            will(returnValue(pojo));

        }});
    }

    @Test
    public void testTitleThrowsException() {
        final String title = _TitleFacetUtil.title(facet, mockOwningAdapter);
        assertThat(title, is("Failed Title"));
    }

}
