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

package org.apache.isis.core.runtime.fixtures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

public class FixturesInstallerFromConfiguration extends FixturesInstallerAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(FixturesInstallerFromConfiguration.class);

    public static final String FIXTURES = ConfigurationConstants.ROOT + "fixtures";

    /**
     * @deprecated - just adds to the cognitive load...
     */
    @Deprecated
    private static final String FIXTURES_PREFIX = ConfigurationConstants.ROOT + "fixtures.prefix";

    public FixturesInstallerFromConfiguration(final IsisSessionFactory isisSessionFactory) {
        super(isisSessionFactory);
    }

    @Override
    protected void addFixturesTo(final FixturesInstallerDelegate delegate) {

        final FixtureConfig fixtureConfig = getFixtureConfig();

        try {
            boolean fixtureLoaded = false;
            for (final String element : fixtureConfig.getFixtures()) {
                final String fixtureFullyQualifiedName = fixtureConfig.getFixturePrefix() + element;
                LOG.info("  adding fixture {}", fixtureFullyQualifiedName);
                final Object fixture = InstanceUtil.createInstance(fixtureFullyQualifiedName);
                fixtureLoaded = true;
                delegate.addFixture(fixture);
            }
            if (!fixtureLoaded) {
                LOG.debug("No fixtures loaded from configuration");
            }
        } catch (final IllegalArgumentException | SecurityException e) {
            throw new IsisException(e);
        }
    }

    private static class FixtureConfig {

        // -- fixtures

        private String[] fixtures;

        String[] getFixtures() {
            return fixtures;
        }

        void setFixtures(String[] fixtures) {
            this.fixtures = fixtures;
        }

        // -- fixturePrefix

        private String fixturePrefix;
        String getFixturePrefix() {
            return fixturePrefix;
        }

        void setFixturePrefix(String fixturePrefix) {
            fixturePrefix = fixturePrefix == null ? "" : fixturePrefix.trim();
            if (fixturePrefix.length() > 0 && !fixturePrefix.endsWith(ConfigurationConstants.DELIMITER)) {
                fixturePrefix = fixturePrefix + ConfigurationConstants.DELIMITER;
            }

            this.fixturePrefix = fixturePrefix;
        }



    }

    private FixtureConfig getFixtureConfig() {
        final FixtureConfig fixtureConfig = new FixtureConfig();

        fixtureConfig.setFixtures(configuration.getList(FIXTURES));
        fixtureConfig.setFixturePrefix(configuration.getString(FIXTURES_PREFIX));

        return fixtureConfig;
    }

}
