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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Factors out the commonality between {@link AppManifestAbstract.Builder} and {@link ModuleAbstract}.
 *
 * @param <B>
 */
abstract class ModuleOrBuilderAbstract<B extends ModuleOrBuilderAbstract> {

    final Set<Class<?>> additionalModules = Sets.newLinkedHashSet();
    final Set<Class<?>> additionalServices  = Sets.newLinkedHashSet();

    final Map<String,String> individualConfigProps = Maps.newLinkedHashMap();
    final List<PropertyResource> propertyResources = Lists.newArrayList();

    ModuleOrBuilderAbstract() {}

    public B withAdditionalDependency(final Module dependency) {
        withTransitiveFrom(dependency);
        return (B) this;
    }

    public B withAdditionalDependencies(final Set<Module> dependencies) {
        for (final Module dependency : dependencies) {
            withAdditionalDependency(dependency);
        }
        return (B) this;
    }

    public B withAdditionalDependencies(final Module... dependencies) {
        return withAdditionalDependencies(Sets.newHashSet(dependencies));
    }

    void withTransitiveFrom(final Module module) {
        withAdditionalModules(asClasses(Module.Util.transitiveDependenciesOf(module)));
        withAdditionalModules(Module.Util.transitiveAdditionalModulesOf(module));
        withAdditionalServices(Module.Util.transitiveAdditionalServicesOf(module));
        withConfigurationPropertyResources(Module.Util.transitivePropertyResourcesOf(module));
        withConfigurationProperties(Module.Util.transitiveIndividualConfigPropsOf(module));
    }

    private static Class[] asClasses(final List<Module> dependencies) {
        final List<Class<? extends Module>> list = new ArrayList<>();
        for (Module dependency : dependencies) {
            Class<? extends Module> aClass = dependency.getClass();
            list.add(aClass);
        }
        return list.toArray(new Class[] {});
    }

    public B withAdditionalModules(final Class<?>... modules) {
        return withAdditionalModules(Arrays.asList(modules));
    }

    public B withAdditionalModules(final List<Class<?>> modules) {
        if(modules == null) {
            return (B)this;
        }
        this.additionalModules.addAll(modules);
        return (B)this;
    }

    public B withAdditionalServices(final Class<?>... additionalServices) {
        return withAdditionalServices(Arrays.asList(additionalServices));
    }

    public B withAdditionalServices(final List<Class<?>> additionalServices) {
        if(additionalServices == null) {
            return (B)this;
        }
        this.additionalServices.addAll(additionalServices);
        return (B)this;
    }

    public B withConfigurationProperties(final Map<String,String> configurationProperties) {
        for (Map.Entry<String, String> keyValue : configurationProperties.entrySet()) {
            withConfigurationProperty(keyValue.getKey(), keyValue.getValue());
        }
        return (B)this;
    }

    public B withConfigurationPropertiesFile(final String propertiesFile) {
        return withConfigurationPropertiesFile(getClass(), propertiesFile);
    }

    public B withConfigurationPropertyResources(final List<PropertyResource> propertyResources) {
        for (PropertyResource propertyResource : propertyResources) {
            withConfigurationPropertyResource(propertyResource);
        }
        return (B)this;
    }

    public B withConfigurationPropertyResource(final PropertyResource propertyResource) {
        addPropertyResource(propertyResource);
        return (B)this;
    }

    public B withConfigurationPropertiesFile(
            final Class<?> propertiesFileContext,
            final String propertiesFile,
            final String... furtherPropertiesFiles) {
        addPropertyResource(propertiesFileContext, propertiesFile);
        for (final String otherFile : furtherPropertiesFiles) {
            addPropertyResource(propertiesFileContext, otherFile);
        }
        return (B)this;
    }

    private void addPropertyResource(final Class<?> propertiesFileContext, final String propertiesFile) {
        addPropertyResource(new PropertyResource(propertiesFileContext, propertiesFile));
    }

    private void addPropertyResource(final PropertyResource propertyResource) {
        propertyResources.add(propertyResource);
    }

    public B withConfigurationProperty(final String key, final String value) {
        individualConfigProps.put(key, value);
        return (B)this;
    }

    public B withConfigurationProperty(final Map.Entry<String, String> keyValue) {
        return withConfigurationProperty(keyValue.getKey(), keyValue.getValue());
    }

    @XmlTransient
    public Map<String,String> getIndividualConfigProps() {
        return individualConfigProps;
    }

    @XmlTransient
    public List<PropertyResource> getPropertyResources() {
        return propertyResources;
    }

}
