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
package org.apache.isis.extensions.commandlog.impl.mixins;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.extensions.commandlog.impl.IsisModuleExtCommandLogImpl;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;


/**
 * @since 2.0 {@index}
 */
@Collection(
    domainEvent = HasUsername_recentCommandsByUser.CollectionDomainEvent.class
)
@CollectionLayout(
    defaultView = "table"
)
public class HasUsername_recentCommandsByUser {

    public static class CollectionDomainEvent
            extends IsisModuleExtCommandLogImpl.CollectionDomainEvent<HasUsername_recentCommandsByUser, CommandJdo> { }

    private final HasUsername hasUsername;
    public HasUsername_recentCommandsByUser(final HasUsername hasUsername) {
        this.hasUsername = hasUsername;
    }

    @MemberOrder(name="user", sequence = "3")
    public List<CommandJdo> coll() {
        final String username = hasUsername.getUsername();
        return username != null
                ? commandServiceRepository.findRecentByUsername(username)
                : Collections.emptyList();
    }
    public boolean hideColl() {
        return hasUsername.getUsername() == null;
    }

    @Inject CommandJdoRepository commandServiceRepository;
}
