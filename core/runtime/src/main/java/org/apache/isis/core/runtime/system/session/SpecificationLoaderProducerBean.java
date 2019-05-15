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
import javax.inject.Singleton;

import org.springframework.context.annotation.Bean;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.IsisSystemException;

/**
 * @deprecated TODO [2033] convert SpecificationLoaderDefault to Singleton
 * Currently `SpecificationLoader` is not life-cycle 
 * managed by CDI/Spring, instead we provide a 'Producer Method' for it.
 * 
 * Consequently the framework is responsible for its life-cycle. 
 * 
 * We want to have CDI/Spring also manage these, which can be achieved by finally 
 * removing the `SpecificationLoaderProducerBean` singleton.
 *
 */
@Singleton
public class SpecificationLoaderProducerBean {

    // let Spring manage destruction , init is handled by isisSessionFactory internally
	@Bean(destroyMethod = "shutdown") 
	@Produces @Singleton 
	//XXX note: the resulting singleton is not life-cycle managed by CDI/Spring, 
	//neither are InjectionPoints resolved by CDI/Spring
	public SpecificationLoader produceSpecificationLoader() throws IsisSystemException {
	    return _Context.computeIfAbsent(SpecificationLoader.class, 
	            ()-> new SpecificationLoaderFactory().createSpecificationLoader());
	}
	
}
