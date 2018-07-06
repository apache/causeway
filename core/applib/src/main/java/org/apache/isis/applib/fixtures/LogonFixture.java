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

package org.apache.isis.applib.fixtures;

import java.util.List;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.commons.internal.collections._Lists;

/**
 * Indicates that the demo or test should be run as the specified user, with the
 * specified roles.
 *
 * <p>
 * Note: this fixture does not in itself do anything (its {@link #install()} is
 * a no-op). However, if present in the fixture list then is &quot;noticed&quot;
 * by the framework, and is used to automatically logon when the framework is
 * booted (providing running in prototype or exploration, not in production).
 *
 * <p>
 * To change the user during the installation of fixtures, either use
 * {@link SwitchUserFixture}.
 *
 * @see SwitchUserFixture
 * @deprecated - use {@link FixtureScript} instead.
 */
@Deprecated
public class LogonFixture implements InstallableFixture {

    private final String username;
    private final List<String> roles;

    /**
     * @deprecated - use {@link FixtureScript} instead.
     */
    @Deprecated
    public LogonFixture(final String username, final String... roles) {
        this(username, _Lists.of(roles));
    }

    /**
     * @deprecated - use {@link FixtureScript} instead.
     */
    @Deprecated
    public LogonFixture(final String username, final List<String> roles) {
        this.username = username;
        this.roles = _Lists.unmodifiable(roles);
    }

    /**
     * @deprecated - use {@link FixtureScript} instead.
     */
    @Deprecated
    public String getUsername() {
        return username;
    }

    /**
     * @deprecated - use {@link FixtureScript} instead.
     */
    @Deprecated
    public List<String> getRoles() {
        return roles;
    }

    /**
     * @deprecated - use {@link FixtureScript} instead.
     */
    @Deprecated
    @Override
    public final void install() {
        // does nothing; see comments above.
    }

    /**
     * @deprecated - use {@link FixtureScript} instead.
     */
    @Deprecated
    @Override
    public FixtureType getType() {
        return FixtureType.OTHER;
    }

    /**
     * @deprecated - use {@link FixtureScript} instead.
     */
    @Deprecated
    @Override
    public String toString() {
        return "LogonFixture [user: " + getUsername() + ", roles: " + getRoles() + "]";
    }

}
