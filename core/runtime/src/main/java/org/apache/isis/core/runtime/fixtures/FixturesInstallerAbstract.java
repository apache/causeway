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

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

public abstract class FixturesInstallerAbstract {

    private final FixturesInstallerDelegate delegate;
    protected final IsisConfiguration configuration;

    public FixturesInstallerAbstract(final IsisSessionFactory isisSessionFactory) {
        delegate = new FixturesInstallerDelegate(isisSessionFactory);
        configuration = isisSessionFactory.getConfiguration();
    }

    public void installFixtures() {
        addFixturesTo(delegate);
        delegate.installFixtures();
    }

    /**
     * Add fixtures to {@link FixturesInstallerDelegate#addFixture(Object)
     * delegate}; these are then installed.
     */
    protected abstract void addFixturesTo(FixturesInstallerDelegate delegate);

    public LogonFixture getLogonFixture() {
        return delegate.getLogonFixture();
    }

    public List<Class<?>> getTypes() {
        return Collections.emptyList();
    }


}
