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
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;

public class RegisterEntities {

    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(RegisterEntities.class);
    
    public final static String PACKAGE_PREFIX_KEY = "isis.persistor.datanucleus.RegisterEntities.packagePrefix";

    // //////////////////////////////////////

    // determines how to handle missing entities in a package
    // if globSpec is in use, just log it (because we use packages also to indicate presence of services);
    // if globSpec NOT in use, then treat this as an error.
    private final boolean globSpecSpecified;

    //region > domPackages
    private final List<String> domPackages;

    public List<String> getDomPackages() {
        return domPackages;
    }
    //endregion

    //region > entityTypes
    private final Set<String> entityTypes;

    public Set<String> getEntityTypes() {
        return entityTypes;
    }
    //endregion

    public RegisterEntities(Map<String,String> configuration) {
        String packagePrefixes = configuration.get(PACKAGE_PREFIX_KEY);
        if(Strings.isNullOrEmpty(packagePrefixes)) {
            throw new IllegalArgumentException(String.format(
                    "Could not locate '%s' key in property files - aborting",
                    PACKAGE_PREFIX_KEY));
        }
        domPackages = parseDomPackages(packagePrefixes);
        this.globSpecSpecified = configuration.get("isis.globSpec") != null;

        this.entityTypes = scanForEntityTypesIn(this.domPackages, this.globSpecSpecified);
    }

    private static List<String> parseDomPackages(String packagePrefixes) {
        return Collections.unmodifiableList(Lists.newArrayList(Iterables.transform(Splitter.on(",").split(packagePrefixes), trim())));
    }

    private static Set<String> scanForEntityTypesIn(final List<String> domPackages, final boolean globSpecSpecified) {
        final Set<String> entityTypes = Sets.newLinkedHashSet();
        for (final String packageName : domPackages) {
            Reflections reflections = new Reflections(packageName);

            final Set<Class<?>> entityTypesInPackage =
                    reflections.getTypesAnnotatedWith(PersistenceCapable.class);

            if(!entitiesIn(entityTypesInPackage)) {

                if(globSpecSpecified) {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("Could not locate any @PersistenceCapable entities in module '%s'; ignoring\n", packageName);
                    }
                } else {
                    throw new IllegalArgumentException(String.format(
                            "Bad configuration.\n\nCould not locate any @PersistenceCapable entities in package '%s'\n" +
                                    "Check value of '%s' key in WEB-INF/*.properties\n",
                            packageName,
                            PACKAGE_PREFIX_KEY));
                }
            }
            for (Class<?> entityType : entityTypesInPackage) {
                if(ignore(entityType)) {
                    continue;
                }
                entityTypes.add(entityType.getCanonicalName());
            }
        }
        return entityTypes;
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

    SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }


    
}
