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
package org.apache.isis.core.webapp;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.core.webapp.modules.WebModule;
import org.apache.isis.core.webapp.modules.WebModuleContext;
import org.springframework.context.ApplicationContext;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * Introduced to render web.xml Filter/Listener/Servlet configurations obsolete.
 * <p> 
 * Acts as the single application entry-point for setting up the 
 * ServletContext programmatically.
 * </p><p> 
 * Installs {@link WebModule}s on the ServletContext. 
 * </p>   
 *  
 * @since 2.0
 *
 */
//@WebListener //[ahuber] to support Servlet 3.0 annotations @WebFilter, @WebListener or others 
//with skinny war deployment requires additional configuration, so for now we disable this annotation
@Log4j2 @Singleton
public class IsisWebAppContextListener implements ServletContextListener {

	private @Inject ServiceRegistry serviceRegistry; // this dependency ensures Isis has been initialized/provisioned 
	
	// -- INTERFACE IMPLEMENTATION

	@Override
	public void contextInitialized(ServletContextEvent event) {
		
		if(!isIsisProvisioned()) {
			log.error("skipping initialization, Spring should already have provisioned all configured Beans");
			return;
		}
		if(!isSpringContextAvailable()) {
			log.error("skipping initialization, SpringContext is required to be initialzed already");
			return;
		}
		if(!isServletContextAvailable()) {
			log.error("skipping initialization, a ServletContext is required on the _Context prior to this");
			return;
		}

		val servletContext = _Context.getIfAny(ServletContext.class);
				
		//[ahuber] set the ServletContext initializing thread as preliminary default until overridden by
		// IsisWicketApplication#init() or others that better know what ClassLoader to use as application default.
		_Context.setDefaultClassLoader(Thread.currentThread().getContextClassLoader(), false);

		val contextPath = servletContext.getContextPath();
		
		log.info("=== PHASE 1 === Setting up ServletContext parameters, contextPath = " + contextPath);
		
		_Resources.putContextPathIfPresent(contextPath);

		final WebModuleContext webModuleContext = new WebModuleContext();
		webModuleContext.prepare();
		
		_Context.putSingleton(WebModuleContext.class, webModuleContext);

		log.info("=== PHASE 2 === Initializing the ServletContext");
		
		webModuleContext.init();	
		log.info("=== DONE === ServletContext initialized.");

	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		val webModuleContext = _Context.getIfAny(WebModuleContext.class);
		if(webModuleContext!=null) {
			webModuleContext.shutdown(event);
		}
	}

	// -- HELPER
	
	private boolean isIsisProvisioned() {
		return serviceRegistry!=null;
	}
	
	private static boolean isSpringContextAvailable() {
		return _Context.getIfAny(ApplicationContext.class)!=null;
	}

	private static boolean isServletContextAvailable() {
		return _Context.getIfAny(ServletContext.class)!=null;
	}

}
