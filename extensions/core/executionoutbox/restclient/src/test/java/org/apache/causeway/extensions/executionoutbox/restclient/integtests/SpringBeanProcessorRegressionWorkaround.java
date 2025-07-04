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

//import java.lang.annotation.Annotation;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import jakarta.persistence.EntityManagerFactory;
//import jakarta.servlet.ServletContext;
//import jakarta.servlet.ServletContextEvent;
//import jakarta.servlet.ServletContextListener;
//import jakarta.ws.rs.Path;
//import jakarta.ws.rs.ext.Provider;
//
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.config.BeanDefinition;
//import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
//import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
//import org.springframework.beans.factory.support.BeanDefinitionRegistry;
//import org.springframework.beans.factory.support.GenericBeanDefinition;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.core.PriorityOrdered;
//import org.springframework.core.env.ConfigurableEnvironment;
//import org.springframework.core.type.filter.AnnotationTypeFilter;
//import org.springframework.util.ClassUtils;



/**
 * RESTEASY013015: could not find the type for bean named jpaSharedEM_entityManagerFactory
 * <p>
 * Overrides {@link org.jboss.resteasy.springboot.ResteasyAutoConfiguration}
 * and fixes regression with {@link SpringBeanProcessor}.
 * <p>
 * If seeing this in production, we need to propagate the workaround to
 * {@link CausewayModuleViewerRestfulObjectsJaxrsResteasy}.
 */
//@Configuration
//@Slf4j
class SpringBeanProcessorRegressionWorkaround {

//    @Bean @Primary
//    @Qualifier("ResteasyProviderFactory")
//    public static BeanFactoryPostProcessor springBeanProcessor() {
//        var blackListed = List.of("jpaSharedEM_entityManagerFactory", "jpaSharedEM_AWC_entityManagerFactory");
//
//        ResteasyProviderFactory resteasyProviderFactory = ResteasyProviderFactory.newInstance();
//        ResourceMethodRegistry resourceMethodRegistry = new ResourceMethodRegistry(resteasyProviderFactory);
//
//        SpringBeanProcessor springBeanProcessor = new SpringBeanProcessor() {
//
//            @Override
//            protected Class<?> processBean(final ConfigurableListableBeanFactory beanFactory,
//                    final List<String> dependsOnProviders, final String name, final BeanDefinition beanDef) {
//
//                if(blackListed.contains(name)) {
//                    return EntityManagerFactory.class;
//                }
//
//                return super.processBean(beanFactory, dependsOnProviders, name, beanDef);
//            }
//
//        };
//        springBeanProcessor.setProviderFactory(resteasyProviderFactory);
//        springBeanProcessor.setRegistry(resourceMethodRegistry);
//
//        return springBeanProcessor;
//    }
//
//    /**
//     * Scanner bean factory post processor that is responsible for scanning classpath
//     * (configured packages for JAX RS resources and providers).
//     *
//     * It's meant to run as on of the first bean factory post processors so others, especially
//     * <code>org.jboss.resteasy.plugins.spring.SpringBeanProcessor</code> can find the bean definitions produced by this class.
//     *
//     * This class is not active unless <code>resteasy.jaxrs.scan-packages</code> property is set.
//     *
//     */
//    public static class JAXRSResourcesAndProvidersScannerPostProcessor implements BeanFactoryPostProcessor, PriorityOrdered {
//
//        private static final String JAXRS_SCAN_PACKAGES_PROPERTY = "resteasy.jaxrs.scan-packages";
//
//        @Override
//        public int getOrder() {
//            return 0;
//        }
//
//        @Override
//        public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
//            ConfigurableEnvironment configurableEnvironment = beanFactory.getBean(ConfigurableEnvironment.class);
//            String jaxrsScanPackages = configurableEnvironment.getProperty(JAXRS_SCAN_PACKAGES_PROPERTY);
//
//
//            Set<Class<?>> provicerClasses = findJaxrsResourcesOrProviderClasses(jaxrsScanPackages, Provider.class);
//            for (Class<?> providerClazz : provicerClasses) {
//                registerScannedBean(beanFactory, providerClazz);
//            }
//
//            Set<Class<?>> resourceClasses = findJaxrsResourcesOrProviderClasses(jaxrsScanPackages, Path.class);
//            for (Class<?> resourceClazz : resourceClasses) {
//                registerScannedBean(beanFactory, resourceClazz);
//            }
//
//        }
//
//        /*
//         * Creates singleton bean definition for found classes that represent either JAX RS resource or provider
//         */
//        private void registerScannedBean(final ConfigurableListableBeanFactory beanFactory, final Class<?> clazz) {
//            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
//
//            GenericBeanDefinition bean = new GenericBeanDefinition();
//            bean.setBeanClass(clazz);
//            bean.setAutowireCandidate(true);
//            bean.setScope("singleton");
//
//            registry.registerBeanDefinition(clazz.getName(), bean);
//        }
//
//        /*
//         * Scan the classpath under the specified packages looking for JAX-RS resources and providers
//         */
//        private static Set<Class<?>> findJaxrsResourcesOrProviderClasses(final String packagesToBeScanned, final Class<? extends Annotation>  annotationType) {
//            log.info("Scanning classpath to find JAX-RS classes annotated with {}", annotationType);
//
//            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
//            scanner.addIncludeFilter(new AnnotationTypeFilter(annotationType));
//
//            Set<BeanDefinition> candidates = new HashSet<BeanDefinition>();
//            Set<BeanDefinition> candidatesSubSet;
//
//            for (String packageToScan : packagesToBeScanned.split(",")) {
//                candidatesSubSet = scanner.findCandidateComponents(packageToScan.trim());
//                candidates.addAll(candidatesSubSet);
//            }
//
//            Set<Class<?>> classes = new HashSet<>();
//            ClassLoader classLoader = JAXRSResourcesAndProvidersScannerPostProcessor.class.getClassLoader();
//            Class<?> type;
//            for (BeanDefinition candidate : candidates) {
//                try {
//                    type = ClassUtils.forName(candidate.getBeanClassName(), classLoader);
//                    classes.add(type);
//                } catch (ClassNotFoundException e) {
//                    log.error("JAX-RS Resource/Provider could not be loaded", e);
//                }
//            }
//            return classes;
//        }
//    }
//
//
//    @Bean
//    @ConditionalOnProperty(name="resteasy.jaxrs.scan-packages")
//    public static JAXRSResourcesAndProvidersScannerPostProcessor providerScannerPostProcessor() {
//        return new JAXRSResourcesAndProvidersScannerPostProcessor();
//    }
//
//    /**
//     * This is a modified version of {@link ResteasyBootstrap}
//     *
//     * @return a ServletContextListener object that configures and start a ResteasyDeployment
//     */
//    @Bean
//    public ServletContextListener resteasyBootstrapListener(@Qualifier("ResteasyProviderFactory") final BeanFactoryPostProcessor beanFactoryPostProcessor) {
//        ServletContextListener servletContextListener = new ServletContextListener() {
//
//            private SpringBeanProcessor springBeanProcessor = (SpringBeanProcessor) beanFactoryPostProcessor;
//
//            protected ResteasyDeployment deployment;
//
//            @Override
//            public void contextInitialized(final ServletContextEvent sce) {
//                ServletContext servletContext = sce.getServletContext();
//                ResteasyContext.pushContext(ServletContext.class, servletContext);
//                ListenerBootstrap config = new ListenerBootstrap(servletContext);
//
//                ResteasyProviderFactory resteasyProviderFactory = springBeanProcessor.getProviderFactory();
//                ResourceMethodRegistry resourceMethodRegistry = (ResourceMethodRegistry) springBeanProcessor.getRegistry();
//
//                deployment = config.createDeployment();
//
//                deployment.setProviderFactory(resteasyProviderFactory);
//                deployment.setRegistry(resourceMethodRegistry);
//
//                if (deployment.isAsyncJobServiceEnabled()) {
//                    AsynchronousDispatcher dispatcher = new AsynchronousDispatcher(resteasyProviderFactory, resourceMethodRegistry);
//                    deployment.setDispatcher(dispatcher);
//                } else {
//                    SynchronousDispatcher dispatcher = new SynchronousDispatcher(resteasyProviderFactory, resourceMethodRegistry);
//                    deployment.setDispatcher(dispatcher);
//                }
//
//                deployment.start();
//
//                servletContext.setAttribute(ResteasyProviderFactory.class.getName(), deployment.getProviderFactory());
//                servletContext.setAttribute(Dispatcher.class.getName(), deployment.getDispatcher());
//                servletContext.setAttribute(Registry.class.getName(), deployment.getRegistry());
//            }
//
//            @Override
//            public void contextDestroyed(final ServletContextEvent sce) {
//                try {
//                    if (deployment != null) {
//                        deployment.stop();
//                    }
//                } finally {
//                    ResteasyContext.popContextData(ServletContext.class);
//                }
//            }
//        };
//
//        return servletContextListener;
//    }

}
