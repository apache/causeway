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
package org.apache.causeway.core.transaction.changetracking.events;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.mixins.updates.OnUpdatedAt;
import org.apache.causeway.applib.mixins.updates.OnUpdatedBy;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.core.transaction.CausewayModuleCoreTransaction;

@Service
@Named(TimestampService.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class TimestampService {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleCoreTransaction.NAMESPACE + ".TimestampService";

    @Inject private UserService userService;
    @Inject private ClockService clockService;

    @EventListener(PreStoreEvent.class)
    public void onPreStore(PreStoreEvent event) {

        var persistableObject = event.persistableObject();

        if(persistableObject instanceof OnUpdatedBy) {
            ((OnUpdatedBy)persistableObject).setUpdatedBy(userService.currentUserNameElseNobody());
        }

        if(persistableObject instanceof OnUpdatedAt) {
            ((OnUpdatedAt)persistableObject).setUpdatedAt(clockService.getClock().nowAsJavaSqlTimestamp());
        }

    }

}
