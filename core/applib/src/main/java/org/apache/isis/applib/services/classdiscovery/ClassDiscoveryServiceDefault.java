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

import java.util.Collections;
import java.util.Set;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.plugins.classdiscovery.ClassDiscoveryPlugin;

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
public class ClassDiscoveryServiceDefault
extends AbstractService
implements ClassDiscoveryService {

    @Override
    public <T> Set<Class<? extends T>> findSubTypesOfClasses(Class<T> type, String packageNamePrefix) {

        if(type == FixtureScript.class) {
            return getFixtureScriptTypes();
        }

        // no appManifest or not asking for FixtureScripts
        return ClassDiscoveryPlugin.get().discoverFullscan(packageNamePrefix).getSubTypesOf(type);
    }

    // -- HELPER

    private static <T> Set<Class<? extends T>> getFixtureScriptTypes() {
        Set<?> fixtureScriptTypes = AppManifest.Registry.instance().getFixtureScriptTypes();
        if (fixtureScriptTypes != null) {
            return _Casts.uncheckedCast(fixtureScriptTypes);
        }
        return Collections.emptySet();
    }


}