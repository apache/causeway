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
package org.apache.isis.applib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;

/**
 * Factors out the commonality between {@link AppManifestAbstract.Builder} and {@link ModuleAbstract}.
 *
 * @param <B>
 */
abstract class ModuleOrBuilderAbstract<B extends ModuleOrBuilderAbstract<B>> {

    final Set<Class<?>> additionalModules = _Sets.newLinkedHashSet();
    final Set<Class<?>> additionalServices  = _Sets.newLinkedHashSet();

    final Map<String,String> individualConfigProps = _Maps.newLinkedHashMap();
    final Map<String,String> fallbackConfigProps = _Maps.newLinkedHashMap();
    final List<PropertyResource> propertyResources = _Lists.newArrayList();

    ModuleOrBuilderAbstract() {}

    public B withAdditionalDependency(final Module dependency) {
        withTransitiveFrom(dependency);
        return self();
    }

    public B withAdditionalDependencies(final Set<Module> dependencies) {
        for (final Module dependency : dependencies) {
            withAdditionalDependency(dependency);
        }
        return self();
    }

    public B withAdditionalDependencies(final Module... dependencies) {
        return withAdditionalDependencies(_Sets.of(dependencies));
    }

    void withTransitiveFrom(final Module module) {
        withAdditionalModules(asClasses(Module.Util.transitiveDependenciesOf(module)));
        withAdditionalModules(Module.Util.transitiveAdditionalModulesOf(module));
        withAdditionalServices(Module.Util.transitiveAdditionalServicesOf(module));
        withConfigurationPropertyResources(Module.Util.transitivePropertyResourcesOf(module));
        withConfigurationProperties(Module.Util.transitiveIndividualConfigPropsOf(module));
        withFallbackConfigurationProperties(Module.Util.transitiveFallbackConfigPropsOf(module));
    }

    private static Class<? extends Module>[] asClasses(final List<Module> dependencies) {
        final List<Class<? extends Module>> list = new ArrayList<>();
        for (Module dependency : dependencies) {
            Class<? extends Module> aClass = dependency.getClass();
            list.add(aClass);
        }
        return _Casts.uncheckedCast( list.toArray(_Constants.emptyClasses) );
    }

    public B withAdditionalModules(final Class<?>... modules) {
        return withAdditionalModules(Arrays.asList(modules));
    }

    public B withAdditionalModules(final Collection<Class<?>> modules) {
        if(modules == null) {
            return self();
        }
        this.additionalModules.addAll(modules);
        return self();
    }

    public B withAdditionalServices(final Class<?>... additionalServices) {
        return withAdditionalServices(Arrays.asList(additionalServices));
    }

    public B withAdditionalServices(final Collection<Class<?>> additionalServices) {
        if(additionalServices == null) {
            return self();
        }
        this.additionalServices.addAll(additionalServices);
        return self();
    }

    public B withConfigurationProperties(final Map<String,String> configurationProperties) {
        configurationProperties.forEach(this::withConfigurationProperty);
        return self();
    }

    public B withFallbackConfigurationProperties(final Map<String,String> fallbackConfigurationProperties) {
        fallbackConfigurationProperties.forEach(this::withFallbackConfigurationProperty);
        return self();
    }

    public B withConfigurationPropertiesFile(final String propertiesFile) {
        return withConfigurationPropertiesFile(getClass(), propertiesFile);
    }

    public B withConfigurationPropertyResources(final List<PropertyResource> propertyResources) {
        for (PropertyResource propertyResource : propertyResources) {
            withConfigurationPropertyResource(propertyResource);
        }
        return self();
    }

    public B withConfigurationPropertyResource(final PropertyResource propertyResource) {
        addPropertyResource(propertyResource);
        return self();
    }

    public B withConfigurationPropertiesFile(
            final Class<?> propertiesFileContext,
            final String propertiesFile,
            final String... furtherPropertiesFiles) {
        addPropertyResource(propertiesFileContext, propertiesFile);
        for (final String otherFile : furtherPropertiesFiles) {
            addPropertyResource(propertiesFileContext, otherFile);
        }
        return self();
    }

    private void addPropertyResource(final Class<?> propertiesFileContext, final String propertiesFile) {
        addPropertyResource(new PropertyResource(propertiesFileContext, propertiesFile));
    }

    private void addPropertyResource(final PropertyResource propertyResource) {
        propertyResources.add(propertyResource);
    }

    public B withConfigurationProperty(final String key, final String value) {
        individualConfigProps.put(key, value);
        return self();
    }

    @XmlTransient
    public Map<String,String> getIndividualConfigProps() {
        return individualConfigProps;
    }

    public B withFallbackConfigurationProperty(final String key, final String value) {
        fallbackConfigProps.put(key, value);
        return self();
    }

    @XmlTransient
    public Map<String,String> getFallbackConfigProps() {
        return fallbackConfigProps;
    }

    @XmlTransient
    public List<PropertyResource> getPropertyResources() {
        return propertyResources;
    }

    // -- HELPER

    protected B self() {
        return _Casts.uncheckedCast(this);
    }

}
