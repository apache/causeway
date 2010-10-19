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


package org.apache.isis.extensions.headless.embedded;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.apache.isis.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.commons.ensure.Ensure.ensureThatState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.isis.commons.components.ApplicationScopedComponent;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.config.internal.PropertiesConfiguration;
import org.apache.isis.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.metamodel.spec.IntrospectableSpecification;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.services.ServicesInjector;
import org.apache.isis.metamodel.services.container.DomainObjectContainerDefault;
import org.apache.isis.metamodel.specloader.JavaReflector;
import org.apache.isis.metamodel.specloader.ObjectReflectorAbstract;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.metamodel.specloader.classsubstitutor.ClassSubstitutorIdentity;
import org.apache.isis.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistryDefault;
import org.apache.isis.metamodel.specloader.progmodelfacets.ProgrammingModelFacets;
import org.apache.isis.metamodel.specloader.progmodelfacets.ProgrammingModelFacetsJava5;
import org.apache.isis.metamodel.specloader.traverser.SpecificationTraverser;
import org.apache.isis.metamodel.specloader.traverser.SpecificationTraverserDefault;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorNoop;
import org.apache.isis.extensions.headless.applib.HeadlessViewer;
import org.apache.isis.extensions.headless.embedded.internal.RuntimeContextForEmbeddedMetaModel;
import org.apache.isis.extensions.headless.viewer.DomainObjectContainerHeadlessViewer;
import org.apache.isis.extensions.headless.viewer.internal.HeadlessViewerImpl;


/**
 * Facade for the entire Isis metamodel and supporting components.
 */
public class IsisMetaModel implements ApplicationScopedComponent {

	private static enum State {
		NOT_INITIALIZED,
		INITIALIZED,
		SHUTDOWN;
	}
	

	private final List<Class<?>> serviceTypes = new ArrayList<Class<?>>();
	private State state = State.NOT_INITIALIZED;
	
	private ObjectReflectorAbstract reflector;
	private RuntimeContextForEmbeddedMetaModel runtimeContext;
	
	private IsisConfiguration configuration;
	private ClassSubstitutor classSubstitutor;
	private CollectionTypeRegistry collectionTypeRegistry;
	private ProgrammingModelFacets programmingModelFacets;
	private SpecificationTraverser specificationTraverser;
	private Set<FacetDecorator> facetDecorators;
	private MetaModelValidator metaModelValidator;
	
	private HeadlessViewer viewer;
	private EmbeddedContext context;
	private List<Object> services;
	
	
	public IsisMetaModel(
			final EmbeddedContext context, 
			final Class<?>... serviceTypes) {
		
		this.serviceTypes.addAll(Arrays.asList(serviceTypes));
		setConfiguration(new PropertiesConfiguration());
		setClassSubstitutor(new ClassSubstitutorIdentity());
		setCollectionTypeRegistry(new CollectionTypeRegistryDefault());
		setSpecificationTraverser(new SpecificationTraverserDefault());
		setFacetDecorators(new TreeSet<FacetDecorator>());
		setProgrammingModelFacets(new ProgrammingModelFacetsJava5());
		setMetaModelValidator(new MetaModelValidatorNoop());
		
		this.context = context;
	}

	
	/**
	 * The list of classes representing services, as specified in the {@link #IsisMetaModel(EmbeddedContext, Class...) constructor}.
	 * 
	 * <p>
	 * To obtain the instantiated services, use the {@link ServicesInjector#getRegisteredServices()} (available from {@link #getServicesInjector()}).
	 */
	public List<Class<?>> getServiceTypes() {
		return Collections.unmodifiableList(serviceTypes);
	}

	/////////////////////////////////////////////////////////
	// init, shutdown
	/////////////////////////////////////////////////////////

	public void init() {
		ensureNotInitialized();
		reflector = new JavaReflector(configuration, classSubstitutor, collectionTypeRegistry, specificationTraverser, programmingModelFacets, facetDecorators, metaModelValidator);
		
		services = createServices(serviceTypes);
		runtimeContext = new RuntimeContextForEmbeddedMetaModel(context, services);
		DomainObjectContainerDefault container = new DomainObjectContainerHeadlessViewer();
		
		runtimeContext.injectInto(container);
		runtimeContext.setContainer(container);
		runtimeContext.injectInto(reflector);
		reflector.injectInto(runtimeContext);
		
		reflector.init();
		runtimeContext.init();
		
		for(Class<?> serviceType: serviceTypes) {
			ObjectSpecification serviceNoSpec = reflector.loadSpecification(serviceType);
            if (serviceNoSpec instanceof IntrospectableSpecification) {
                IntrospectableSpecification introspectableSpecification = (IntrospectableSpecification) serviceNoSpec;
                introspectableSpecification.markAsService();
            }

		}
		state = State.INITIALIZED;
		
		viewer = new HeadlessViewerImpl(runtimeContext);
	}

	public void shutdown() {
		ensureInitialized();
		state = State.SHUTDOWN;
	}

	private List<Object> createServices(List<Class<?>> serviceTypes) {
		List<Object> services = new ArrayList<Object>();
		for(Class<?> serviceType: serviceTypes) {
			try {
				services.add(serviceType.newInstance());
			} catch (InstantiationException e) {
				throw new IsisException("Unable to instantiate service", e);
			} catch (IllegalAccessException e) {
				throw new IsisException("Unable to instantiate service", e);
			}
		}
		return services;
	}



	/////////////////////////////////////////////////////////
	// SpecificationLoader
	/////////////////////////////////////////////////////////

	/**
	 * Available once {@link #init() initialized}.
	 */
	public SpecificationLoader getSpecificationLoader() {
		return reflector;
	}
	
	/////////////////////////////////////////////////////////
	// Viewer
	/////////////////////////////////////////////////////////
	
	/**
	 * Available once {@link #init() initialized}.
	 */
	public HeadlessViewer getViewer() {
		ensureInitialized();
		return viewer;
	}

	/////////////////////////////////////////////////////////
	// ServicesInjector
	/////////////////////////////////////////////////////////
	
	/**
	 * The {@link ServicesInjector}; can use to obtain the set of registered services.
	 * 
	 * <p>
	 * Available once {@link #init() initialized}.
	 */
	public ServicesInjector getServicesInjector() {
		ensureInitialized();
		return runtimeContext.getServicesInjector();
	}

	/////////////////////////////////////////////////////////
	// Override defaults
	/////////////////////////////////////////////////////////

	/**
	 * The {@link IsisConfiguration} in force, either defaulted or specified
	 * {@link #setConfiguration(IsisConfiguration) explicitly.}
	 */
	public IsisConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Optionally specify the {@link IsisConfiguration}.
	 * 
	 * <p>
	 * Call prior to {@link #init()}.
	 */
	public void setConfiguration(IsisConfiguration configuration) {
		ensureNotInitialized();
		ensureThatArg(configuration, is(notNullValue()));
		this.configuration = configuration;
	}

	/**
	 * The {@link ClassSubstitutor} in force, either defaulted or
	 * specified {@link #setClassSubstitutor(ClassSubstitutor) explicitly}.
	 */
	public ClassSubstitutor getClassSubstitutor() {
		return classSubstitutor;
	}

	/**
	 * Optionally specify the {@link ClassSubstitutor}.
	 * 
	 * <p>
	 * Call prior to {@link #init()}.
	 */
	public void setClassSubstitutor(ClassSubstitutor classSubstitutor) {
		ensureNotInitialized();
		ensureThatArg(classSubstitutor, is(notNullValue()));
		this.classSubstitutor = classSubstitutor;
	}

	/**
	 * The {@link CollectionTypeRegistry} in force, either defaulted or
	 * specified {@link #setCollectionTypeRegistry(CollectionTypeRegistry) explicitly.}
	 */
	public CollectionTypeRegistry getCollectionTypeRegistry() {
		return collectionTypeRegistry;
	}

	/**
	 * Optionally specify the {@link CollectionTypeRegistry}.
	 * 
	 * <p>
	 * Call prior to {@link #init()}.
	 */
	public void setCollectionTypeRegistry(
			CollectionTypeRegistry collectionTypeRegistry) {
		ensureNotInitialized();
		ensureThatArg(collectionTypeRegistry, is(notNullValue()));
		this.collectionTypeRegistry = collectionTypeRegistry;
	}

	/**
	 * The {@link SpecificationTraverser} in force, either defaulted or
	 * specified {@link #setSpecificationTraverser(SpecificationTraverser) explicitly}.
	 */
	public SpecificationTraverser getSpecificationTraverser() {
		return specificationTraverser;
	}

	/**
	 * Optionally specify the {@link SpecificationTraverser}.
	 */
	public void setSpecificationTraverser(
			SpecificationTraverser specificationTraverser) {
		this.specificationTraverser = specificationTraverser;
	}
	
	/**
	 * The {@link ProgrammingModelFacets} in force, either defaulted or
	 * specified {@link #setProgrammingModelFacets(ProgrammingModelFacets) explicitly}.
	 */
	public ProgrammingModelFacets getProgrammingModelFacets() {
		return programmingModelFacets;
	}

	/**
	 * Optionally specify the {@link ProgrammingModelFacets}.
	 * 
	 * <p>
	 * Call prior to {@link #init()}.
	 */
	public void setProgrammingModelFacets(
			ProgrammingModelFacets programmingModelFacets) {
		ensureNotInitialized();
		ensureThatArg(programmingModelFacets, is(notNullValue()));
		this.programmingModelFacets = programmingModelFacets;
	}

	/**
	 * The {@link FacetDecorator}s in force, either defaulted or specified
	 * {@link #setFacetDecorators(Set) explicitly}.
	 */
	public Set<FacetDecorator> getFacetDecorators() {
		return Collections.unmodifiableSet(facetDecorators);
	}

	/**
	 * Optionally specify the {@link FacetDecorator}s.
	 * 
	 * <p>
	 * Call prior to {@link #init()}.
	 */
	public void setFacetDecorators(Set<FacetDecorator> facetDecorators) {
		ensureNotInitialized();
		ensureThatArg(facetDecorators, is(notNullValue()));
		this.facetDecorators = facetDecorators;
	}


	/**
	 * The {@link MetaModelValidator} in force, either defaulted or specified
	 * {@link #setMetaModelValidator(MetaModelValidator) explicitly}.
	 */
	public MetaModelValidator getMetaModelValidator() {
		return metaModelValidator;
	}
	
	/**
	 * Optionally specify the {@link MetaModelValidator}.
	 */
	public void setMetaModelValidator(
			MetaModelValidator metaModelValidator) {
		this.metaModelValidator = metaModelValidator;
	}


	/////////////////////////////////////////////////////////
	// State management
	/////////////////////////////////////////////////////////
	
	private State ensureNotInitialized() {
		return ensureThatState(state, is(State.NOT_INITIALIZED));
	}
	
	private State ensureInitialized() {
		return ensureThatState(state, is(State.INITIALIZED));
	}
	
}
