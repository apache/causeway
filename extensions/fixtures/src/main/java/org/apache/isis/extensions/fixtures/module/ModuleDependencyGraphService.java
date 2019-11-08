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
package org.apache.isis.extensions.fixtures.module;

import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service @Log4j2
public class ModuleDependencyGraphService {

    private final ConfigurableApplicationContext context;

    public ModuleDependencyGraphService(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed(ContextRefreshedEvent event) {

        log.info("onContextRefreshed");


//        final ApplicationContext rootContext = root(event.getApplicationContext(), 0);

//        final String[] beanDefinitionNames =  rootContext.getBeanDefinitionNames();
//
//        final List<String> beanList = Arrays.stream(beanDefinitionNames).sorted().collect(Collectors.toList());
//        for (String beanDefinitionName : beanList) {
//            log.info(beanDefinitionName);
//        }

    }

//    private static ApplicationContext root(ApplicationContext applicationContext, int level) {
//
//        // guard
//        if(level > 100) {
//            throw new IllegalStateException("Seem to be in an infinite loop trying to obtain the root ApplicationContext");
//        }
//
//        if(applicationContext == null) {
//            return null;
//        }
//
//        final ApplicationContext parent = applicationContext.getParent();
//        if(parent == null) {
//            return applicationContext;
//        }
//
//        return root(parent, ++level);
//    }


    public ApplicationBeans beans() {
        Map<String, ContextBeans> contexts = new HashMap<>();

        for(ConfigurableApplicationContext context = this.context; context != null; context = getConfigurableParent(context)) {
            contexts.put(context.getId(), ContextBeans.describing(context));
        }

        return new ApplicationBeans(contexts);
    }

    private static ConfigurableApplicationContext getConfigurableParent(ConfigurableApplicationContext context) {
        ApplicationContext parent = context.getParent();
        return parent instanceof ConfigurableApplicationContext ? (ConfigurableApplicationContext)parent : null;
    }

    public static final class BeanDescriptor {
        private final String[] aliases;
        private final String scope;
        private final Class<?> type;
        private final String resource;
        private final String[] dependencies;

        private BeanDescriptor(String[] aliases, String scope, Class<?> type, String resource, String[] dependencies) {
            this.aliases = aliases;
            this.scope = StringUtils.hasText(scope) ? scope : "singleton";
            this.type = type;
            this.resource = resource;
            this.dependencies = dependencies;
        }

        public String[] getAliases() {
            return this.aliases;
        }

        public String getScope() {
            return this.scope;
        }

        public Class<?> getType() {
            return this.type;
        }

        public String getResource() {
            return this.resource;
        }

        public String[] getDependencies() {
            return this.dependencies;
        }
    }

    public static final class ContextBeans {
        private final Map<String, BeanDescriptor> beans;
        private final String parentId;

        private ContextBeans(Map<String, BeanDescriptor> beans, String parentId) {
            this.beans = beans;
            this.parentId = parentId;
        }

        public String getParentId() {
            return this.parentId;
        }

        public Map<String, BeanDescriptor> getBeans() {
            return this.beans;
        }

        private static ContextBeans describing(ConfigurableApplicationContext context) {
            if (context == null) {
                return null;
            } else {
                ConfigurableApplicationContext parent = getConfigurableParent(context);
                return new ContextBeans(describeBeans(context.getBeanFactory()), parent != null ? parent.getId() : null);
            }
        }

        private static Map<String, BeanDescriptor> describeBeans(ConfigurableListableBeanFactory beanFactory) {
            Map<String, BeanDescriptor> beans = new HashMap();
            String[] var2 = beanFactory.getBeanDefinitionNames();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String beanName = var2[var4];
                BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
                if (isBeanEligible(beanName, definition, beanFactory)) {
                    beans.put(beanName, describeBean(beanName, definition, beanFactory));
                }
            }

            return beans;
        }

        private static BeanDescriptor describeBean(String name, BeanDefinition definition, ConfigurableListableBeanFactory factory) {
            return new BeanDescriptor(factory.getAliases(name), definition.getScope(), factory.getType(name), definition.getResourceDescription(), factory.getDependenciesForBean(name));
        }

        private static boolean isBeanEligible(String beanName, BeanDefinition bd, ConfigurableBeanFactory bf) {
            return bd.getRole() != 2 && (!bd.isLazyInit() || bf.containsSingleton(beanName));
        }
    }

    public static final class ApplicationBeans {
        private final Map<String, ContextBeans> contexts;

        private ApplicationBeans(Map<String, ContextBeans> contexts) {
            this.contexts = contexts;
        }

        public Map<String, ContextBeans> getContexts() {
            return this.contexts;
        }
    }

}
