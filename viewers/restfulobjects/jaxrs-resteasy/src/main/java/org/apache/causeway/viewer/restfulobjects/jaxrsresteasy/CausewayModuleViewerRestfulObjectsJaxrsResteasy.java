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
package org.apache.causeway.viewer.restfulobjects.jaxrsresteasy;





//import org.jboss.resteasy.core.AsynchronousDispatcher;
//import org.jboss.resteasy.core.ResourceMethodRegistry;
//import org.jboss.resteasy.core.SynchronousDispatcher;
//import org.jboss.resteasy.core.providerfactory.ResteasyProviderFactoryImpl;
//import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
//import org.jboss.resteasy.plugins.server.servlet.ListenerBootstrap;
//import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
//import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
//import org.jboss.resteasy.plugins.servlet.ResteasyServletInitializer;
//import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
//import org.jboss.resteasy.spi.Dispatcher;
//import org.jboss.resteasy.spi.Registry;
//import org.jboss.resteasy.spi.ResteasyDeployment;
//import org.jboss.resteasy.spi.ResteasyProviderFactory;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.apache.causeway.viewer.restfulobjects.jaxrsresteasy.webmodule.WebModuleJaxrsResteasy;
import org.apache.causeway.viewer.restfulobjects.viewer.CausewayModuleViewerRestfulObjectsViewer;

/**
 * TODO[CAUSEWAY-3892] temporarily incorporates code from https://github.com/resteasy/resteasy-spring-boot
 * which does not support Sprint Boot 4 yet
 *
 * @since 1.x {@index}
 */
@Configuration
@Import({
        // Modules
        CausewayModuleViewerRestfulObjectsViewer.class,

        // @Service's
        WebModuleJaxrsResteasy.class,

        // @Component's
        //RestfulObjectsJaxbWriterForXml.class,

})
//@AutoConfiguration(after = WebMvcAutoConfiguration.class)
//@EnableConfigurationProperties
//@Slf4j
public class CausewayModuleViewerRestfulObjectsJaxrsResteasy {


//    import java.lang.annotation.Annotation;
//    import java.lang.reflect.Type;
//
//    import jakarta.inject.Inject;
//    import jakarta.ws.rs.Produces;
//    import org.springframework.http.MediaType;
//    import jakarta.ws.rs.ext.Provider;
//    import jakarta.xml.bind.Marshaller;
//
//    import org.jboss.resteasy.plugins.providers.jaxb.JAXBXmlRootElementProvider;
//
//    import org.springframework.stereotype.Component;
//
//    import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
//    import org.apache.causeway.applib.services.inject.ServiceInjector;
//    @Component
//    @Provider
//    @Produces({"application/xml", "application/*+xml", "text/*+xml"})
//    public static class RestfulObjectsJaxbWriterForXml extends JAXBXmlRootElementProvider {
//
//        @Inject private ServiceInjector serviceInjector;
//
//        @Override
//        protected boolean isReadWritable(
//                final Class<?> type,
//                final Type genericType,
//                final Annotation[] annotations,
//                final MediaType mediaType) {
//
//            return super.isReadWritable(type, genericType, annotations, mediaType) &&
//                    hasXRoDomainTypeParameter(mediaType);
//        }
//
//        @Override
//        protected Marshaller getMarshaller(
//                final Class<?> type,
//                final Annotation[] annotations,
//                final MediaType mediaType) {
//
//            var adapter = serviceInjector.injectServicesInto(new PersistentEntityAdapter());
//
//            var marshaller = super.getMarshaller(type, annotations, mediaType);
//            marshaller.setAdapter(PersistentEntityAdapter.class, adapter);
//            return marshaller;
//        }
//
//        // HELPER
//
//        private static boolean hasXRoDomainTypeParameter(final MediaType mediaType) {
//            final boolean retval = mediaType.getParameters().containsKey("x-ro-domain-type");
//            return retval;
//        }
//
//    }

//    /**
//     * The main function of this class is to prepare, configure and initialize the core components of a RESTEasy deployment.
//     */
//    public static class DeploymentCustomizer {
//
//        /**
//         * Configures and initializes a resteasy deployment with:<br>
//         * - A {@code org.jboss.resteasy.spi.Dispatcher}<br>
//         * - A {@code org.jboss.resteasy.spi.ResteasyProviderFactory}<br>
//         * - A {@code org.jboss.resteasy.core.ResourceMethodRegistry}
//         *
//         * @param resteasySpringBeanProcessor
//         *            - The spring bean processor to acquire the provider and resource factories from.
//         * @param deployment
//         *            - The deployment to customize.
//         * @param enableAsyncJob
//         *            - Indicates whether the async job service should be enabled.
//         */
//        public static void customizeRestEasyDeployment(final SpringBeanProcessor resteasySpringBeanProcessor, final ResteasyDeployment deployment, final boolean enableAsyncJob) {
//
//            Objects.requireNonNull(resteasySpringBeanProcessor);
//            Objects.requireNonNull(deployment);
//
//            final ResteasyProviderFactory resteasyProviderFactory = resteasySpringBeanProcessor.getProviderFactory();
//            final ResourceMethodRegistry resourceMethodRegistry = (ResourceMethodRegistry) resteasySpringBeanProcessor.getRegistry();
//
//            deployment.setProviderFactory(resteasyProviderFactory);
//            deployment.setRegistry(resourceMethodRegistry);
//
//            if(enableAsyncJob) {
//                deployment.setAsyncJobServiceEnabled(true);
//                final AsynchronousDispatcher dispatcher = new AsynchronousDispatcher(resteasyProviderFactory, resourceMethodRegistry);
//                deployment.setDispatcher(dispatcher);
//            } else {
//                final SynchronousDispatcher dispatcher = new SynchronousDispatcher(resteasyProviderFactory, resourceMethodRegistry);
//                deployment.setDispatcher(dispatcher);
//            }
//
//        }
//
//    }
//
//    /**
//     * This is a modified version of {@link ResteasyBootstrap}
//     * @param resteasySpringBeanProcessor - A bean processor for Resteasy.
//     *
//     * @return a ServletContextListener object that configures and start a ResteasyDeployment
//     */
//    @Bean
//    public ServletContextListener resteasyBootstrapListener(
//            final @Qualifier("resteasySpringBeanProcessor") SpringBeanProcessor resteasySpringBeanProcessor) {
//
//        ServletContextListener servletContextListener = new ServletContextListener() {
//
//            protected ResteasyDeployment deployment;
//
//            @Override
//            public void contextInitialized(final ServletContextEvent sce) {
//                ServletContext servletContext = sce.getServletContext();
//
//                deployment = new ListenerBootstrap(servletContext).createDeployment();
//                DeploymentCustomizer.customizeRestEasyDeployment(resteasySpringBeanProcessor, deployment,
//                        deployment.isAsyncJobServiceEnabled());
//                deployment.start();
//
//                servletContext.setAttribute(ResteasyProviderFactory.class.getName(), deployment.getProviderFactory());
//                servletContext.setAttribute(Dispatcher.class.getName(), deployment.getDispatcher());
//                servletContext.setAttribute(Registry.class.getName(), deployment.getRegistry());
//            }
//
//            @Override
//            public void contextDestroyed(final ServletContextEvent sce) {
//                if (deployment != null) {
//                    deployment.stop();
//                }
//            }
//        };
//
//        log.debug("ServletContextListener has been created");
//
//        return servletContextListener;
//    }
//
//    /**
//     * This class is the Spring Boot equivalent of {@link ResteasyServletInitializer},
//     * which implements the Servlet API {@link ServletContainerInitializer} interface
//     * to find all JAX-RS Application, Provider and Path classes in the classpath.
//     *
//     * As we all know, in Spring Boot we use an embedded servlet container. However,
//     * the Servlet spec does not support embedded containers, and many portions of it
//     * do not apply to embedded containers, and ServletContainerInitializer is one of them.
//     *
//     * This class fills in this gap.
//     *
//     * Notice that the JAX-RS Application classes are found in this RESTEasy starter by class
//     * ResteasyEmbeddedServletInitializer, and that is done by scanning the classpath.
//     *
//     * The Path and Provider annotated classes are found by using Spring framework (instead of
//     * scanning the classpath), since it is assumed those classes are ALWAYS necessarily
//     * Spring beans (this starter is meant for Spring Boot applications that use RESTEasy
//     * as the JAX-RS implementation)
//     *
//     * @author Fabio Carvalho (facarvalho@paypal.com or fabiocarvalho777@gmail.com)
//     */
//    @Slf4j
//    public static class ResteasyApplicationBuilder {
//
//        public static final String BEAN_NAME = "JaxrsApplicationServletBuilder";
//
//        public ServletRegistrationBean build(final String applicationClassName, final String path, final Set<Class<?>> resources, final Set<Class<?>> providers) {
//            Servlet servlet = new HttpServlet30Dispatcher();
//
//            ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(servlet);
//
//            servletRegistrationBean.setName(applicationClassName);
//            servletRegistrationBean.setLoadOnStartup(1);
//            servletRegistrationBean.setAsyncSupported(true);
////          servletRegistrationBean.addInitParameter(Application.class.getTypeName(), applicationClassName);
//            servletRegistrationBean.addInitParameter("jakarta.ws.rs.Application", applicationClassName);
//
//            if (path != null) {
//                String mapping = path;
//                if (!mapping.startsWith("/"))
//                    mapping = "/" + mapping;
//                String prefix = mapping;
//                if (!"/".equals(prefix) && prefix.endsWith("/"))
//                    prefix = prefix.substring(0, prefix.length() - 1);
//                if (mapping.endsWith("/"))
//                    mapping += "*";
//                else
//                    mapping += "/*";
//                servletRegistrationBean.addInitParameter("resteasy.servlet.mapping.prefix", prefix);
//                servletRegistrationBean.addUrlMappings(mapping);
//            }
//
//            if (resources.size() > 0) {
//                StringBuilder builder = new StringBuilder();
//                boolean first = true;
//                for (Class<?> resource : resources) {
//                    if (first) {
//                        first = false;
//                    } else {
//                        builder.append(",");
//                    }
//
//                    builder.append(resource.getName());
//                }
//                servletRegistrationBean.addInitParameter(ResteasyContextParameters.RESTEASY_SCANNED_RESOURCES, builder.toString());
//            }
//            if (providers.size() > 0) {
//                StringBuilder builder = new StringBuilder();
//                boolean first = true;
//                for (Class<?> provider : providers) {
//                    if (first) {
//                        first = false;
//                    } else {
//                        builder.append(",");
//                    }
//                    builder.append(provider.getName());
//                }
//                servletRegistrationBean.addInitParameter(ResteasyContextParameters.RESTEASY_SCANNED_PROVIDERS, builder.toString());
//            }
//
//            log.debug("ServletRegistrationBean has just bean created for JAX-RS class " + applicationClassName);
//
//            return servletRegistrationBean;
//        }
//
//    }
//
//    @Bean(name = ResteasyApplicationBuilder.BEAN_NAME)
//    public ResteasyApplicationBuilder resteasyApplicationBuilder() {
//        return new ResteasyApplicationBuilder();
//    }
//
//    /**
//     * Helper class to scan the classpath under the specified packages
//     * searching for JAX-RS Application sub-classes
//     *
//     * @author Fabio Carvalho (facarvalho@paypal.com or fabiocarvalho777@gmail.com)
//     */
//    @Slf4j
//    public static abstract class JaxrsApplicationScanner {
//
//        private static Map<String, Set<Class<? extends Application>>> packagesToClassesMap = new HashMap<>();
//
//        public static Set<Class<? extends Application>> getApplications(final List<String> packagesToBeScanned) {
//            final String packagesKey = createPackagesKey(packagesToBeScanned);
//            if(!packagesToClassesMap.containsKey(packagesKey)) {
//                packagesToClassesMap.put(packagesKey, findJaxrsApplicationClasses(packagesToBeScanned));
//            }
//
//            return packagesToClassesMap.get(packagesKey);
//        }
//
//        private static String createPackagesKey(final List<String> packagesToBeScanned) {
//            return String.join(",", packagesToBeScanned);
//        }
//
//        /*
//         * Scan the classpath under the specified packages looking for JAX-RS Application sub-classes
//         */
//        private static Set<Class<? extends Application>> findJaxrsApplicationClasses(final List<String> packagesToBeScanned) {
//            log.info("Scanning classpath to find JAX-RS Application classes");
//
//            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
//            scanner.addIncludeFilter(new AssignableTypeFilter(Application.class));
//
//            Set<BeanDefinition> candidates = new HashSet<BeanDefinition>();
//            Set<BeanDefinition> candidatesSubSet;
//
//            for (String packageToScan : packagesToBeScanned) {
//                candidatesSubSet = scanner.findCandidateComponents(packageToScan);
//                candidates.addAll(candidatesSubSet);
//            }
//
//            Set<Class<? extends Application>> classes = new HashSet<Class<? extends Application>>();
//            ClassLoader classLoader = JaxrsApplicationScanner.class.getClassLoader();
//            Class<? extends Application> type;
//            for (BeanDefinition candidate : candidates) {
//                try {
//                    type = (Class<? extends Application>) ClassUtils.forName(candidate.getBeanClassName(), classLoader);
//                    classes.add(type);
//                } catch (ClassNotFoundException e) {
//                    log.error("JAX-RS Application subclass could not be loaded", e);
//                }
//            }
//
//            // We don't want the JAX-RS Application class itself in there
//            classes.remove(Application.class);
//
//            return classes;
//        }
//
//    }
//
//    /**
//     * Helper class that finds JAX-RS classes annotated with {@link jakarta.ws.rs.Path} and with
//     * {@link jakarta.ws.rs.ext.Provider}.
//     */
//    @Slf4j
//    public static class ResteasyResourcesFinder {
//
//        /**
//         * This is how {@code JAXRS_APP_CLASSES_PROPERTY} was named originally. It conflicted with {@code resteasy.jaxrs.app.registration}<br>
//         * in case of YAML files, since registration was a child of app from an YAML perspective, which is not allowed.<br>
//         * Because of that its name was changed (the ".classes" suffix was added).
//         * This legacy property has not been removed though, to keep backward compatibility, but it is marked as deprecated. It will be
//         * available only for {@code .properties} files, but not for {@code YAML} files. It should be finally removed in a future major release.
//         */
//        private static final String JAXRS_APP_CLASSES_PROPERTY_LEGACY = "resteasy.jaxrs.app";
//        private static final String JAXRS_APP_CLASSES_PROPERTY = "resteasy.jaxrs.app.classes";
//        private static final String JAXRS_APP_CLASSES_DEFINITION_PROPERTY = "resteasy.jaxrs.app.registration";
//
//
//        private enum JaxrsAppClassesRegistration {
//            BEANS, PROPERTY, SCANNING, AUTO
//        }
//
//        private Set<Class<? extends Application>> applications = new HashSet<Class<? extends Application>>();
//        private final Set<Class<?>> allResources = new HashSet<Class<?>>();
//        private final Set<Class<?>> providers = new HashSet<Class<?>>();
//
//        /*
//         * Find the JAX-RS application classes.
//         * This is done by one of these three options in this order:
//         *
//         * 1- By having them defined as Spring beans
//         * 2- By setting property {@code resteasy.jaxrs.app.classes} via Spring Boot application properties file.
//         *    This property should contain a comma separated list of JAX-RS sub-classes
//         * 3- Via classpath scanning (looking for javax.ws.rs.core.Application sub-classes)
//         *
//         * First try to find JAX-RS Application sub-classes defined as Spring beans. If that is existent,
//         * the search stops, and those are the only JAX-RS applications to be registered.
//         * If no JAX-RS application Spring beans are found, then see if Spring Boot property {@code resteasy.jaxrs.app.classes}
//         * has been set. If it has, the search stops, and those are the only JAX-RS applications to be registered.
//         * If not, then scan the classpath searching for JAX-RS applications.
//         *
//         * There is a way though to force one of the options above, which is by setting property
//         * {@code resteasy.jaxrs.app.registration} via Spring Boot application properties file. The possible valid
//         * values are {@code beans}, {@code property}, {@code scanning} or {@code auto}. If this property is not
//         * present, the default value is {@code auto}, which means every approach will be tried in the order and way
//         * explained earlier.
//         *
//         * @param beanFactory
//         */
//        public void findJaxrsApplications(final ConfigurableListableBeanFactory beanFactory) {
//            log.info("Finding JAX-RS Application classes");
//
//            final JaxrsAppClassesRegistration registration = getJaxrsAppClassesRegistration(beanFactory);
//
//            switch (registration) {
//                case AUTO:
//                    findJaxrsApplicationBeans(beanFactory);
//                    if(applications.isEmpty()) findJaxrsApplicationProperty(beanFactory);
//                    if(applications.isEmpty()) findJaxrsApplicationScanning(beanFactory);
//                    break;
//                case BEANS:
//                    findJaxrsApplicationBeans(beanFactory);
//                    break;
//                case PROPERTY:
//                    findJaxrsApplicationProperty(beanFactory);
//                    break;
//                case SCANNING:
//                    findJaxrsApplicationScanning(beanFactory);
//                    break;
//                default:
//                    log.error("JAX-RS application registration method (%s) not known, no application will be configured", registration.name());
//                    break;
//            }
//
//            applications = applications.stream().filter(app -> {
//                final ApplicationPath path = AnnotationUtils.findAnnotation(app, ApplicationPath.class);
//                if (path == null) {
//                    log.warn("JAX-RS Application class {} has no ApplicationPath annotation, so it will not be configured", app.getName());
//                } else {
//                    log.info("JAX-RS Application class found: {}", ((Class<Application>) app).getName());
//                }
//                return path != null;
//            }).collect(Collectors.toSet());
//
//        }
//
//
//        private JaxrsAppClassesRegistration getJaxrsAppClassesRegistration(final ConfigurableListableBeanFactory beanFactory) {
//            final ConfigurableEnvironment configurableEnvironment = beanFactory.getBean(ConfigurableEnvironment.class);
//            final String jaxrsAppClassesRegistration = configurableEnvironment.getProperty(JAXRS_APP_CLASSES_DEFINITION_PROPERTY);
//            JaxrsAppClassesRegistration registration = JaxrsAppClassesRegistration.AUTO;
//
//            if(jaxrsAppClassesRegistration == null) {
//                log.info("Property {} has not been set, JAX-RS Application classes registration is being set to AUTO", JAXRS_APP_CLASSES_DEFINITION_PROPERTY);
//            } else {
//                log.info("Property {} has been set to {}", JAXRS_APP_CLASSES_DEFINITION_PROPERTY, jaxrsAppClassesRegistration);
//                try {
//                    registration = JaxrsAppClassesRegistration.valueOf(jaxrsAppClassesRegistration.toUpperCase());
//                } catch(IllegalArgumentException ex) {
//                    final String errorMesage = String.format(
//                            "Property %s has not been properly set, value %s is invalid. JAX-RS Application classes registration is being set to AUTO.",
//                            JAXRS_APP_CLASSES_DEFINITION_PROPERTY, jaxrsAppClassesRegistration);
//                    log.error(errorMesage);
//                    throw new IllegalArgumentException(errorMesage, ex);
//                }
//            }
//
//            return registration;
//        }
//
//        /*
//         * Find JAX-RS application classes by searching for their related
//         * Spring beans
//         *
//         * @param beanFactory
//         */
//        private void findJaxrsApplicationBeans(final ConfigurableListableBeanFactory beanFactory) {
//            log.info("Searching for JAX-RS Application Spring beans");
//
//            final Map<String, Application> applicationBeans = beanFactory.getBeansOfType(Application.class, true, false);
//            if(applicationBeans == null || applicationBeans.isEmpty()) {
//                log.info("No JAX-RS Application Spring beans found");
//                return;
//            }
//
//            for (Application application : applicationBeans.values()) {
//                applications.add(application.getClass());
//            }
//        }
//
//        /*
//         * Find JAX-RS application classes via property {@code resteasy.jaxrs.app.classes}
//         */
//        private void findJaxrsApplicationProperty(final ConfigurableListableBeanFactory beanFactory) {
//            final ConfigurableEnvironment configurableEnvironment = beanFactory.getBean(ConfigurableEnvironment.class);
//            String jaxrsAppsProperty = configurableEnvironment.getProperty(JAXRS_APP_CLASSES_PROPERTY);
//            if(jaxrsAppsProperty == null) {
//                jaxrsAppsProperty = configurableEnvironment.getProperty(JAXRS_APP_CLASSES_PROPERTY_LEGACY);
//                if(jaxrsAppsProperty == null) {
//                    log.info("No JAX-RS Application set via property {}", JAXRS_APP_CLASSES_PROPERTY);
//                    return;
//                }
//                log.warn(
//                        "Property {} has been set. Notice that this property has been deprecated and will be removed soon. Please replace it by property {}",
//                        JAXRS_APP_CLASSES_PROPERTY_LEGACY, JAXRS_APP_CLASSES_PROPERTY);
//            } else {
//                log.info("Property {} has been set to {}", JAXRS_APP_CLASSES_PROPERTY, jaxrsAppsProperty);
//            }
//
//            final String[] jaxrsClassNames = jaxrsAppsProperty.split(",");
//
//            for(String jaxrsClassName : jaxrsClassNames) {
//                Class<? extends Application> jaxrsClass = null;
//                try {
//                    jaxrsClass = (Class<? extends Application>) Class.forName(jaxrsClassName.trim());
//                } catch (ClassNotFoundException e) {
//                    final String exceptionMessage = String.format("JAX-RS Application class %s has not been found", jaxrsClassName.trim());
//                    log.error(exceptionMessage, e);
//                    throw new BeansException(exceptionMessage, e){};
//                }
//                applications.add(jaxrsClass);
//            }
//        }
//
//        /*
//         * Find JAX-RS application classes by scanning the classpath under
//         * packages already marked to be scanned by Spring framework
//         */
//        private void findJaxrsApplicationScanning(final BeanFactory beanFactory) {
//            final List<String> packagesToBeScanned = getSpringApplicationPackages(beanFactory);
//
//            final Set<Class<? extends Application>> applications = JaxrsApplicationScanner.getApplications(packagesToBeScanned);
//            if(applications == null || applications.isEmpty()) {
//                return;
//            }
//            this.applications.addAll(applications);
//        }
//
//        /*
//         * Return the name of the packages to be scanned by Spring framework
//         */
//        private List<String> getSpringApplicationPackages(final BeanFactory beanFactory) {
//            return AutoConfigurationPackages.get(beanFactory);
//        }
//
//        /*
//         * Search for JAX-RS resource and provider Spring beans,
//         * which are the ones whose classes are annotated with
//         * {@link Path} or {@link Provider} respectively
//         *
//         * @param beanFactory
//         */
//        public void findJaxrsResourcesAndProviderClasses(final ConfigurableListableBeanFactory beanFactory) {
//            log.debug("Finding JAX-RS resources and providers Spring bean classes");
//
//            final String[] resourceBeans = beanFactory.getBeanNamesForAnnotation(Path.class);
//            final String[] providerBeans = beanFactory.getBeanNamesForAnnotation(Provider.class);
//
//            if(resourceBeans != null) {
//                for(String resourceBean : resourceBeans) {
//                    allResources.add(beanFactory.getType(resourceBean));
//                }
//            }
//
//            if (this.getAllResources().isEmpty()) {
//                log.warn("No JAX-RS resource Spring beans have been found");
//            }
//
//            if(providerBeans != null) {
//                for(String providerBean : providerBeans) {
//                    providers.add(beanFactory.getType(providerBean));
//                }
//            }
//
//            if(log.isDebugEnabled()) {
//                for (Object resourceClass : allResources.toArray()) {
//                    log.debug("JAX-RS resource class found: {}", ((Class) resourceClass).getName());
//                }
//            }
//            if(log.isDebugEnabled()) {
//                for (Object providerClass: providers.toArray()) {
//                    log.debug("JAX-RS provider class found: {}", ((Class) providerClass).getName());
//                }
//            }
//        }
//
//        public Set<Class<? extends Application>> getApplications() {
//            return this.applications;
//        }
//
//        public Set<Class<?>> getAllResources() {
//            return this.allResources;
//        }
//
//        public Set<Class<?>> getProviders() {
//            return this.providers;
//        }
//
//    }
//
//    /**
//     * This is a Spring version of {@link ResteasyServletInitializer}.
//     * It does not register the servlets though, that is done by {@link ResteasyApplicationBuilder}
//     * It only finds the JAX-RS Application classes (by scanning the classpath), and
//     * the JAX-RS Path and Provider annotated Spring beans, and then register the
//     * Spring bean definitions that represent each servlet registration.
//     *
//     * @author Fabio Carvalho (facarvalho@paypal.com or fabiocarvalho777@gmail.com)
//     */
//    @Slf4j
//    public static class ResteasyBeanProcessorTomcat extends ResteasyResourcesFinder implements BeanFactoryPostProcessor {
//
//        private static final String JAXRS_DEFAULT_PATH = "resteasy.jaxrs.defaultPath";
//        private static final String DEFAULT_BASE_APP_PATH = "/";
//
//        @Override
//        public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
//
//            log.debug("Post process bean factory has been called");
//
//            findJaxrsApplications(beanFactory);
//
//            // This is done by finding their related Spring beans
//            findJaxrsResourcesAndProviderClasses(beanFactory);
//
//            if (getApplications().size() == 0) {
//                registerDefaultJaxrsApp(beanFactory);
//                return;
//            }
//
//            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
//
//            for (Class<? extends Application> applicationClass : getApplications()) {
//
//                ApplicationPath path = AnnotationUtils.findAnnotation(applicationClass, ApplicationPath.class);
//                log.debug("registering JAX-RS application class " + applicationClass.getName());
//                GenericBeanDefinition applicationServletBean = createApplicationServlet(applicationClass, path.value());
//                registry.registerBeanDefinition(applicationClass.getName(), applicationServletBean);
//
//            }
//
//        }
//
//        /**
//         * Register a default JAX-RS application, in case no other is present in the application.
//         * Read section 2.3.2 in JAX-RS 2.0 specification.
//         *
//         * @param beanFactory
//         */
//        private void registerDefaultJaxrsApp(final ConfigurableListableBeanFactory beanFactory) {
//            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
//            ConfigurableEnvironment configurableEnvironment = beanFactory.getBean(ConfigurableEnvironment.class);
//            String path = configurableEnvironment.getProperty(JAXRS_DEFAULT_PATH, DEFAULT_BASE_APP_PATH);
//            GenericBeanDefinition applicationServletBean =
//                    createApplicationServlet(Application.class, path);
//
//            log.info("No JAX-RS Application classes have been found. A default, one mapped to '{}', will be registered.", path);
//            registry.registerBeanDefinition(Application.class.getName(), applicationServletBean);
//        }
//
//        /**
//         * Creates a Servlet bean definition for the given JAX-RS application
//         *
//         * @param applicationClass
//         * @param path
//         * @return a Servlet bean definition for the given JAX-RS application
//         */
//        private GenericBeanDefinition createApplicationServlet(final Class<? extends Application> applicationClass, final String path) {
//            GenericBeanDefinition applicationServletBean = new GenericBeanDefinition();
//            applicationServletBean.setFactoryBeanName(ResteasyApplicationBuilder.BEAN_NAME);
//            applicationServletBean.setFactoryMethodName("build");
//
//            Set<Class<?>> resources = getAllResources();
//
//            ConstructorArgumentValues values = new ConstructorArgumentValues();
//            values.addIndexedArgumentValue(0, applicationClass.getName());
//            values.addIndexedArgumentValue(1, path);
//            values.addIndexedArgumentValue(2, resources);
//            values.addIndexedArgumentValue(3, getProviders());
//            applicationServletBean.setConstructorArgumentValues(values);
//
//            applicationServletBean.setAutowireCandidate(false);
//            applicationServletBean.setScope("singleton");
//
//            return applicationServletBean;
//        }
//
//    }
//
//    @Bean
//    public static ResteasyBeanProcessorTomcat resteasyBeanProcessorTomcat() {
//        return new ResteasyBeanProcessorTomcat();
//    }
//
//    /**
//     * Class that creates a spring bean processor for Resteasy. See
//     * {@link org.jboss.resteasy.plugins.spring.SpringBeanProcessor}.
//     */
//    @Slf4j
//    public static class ResteasyBeanProcessorFactory {
//
//        public static SpringBeanProcessor resteasySpringBeanProcessor() {
//            ResteasyProviderFactory resteasyProviderFactory = new ResteasyProviderFactoryImpl();
//            ResourceMethodRegistry resourceMethodRegistry = new ResourceMethodRegistry(resteasyProviderFactory);
//
//            SpringBeanProcessor resteasySpringBeanProcessor = new SpringBeanProcessor();
//            resteasySpringBeanProcessor.setProviderFactory(resteasyProviderFactory);
//            resteasySpringBeanProcessor.setRegistry(resourceMethodRegistry);
//
//            log.debug("Resteasy Spring Bean Processor has been created");
//
//            return resteasySpringBeanProcessor;
//        }
//
//    }
//
//    @Bean("resteasySpringBeanProcessor")
//    public static SpringBeanProcessor resteasySpringBeanProcessor() {
//        return ResteasyBeanProcessorFactory.resteasySpringBeanProcessor();
//    }

}
