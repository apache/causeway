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
package org.apache.isis.testing.fixtures.applib.fixturespec;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.security.authentication.Authenticator;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.val;

@AutoConfigureOrder(OrderPrecedence.LATE)
@Configuration
public class FixtureScriptsSpecificationProviderAutoConfiguration  {

    @Bean("isis.testing.fixtures.FixtureScriptsSpecificationProviderDefault")
    @ConditionalOnMissingBean(FixtureScriptsSpecificationProvider.class)
    @Qualifier("Default")
    @Nullable FixtureScriptsSpecificationProvider fixtureScriptsSpecificationProvider(final IsisConfiguration isisConfiguration) {
        val fixturesConfig = isisConfiguration.getTesting().getFixtures().getFixtureScriptsSpecification();
        val builder = builder(fixturesConfig);
        if(builder == null) {
            return null;
        }
        builder.with(FixtureScripts.NonPersistedObjectsStrategy.valueOf(fixturesConfig.getNonPersistedObjectsStrategy().name()));
        builder.with(FixtureScripts.MultipleExecutionStrategy.valueOf(fixturesConfig.getMultipleExecutionStrategy().name()));
        builder.withRecreate((Class) fixturesConfig.getRecreate());
        builder.withRunScriptDefault((Class) fixturesConfig.getRunScriptDefault());

        return () -> builder.build();
    }

    private FixtureScriptsSpecification.Builder builder(IsisConfiguration.Testing.Fixtures.FixtureScriptsSpecification fixturesConfig) {
        val contextClass = fixturesConfig.getContextClass();
        if(contextClass != null) {
            return FixtureScriptsSpecification.builder(contextClass);
        }
        val packagePrefix = fixturesConfig.getPackagePrefix();
        if(packagePrefix != null) {
            return FixtureScriptsSpecification.builder(packagePrefix);
        }
        return null;
    }

}
