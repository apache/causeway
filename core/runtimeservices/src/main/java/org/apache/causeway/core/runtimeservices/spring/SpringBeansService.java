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
package org.apache.causeway.core.runtimeservices.spring;

import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.RequiredArgsConstructor;

/**
 * Borrowed from BeansEndpoint.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".SpringBeansService")
@jakarta.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SpringBeansService {

    private final ConfigurableApplicationContext context;

    public Map<String, ContextBeans> beansByContextId() {
        var contexts = new HashMap<String, ContextBeans>();
        for(ConfigurableApplicationContext context = this.context; context != null; context = getConfigurableParent(context)) {
            contexts.put(context.getId(), ContextBeans.describing(context));
        }
        return contexts;
    }

    static ConfigurableApplicationContext getConfigurableParent(final ConfigurableApplicationContext context) {
        return context.getParent() instanceof ConfigurableApplicationContext cac 
            ? cac 
            : null;
    }

}
