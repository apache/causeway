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

package org.apache.isis.core.plugins.classdiscovery;

import java.util.List;

import org.apache.isis.commons.internal.context._Plugin;

public interface ClassDiscoveryPlugin {

    //TODO missing java-doc
    public ClassDiscovery discover(String packageNamePrefix);

    //TODO missing java-doc
    public ClassDiscovery discover(List<String> packageNamePrefixes);

    //TODO missing java-doc
    //TODO [ahuber] REVIEW how is this different from discover(String)
    public ClassDiscovery discoverFullscan(String packageNamePrefix);

    // -- LOOKUP

    public static ClassDiscoveryPlugin get() {
        return _Plugin.getOrElse(ClassDiscoveryPlugin.class,
                ambiguousPlugins->{
                    return _Plugin.pickAnyAndWarn(ClassDiscoveryPlugin.class, ambiguousPlugins);
                },
                ()->{
                    throw _Plugin.absenceNonRecoverable(ClassDiscoveryPlugin.class);
                });
    }

    //	// -- NOP IMPLEMENTATION
    //
    //	public static ClassDiscoveryPlugin nop() {
    //		return new ClassDiscoveryPlugin() {
    //
    //			private final Logger LOG = LoggerFactory.getLogger(ClassDiscoveryPlugin.class);
    //
    //			@Override
    //			public ClassDiscovery discoverFullscan(String packageNamePrefix) {
    //				return warn();
    //			}
    //
    //			@Override
    //			public ClassDiscovery discover(List<String> packageNamePrefixes) {
    //				return warn();
    //			}
    //
    //			@Override
    //			public ClassDiscovery discover(String packageNamePrefix) {
    //				return warn();
    //			}
    //
    //			private ClassDiscovery warn() {
    //				LOG.error("you need a ClassDiscoveryPlugin on your class path, class discovery will not work");
    //				return ClassDiscovery.empty();
    //			}
    //
    //		};
    //	}

}
