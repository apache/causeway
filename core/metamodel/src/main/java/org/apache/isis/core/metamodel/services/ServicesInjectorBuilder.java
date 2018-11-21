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

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.metamodel.spec.InjectorMethodEvaluator;
import org.apache.isis.core.metamodel.specloader.InjectorMethodEvaluatorDefault;

import static org.apache.isis.commons.internal.base._With.computeIfAbsent;

public class ServicesInjectorBuilder {

    private final List<Object> services = new ArrayList<>();
    private IsisConfigurationBuilder configBuilder;
    private InjectorMethodEvaluator injectorMethodEvaluator;
    private boolean autowireSetters = false; 
    private boolean autowireInject = false;
    
    public ServicesInjectorBuilder addService(Object service) {
        services.add(service);
        return this;
    }
    
    public ServicesInjectorBuilder addServices(List<Object> services) {
        this.services.addAll(services);
        return this;
    }
    
    public ServicesInjectorBuilder configBuilder(IsisConfigurationBuilder configBuilder) {
        this.configBuilder = configBuilder;
        return this;
    }
    
    public ServicesInjectorBuilder injectorMethodEvaluator(InjectorMethodEvaluator injectorMethodEvaluator) {
        this.injectorMethodEvaluator = injectorMethodEvaluator;
        return this;
    }
    
    public ServicesInjectorBuilder autowireSetters(boolean autowireSetters) {
        this.autowireSetters = autowireSetters;
        return this;
    }
    
    public ServicesInjectorBuilder autowireInject(boolean autowireInject) {
        this.autowireInject = autowireInject;
        return this;
    }
    
    // -- BUILD
    
    public ServicesInjector build() {
        return new ServicesInjector(
                new ArrayList<>(services), 
//              computeIfAbsent(configBuilder, this::defaultConfigBuilder), 
                computeIfAbsent(injectorMethodEvaluator, this::defaultInjectorMethodEvaluatorDefault),
                autowireSetters,
                autowireInject
                );
    }
    
    // -- HELPER
    
    private IsisConfigurationBuilder defaultConfigBuilder() {
        return new IsisConfigurationBuilder();
    }
    
    private InjectorMethodEvaluator defaultInjectorMethodEvaluatorDefault() {
        return new InjectorMethodEvaluatorDefault();
    }


    
}
