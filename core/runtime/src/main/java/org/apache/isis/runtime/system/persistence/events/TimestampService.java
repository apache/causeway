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
package org.apache.isis.runtime.system.persistence.events;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.mixins.timestamp.HoldsUpdatedAt;
import org.apache.isis.applib.mixins.timestamp.HoldsUpdatedBy;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.user.UserService;

import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import lombok.val;

@Service
@Named("isisRuntime.timestampService")
@Log4j2
public class TimestampService {

    @EventListener(PreStoreEvent.class)
    public void onPreStore(PreStoreEvent event) {

        val persistableObject = event.getPersistableObject();

        if(persistableObject instanceof HoldsUpdatedBy) {
            ((HoldsUpdatedBy)persistableObject).setUpdatedBy(userService.getUser().getName());
        }
        
        if(persistableObject instanceof HoldsUpdatedAt) {
            ((HoldsUpdatedAt)persistableObject).setUpdatedAt(clockService.nowAsJavaSqlTimestamp());
        }
        
    }

    @Inject UserService userService;
    @Inject ClockService clockService;

}