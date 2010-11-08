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


package org.apache.isis.alternatives.embedded;

import java.util.TreeSet;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.metamodel.specloader.progmodelfacets.ProgrammingModelFacets;
import org.apache.isis.alternatives.embedded.EmbeddedContext;
import org.apache.isis.alternatives.embedded.IsisMetaModel;
import org.apache.isis.alternatives.embedded.dom.claim.ClaimRepositoryImpl;
import org.apache.isis.alternatives.embedded.dom.employee.EmployeeRepositoryImpl;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;

@RunWith(JMock.class)
public class GivenMetaModelWhenShutdown {

	private Mockery mockery = new JUnit4Mockery();
	
	private IsisConfiguration mockConfiguration;
	private ProgrammingModelFacets mockProgrammingModelFacets;
	private FacetDecorator mockFacetDecorator;
	private ClassSubstitutor mockClassSubstitutor;
	private CollectionTypeRegistry mockCollectionTypeRegistry;
	private EmbeddedContext mockContext;
	
	private IsisMetaModel metaModel;
	
	
	@Before
	public void setUp() {
		mockContext = mockery.mock(EmbeddedContext.class);
		mockConfiguration = mockery.mock(IsisConfiguration.class);
		mockProgrammingModelFacets = mockery.mock(ProgrammingModelFacets.class);
		mockCollectionTypeRegistry = mockery.mock(CollectionTypeRegistry.class);
		mockFacetDecorator = mockery.mock(FacetDecorator.class);
		mockClassSubstitutor = mockery.mock(ClassSubstitutor.class);
	
		metaModel = new IsisMetaModel(mockContext, EmployeeRepositoryImpl.class, ClaimRepositoryImpl.class);
	}
	

	@Test
	public void shouldSucceedWithoutThrowingAnyExceptions() {
		metaModel.init();
	}

	@Test(expected=IllegalStateException.class)
	public void shouldNotBeAbleToChangeConfiguration() {
		metaModel.init();
		metaModel.setConfiguration(mockConfiguration);
	}
	
	@Test(expected=IllegalStateException.class)
	public void shouldNotBeAbleToChangeProgrammingModelFacets() {
		metaModel.init();
		metaModel.setProgrammingModelFacets(mockProgrammingModelFacets);
	}
	
	@Test(expected=IllegalStateException.class)
	public void shouldNotBeAbleToChangeCollectionTypeRegistry() {
		metaModel.init();
		metaModel.setCollectionTypeRegistry(mockCollectionTypeRegistry);
	}

	@Test(expected=IllegalStateException.class)
	public void shouldNotBeAbleToChangeClassSubstitutor() {
		metaModel.init();
		metaModel.setClassSubstitutor(mockClassSubstitutor);
	}

	@Test(expected=IllegalStateException.class)
	public void shouldNotBeAbleToChangeFacetDecorators() {
		metaModel.init();
		metaModel.setFacetDecorators(new TreeSet<FacetDecorator>());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void shouldNotBeAbleToAddToFacetDecorators() {
		metaModel.init();
		metaModel.getFacetDecorators().add(mockFacetDecorator);
	}

	@Test(expected=IllegalStateException.class)
	public void shouldNotBeAbleToInitializeAgain() {
		metaModel.init();
		//
		metaModel.init();
	}


}
