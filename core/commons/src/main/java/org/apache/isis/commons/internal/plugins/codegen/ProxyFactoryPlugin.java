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
package org.apache.isis.commons.internal.plugins.codegen;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.context._Plugin;

public interface ProxyFactoryPlugin {

    // -- INTERFACE

    public <T> ProxyFactory<T> factory(
            Class<T> base,
            @Nullable Class<?>[] interfaces,
            @Nullable Class<?>[] constructorArgTypes);


    // -- LOOKUP

    public static ProxyFactoryPlugin get() {
        return _Plugin.getOrElse(ProxyFactoryPlugin.class,
                ambiguousPlugins->{
                    return _Plugin.pickAnyAndWarn(ProxyFactoryPlugin.class, ambiguousPlugins);
                },
                ()->{
                    throw _Plugin.absenceNonRecoverable(ProxyFactoryPlugin.class);
                });
    }
}
