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


package org.apache.isis.core.progmodel.facets.object.ident.title.annotation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.annotation.Title;
import org.apache.isis.core.metamodel.adapter.LocalizationProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.spec.SpecificationLookup;
import org.apache.isis.core.progmodel.facets.MethodFinderUtils;
import org.apache.isis.core.progmodel.facets.object.title.annotation.TitleFacetViaTitleAnnotation;
import org.apache.isis.core.progmodel.facets.object.title.annotation.TitleFacetViaTitleAnnotation.TitleComponent;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;

@RunWith(JMock.class)
public class TitleFacetViaTitleAnnotationTest  {

	private Mockery context = new JUnit4Mockery();
	private FacetHolder mockFacetHolder;
	private ObjectAdapter mockOwningAdapter;
    private SpecificationLookup mockSpecificationLookup;
    private AdapterMap mockAdapterMap;
    private LocalizationProvider mockLocalizationProvider;

    protected static class DomainObjectWithProblemInItsAnnotatedTitleMethod {

    	@Title
        public String screwedTitle() {
            throw new NullPointerException();
        }

    }

    protected static class NormalDomainObject {

    	@Title(sequence="1.0")
        public String titleElement1() {
            return "Normal";
        }

    	@Title(sequence="2.0")
        public String titleElement2() {
            return "Domain";
        }

    	@Title(sequence="3.0")
        public String titleElement3() {
            return "Object";
        }
    	
    }

    @Before
    public void setUp() throws Exception {
    	mockFacetHolder = context.mock(FacetHolder.class);
    	mockOwningAdapter = context.mock(ObjectAdapter.class);
    	mockSpecificationLookup = context.mock(SpecificationLookup.class);
    	mockAdapterMap = context.mock(AdapterMap.class);
    	mockLocalizationProvider = context.mock(LocalizationProvider.class);
    }

    @Test
    public void testTitle() throws Exception {
        List<Method> methods = Arrays.asList(NormalDomainObject.class.getMethod("titleElement1"), NormalDomainObject.class.getMethod("titleElement2"), NormalDomainObject.class.getMethod("titleElement3"));

        final List<TitleComponent> components = Lists.transform(methods, TitleComponent.FROM_METHOD);
        TitleFacetViaTitleAnnotation facet = new TitleFacetViaTitleAnnotation(components, mockFacetHolder, mockSpecificationLookup, mockAdapterMap, mockLocalizationProvider);
    	final NormalDomainObject normalPojo = new NormalDomainObject();
    	final Sequence sequence = context.sequence("in-title-element-order");
		context.checking(new Expectations(){{
			allowing(mockOwningAdapter).getObject();
			will(returnValue(normalPojo));
			
			allowing(mockAdapterMap).getAdapterFor("Normal");
            inSequence(sequence);

            allowing(mockAdapterMap).getAdapterFor("Domain");
            inSequence(sequence);

            allowing(mockAdapterMap).getAdapterFor("Object");
            inSequence(sequence);
		}});
		
    	String title = facet.title(mockOwningAdapter, null);
    	assertThat(title, is("Normal Domain Object"));
    }

    @Test
    public void titleThrowsException() {
        List<Method> methods = MethodFinderUtils.findMethodsWithAnnotation(DomainObjectWithProblemInItsAnnotatedTitleMethod.class, MethodScope.OBJECT,
        		Title.class);

        final List<TitleComponent> components = Lists.transform(methods, TitleComponent.FROM_METHOD);
        TitleFacetViaTitleAnnotation facet = new TitleFacetViaTitleAnnotation(components, mockFacetHolder, mockSpecificationLookup, mockAdapterMap, mockLocalizationProvider);
    	final DomainObjectWithProblemInItsAnnotatedTitleMethod screwedPojo = new DomainObjectWithProblemInItsAnnotatedTitleMethod();
		context.checking(new Expectations(){{
			allowing(mockOwningAdapter).getObject();
			will(returnValue(screwedPojo));
		}});

    	String title = facet.title(mockOwningAdapter, null);
    	assertThat(title, is("Failed Title"));
    }

}
