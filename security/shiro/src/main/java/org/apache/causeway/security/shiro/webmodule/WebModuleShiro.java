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
package org.apache.causeway.security.shiro.webmodule;

//import org.apache.shiro.web.env.IniWebEnvironment;
//import org.apache.shiro.web.env.WebEnvironment;
//import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
//import org.apache.shiro.web.servlet.ShiroFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.webapp.modules.WebModuleAbstract;
import org.apache.causeway.core.webapp.modules.WebModuleContext;
//import org.apache.causeway.security.shiro.webmodule.WebModuleShiro.EnvironmentLoaderListenerForCauseway;
//import org.apache.causeway.security.shiro.webmodule.WebModuleShiro.IniWebEnvironmentUsingSystemProperty;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * WebModule to enable support for Shiro.
 * <p>
 * Can be customized via static {@link WebModuleShiro#setShiroEnvironmentClass(Class)}
 *
 * @since 2.0 {@index}
 */
@Service
@Named("causeway.security.WebModuleShiro")
@jakarta.annotation.Priority(PriorityPrecedence.FIRST + 100)
@Qualifier("Shiro")
@Log4j2
public class WebModuleShiro extends WebModuleAbstract {

    private static final String SHIRO_FILTER_NAME = "ShiroFilter";

    @Inject
    public WebModuleShiro(final ServiceInjector serviceInjector) {
        super(serviceInjector);
    }

    // -- CONFIGURATION

    /* TODO[ISIS-3275] shiro-web no jakarta API support
    public static void setShiroEnvironmentClass(final Class<? extends WebEnvironment> shiroEnvironmentClass) {
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
*/
    public static void setShiroIniResource(final String resourcePath) {
        /* TODO[ISIS-3275] shiro-web no jakarta API support
        if(resourcePath==null) {
            System.setProperty("shiroIniResource", null);
            setShiroEnvironmentClass(null);
            return;
        }
        System.setProperty("shiroIniResource", resourcePath);
        setShiroEnvironmentClass(IniWebEnvironmentUsingSystemProperty.class);
        */
    }



    /* TODO[ISIS-3275] shiro-web no jakarta API support
     * Adds support for dependency injection into security realms
     * @since 2.0
     *
    @NoArgsConstructor // don't remove, this is class is managed by Causeway
    public static class EnvironmentLoaderListenerForCauseway extends EnvironmentLoaderListener {

        @Inject private ServiceInjector serviceInjector;

        // testing support
        public EnvironmentLoaderListenerForCauseway(final ServiceInjector serviceInjector) {
            this.serviceInjector = serviceInjector;
        }

        @Override
        public void contextInitialized(final ServletContextEvent sce) {
            super.contextInitialized(sce);
        }

        @Override
        protected WebEnvironment createEnvironment(final ServletContext servletContext) {
            val shiroEnvironment = super.createEnvironment(servletContext);
            val securityManager = shiroEnvironment.getSecurityManager();

            injectServicesIntoRealms(securityManager);

            //[CAUSEWAY-3246] Shiro Filter throws NPE on init since Shiro v1.10.0
            if(shiroEnvironment.getShiroFilterConfiguration()==null) {
                _Casts.castTo(MutableWebEnvironment.class, shiroEnvironment)
                .ifPresent(mutableWebEnvironment->
                    mutableWebEnvironment.setShiroFilterConfiguration(new ShiroFilterConfiguration()));
            }

            return shiroEnvironment;
        }

        @SuppressWarnings("unchecked")
        @SneakyThrows
        public void injectServicesIntoRealms(
                final org.apache.shiro.mgt.SecurityManager securityManager) {

            // reflective access to SecurityManager.getRealms()
            val realmsGetter = ReflectionUtils
                    .findMethod(securityManager.getClass(), "getRealms");
            if(realmsGetter==null) {
                log.warn("Could not find method 'getRealms()' with Shiro's SecurityManager. "
                        + "As a consequence cannot enumerate realms.");
                return;
            }

            val realms = (Collection<Realm>) realmsGetter
                    .invoke(securityManager, _Constants.emptyObjects);

            realms.stream().forEach(serviceInjector::injectServicesInto);
        }

    }
    */

    // --

    @Getter
    private final String name = "Shiro";

    @Override
    public void prepare(final WebModuleContext ctx) {
        super.prepare(ctx);/*TODO[ISIS-3275] shiro-web no jakarta API support
        val customShiroEnvironmentClassName = System.getProperty("shiroEnvironmentClass");
        if(_Strings.isEmpty(customShiroEnvironmentClassName)) {
            setShiroEnvironmentClass(IniWebEnvironmentUsingSystemProperty.class);
        }*/
    }

    @Override
    public Can<ServletContextListener> init(final ServletContext ctx) throws ServletException {
/* TODO[ISIS-3275] shiro-web no jakarta API support
        registerFilter(ctx, SHIRO_FILTER_NAME, ShiroFilter.class)
            .ifPresent(filterReg -> {
                filterReg.addMappingForUrlPatterns(
                        EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC),
                        false, // filter is forced first
                        "/*");
            });

        val customShiroEnvironmentClassName = System.getProperty("shiroEnvironmentClass");
        if(_Strings.isNotEmpty(customShiroEnvironmentClassName)) {
            ctx.setInitParameter("shiroEnvironmentClass", customShiroEnvironmentClassName);
        }

        val listener = createListener(EnvironmentLoaderListenerForCauseway.class);
        return Can.ofSingleton(listener);
*/
        return Can.empty();
    }



}
