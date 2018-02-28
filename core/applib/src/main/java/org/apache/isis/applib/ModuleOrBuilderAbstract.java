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

import org.apache.isis.applib.internal.collections._Lists;
import org.apache.isis.applib.internal.collections._Maps;
import org.apache.isis.applib.internal.collections._Sets;

/**
 * Factors out the commonality between {@link AppManifestAbstract.Builder} and {@link ModuleAbstract}.
 *
 * @param <B>
 */
abstract class ModuleOrBuilderAbstract<B extends ModuleOrBuilderAbstract<B>> {

    final Set<Class<?>> additionalModules = _Sets.newLinkedHashSet();
    final Set<Class<?>> additionalServices  = _Sets.newLinkedHashSet();

    final Map<String,String> individualConfigProps = _Maps.newLinkedHashMap();
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
        return withAdditionalDependencies(_Sets.unmodifiable(dependencies));
    }

    void withTransitiveFrom(final Module module) {
        withAdditionalModules(asClasses(Module.Util.transitiveDependenciesOf(module)));
        withAdditionalModules(Module.Util.transitiveAdditionalModulesOf(module));
        withAdditionalServices(Module.Util.transitiveAdditionalServicesOf(module));
        withConfigurationPropertyResources(Module.Util.transitivePropertyResourcesOf(module));
        withConfigurationProperties(Module.Util.transitiveIndividualConfigPropsOf(module));
    }

    @SuppressWarnings("unchecked") //[ahuber] it's safe to assume correct type casting here
    private static Class<? extends Module>[] asClasses(final List<Module> dependencies) {
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
            return self();
        }
        this.additionalModules.addAll(modules);
        return self();
    }

    public B withAdditionalServices(final Class<?>... additionalServices) {
        return withAdditionalServices(Arrays.asList(additionalServices));
    }

    public B withAdditionalServices(final List<Class<?>> additionalServices) {
        if(additionalServices == null) {
            return self();
        }
        this.additionalServices.addAll(additionalServices);
        return self();
    }

    public B withConfigurationProperties(final Map<String,String> configurationProperties) {
        for (Map.Entry<String, String> keyValue : configurationProperties.entrySet()) {
            withConfigurationProperty(keyValue.getKey(), keyValue.getValue());
        }
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

    @XmlTransient
    public List<PropertyResource> getPropertyResources() {
        return propertyResources;
    }
    
    // -- HELPER
    
    @SuppressWarnings("unchecked") //[ahuber] it's safe to assume this object is an instance of B
	protected B self() {
    	return (B) this;
    }

}
