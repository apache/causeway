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

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

import org.apache.isis.applib.fixturescripts.FixtureScript;

/**
 * Adapter for {@link Module} which has a default no-op implementation.
 *
 * <p>
 *     Subclasses can either override the methods, or can use the various {@link #withAdditionalModules(Class[])}.
 * </p>
 */
public abstract class ModuleAbstract
        extends ModuleOrBuilderAbstract<ModuleAbstract>
        implements Module {


    @Override
    @XmlTransient
    public Set<Module> getDependencies() {
        return Sets.newLinkedHashSet();
    }

    @XmlElement(name = "module", required = true)
    private Set<ModuleAbstract> getModuleDependencies() {
        return FluentIterable.from(getDependencies())
                    .filter(new Predicate<Module>() {
                        @Override
                        public boolean apply(@Nullable final Module module) {
                            return module instanceof ModuleAbstract;
                        }
                    })
                    .transform(new Function<Module, ModuleAbstract>() {
                        @Nullable @Override
                        public ModuleAbstract apply(@Nullable final Module module) {
                            return (ModuleAbstract) module;
                        }
                    })
                    .toSet();
    }

    /**
     * Support for "legacy" modules that do not implement {@link Module}.
     */
    @Override
    @XmlTransient
    public Set<Class<?>> getAdditionalModules() {
        return additionalModules;
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