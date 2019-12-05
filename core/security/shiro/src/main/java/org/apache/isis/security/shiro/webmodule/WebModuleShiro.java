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
package org.apache.isis.security.shiro.webmodule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.shiro.config.Ini;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.webapp.util.IsisWebAppUtils;
import org.apache.isis.webapp.modules.WebModule;
import org.apache.isis.webapp.modules.WebModuleContext;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;
import static org.apache.isis.commons.internal.context._Context.getDefaultClassLoader;
import static org.apache.isis.commons.internal.exceptions._Exceptions.unexpectedCodeReach;

import lombok.SneakyThrows;
import lombok.val;

/**
 * WebModule to enable support for Shiro.
 * <p>
 * Can be customized via static {@link WebModuleShiro#setShiroEnvironmentClass(Class)}
 * @since 2.0
 */
@Service @Order(Ordered.HIGHEST_PRECEDENCE + 100)
public final class WebModuleShiro implements WebModule  {
    
    private final static String SHIRO_LISTENER_CLASS_NAME = EnvironmentLoaderListenerForIsis.class.getName();
    private final static String SHIRO_FILTER_CLASS_NAME = ShiroFilter.class.getName();

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
        // see https://issues.apache.org/jira/browse/SHIRO-610
        @Override
        protected Map<String, Object> getDefaults() {
            Map<String, Object> defaults = new HashMap<String, Object>();
            defaults.put(FILTER_CHAIN_RESOLVER_NAME, new PathMatchingFilterChainResolver());
            return defaults;
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
    
    /**
     * Adds support for dependency injection into security realms
     * @since 2.0
     */
    public static class EnvironmentLoaderListenerForIsis extends EnvironmentLoaderListener {
        
        @Override 
        protected WebEnvironment createEnvironment(ServletContext servletContext) {
            val shiroEnvironment = super.createEnvironment(servletContext);
            val securityManager = shiroEnvironment.getSecurityManager();
            val serviceInjector = IsisWebAppUtils.getManagedBean(ServiceInjector.class, servletContext);
            
            injectServicesIntoReamls(serviceInjector, securityManager);
            
            return shiroEnvironment;
        }
        
        @SuppressWarnings("unchecked")
        @SneakyThrows
        public static void injectServicesIntoReamls(
                ServiceInjector serviceInjector, 
                org.apache.shiro.mgt.SecurityManager securityManager) {

            // reflective access to SecurityManager.getRealms()
            val realms = (Collection<Realm>) ReflectionUtils
                    .findMethod(securityManager.getClass(), "getRealms")
                    .invoke(securityManager, _Constants.emptyObjects);

            realms.stream().forEach(serviceInjector::injectServicesInto);
        }
        
    }

    // -- 

    @Override
    public String getName() {
        return "Shiro";
    }
    
    @Override
    public void prepare(WebModuleContext ctx) {
        val customShiroEnvironmentClassName = System.getProperty("shiroEnvironmentClass");
        if(_Strings.isEmpty(customShiroEnvironmentClassName)) {
            setShiroEnvironmentClass(IniWebEnvironmentUsingSystemProperty.class);
        }
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
