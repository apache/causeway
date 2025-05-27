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
package org.apache.causeway.testing.fixtures.applib.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.runtimeservices.spring.BeanDescriptor;
import org.apache.causeway.core.runtimeservices.spring.ContextBeans;
import org.apache.causeway.core.runtimeservices.spring.SpringBeansService;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;

import lombok.Data;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @since 2.x {@index}
 */
@Service
@Named("causeway.testing.fixtures.ModuleWithFixturesService")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModuleWithFixturesService {

    private final SpringBeansService springBeansService;

    public FixtureScript getRefDataSetupFixture() {
        return new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                final List<ModuleWithFixturesService.ModuleWithFixturesDescriptor> descriptors = modules();
                executionContext.executeChildren(this,
                        descriptors.stream()
                                .map(ModuleWithFixturesService.ModuleWithFixturesDescriptor::getModule)
                                .map(ModuleWithFixtures::getRefDataSetupFixture));
            }
        };
    }

    public FixtureScript getTeardownFixture() {
        return new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                final List<ModuleWithFixturesService.ModuleWithFixturesDescriptor> descriptors = modules();
                Collections.reverse(descriptors);
                executionContext.executeChildren(this,
                        descriptors.stream()
                                .map(ModuleWithFixturesService.ModuleWithFixturesDescriptor::getModule)
                                .map(ModuleWithFixtures::getTeardownFixture));
            }

        };
    }

    public List<ModuleWithFixturesDescriptor> modules() {
        var modules = modulesWithin(springBeansService.beansByContextId().entrySet());
        return sequenced(modules);
    }

    static List<ModuleWithFixturesDescriptor> modulesWithin(final Iterable<Map.Entry<String, ContextBeans>> beans) {

        var descriptors = new ArrayList<ModuleWithFixturesDescriptor>();
        for (Map.Entry<String, ContextBeans> contextEntry : beans) {
            final String contextId = contextEntry.getKey();
            final ContextBeans contextBeans = contextEntry.getValue();
            final ConfigurableApplicationContext context = contextBeans.context();

            final Map<String, ModuleWithFixtures> modulesByBeanName = context.getBeansOfType(ModuleWithFixtures.class);
            final Map<String, Object> configurationBeansByBeanName = context.getBeansWithAnnotation(Configuration.class);
            final Map<String, Object> beansAnnotatedWithImportByBeanName = context.getBeansWithAnnotation(Import.class);

            for (Map.Entry<String, BeanDescriptor> beanEntry : contextBeans.beans().entrySet()) {
                final String beanName = beanEntry.getKey();

                final ModuleWithFixtures module = modulesByBeanName.get(beanName);
                if(module != null) {

                    final Object annotatedWithImport = beansAnnotatedWithImportByBeanName.get(beanName);

                    final Optional<Import> importIfAny = annotatedWithImport != null
                            && configurationBeansByBeanName.get(beanName) != null
                                ? Optional.ofNullable(_Reflect.getAnnotation(annotatedWithImport.getClass(), Import.class))
                                : Optional.empty();

                    final Map<String, ModuleWithFixtures> importedModulesByBeanName =
                        importIfAny
                            .map(importAnnot->{
                                final Map<String, ModuleWithFixtures> map = new LinkedHashMap<>();
                                visitImportAnnotation(importAnnot, ModuleWithFixtures.class, context, map::put);
                                return map;
                            })
                            .orElseGet(Collections::emptyMap);

                    var descriptor = new ModuleWithFixturesDescriptor(contextId, beanName, module, importedModulesByBeanName);
                    descriptors.add(descriptor);
                }
            }
        }
        return descriptors;
    }

    static <T> void visitImportAnnotation(
            final @Nullable Import importAnnot,
            final @NonNull Class<T> requiredType,
            final @NonNull ConfigurableApplicationContext springContext,
            final @NonNull BiConsumer<String, T> visitor) {

        if(importAnnot==null) return;
        final Class<?>[] importedClasses = importAnnot.value();

        _NullSafe.stream(importedClasses)
        .filter(type->requiredType.isAssignableFrom(type))
        .forEach(importedClass -> {
            final Map<String, ?> importedBeansOfType = springContext.getBeansOfType(importedClass);
            importedBeansOfType.forEach((name, entryValue) -> {
                final Collection<?> beanCollection = entryValue instanceof Collection
                        ? (Collection<?>) entryValue
                        : Collections.singletonList(entryValue);

                beanCollection.stream()
                        .filter(requiredType::isInstance)
                        .map(requiredType::cast)
                        .forEach(mod -> visitor.accept(name, mod));
            });
        });
    }

    static List<ModuleWithFixturesDescriptor> sequenced(final List<ModuleWithFixturesDescriptor> modules) {
        var remaining = new ArrayList<>(modules);
        var sequenced = new ArrayList<ModuleWithFixturesDescriptor>();

        var moduleByName = new LinkedHashMap<String, ModuleWithFixturesDescriptor>();
        modules.forEach(module -> {
            moduleByName.put(module.getBeanName(), module);
        });

        while(!remaining.isEmpty()) {
            ModuleWithFixturesDescriptor added = addNextModule(sequenced, remaining, moduleByName);
            if (added == null) {
                throw new IllegalStateException(String.format(
                        "Unable to determine next module.\nfound = %s\nremaining = %s",
                        beanNamesOf(sequenced), beanNamesOf(remaining)));
            }
            remaining.remove(added);
        }

        return sequenced;
    }

    static List<String> beanNamesOf(final ArrayList<ModuleWithFixturesDescriptor> result) {
        return result.stream().map(ModuleWithFixturesDescriptor::getBeanName).collect(Collectors.toList());
    }

    static ModuleWithFixturesDescriptor addNextModule(
            final List<ModuleWithFixturesDescriptor> result,
            final List<ModuleWithFixturesDescriptor> remaining,
            final LinkedHashMap<String, ModuleWithFixturesDescriptor> moduleByName) {

        for (ModuleWithFixturesDescriptor module : remaining) {
            var numDependenciesNotYetEncountered =
                    module.getDependenciesByName().keySet().stream()
                            .map(moduleByName::get)
                            .filter(dependency -> !result.contains(dependency)) // ignore if already known about
                            .count();
            if(numDependenciesNotYetEncountered == 0) {
                result.add(module);
                return module;
            }
        }
        return null;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed(final ContextRefreshedEvent event) {
        log.info("onContextRefreshed");
        for (final ModuleWithFixturesDescriptor descriptor : modules()) {
            log.info("%s".formatted(descriptor));
        }
    }

    @Data
    public static class ModuleWithFixturesDescriptor {
        private final String contextId;
        private final String beanName;
        private final ModuleWithFixtures module;
        private final Map<String, ModuleWithFixtures> dependenciesByName;

        @Override
        public String toString() {
            return "ModuleWithFixturesDescriptor{" +
                    "contextId='" + contextId + '\'' +
                    ", beanName='" + beanName + '\'' +
                    ", dependenciesByName=" + dependenciesByName.keySet() +
                    '}';
        }
    }

}
