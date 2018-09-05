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

package org.apache.isis.core.metamodel.facets.object.ident.title.annotation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.google.common.collect.Lists;

import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.annotation.Title;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.object.title.annotation.TitleAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.title.annotation.TitleFacetViaTitleAnnotation;
import org.apache.isis.core.metamodel.facets.object.title.annotation.TitleFacetViaTitleAnnotation.TitleComponent;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class TitleFacetViaTitleAnnotationTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);

    @Mock
    private FacetHolder mockFacetHolder;

    @Mock
    private ObjectAdapter mockObjectAdapter;

    @Mock
    private ObjectAdapterProvider mockAdapterManager;
    
    protected static class DomainObjectWithProblemInItsAnnotatedTitleMethod {

        @Title
        public String brokenTitle() {
            throw new NullPointerException();
        }

    }

    protected static class NormalDomainObject {

        @Title(sequence = "1.0")
        public String titleElement1() {
            return "Normal";
        }

        @Title(sequence = "2.0")
        public String titleElement2() {
            return "Domain";
        }

        @Title(sequence = "3.0")
        public String titleElement3() {
            return "Object";
        }

    }

    @Test
    public void testTitle() throws Exception {
        final List<Annotations.Evaluator<Title>> evaluatorList = Annotations
                .getEvaluators(NormalDomainObject.class, Title.class);

        TitleAnnotationFacetFactory.sort(evaluatorList);

        final List<TitleComponent> components = Lists.transform(evaluatorList, TitleComponent.FROM_EVALUATORS);
        final TitleFacetViaTitleAnnotation facet = new TitleFacetViaTitleAnnotation(components, mockFacetHolder, mockAdapterManager);
        final NormalDomainObject normalPojo = new NormalDomainObject();
        final Sequence sequence = context.sequence("in-title-element-order");
        context.checking(new Expectations() {
            {
                allowing(mockObjectAdapter).getObject();
                will(returnValue(normalPojo));

                allowing(mockAdapterManager).adapterFor("Normal");
                inSequence(sequence);

                allowing(mockAdapterManager).adapterFor("Domain");
                inSequence(sequence);

                allowing(mockAdapterManager).adapterFor("Object");
                inSequence(sequence);
            }
        });

        final String title = facet.title(mockObjectAdapter);
        assertThat(title, is("Normal Domain Object"));
    }

    @Test
    public void titleThrowsException() {

        final List<Annotations.Evaluator<Title>> evaluators = Annotations
                .getEvaluators(DomainObjectWithProblemInItsAnnotatedTitleMethod.class, Title.class);

        final List<TitleComponent> components = Lists.transform(evaluators, TitleComponent.FROM_EVALUATORS);
        final TitleFacetViaTitleAnnotation facet = new TitleFacetViaTitleAnnotation(components, mockFacetHolder, mockAdapterManager);
        final DomainObjectWithProblemInItsAnnotatedTitleMethod screwedPojo = new DomainObjectWithProblemInItsAnnotatedTitleMethod();
        context.checking(new Expectations() {
            {
                allowing(mockObjectAdapter).getObject();
                will(returnValue(screwedPojo));
            }
        });

        final String title = facet.title(mockObjectAdapter);
        assertThat(title, is("Failed Title"));
    }

}
