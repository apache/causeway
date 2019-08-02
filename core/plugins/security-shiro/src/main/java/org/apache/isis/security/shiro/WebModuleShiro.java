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
package org.apache.isis.security.shiro;

import javax.inject.Singleton;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.shiro.config.Ini;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.apache.shiro.web.env.WebEnvironment;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.webapp.modules.WebModule;
import org.apache.isis.webapp.modules.WebModuleContext;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;
import static org.apache.isis.commons.internal.context._Context.getDefaultClassLoader;
import static org.apache.isis.commons.internal.exceptions._Exceptions.unexpectedCodeReach;

import lombok.val;

/**
 * WebModule to enable support for Shiro.
 * <p>
 * Can be customized via static {@link WebModuleShiro#setShiroEnvironmentClass(Class)}
 * @since 2.0
 */
@Singleton @Order(Ordered.HIGHEST_PRECEDENCE)
public final class WebModuleShiro implements WebModule  {

    private final static String SHIRO_LISTENER_CLASS_NAME = 
            "org.apache.shiro.web.env.EnvironmentLoaderListener";

    private final static String SHIRO_FILTER_CLASS_NAME = 
            "org.apache.shiro.web.servlet.ShiroFilter";
    
    private final static String SHIRO_FILTER_NAME = "ShiroFilter";
    
    // -- CONFIGURATION
    
    public static void setShiroEnvironmentClass(Class<? extends WebEnvironment> shiroEnvironmentClass) {
    	if(shiroEnvironmentClass==null) {
    		System.setProperty("shiroEnvironmentClass", null);
    		return;
    	} 
    	System.setProperty("shiroEnvironmentClass", shiroEnvironmentClass.getName());
    }
    
	public static class IniWebEnvironmentUsingSystemProperty extends IniWebEnvironment {
		@Override
		public Ini getIni() {
			val customShiroIniResource = System.getProperty("shiroIniResource");
			if(_Strings.isNotEmpty(customShiroIniResource)) {
				val ini = new Ini();
				ini.loadFromPath(customShiroIniResource);
				return ini;	
	        } 
	        return null;
		}
	}
    
	public static void setShiroIniResource(String resourcePath) {
    	if(resourcePath==null) {
    		System.setProperty("shiroIniResource", null);
    		setShiroEnvironmentClass(null);
    		return;
    	}
    	System.setProperty("shiroIniResource", resourcePath);
		setShiroEnvironmentClass(IniWebEnvironmentUsingSystemProperty.class);
	}
	
	// -- 

    @Override
    public String getName() {
        return "Shiro";
    }
    
    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {
        
        final Dynamic filter;
        try {
            val filterClass = getDefaultClassLoader().loadClass(SHIRO_FILTER_CLASS_NAME);
            val filterInstance = ctx.createFilter(uncheckedCast(filterClass));
            filter = ctx.addFilter(SHIRO_FILTER_NAME, filterInstance);
            if(filter==null) {
                return null; // filter was already registered somewhere else (eg web.xml)
            }
        } catch (ClassNotFoundException e) {
            // guarded against by isAvailable()
            throw unexpectedCodeReach();
        }
        
        val customShiroEnvironmentClassName = System.getProperty("shiroEnvironmentClass");
        if(_Strings.isNotEmpty(customShiroEnvironmentClassName)) {
        	ctx.setInitParameter("shiroEnvironmentClass", customShiroEnvironmentClassName);	
        }
        
        val urlPattern = "/*";
        filter.addMappingForUrlPatterns(null, false, urlPattern); // filter is forced first
        
        try {
            val listenerClass = getDefaultClassLoader().loadClass(SHIRO_LISTENER_CLASS_NAME);
            return ctx.createListener(uncheckedCast(listenerClass));
        } catch (ClassNotFoundException e) {
            // guarded against by isAvailable()
            throw unexpectedCodeReach();
        }
      
    }

    @Override
    public boolean isApplicable(WebModuleContext ctx) {
        try {
            getDefaultClassLoader().loadClass(SHIRO_LISTENER_CLASS_NAME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }



}
