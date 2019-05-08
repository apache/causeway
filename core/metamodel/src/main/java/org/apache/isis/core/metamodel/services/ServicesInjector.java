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
package org.apache.isis.core.metamodel.services;

import java.util.List;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.metamodel.spec.InjectorMethodEvaluator;

/**
 * @deprecated JUnit tests need to be migrated. 
 *
 */
@Deprecated
public class ServicesInjector implements ApplicationScopedComponent {
    private static final String KEY_SET_PREFIX = "isis.services.injector.setPrefix";
    private static final String KEY_INJECT_PREFIX = "isis.services.injector.injectPrefix";


    // -- BUILDER
    
    public static ServicesInjectorBuilder builder() {
        final IsisConfiguration config = _Config.getConfiguration();
        return new ServicesInjectorBuilder()
                .addService(config)
                .autowireSetters(config.getBoolean(KEY_SET_PREFIX, true))
                .autowireInject(config.getBoolean(KEY_INJECT_PREFIX, true));
    }
    
    public static ServicesInjectorBuilder builderForTesting() {
        return builder()
                .autowireSetters(true)
                .autowireInject(false);
    }
    
    // -- CONSTRUCTOR (NOT EXPOSED)

    ServicesInjector(
            final List<Object> services,
            final InjectorMethodEvaluator injectorMethodEvaluator,
            final boolean autowireSetters,
            final boolean autowireInject
            ) {
        
    }

    
}
