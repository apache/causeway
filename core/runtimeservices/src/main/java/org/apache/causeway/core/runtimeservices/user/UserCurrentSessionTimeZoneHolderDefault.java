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
package org.apache.causeway.core.runtimeservices.user;

import java.time.ZoneId;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.keyvaluestore.KeyValueSessionStore;
import org.apache.causeway.applib.services.user.UserCurrentSessionTimeZoneHolder;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.NonNull;

@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".UserCurrentSessionTimeZoneHolderDefault")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
public class UserCurrentSessionTimeZoneHolderDefault
implements UserCurrentSessionTimeZoneHolder {

    private static final String SESSION_KEY_ZONE_ID =
            UserCurrentSessionTimeZoneHolderDefault.class.getName() + "#zoneId";

    @Inject private Optional<KeyValueSessionStore> keyValueSessionStore = Optional.empty();

    @Override
    public void setUserTimeZone(final @NonNull ZoneId zoneId) {
        keyValueSessionStore
            .ifPresent(store->store.put(SESSION_KEY_ZONE_ID, zoneId));
    }

    @Override
    public Optional<ZoneId> getUserTimeZone() {
        return keyValueSessionStore
            .flatMap(store->store.lookupAs(SESSION_KEY_ZONE_ID, ZoneId.class));
    }

    @Override
    public void clearUserTimeZone() {
        keyValueSessionStore
            .ifPresent(store->store.clear(SESSION_KEY_ZONE_ID));
    }

}
