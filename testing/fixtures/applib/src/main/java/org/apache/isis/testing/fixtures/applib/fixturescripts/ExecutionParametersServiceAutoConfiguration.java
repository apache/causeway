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
package org.apache.isis.testing.fixtures.applib.fixturescripts;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;


/**
 * Provides a fallback implementation of {@link ExecutionParametersService} if
 * none has been provided explicitly by the application itself.
 *
 * @since 2.0 {@index}
 */
@AutoConfigureOrder(OrderPrecedence.LATE)
@Configuration
public class ExecutionParametersServiceAutoConfiguration {


    /**
     * Returns an implementation of {@link ExecutionParametersService} that
     * simply instantiates {@link ExecutionParameters} with the provided
     * parameters (so that the latter parses the parameters and presents them
     * to {@link FixtureScripts} for inclusion within {@link org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript.ExecutionContext}.
     *
     * @return
     */
    @Bean
    @Named("isis.testing.fixtures.ExecutionParametersServiceDefault")
    @Order(OrderPrecedence.LATE)
    @Primary
    @Qualifier("Default")
    @ConditionalOnMissingBean(ExecutionParametersService.class)
    public ExecutionParametersService executionParametersService() {
        return ExecutionParametersDefault::new;
    }

}
