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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.util.ScanUtils;

public class RegisterEntities {

    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(RegisterEntities.class);
    
    public final static String PACKAGE_PREFIX_KEY = "isis.persistor.datanucleus.RegisterEntities.packagePrefix";

    private String packagePrefixes;

    public RegisterEntities(final Map<String, String> configuration) {
        packagePrefixes = configuration.get(PACKAGE_PREFIX_KEY);
        if(Strings.isNullOrEmpty(packagePrefixes)) {
            throw new IllegalStateException("Could not locate '" + PACKAGE_PREFIX_KEY + "' key in property files - aborting");
        }
        
        discoverAllPersistenceCapables();
    }

    private void discoverAllPersistenceCapables() {

        final List<String> packagePrefixList = Lists.newArrayList(Iterables.transform(Splitter.on(",").split(packagePrefixes), trim()));
        for (final String packagePrefix : packagePrefixList) {

            final Iterable<String> entityTypes = ScanUtils.scanForNamesOfClassesWithAnnotation(packagePrefixList, PersistenceCapable.class);
            if(notEmpty(entityTypes)) {
                throw new IllegalStateException("Could not locate any @PersistenceCapable entities in package " + packagePrefix);
            }
            this.entityTypes = Sets.newLinkedHashSet(entityTypes);
        }
    }

    private Set<String> entityTypes;

    @Programmatic
    public Set<String> getEntityTypes() {
        return entityTypes;
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
     * legacy from using <tt>org.reflections.Reflections</tt> that seems to return a set with 1 null element if none can be found.
     */
    private static <T> boolean notEmpty(Iterable<T> set) {
        return Iterables.filter(set, isNull()).iterator().hasNext();
    }

    private static <T> Predicate<T> isNull() {
        return new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return input == null;
            }
        };
    }

}
