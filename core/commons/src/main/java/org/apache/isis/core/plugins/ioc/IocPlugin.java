/*
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
package org.apache.isis.core.plugins.ioc;

import java.util.stream.Stream;

import javax.enterprise.inject.spi.CDIProvider;

import org.apache.isis.commons.internal.context._Plugin;

/**
 * 
 * @since 2.0.0-M2
 *
 */
public interface IocPlugin {

    // -- INTERFACE

    public CDIProvider getCDIProvider(Stream<Class<?>> discover);

    // -- LOOKUP

    public static IocPlugin get() {
        
        return _Plugin.getOrElse(IocPlugin.class,
                ambiguousPlugins->{
                    return _Plugin.pickAnyAndWarn(IocPlugin.class, ambiguousPlugins);
                },
                ()->{
                    throw _Plugin.absenceNonRecoverable(IocPlugin.class);
                });
    }
    

}
