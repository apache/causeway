/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.services.background;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

import java.lang.reflect.InvocationHandler;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.background.BackgroundCommandService;
import org.apache.isis.applib.services.background.BackgroundCommandService2;
import org.apache.isis.applib.services.background.BackgroundService2;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.core.commons.lang.ArrayExtensions;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ProxyEnhanced;
import org.apache.isis.core.plugins.codegen.ProxyFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For command-reification depends on an implementation of 
 * {@link org.apache.isis.applib.services.background.BackgroundCommandService} to
 * be configured.
 */
@DomainService(
		nature = NatureOfService.DOMAIN,
		menuOrder = "" + Integer.MAX_VALUE
		)
public class BackgroundServiceDefault implements BackgroundService2 {
	
	static final Logger LOG = LoggerFactory.getLogger(BackgroundServiceDefault.class);

	/*
	 * For the fixed thread-pool let there be 1-4 concurrent threads,
	 * limited by the number of available (logical) processor cores.
	 * 
	 * Note: Future improvements might make these values configurable, 
	 * but for now lets try to be reasonably nice here.
	 * 
	 */
	private final int minThreadCount = 1; // only used if there is no BackgroundCommandService
	private final int maxThreadCount = 4; // only used if there is no BackgroundCommandService

	private final int threadCount = // only used if there is no BackgroundCommandService
			Math.max(minThreadCount, 
					Math.min(maxThreadCount,
							Runtime.getRuntime().availableProcessors()));

	// only used if there is no BackgroundCommandService
	private ExecutorService backgroundExecutorService; 

	@Programmatic
	@PostConstruct
	public void init(Map<String,String> props) {
		if(backgroundCommandService==null) {
			backgroundExecutorService = Executors.newFixedThreadPool(threadCount);	
		}
	}

	@Programmatic
	@PreDestroy
	public void shutdown() {
		if(backgroundExecutorService!=null) {
			backgroundExecutorService.shutdownNow();
			backgroundExecutorService = null;
		}
	}

	ObjectSpecification getSpecification(final Class<?> type) {
		return specificationLoader.loadSpecification(type);
	}

	// //////////////////////////////////////

	@Programmatic
	@Override
	public <T> T execute(final T domainObject) {
		final Class<T> cls = uncheckedCast(domainObject.getClass());
		final InvocationHandler methodHandler = newMethodHandler(domainObject, null);
		return newProxy(cls, null, methodHandler);
	}

	@Override
	public <T> T executeMixin(Class<T> mixinClass, Object mixedIn) {
		final T mixin = factoryService.mixin(mixinClass, mixedIn);
		final InvocationHandler methodHandler = newMethodHandler(mixin, mixedIn);
		return newProxy(mixinClass, mixedIn, methodHandler);
	}

	private <T> T newProxy(
			final Class<T> cls,
			final Object mixedInIfAny,
			final InvocationHandler methodHandler) {

		final Class<?>[] interfaces = ArrayExtensions.combine(
				cls.getInterfaces(), 
				new Class<?>[] { ProxyEnhanced.class }); 

		final boolean initialize = mixedInIfAny!=null;


		final Class<?>[] constructorArgTypes = initialize ? new Class<?>[] {mixedInIfAny.getClass()} : _Constants.emptyClasses;
		final Object[] constructorArgs = initialize ? new Object[] {mixedInIfAny} : _Constants.emptyObjects;

		final ProxyFactory<T> proxyFactory = ProxyFactory.builder(cls)
				.interfaces(interfaces)
				.constructorArgTypes(constructorArgTypes)
				.build();

		return initialize 
				? proxyFactory.createInstance(methodHandler, constructorArgs)  
						: proxyFactory.createInstance(methodHandler, false)
						;
	}

	/**
	 *
	 * @param target - the object that is proxied, either a domain object or a mixin around a domain object
	 * @param mixedInIfAny - if target is a mixin, then this is the domain object that is mixed-in to.
	 */
	private <T> InvocationHandler newMethodHandler(final T target, final Object mixedInIfAny) {

		if(backgroundCommandService==null) {
			return new ForkingInvocationHandler<T>(target, mixedInIfAny, backgroundExecutorService);
		}

		return new CommandInvocationHandler<T>(
				(BackgroundCommandService2) backgroundCommandService, 
				target, 
				mixedInIfAny, 
				specificationLoader,
				commandDtoServiceInternal,
				commandContext,
				this::getAdapterManager);

	}


	// //////////////////////////////////////

	@javax.inject.Inject
	private BackgroundCommandService backgroundCommandService;

	@javax.inject.Inject
	private CommandDtoServiceInternal commandDtoServiceInternal;

	@javax.inject.Inject
	private CommandContext commandContext;

	@javax.inject.Inject
	private FactoryService factoryService;

	@javax.inject.Inject
	private SpecificationLoader specificationLoader;

	@javax.inject.Inject
	private IsisSessionFactory isisSessionFactory;

	protected AdapterManager getAdapterManager() {
		return isisSessionFactory.getCurrentSession().getPersistenceSession();
	}

}
