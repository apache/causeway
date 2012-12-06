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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.isis.applib.fixtures.switchuser.SwitchUserService;
import org.apache.isis.applib.fixtures.switchuser.SwitchUserServiceAware;

/**
 * Sole purpose is to switch the current user while object fixtures are being
 * installed.
 * 
 * <p>
 * An alternative is to switch user using the
 * {@link AbstractFixture#switchUser(String, String...) switchUser} method.
 * 
 * <p>
 * Note that (unlike the otherwise similar {@link DateFixture}) the last user
 * switched to is <i>not</i> used as the logon fixture. If you want to
 * automatically logon as some user, use the {@link LogonFixture}.
 * 
 * @see DateFixture
 * @see LogonFixture
 */
public class SwitchUserFixture extends BaseFixture implements SwitchUserServiceAware {

    private final String username;
    private final List<String> roles;
    private SwitchUserService switchUserService;

    public SwitchUserFixture(final String username, final String... roles) {
        this(username, Lists.newArrayList(roles));
    }

    public SwitchUserFixture(final String username, final List<String> roles) {
        super(FixtureType.OTHER);
        this.username = username;
        this.roles = ImmutableList.copyOf(roles);
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }

    @Override
    public void install() {
        switchUserService.switchUser(username, roles.toArray(new String[] {}));
    }

    @Override
    public void setService(final SwitchUserService switchUserService) {
        this.switchUserService = switchUserService;
    }

}
