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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.isis.applib.fixturescripts.FixtureScript;

public abstract class ModuleAbstract implements Module, AppManifestBuilder<ModuleAbstract> {


    /**
     * As per Maven's &lt;dependencies&gt;&lt;/dependencies&gt; element; in the future might be derived (code generated?) from java 9's <code>module-info.java</code> metadata
     *
     * <p>
     *     We use Set (rather than List) because we rely on {@link Module} being a value type based solely on its
     *     class.  What this means is that each module can simply instantiate its dependencies, and the framework will
     *     be able to eliminate duplicates.
     * </p>
     */
    @Override
    @XmlTransient
    public Set<Module> getDependencies() {
        return Collections.emptySet();
    }

    /**
     * Support for "legacy" modules that do not implement {@link Module}.
     */
    @Override
    @XmlTransient
    public Set<Class<?>> getDependenciesAsClass() {
        return modules;
    }

    @Override
    @XmlTransient
    public FixtureScript getRefDataSetupFixture() {
        return null;
    }

    @Override
    @XmlTransient
    public FixtureScript getTeardownFixture() {
        return null;
    }


    @Override
    @XmlTransient
    public Set<Class<?>> getAdditionalServices() {
        return additionalServices;
    }


    @XmlAttribute(required = true)
    public String getName() {
        return getClass().getSimpleName();
    }

    private String getFullName() {
        return getClass().getName();
    }


    @XmlElement(name = "module", required = true)
    private Set<ModuleAbstract> getModuleDependencies() {
        return (Set) getDependencies();
    }




    private final Set<Class<?>> modules = Sets.newLinkedHashSet();
    private final Set<Class<?>> additionalServices  = Sets.newLinkedHashSet();

    private final List<AppManifestAbstract.ConfigurationProperty> individualConfigProps = Lists.newArrayList();
    private final List<AppManifestAbstract.PropertyResource> propertyResources = Lists.newArrayList();

    private Map<String,String> configurationProperties = Maps.newHashMap();

    public ModuleAbstract withAdditionalModules(final Class<?>... modules) {
        return withAdditionalModules(Arrays.asList(modules));
    }

    public ModuleAbstract withAdditionalModules(final List<Class<?>> modules) {
        if(modules == null) {
            throw new IllegalArgumentException("List of modules must not be null");
        }
        this.modules.addAll(modules);
        return this;
    }

    public ModuleAbstract withAdditionalServices(final Class<?>... additionalServices) {
        return withAdditionalServices(Arrays.asList(additionalServices));
    }

    public ModuleAbstract withAdditionalServices(final List<Class<?>> additionalServices) {
        if(additionalServices == null) {
            throw new IllegalArgumentException("List of additional services must not be null");
        }
        this.additionalServices.addAll(additionalServices);
        return this;
    }

    public ModuleAbstract withConfigurationProperties(final Map<String,String> configurationProperties) {
        this.configurationProperties.putAll(configurationProperties);
        return this;
    }

    public ModuleAbstract withConfigurationPropertiesFile(final String propertiesFile) {
        return withConfigurationPropertiesFile(getClass(), propertiesFile);
    }

    public ModuleAbstract withConfigurationPropertiesFile(
            final Class<?> propertiesFileContext, final String propertiesFile, final String... furtherPropertiesFiles) {
        addPropertyResource(propertiesFileContext, propertiesFile);
        for (final String otherFile : furtherPropertiesFiles) {
            addPropertyResource(propertiesFileContext, otherFile);
        }
        return this;
    }

    private void addPropertyResource(final Class<?> propertiesFileContext, final String propertiesFile) {
        propertyResources.add(new AppManifestAbstract.PropertyResource(propertiesFileContext, propertiesFile));
    }

    public ModuleAbstract withConfigurationProperty(final String key, final String value) {
        individualConfigProps.add(new AppManifestAbstract.ConfigurationProperty(key,value));
        return this;
    }


    @XmlTransient
    @Override
    public List<Class<?>> getAllModulesAsClass() {

        final List<Class<?>> modules = Lists.newArrayList();

        final List<Module> transitiveDependencies = Module.Util.transitiveDependenciesOf(this);
        final List<Class<? extends Module>> moduleTransitiveDependencies = asClasses(transitiveDependencies);
        modules.addAll(moduleTransitiveDependencies);

        final List<Class<?>> additionalModules = Module.Util.transitiveDependenciesAsClassOf(this);
        modules.addAll(additionalModules);

        return modules;
    }


    private static List<Class<? extends Module>> asClasses(final List<Module> dependencies) {
        final List<Class<? extends Module>> list = new ArrayList<>();
        for (Module dependency : dependencies) {
            Class<? extends Module> aClass = dependency.getClass();
            list.add(aClass);
        }
        return list;
    }

    @Override
    @XmlTransient
    public final Set<Class<?>> getAllAdditionalServices() {
        final List<Class<?>> additionalServices = Module.Util.transitiveAdditionalServicesOf(this);
        return Sets.newLinkedHashSet(additionalServices);
    }

    @XmlTransient
    @Override
    public final List<AppManifestAbstract.PropertyResource> getAllPropertyResources() {

        List<AppManifestAbstract.PropertyResource> transitivePropertyResources = Lists.newArrayList();

        final List<Module> transitiveDependencies = Module.Util.transitiveDependenciesOf(this);
        for (Module transitiveDependency : transitiveDependencies) {
            if(transitiveDependency instanceof ModuleAbstract) {
                ModuleAbstract moduleAbstract = (ModuleAbstract) transitiveDependency;
                transitivePropertyResources.addAll(moduleAbstract.propertyResources);
            }
        }

        return transitivePropertyResources;
    }

    @XmlTransient
    @Override
    public List<AppManifestAbstract.ConfigurationProperty> getAllIndividualConfigProps() {
        List<AppManifestAbstract.ConfigurationProperty> transitiveIndividualConfigProps = Lists.newArrayList();

        final List<Module> transitiveDependencies = Module.Util.transitiveDependenciesOf(this);
        for (Module transitiveDependency : transitiveDependencies) {
            if(transitiveDependency instanceof ModuleAbstract) {
                ModuleAbstract moduleAbstract = (ModuleAbstract) transitiveDependency;
                transitiveIndividualConfigProps.addAll(moduleAbstract.individualConfigProps);
            }
        }
        return transitiveIndividualConfigProps;
    }




    @Override
    public String toString() {
        return getFullName();
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ModuleAbstract)) {
            return false;
        }
        final ModuleAbstract other = (ModuleAbstract) o;
        return Objects.equals(getFullName(), other.getFullName());
    }

    public int hashCode() {
        return getFullName().hashCode();
    }

}