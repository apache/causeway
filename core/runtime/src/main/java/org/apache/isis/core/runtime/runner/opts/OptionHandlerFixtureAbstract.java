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

package org.apache.isis.core.runtime.runner.opts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.runtime.optionhandler.OptionHandlerAbstract;
import org.apache.isis.core.runtime.system.SystemConstants;

public abstract class OptionHandlerFixtureAbstract extends OptionHandlerAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(OptionHandlerFixtureAbstract.class);

    public static final String DATANUCLEUS_ROOT_KEY = ConfigurationConstants.ROOT + "persistor.datanucleus.";
    public static final String DATANUCLEUS_INSTALL_FIXTURES_KEY = DATANUCLEUS_ROOT_KEY + "install-fixtures";

    protected String fixtureClassName;

    @Override
    public void prime(final IsisConfigurationBuilder isisConfigurationBuilder) {
        if (fixtureClassName == null) {
            return;
        }
        prime(isisConfigurationBuilder, SystemConstants.FIXTURE_KEY, fixtureClassName);
        prime(isisConfigurationBuilder, DATANUCLEUS_INSTALL_FIXTURES_KEY, "true");
    }

    static void prime(IsisConfigurationBuilder isisConfigurationBuilder, String key, String value) {
        LOG.info("priming: {}={}", key, value);
        isisConfigurationBuilder.add(key, value);
    }

}
