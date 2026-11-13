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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import org.apache.causeway.commons.collections.Can;

import lombok.extern.slf4j.Slf4j;

/**
 * @since 2.0 {@index}
 */
@Slf4j
public record BeanDescriptor(
    String beanName,
    Can<String> aliases,
    String scope,
    Class<?> type,
    String resource) {

    static BeanDescriptor of(
            final String beanName,
            final ConfigurableApplicationContext context) {

        final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        final BeanDefinition definition = beanFactory.getBeanDefinition(beanName);

        var aliases = Can.ofArray(beanFactory.getAliases(beanName));

        var scope = definition.getScope();
        scope = StringUtils.hasText(scope) ? scope : "singleton";

        var type = beanFactory.getType(beanName);
        var resource = definition.getResourceDescription();

        return new BeanDescriptor(beanName, aliases, scope, type, resource);
    }

}
