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
package org.apache.isis.core.metamodel.services.registry;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName="of")
public class ServiceRegistryBuilder_forTesting {

    private final List<Object> services = new ArrayList<>();
    private final JUnitRuleMockery2 mockeryContext;
    
    public ServiceRegistryBuilder_forTesting addService(Object service) {
        services.add(service);
        return this;
    }
    
    public ServiceRegistryBuilder_forTesting addServices(List<Object> services) {
        this.services.addAll(services);
        return this;
    }
    
    // -- BUILD
    
    public ServiceRegistry build() {
        val registry = new ServiceRegistryDefault();
        
      //TODO[2112] migrate to spring
//        stream(services)
//        .forEach(mockeryContext::put);
//        
//        mockeryContext.put(registry);
        
        return registry;
    }
    
}
