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
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.isis.applib.util.ScanUtils;

public class RegisterEntities {

    public final static String PACKAGE_PREFIX_KEY = "isis.persistor.datanucleus.RegisterEntities.packagePrefix";

    //region > init

    public RegisterEntities(final Map<String, String> configuration) {
        String packagePrefixes = configuration.get(PACKAGE_PREFIX_KEY);
        if(Strings.isNullOrEmpty(packagePrefixes)) {
            throw new IllegalArgumentException(String.format(
                    "Could not locate '%s' key in property files - aborting",
                    PACKAGE_PREFIX_KEY));
        }

        domPackages = parseDomPackages(packagePrefixes);
        this.entityTypes = scanForEntityTypesIn(this.domPackages);
    }

    //endregion

    //region > domPackages
    private List<String> domPackages;

    public List<String> getDomPackages() {
        return domPackages;
    }
    //endregion

    //region > entityTypes
    private Set<String> entityTypes;

    public Set<String> getEntityTypes() {
        return entityTypes;
    }
    //endregion

    //region > helpers
    private static Set<String> scanForEntityTypesIn(final List<String> domPackages) {
        Set<String> entityTypes = Sets.newLinkedHashSet();
        for (final String packagePrefix : domPackages) {
            final Iterable<String> entityTypes1 = ScanUtils.scanForNamesOfClassesWithAnnotation(domPackages, PersistenceCapable.class);
            if(Iterables.isEmpty(entityTypes1)) {
                throw new IllegalArgumentException(String.format(
                        "Bad configuration.\n\nCould not locate any @PersistenceCapable entities in package '%s'\n" +
                        "Check value of '%s' key in isis.properties etc.\n",
                        packagePrefix,
                        PACKAGE_PREFIX_KEY));
            }
            Iterables.addAll(entityTypes, entityTypes1);
        }
        return entityTypes;
    }

    private static List<String> parseDomPackages(String packagePrefixes) {
        return Collections.unmodifiableList(Lists.newArrayList(Iterables.transform(Splitter.on(",").split(packagePrefixes), trim())));
    }

    private static Function<String,String> trim() {
        return new Function<String,String>(){
            @Override
            public String apply(String input) {
                return input.trim();
            }
        };
    }
    //endregion

}
