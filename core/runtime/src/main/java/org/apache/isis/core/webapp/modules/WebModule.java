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
package org.apache.isis.core.webapp.modules;

import static org.apache.isis.commons.internal.base._Strings.isEmpty;
import static org.apache.isis.commons.internal.base._With.ifPresentElse;
import static org.apache.isis.commons.internal.base._With.requires;

import java.util.stream.Stream;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.webapp.IsisWebAppConfigProvider;
import org.apache.isis.core.webapp.IsisWebAppContextListener;

/**
 * Introduced to render web.xml Filter/Listener/Servlet configurations obsolete.
 * <p>
 * WebModule instances are used by the {@link IsisWebAppContextListener} to setup 
 * the ServletContext programmatically.
 * </p>
 * 
 * @since 2.0.0
 */
public interface WebModule {
    
    // -- INTERFACE

    /**
     * @return (display-) name of this module
     */
    public String getName();
    
    /**
     * Before initializing any WebModule we call each WebModule's prepare method 
     * to allow for a WebModule to leave information useful for other modules on 
     * the shared ServletContext.
     * 
     * @param ctx ServletContext
     */
    default public void prepare(ServletContext ctx) {}
    
    /**
     * Expected to be called after all WebModules had a chance to prepare the ServletContext.
     * Sets this WebModule's {@link Filter}s, {@link Servlet}s or {@link WebListener}s 
     * up and registers them with the given {@link ServletContext} {@code ctx}.
     * @param ctx ServletContext
     */
    public ServletContextListener init(ServletContext ctx) throws ServletException;
    
    /**
     * Expected to be called after all WebModules had a chance to prepare the ServletContext.
     * @param ctx ServletContext
     * @return whether this module is applicable/usable
     */
    public boolean isApplicable(ServletContext ctx);
    
    // -- DISCOVERY 
    
    /**
     * @return Stream of 'known' WebModules, whether applicable or not is not decided here 
     */
    static Stream<WebModule> discoverWebModules() {
        
        //TODO [ahuber] instead of providing a static list of modules, modules could be discovered on 
        // the class-path (in case we have plugins that provide such modules).
        // We need yet to decide a mechanism, that enforces a certain ordering of these modules, since
        // this influences the order in which filters are processed.
        
        return Stream.of(
                new WebModule_Shiro(), // filters before all others
                new WebModule_Wicket(),
                new WebModule_FallbackBootstrapper(), // not required if the Wicket module is in use
                new WebModule_RestEasy(), // default REST provider
                new WebModule_LogOnExceptionLogger() // log any logon exceptions, filters after all others
                );
    }
    
    // -- UTILITY
    
    static final class ContextUtil {
        
        /**
         * Tell other modules that a bootstrapper is present.
         * @param ctx
         * @param bootstrapper
         */
        public static void registerBootstrapper(ServletContext ctx, WebModule bootstrapper) {
            ctx.setAttribute("isis.bootstrapper", bootstrapper);    
        }
        
        /**
         * @param ctx
         * @return whether this context has a bootstrapper registered
         */
        public static boolean hasBootstrapper(ServletContext ctx) {
            return ctx.getAttribute("isis.bootstrapper")!=null;    
        }
        
        /**
         *  Adds to the list of viewer names "isis.viewers"
         * @param ctx
         * @param viewerName
         */
        public static void registerViewer(ServletContext ctx, String viewerName) {
            String viewers = (String) ctx.getAttribute("isis.viewers");
            if(isEmpty(viewers)) {
                viewers = viewerName;
            } else {
                viewers = viewers + "," + viewerName;
            }
            ctx.setAttribute("isis.viewers", viewers);
        }
        
        /**
         * Puts the list of viewer names "isis.viewers" into a context parameter.
         * @param ctx
         */
        public static void commitViewers(ServletContext ctx) {
            String viewers = (String) ctx.getAttribute("isis.viewers");
            if(!isEmpty(viewers)) {
                ctx.setInitParameter("isis.viewers", viewers);    
            }
        }
        
        /**
         *  Adds to the list of protected path names "isis.protected"
         * @param ctx
         * @param path
         */
        public static void registerProtectedPath(ServletContext ctx, String path) {
            String list = (String) ctx.getAttribute("isis.protected");
            if(isEmpty(list)) {
                list = path;
            } else {
                list = list + "," + path;
            }
            ctx.setAttribute("isis.protected", list);
        }
        
        /**
         * Streams the protected path names "isis.protected".
         * @param ctx
         */
        public static Stream<String> streamProtectedPaths(ServletContext ctx) {
            final String list = (String) ctx.getAttribute("isis.protected");
            return _Strings.splitThenStream(list, ",");
        }

        /**
         * Try to fetch the value from config stored under {@code key} else fallback to {@code defaultValue}
         * @param ctx
         * @param key
         * @param defaultValue
         * @return non-null
         */
        public static String getConfigOrDefault(ServletContext ctx, String key, String defaultValue) {
            requires(key, "key");
            requires(defaultValue, "defaultValue");
            
            final IsisConfiguration webXmlConfig = 
                    IsisWebAppConfigProvider.getInstance().peekConfiguration(ctx);
            
            return ifPresentElse(webXmlConfig.getString(key), defaultValue);
        }
        
        
    }
    
    
}
