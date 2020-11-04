/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.isis.extensions.cors.impl;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.servlet.Filter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IsisModuleExtCorsImplTest {

    @Test
    void ensureUrlPatternsContainRestfulAndWildcard() {
        //given
        final MetaModelContext metaModelContext = MetaModelContext_forTesting.buildDefault();
        final ConfigurableEnvironment environment = metaModelContext.getConfiguration().getEnvironment();
        final IsisConfiguration configuration = new IsisConfiguration(environment);
        final Map<String,String> map = new HashMap<String,String>();
        configuration.setIsisSettings(map);
        final IsisModuleExtCorsImpl classUnderTest = new IsisModuleExtCorsImpl(configuration);
        // when
        final FilterRegistrationBean<Filter> filterRegistration = classUnderTest.corsFilterRegistration();
        // then
        final Collection<String> urlPatterns = filterRegistration.getUrlPatterns();
        assertTrue(urlPatterns.contains("/restful/*"));
    }
}