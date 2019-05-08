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

import java.util.List;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixturesInstallerFromConfiguration extends FixturesInstallerAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(FixturesInstallerFromConfiguration.class);

    public FixturesInstallerFromConfiguration() {
        super();
    }

    protected void addFixturesTo(final FixturesInstallerDelegate delegate) {
        
        final List<Class<? extends FixtureScript>> fixtureClasses = 
                configuration.getAppManifest().getFixtures();

        try {
            boolean fixtureLoaded = false;
            for (final Class<? extends FixtureScript> fixtureClass : fixtureClasses) {
                
                LOG.info("  adding fixture {}", fixtureClass.getName());
                final Object fixture = InstanceUtil.createInstance(fixtureClass);
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


}
