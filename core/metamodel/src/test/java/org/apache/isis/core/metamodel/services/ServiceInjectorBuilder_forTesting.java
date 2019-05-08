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
package org.apache.isis.core.metamodel.services;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._With;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.metamodel.spec.InjectorMethodEvaluator;
import org.apache.isis.core.metamodel.specloader.InjectorMethodEvaluatorDefault;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * @deprecated Only introduced to support existing tests, don't use for new tests
 */
@Deprecated
@RequiredArgsConstructor(staticName="of")
public class ServiceInjectorBuilder_forTesting {

    private final JUnitRuleMockery2 mockeryContext;
    
    private ServiceRegistry serviceRegistry;
    private InjectorMethodEvaluator injectorMethodEvaluator;
    private boolean autowireSetters = true; 
    private boolean autowireInject = false;
    
    public ServiceInjectorBuilder_forTesting serviceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        return this;
    }
    
    public ServiceInjectorBuilder_forTesting injectorMethodEvaluator(InjectorMethodEvaluator injectorMethodEvaluator) {
        this.injectorMethodEvaluator = injectorMethodEvaluator;
        return this;
    }
    
    public ServiceInjectorBuilder_forTesting autowireSetters(boolean autowireSetters) {
        this.autowireSetters = autowireSetters;
        return this;
    }
    
    public ServiceInjectorBuilder_forTesting autowireInject(boolean autowireInject) {
        this.autowireInject = autowireInject;
        return this;
    }
    
    // -- BUILD
    
    public ServiceInjector build() {
        val injector = new ServiceInjectorDefault();
    
        injector.serviceRegistry = serviceRegistry;
        
        injector.injectorMethodEvaluator=
                _Context.computeIfAbsent(InjectorMethodEvaluator.class, 
                        __->_With.computeIfAbsent(injectorMethodEvaluator, 
                                this::defaultInjectorMethodEvaluatorDefault));
        
        injector.autowireSetters = autowireSetters; 
        injector.autowireInject = autowireInject;
        
        //injector.init(); 
        
      //TODO[2112] migrate to spring
        //mockeryContext.put(injector);
        
        return injector;
    }
    
    // -- HELPER
    
    private InjectorMethodEvaluator defaultInjectorMethodEvaluatorDefault() {
        return new InjectorMethodEvaluatorDefault();
    }
    
}
