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
package org.apache.causeway.core.configtest;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.util.TestPropertyValues;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayModuleCoreConfig;

/**
 * <pre>
 * --add-exports org.hibernate.validator/org.hibernate.validator.internal.engine=spring.core
 * --add-exports org.hibernate.validator/org.hibernate.validator.internal.constraintvalidators.bv=org.apache.causeway.core.config
 * </pre>
 */
public record ConfigurationTester(
    /**
     * {@code TestPropertyValues.of("causeway.property=value")}
     */
    TestPropertyValues testPropertyValues) {

    public void test(final Consumer<CausewayConfiguration.Causeway> configConsumer) {
        testPropertyValues.applyToSystemProperties(()->{
            new ApplicationContextRunner()
                .withUserConfiguration(CausewayModuleCoreConfig.class)
                .run(ctx -> {
                    var config = ctx.getBean(CausewayConfiguration.Causeway.class);
                    configConsumer.accept(config);
                });
        });
    }

    public CausewayConfiguration.Causeway causeway() {
        final var causewayRef = new AtomicReference<CausewayConfiguration.Causeway>();
        testPropertyValues.applyToSystemProperties(()->{
            new ApplicationContextRunner()
                .withUserConfiguration(CausewayModuleCoreConfig.class)
                .run(ctx -> {
                    var causeway = ctx.getBean(CausewayConfiguration.Causeway.class);
                    causewayRef.set(causeway);
                });
        });
        return causewayRef.get();
    }

}
