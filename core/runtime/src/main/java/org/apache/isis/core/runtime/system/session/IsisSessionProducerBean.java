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

package org.apache.isis.core.runtime.system.session;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.context.annotation.Bean;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

/**
 * @deprecated TODO [2033] convert ...
 * Currently `IsisSessionFactory` and those singletons that depend on it 
 * (`SpecificationLoader` and `PersistenceSessionFactory`) are not life-cycle 
 * managed by CDI, instead we provide Producer Methods for these.
 * 
 * Consequently the framework is responsible for their life-cycles. 
 * 
 * We want to have CDI also manage these, which can be achieved by finally 
 * removing the `IsisSessionProducerBean` singleton.
 *
 */
@Singleton
public class IsisSessionProducerBean {

    @Inject ServiceRegistry serviceRegistry; // depends on
    
	@Bean @Produces @Singleton //XXX note: the resulting singleton is not life-cycle managed by CDI, neither are InjectionPoints resolved by CDI
	public IsisSessionFactory produceIsisSessionFactory() {
		return _Context.computeIfAbsent(IsisSessionFactory.class, this::newIsisSessionFactory);
	}

	@Bean @Produces @Singleton //XXX note: the resulting singleton is not life-cycle managed by CDI, neither are InjectionPoints resolved by CDI
	public SpecificationLoader produceSpecificationLoader() {
		return produceIsisSessionFactory().getSpecificationLoader();
	}
	
	// -- HELPER
	
	private final _Lazy<IsisSessionFactory> isisSessionFactorySingleton = 
			_Lazy.threadSafe(this::newIsisSessionFactory);
	
	private final static _Probe probe = _Probe.maxCallsThenExitWithStacktrace(10).label("IsisSessionProducerBean");
	
	private IsisSessionFactory newIsisSessionFactory() {

		try {		
		
			probe.println("newIsisSessionFactory");
	
			final IsisSessionFactoryBuilder builder = new IsisSessionFactoryBuilder();
	
			// as a side-effect, if the metamodel turns out to be invalid, then
			// this will push the MetaModelInvalidException into IsisContext.
			return builder.buildSessionFactory();
		
		} catch (Exception e) {
			System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			e.printStackTrace();
			System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			System.exit(1);
			throw e;
		}
		
	}
	
	
}
