/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.plugins.classdiscovery.reflections;

import java.util.List;

import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.plugins.classdiscovery.ClassDiscovery;
import org.apache.isis.core.plugins.classdiscovery.ClassDiscoveryPlugin;

public class ClassDiscoveryPluginUsingReflections implements ClassDiscoveryPlugin {

    @Override
    public ClassDiscovery discover(String packageNamePrefix) {
        ReflectManifest.prepareDiscovery(); 	//TODO [ahuber] REVIEW why is this required?
        return ReflectDiscovery.of(packageNamePrefix);
    }

    @Override
    public ClassDiscovery discover(List<String> packageNamePrefixes) {
        ReflectManifest.prepareDiscovery();	//TODO [ahuber] REVIEW why is this required?
        return ReflectDiscovery.of(packageNamePrefixes);
    }

    @Override
    public ClassDiscovery discoverFullscan(String packageNamePrefix) {
        ReflectManifest.prepareDiscovery();	//TODO [ahuber] REVIEW why is this required?
        return ReflectDiscovery.of(
                ClasspathHelper.forClassLoader(_Context.getDefaultClassLoader()),
                ClasspathHelper.forClass(Object.class),
                ClasspathHelper.forPackage(packageNamePrefix),
                new SubTypesScanner(false)
                );
    }

}
