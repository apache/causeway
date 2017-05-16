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
package org.apache.isis.applib.services.classdiscovery;

import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.vfs.Vfs;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.fixturescripts.FixtureScript;

/**
 * This utility service supports the dynamic discovery of classes from the classpath.  One service that uses this
 * is the <tt>FixtureScripts</tt> domain service.
 *
 * <p>
 * This service has no UI and there is only one implementation (this class) in applib, so it is annotated with
 * {@link DomainService}.  This means that it is automatically registered and
 * available for use; no further configuration is required.
 * </p>
 */
@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
)
public class ClassDiscoveryServiceUsingReflections
            extends AbstractService 
            implements ClassDiscoveryService2 {

    @Programmatic
    @Override
    public <T> Set<Class<? extends T>> findSubTypesOfClasses(Class<T> type) {
        Vfs.setDefaultURLTypes(getUrlTypes());

        final Reflections reflections = new Reflections(
                ClasspathHelper.forClassLoader(Thread.currentThread().getContextClassLoader()),
                ClasspathHelper.forClass(Object.class),
                new SubTypesScanner(false)
        );
        return reflections.getSubTypesOf(type);
    }

    @Programmatic
    @Override
    public <T> Set<Class<? extends T>> findSubTypesOfClasses(Class<T> type, String packagePrefix) {

        if(type == FixtureScript.class) {
            Set fixtureScriptTypes = AppManifest.Registry.instance().getFixtureScriptTypes();
            if (fixtureScriptTypes != null) {
                return fixtureScriptTypes;
            }
        }

        // no appManifest or not asking for FixtureScripts
        Vfs.setDefaultURLTypes(getUrlTypes());

        final Reflections reflections = new Reflections(
                ClasspathHelper.forClassLoader(Thread.currentThread().getContextClassLoader()),
                ClasspathHelper.forClass(Object.class),
                ClasspathHelper.forPackage(packagePrefix),
                new SubTypesScanner(false)
        );
        return reflections.getSubTypesOf(type);
    }

    // //////////////////////////////////////

    /**
     * Has <tt>public</tt> visibility only so can be reused by other services (including Isis runtime itself).
     */
    public static List<Vfs.UrlType> getUrlTypes() {
        return AppManifest.Registry.instance().getUrlTypes();
    }


}