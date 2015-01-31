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

import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jdo.annotations.PersistenceCapable;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;

@Hidden
public class RegisterEntities {

    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(RegisterEntities.class);
    
    public final static String PACKAGE_PREFIX_KEY = "isis.persistor.datanucleus.RegisterEntities.packagePrefix";

    // //////////////////////////////////////

    private String packagePrefixes;

    @PostConstruct
    public void init(Map<String,String> configuration) {
        packagePrefixes = configuration.get(PACKAGE_PREFIX_KEY);
        if(Strings.isNullOrEmpty(packagePrefixes)) {
            throw new IllegalStateException("Could not locate '" + PACKAGE_PREFIX_KEY + "' key in property files - aborting");
        }
        
        registerAllPersistenceCapables();
    }

    @PreDestroy
    public void shutdown() {
    }

    private void registerAllPersistenceCapables() {

        for (final String packagePrefix : Iterables.transform(Splitter.on(",").split(packagePrefixes), trim())) {
            Reflections reflections = new Reflections(packagePrefix);
            
            Set<Class<?>> entityTypes = 
                    reflections.getTypesAnnotatedWith(PersistenceCapable.class);
            
            if(noEntitiesIn(entityTypes)) {
                throw new IllegalStateException("Could not locate any @PersistenceCapable entities in package " + packagePrefix);
            }
            for (Class<?> entityType : entityTypes) {
                if(ignore(entityType)) {
                    // ignore (probably a testing class)
                    continue;
                }
                getSpecificationLoader().loadSpecification(entityType);
            }
        }
    }

    private static boolean ignore(final Class<?> entityType) {
        try {
            if(entityType.isAnonymousClass() || entityType.isLocalClass() || entityType.isMemberClass()) {
                return true;
            }
            final PersistenceCapable persistenceCapable = entityType.getAnnotation(PersistenceCapable.class);
            final boolean hasPersistenceCapable = persistenceCapable != null;
            return !hasPersistenceCapable; // don't ignore if has @PersistenceCapable
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

    /**
     * {@link Reflections} seems to return a set with 1 null element if none can be found.
     */
    private static boolean noEntitiesIn(Set<Class<?>> entityTypes) {
        return Iterables.filter(entityTypes, nullClass()).iterator().hasNext();
    }

    private static Predicate<Class<?>> nullClass() {
        return new Predicate<Class<?>>() {

            @Override
            public boolean apply(Class<?> input) {
                return input == null;
            }
        };
    }

    // //////////////////////////////////////

    SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }


    
}
