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

import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.isis.extensions.spring.service.SpringBeansService;
import org.apache.isis.extensions.spring.service.BeanDescriptor;
import org.apache.isis.extensions.spring.service.ContextBeans;
import org.apache.isis.metamodel.facets.Annotations;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ModuleService {

    private final SpringBeansService springBeansService;

    public ModuleService(final SpringBeansService springBeansService) {
        this.springBeansService = springBeansService;
    }

    public List<ModuleDescriptor> modules() {

        final List<ModuleDescriptor> moduleDescriptors = new ArrayList<>();
        final Map<String, ContextBeans> contexts = springBeansService.beans();
        for (Map.Entry<String, ContextBeans> contextEntry : contexts.entrySet()) {
            final String contextId = contextEntry.getKey();
            final ContextBeans contextBeans = contextEntry.getValue();
            final ConfigurableApplicationContext context = contextBeans.getContext();

            final Map<String, Module> modulesByBeanName = context.getBeansOfType(Module.class);
            final Map<String, Object> configurationBeansByBeanName = context.getBeansWithAnnotation(Configuration.class);
            final Map<String, Object> beansAnnotatedWithImportByBeanName = context.getBeansWithAnnotation(Import.class);

            for (Map.Entry<String, BeanDescriptor> beanEntry : contextBeans.getBeans().entrySet()) {
                final String beanName = beanEntry.getKey();

                final Module module = modulesByBeanName.get(beanName);
                if(module != null) {

                    final Object annotatedWithConfiguration = configurationBeansByBeanName.get(beanName);
                    final Object annotatedWithImport = beansAnnotatedWithImportByBeanName.get(beanName);

                    final List<Module> importedModules;
                    if(annotatedWithConfiguration != null && annotatedWithImport != null) {

                        final Import importAnnot = Annotations.getAnnotation(annotatedWithImport.getClass(), Import.class);
                        final Class<?>[] importedClasses = importAnnot.value();

                        importedModules = Arrays.stream(importedClasses)
                                .map(importedClass -> context.getBeansOfType(importedClass).values())
                                .flatMap(Collection::stream)
                                .filter(Module.class::isInstance)
                                .map(Module.class::cast)
                                .collect(Collectors.toList());
                    } else {
                        importedModules = Collections.emptyList();
                    }

                    final Map<String, Module> importedModulesByBeanName = new LinkedHashMap<>();
                    if(annotatedWithConfiguration != null && annotatedWithImport != null) {

                        final Import importAnnot = Annotations.getAnnotation(annotatedWithImport.getClass(), Import.class);
                        final Class<?>[] importedClasses = importAnnot.value();

                        Arrays.stream(importedClasses)
                                .forEach(importedClass -> {
                                    final Map<String, ?> importedBeansOfType = context.getBeansOfType(importedClass);
                                    importedBeansOfType.forEach((name, entryValue) -> {
                                        final Collection<?> beanCollection;
                                        if (entryValue instanceof Collection) {
                                            beanCollection = (Collection) entryValue;
                                        } else {
                                            beanCollection = Collections.singletonList(entryValue);
                                        }
                                        beanCollection.stream()
                                                .filter(Module.class::isInstance)
                                                .map(Module.class::cast)
                                                .forEach(mod -> importedModulesByBeanName.put(name, mod));
                                    });
                                });
                    }

                    final ModuleDescriptor moduleDescriptor = new ModuleDescriptor(contextId, beanName, module, importedModules, importedModulesByBeanName);
                    moduleDescriptors.add(moduleDescriptor);
                }
            }
        }
        return moduleDescriptors;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed(ContextRefreshedEvent event) {
        log.info("onContextRefreshed");
        final List<ModuleDescriptor> modules = modules();
        for (final ModuleDescriptor module : modules) {
            log.info(module);
        }
    }

    @Data
    public static class ModuleDescriptor {
        private final String contextId;
        private final String beanName;
        private final Module module;
        private final List<Module> moduleDependencies;
        private final Map<String,Module> dependenciesByName;

        @Override
        public String toString() {
            return "ModuleDescriptor{" +
                    "contextId='" + contextId + '\'' +
                    ", beanName='" + beanName + '\'' +
                    ", dependenciesByName=" + dependenciesByName.keySet() +
                    '}';
        }
    }

}
