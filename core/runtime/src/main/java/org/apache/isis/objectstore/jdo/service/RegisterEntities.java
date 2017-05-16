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
package org.apache.isis.objectstore.jdo.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.annotations.PersistenceCapable;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public class RegisterEntities {

    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(RegisterEntities.class);
    
    public final static String PACKAGE_PREFIX_KEY = "isis.persistor.datanucleus.RegisterEntities.packagePrefix";

    // //////////////////////////////////////


    //region > entityTypes
    private final Set<String> entityTypes = Sets.newLinkedHashSet();
    private final SpecificationLoader specificationLoader;

    public Set<String> getEntityTypes() {
        return entityTypes;
    }
    //endregion

    public RegisterEntities(final Map<String, String> configuration, final SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;

        Set<Class<?>> persistenceCapableTypes = AppManifest.Registry.instance().getPersistenceCapableTypes();

        if(persistenceCapableTypes == null) {
            persistenceCapableTypes = searchForPersistenceCapables(configuration);
        }

        final List<String> classNamesNotEnhanced = Lists.newArrayList();
        for (Class<?> persistenceCapableType : persistenceCapableTypes) {
            if(ignore(persistenceCapableType)) {
                continue;
            }
            if(!org.datanucleus.enhancement.Persistable.class.isAssignableFrom(persistenceCapableType)) {
                classNamesNotEnhanced.add(persistenceCapableType.getCanonicalName());
            }
            this.entityTypes.add(persistenceCapableType.getCanonicalName());
        }

        if(!classNamesNotEnhanced.isEmpty()) {
            final String classNamesNotEnhancedStr = Joiner.on("\n* ").join(classNamesNotEnhanced);
            throw new IllegalStateException("Non-enhanced @PersistenceCapable classes found, will abort.  The classes in error are:\n\n* " + classNamesNotEnhancedStr + "\n\nDid the DataNucleus enhancer run correctly?\n");
        }
    }

    /**
     * only called if no appManifest
     */
    Set<Class<?>> searchForPersistenceCapables(final Map<String, String> configuration) {

        final String packagePrefixes = lookupPackagePrefixes(configuration);

        final Set<Class<?>> persistenceCapableTypes = Sets.newLinkedHashSet();
        final List<String> domPackages = parseDomPackages(packagePrefixes);
        for (final String packageName : domPackages) {
            Reflections reflections = new Reflections(packageName);
            final Set<Class<?>> entityTypesInPackage =
                    reflections.getTypesAnnotatedWith(PersistenceCapable.class);

            if(!entitiesIn(entityTypesInPackage)) {
                throw new IllegalArgumentException(String.format(
                        "Bad configuration.\n\nCould not locate any @PersistenceCapable entities in package '%s'\n" +
                                "Check value of '%s' key in WEB-INF/*.properties\n",
                        packageName,
                        PACKAGE_PREFIX_KEY));
            }
            persistenceCapableTypes.addAll(entityTypesInPackage);
        }
        return persistenceCapableTypes;
    }

    private String lookupPackagePrefixes(final Map<String, String> configuration) {
        final String packagePrefixes = configuration.get(PACKAGE_PREFIX_KEY);
        if(Strings.isNullOrEmpty(packagePrefixes)) {
            throw new IllegalArgumentException(String.format(
                    "Could not locate '%s' key in property files - aborting",
                    PACKAGE_PREFIX_KEY));
        }
        return packagePrefixes;
    }

    private static List<String> parseDomPackages(String packagePrefixes) {
        return Collections.unmodifiableList(Lists.newArrayList(Iterables.transform(Splitter.on(",").split(packagePrefixes), trim())));
    }

    private static boolean ignore(final Class<?> entityType) {
        try {
            if(entityType.isAnonymousClass() || entityType.isLocalClass() || entityType.isMemberClass()) {
                return true;
            }
            final PersistenceCapable persistenceCapable = entityType.getAnnotation(PersistenceCapable.class);
            return persistenceCapable == null; // ignore if doesn't have @PersistenceCapable
        } catch (NoClassDefFoundError ex) {
            return true;
        }
    }

    private static Function<String,String> trim() {
        return new Function<String,String>(){
            @Override
            public String apply(String input) {
                return input.trim();
            }
        };
    }

    private static boolean entitiesIn(Set<Class<?>> entityTypes) {
        return Iterables.filter(entityTypes, notNullClass()).iterator().hasNext();
    }

    /**
     * {@link Reflections} seems to return a set with 1 null element if none can be found, so we ignore these.
     */
    private static <T> Predicate<T> notNullClass() {
        return new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return input != null;
            }
        };
    }

    // //////////////////////////////////////

    SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }


}
