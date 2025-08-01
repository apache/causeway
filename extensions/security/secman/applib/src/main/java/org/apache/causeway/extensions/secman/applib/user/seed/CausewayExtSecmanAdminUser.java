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
package org.apache.causeway.extensions.secman.applib.user.seed;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.Secman;
import org.apache.causeway.extensions.secman.applib.tenancy.seed.GlobalTenancy;
import org.apache.causeway.extensions.secman.applib.user.dom.AccountType;
import org.apache.causeway.extensions.secman.applib.user.fixtures.AbstractUserAndRolesFixtureScript;

/**
 * Sets up a user, as defined in
 * @since 2.0 {@index}
 */
public class CausewayExtSecmanAdminUser extends AbstractUserAndRolesFixtureScript {

    public CausewayExtSecmanAdminUser(final Secman config, final String... roleNames) {
        super(
                config.seed().admin().userName(),
                config.seed().admin().password(),
                null,
                GlobalTenancy.TENANCY_PATH,
                AccountType.LOCAL,
                Can.of(
                        config.seed().admin().roleName(),
                        config.seed().regularUser().roleName()
                ).addAll(
                        Can.of(roleNames)
                )
        );
    }

}
