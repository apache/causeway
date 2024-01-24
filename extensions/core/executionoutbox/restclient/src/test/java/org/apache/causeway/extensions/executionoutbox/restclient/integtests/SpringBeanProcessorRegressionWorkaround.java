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
package org.apache.causeway.extensions.executionoutbox.restclient.integtests;

import java.util.List;

import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.jboss.resteasy.core.AsynchronousDispatcher;
import org.jboss.resteasy.core.ResourceMethodRegistry;
import org.jboss.resteasy.core.ResteasyContext;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ListenerBootstrap;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
import org.jboss.resteasy.spi.Dispatcher;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.springboot.JAXRSResourcesAndProvidersScannerPostProcessor;
import org.jboss.resteasy.springboot.ResteasyApplicationBuilder;
import org.jboss.resteasy.springboot.ResteasyEmbeddedServletInitializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import org.apache.causeway.viewer.restfulobjects.jaxrsresteasy.CausewayModuleViewerRestfulObjectsJaxrsResteasy;

/**
 * RESTEASY013015: could not find the type for bean named jpaSharedEM_entityManagerFactory
 * <p>
 * Overrides {@link org.jboss.resteasy.springboot.ResteasyAutoConfiguration}
 * and fixes regression with {@link SpringBeanProcessor}.
 * <p>
 * If seeing this in production, we need to propagate the workaround to
 * {@link CausewayModuleViewerRestfulObjectsJaxrsResteasy}.
 */
@Configuration
class SpringBeanProcessorRegressionWorkaround {

    @Bean @Primary
    @Qualifier("ResteasyProviderFactory")
    public static BeanFactoryPostProcessor springBeanProcessor() {
        var blackListed = List.of("jpaSharedEM_entityManagerFactory", "jpaSharedEM_AWC_entityManagerFactory");

        ResteasyProviderFactory resteasyProviderFactory = ResteasyProviderFactory.newInstance();
        ResourceMethodRegistry resourceMethodRegistry = new ResourceMethodRegistry(resteasyProviderFactory);

        SpringBeanProcessor springBeanProcessor = new SpringBeanProcessor() {

            @Override
            protected Class<?> processBean(final ConfigurableListableBeanFactory beanFactory,
                    final List<String> dependsOnProviders, final String name, final BeanDefinition beanDef) {

                if(blackListed.contains(name)) {
                    return EntityManagerFactory.class;
                }

                return super.processBean(beanFactory, dependsOnProviders, name, beanDef);
            }

        };
        springBeanProcessor.setProviderFactory(resteasyProviderFactory);
        springBeanProcessor.setRegistry(resourceMethodRegistry);

        return springBeanProcessor;
    }

    @Bean
    @ConditionalOnProperty(name="resteasy.jaxrs.scan-packages")
    public static JAXRSResourcesAndProvidersScannerPostProcessor providerScannerPostProcessor() {
        return new JAXRSResourcesAndProvidersScannerPostProcessor();
    }

    /**
     * This is a modified version of {@link ResteasyBootstrap}
     *
     * @return a ServletContextListener object that configures and start a ResteasyDeployment
     */
    @Bean
    public ServletContextListener resteasyBootstrapListener(@Qualifier("ResteasyProviderFactory") final BeanFactoryPostProcessor beanFactoryPostProcessor) {
        ServletContextListener servletContextListener = new ServletContextListener() {

            private SpringBeanProcessor springBeanProcessor = (SpringBeanProcessor) beanFactoryPostProcessor;

            protected ResteasyDeployment deployment;

            @Override
            public void contextInitialized(final ServletContextEvent sce) {
                ServletContext servletContext = sce.getServletContext();
                ResteasyContext.pushContext(ServletContext.class, servletContext);
                ListenerBootstrap config = new ListenerBootstrap(servletContext);

                ResteasyProviderFactory resteasyProviderFactory = springBeanProcessor.getProviderFactory();
                ResourceMethodRegistry resourceMethodRegistry = (ResourceMethodRegistry) springBeanProcessor.getRegistry();

                deployment = config.createDeployment();

                deployment.setProviderFactory(resteasyProviderFactory);
                deployment.setRegistry(resourceMethodRegistry);

                if (deployment.isAsyncJobServiceEnabled()) {
                    AsynchronousDispatcher dispatcher = new AsynchronousDispatcher(resteasyProviderFactory, resourceMethodRegistry);
                    deployment.setDispatcher(dispatcher);
                } else {
                    SynchronousDispatcher dispatcher = new SynchronousDispatcher(resteasyProviderFactory, resourceMethodRegistry);
                    deployment.setDispatcher(dispatcher);
                }

                deployment.start();

                servletContext.setAttribute(ResteasyProviderFactory.class.getName(), deployment.getProviderFactory());
                servletContext.setAttribute(Dispatcher.class.getName(), deployment.getDispatcher());
                servletContext.setAttribute(Registry.class.getName(), deployment.getRegistry());
            }

            @Override
            public void contextDestroyed(final ServletContextEvent sce) {
                try {
                    if (deployment != null) {
                        deployment.stop();
                    }
                } finally {
                    ResteasyContext.popContextData(ServletContext.class);
                }
            }
        };

        return servletContextListener;
    }

    @Bean(name = ResteasyApplicationBuilder.BEAN_NAME)
    public ResteasyApplicationBuilder resteasyApplicationBuilder() {
        return new ResteasyApplicationBuilder();
    }

    @Bean
    public static ResteasyEmbeddedServletInitializer resteasyEmbeddedServletInitializer() {
        return new ResteasyEmbeddedServletInitializer();
    }


}
