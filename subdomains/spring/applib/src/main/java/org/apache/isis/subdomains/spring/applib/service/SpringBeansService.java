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
package org.apache.isis.subdomains.spring.applib.service;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;

import lombok.experimental.UtilityClass;

/**
 * Borrowed from BeansEndpoint.
 */
@Service
@Named("isisExtSpring.SpringBeansService")
@Order(OrderPrecedence.MIDPOINT)
@Qualifier("Default")
public class SpringBeansService {

    private final ConfigurableApplicationContext context;

    public SpringBeansService(final ConfigurableApplicationContext context) {
        this.context = context;
    }

    public Map<String, ContextBeans> beans() {
        Map<String, ContextBeans> contexts = new HashMap<>();

        for(ConfigurableApplicationContext context = this.context; context != null; context = Util.getConfigurableParent(context)) {
            contexts.put(context.getId(), ContextBeans.describing(context));
        }

        return contexts;
    }

    @UtilityClass
    static
    class Util {

        static ConfigurableApplicationContext getConfigurableParent(final ConfigurableApplicationContext context) {
            ApplicationContext parent = context.getParent();
            return parent instanceof ConfigurableApplicationContext ? (ConfigurableApplicationContext)parent : null;
        }
    }
}
