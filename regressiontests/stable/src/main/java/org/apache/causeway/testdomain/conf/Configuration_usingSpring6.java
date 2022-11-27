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
package org.apache.causeway.testdomain.conf;

import java.util.Set;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContainerInitializer;

import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.jboss.resteasy.plugins.servlet.ResteasyServletInitializer;
import org.jboss.resteasy.springboot.ResteasyEmbeddedServletInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.testing.fixtures.applib.fixturescripts.ExecutionParametersServiceAutoConfiguration;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScriptsSpecificationProviderAutoConfiguration;

import lombok.extern.log4j.Log4j2;

/**
 * Introduced for the purpose of troubleshooting Spring 5.x to 6.x migration issues.
 * @deprecated marked deprecated, to be removed once no longer needed
 */
@Deprecated(forRemoval = true)
@Configuration
@Import({
    ResteasyEmbeddedServletInitializer.class, //FIXME[ISIS-3275] move up the hierarchy?
    FixtureScriptsSpecificationProviderAutoConfiguration.class, // because something? disables autoconfiguration
    ExecutionParametersServiceAutoConfiguration.class           // because something? disables autoconfiguration
})
@Log4j2
public class Configuration_usingSpring6 {

    /**
     * This class is the Spring Boot equivalent of {@link ResteasyServletInitializer},
     * which implements the Servlet API {@link ServletContainerInitializer} interface
     * to find all JAX-RS Application, Provider and Path classes in the classpath.
     * <p>
     * As we all know, in Spring Boot we use an embedded servlet container. However,
     * the Servlet spec does not support embedded containers, and many portions of it
     * do not apply to embedded containers, and ServletContainerInitializer is one of them.
     * <p>
     * This class fills in this gap.
     * <p>
     * Notice that the JAX-RS Application classes are found in this RESTEasy starter by class
     * ResteasyEmbeddedServletInitializer, and that is done by scanning the classpath.
     * <p>
     * The Path and Provider annotated classes are found by using Spring framework (instead of
     * scanning the classpath), since it is assumed those classes are ALWAYS necessarily
     * Spring beans (this starter is meant for Spring Boot applications that use RESTEasy
     * as the JAX-RS implementation)
     *
     * @see "https://github.com/paypal/resteasy-spring-boot/blob/master/resteasy-spring-boot-starter/src/main/java/com/paypal/springboot/resteasy/ResteasyApplicationBuilder.java"
     */
    public static class ResteasyApplicationBuilder {

        public static final String BEAN_NAME = "JaxrsApplicationServletBuilder";

        public ServletRegistrationBean<Servlet> build(
                final String applicationClassName,
                final String path,
                final Set<Class<?>> resources,
                final Set<Class<?>> providers) {
            Servlet servlet = new HttpServlet30Dispatcher();

            ServletRegistrationBean<Servlet> servletRegistrationBean = new ServletRegistrationBean<Servlet>(servlet);

            servletRegistrationBean.setName(applicationClassName);
            servletRegistrationBean.setLoadOnStartup(1);
            servletRegistrationBean.setAsyncSupported(true);
            servletRegistrationBean.addInitParameter("javax.ws.rs.Application", applicationClassName);

            if (path != null) {
                String mapping = path;
                if (!mapping.startsWith("/"))
                    mapping = "/" + mapping;
                String prefix = mapping;
                if (!"/".equals(prefix) && prefix.endsWith("/"))
                    prefix = prefix.substring(0, prefix.length() - 1);
                if (mapping.endsWith("/"))
                    mapping += "*";
                else
                    mapping += "/*";
                // resteasy.servlet.mapping.prefix
                servletRegistrationBean.addInitParameter("resteasy.servlet.mapping.prefix", prefix);
                servletRegistrationBean.addUrlMappings(mapping);
            }

            if (resources.size() > 0) {
                StringBuilder builder = new StringBuilder();
                boolean first = true;
                for (Class<?> resource : resources) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(",");
                    }

                    builder.append(resource.getName());
                }
                servletRegistrationBean.addInitParameter(ResteasyContextParameters.RESTEASY_SCANNED_RESOURCES, builder.toString());
            }
            if (providers.size() > 0) {
                StringBuilder builder = new StringBuilder();
                boolean first = true;
                for (Class<?> provider : providers) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(",");
                    }
                    builder.append(provider.getName());
                }
                servletRegistrationBean.addInitParameter(ResteasyContextParameters.RESTEASY_SCANNED_PROVIDERS, builder.toString());
            }

            log.debug("ServletRegistrationBean has just bean created for JAX-RS class " + applicationClassName);

            return servletRegistrationBean;
        }

    }


    @Bean(name = "JaxrsApplicationServletBuilder")
    public ResteasyApplicationBuilder resteasyApplicationBuilder() {
        return new ResteasyApplicationBuilder();
    }

}
