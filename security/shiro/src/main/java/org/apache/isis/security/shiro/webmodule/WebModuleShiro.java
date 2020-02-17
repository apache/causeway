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

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.shiro.config.Ini;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal._Constants;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.webapp.modules.WebModuleAbstract;
import org.apache.isis.core.webapp.modules.WebModuleContext;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

/**
 * WebModule to enable support for Shiro.
 * <p>
 * Can be customized via static {@link WebModuleShiro#setShiroEnvironmentClass(Class)}
 * @since 2.0
 */
@Service
@Named("isisSecurityKeycloak.WebModuleKeycloak")
@Order(OrderPrecedence.FIRST + 200)
@Qualifier("Shiro")
public class WebModuleShiro extends WebModuleAbstract {
    

    private static final String SHIRO_FILTER_NAME = "ShiroFilter";

    @Inject
    public WebModuleShiro(final ServiceInjector serviceInjector) {
        super(serviceInjector);
    }

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
    @NoArgsConstructor // don't remove, this is class is managed by Isis
    public static class EnvironmentLoaderListenerForIsis extends EnvironmentLoaderListener {

        @Inject private ServiceInjector serviceInjector;

        // testing support
        public EnvironmentLoaderListenerForIsis(ServiceInjector serviceInjector) {
            this.serviceInjector = serviceInjector;
        }

        @Override
        public void contextInitialized(ServletContextEvent sce) {
            super.contextInitialized(sce);
        }

        @Override 
        protected WebEnvironment createEnvironment(ServletContext servletContext) {
            val shiroEnvironment = super.createEnvironment(servletContext);
            val securityManager = shiroEnvironment.getSecurityManager();

            injectServicesIntoRealms(securityManager);
            
            return shiroEnvironment;
        }
        
        @SuppressWarnings("unchecked")
        @SneakyThrows
        public void injectServicesIntoRealms(
                org.apache.shiro.mgt.SecurityManager securityManager) {

            // reflective access to SecurityManager.getRealms()
            val realms = (Collection<Realm>) ReflectionUtils
                    .findMethod(securityManager.getClass(), "getRealms")
                    .invoke(securityManager, _Constants.emptyObjects);

            realms.stream().forEach(serviceInjector::injectServicesInto);
        }
        
    }

    // -- 

    @Getter
    private final String name = "Shiro";

    @Override
    public void prepare(final WebModuleContext ctx) {
        super.prepare(ctx);
        val customShiroEnvironmentClassName = System.getProperty("shiroEnvironmentClass");
        if(_Strings.isEmpty(customShiroEnvironmentClassName)) {
            setShiroEnvironmentClass(IniWebEnvironmentUsingSystemProperty.class);
        }
    }

    @Override
    public Can<ServletContextListener> init(ServletContext ctx) throws ServletException {

        registerFilter(ctx, SHIRO_FILTER_NAME, ShiroFilter.class)
            .ifPresent(filterReg -> {
                filterReg.addMappingForUrlPatterns(
                        null,
                        false, // filter is forced first
                        "/*");
            });

        val customShiroEnvironmentClassName = System.getProperty("shiroEnvironmentClass");
        if(_Strings.isNotEmpty(customShiroEnvironmentClassName)) {
            ctx.setInitParameter("shiroEnvironmentClass", customShiroEnvironmentClassName);
        }

        val listener = createListener(EnvironmentLoaderListenerForIsis.class);
        return Can.ofSingleton(listener);

    }



}
