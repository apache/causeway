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
package org.apache.isis.runtime.spring;

import javax.inject.Singleton;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.ConfigurableEnvironment;

import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.spring._Spring;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.metamodel.IsisMetamodelModule;
import org.apache.isis.core.metamodel.services.registry.ServiceRegistryDefault;
import org.apache.isis.core.metamodel.services.registry.SpringContextProvider;
import org.apache.isis.core.runtime.IsisRuntimeModule;
import org.apache.isis.core.runtime.services.IsisRuntimeServicesModule;
import org.apache.isis.core.runtime.threadpool.ThreadPoolExecutionMode;
import org.apache.isis.core.runtime.threadpool.ThreadPoolSupport;
import org.apache.isis.core.wrapper.IsisWrapperModule;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Configuration 
@ComponentScan(
		basePackageClasses= {
				IsisApplibModule.class,
				IsisMetamodelModule.class,
				IsisRuntimeModule.class,
				IsisRuntimeServicesModule.class,
				IsisWrapperModule.class},
		includeFilters= {
				@Filter(type = FilterType.CUSTOM, classes= {BeanScanInterceptorForSpring.class})
		})
@Slf4j
public class IsisBoot implements ApplicationContextAware {
	
    @Autowired
    private ConfigurableEnvironment configurableEnvironment;
    
	@Override
	public void setApplicationContext(ApplicationContext springContext) throws BeansException {
	    
        // disables concurrent Spec-Loading
        ThreadPoolSupport.HIGHEST_CONCURRENCY_EXECUTION_MODE_ALLOWED = 
                ThreadPoolExecutionMode.SEQUENTIAL_WITHIN_CALLING_THREAD;
        
        _Context.clear(); // ensures a well defined precondition

	    _Context.putSingleton(ApplicationContext.class, springContext);
	    _Config.putAll(_Spring.copySpringEnvironmentToMap(configurableEnvironment));

	    log.info("Spring's context was passed over to Isis");
	    
	    val config = _Config.getConfiguration();
	    
	    // dump config to log
	    if(log.isInfoEnabled() && !config.getEnvironment().isUnitTesting()) {
	        log.info("\n" + _Config.getConfiguration().toStringFormatted());
	    }    
	    
	}
	
	/**
	 * With this producer we create a dependency from {@link ServiceRegistryDefault} to here.
	 * Lets {@link ServiceRegistryDefault} wait on Spring's context to become available.
	 * @return just a dummy
	 */
	@Bean @Singleton 
	public SpringContextProvider springContextProvider() {
	    return new SpringContextProvider() {};
	}

	
}
