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
package org.apache.isis.core.runtimeservices.publish;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.publish.ChangingEntitiesListener;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.runtime.persistence.changetracking.ChangingEntitiesDispatcher;
import org.apache.isis.core.runtime.persistence.changetracking.HasEnlistedChangingEntities;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Wrapper around {@link org.apache.isis.applib.services.audit.ChangingEntitiesListener}.
 */
@Service
@Named("isisRuntime.ChangingEntitiesDispatcher")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//@Log4j2
public class ChangingEntitiesDispatcherDefault implements ChangingEntitiesDispatcher {
    
    private final List<ChangingEntitiesListener> changingEntitiesListenersNullable;
    private final ClockService clockService;
    private final UserService userService;
    
    private Can<ChangingEntitiesListener> changingEntitiesListeners;
    
    @PostConstruct
    public void init() {
        changingEntitiesListeners = Can.ofCollection(changingEntitiesListenersNullable);
    }

    public void dispatchChangingEntities(HasEnlistedChangingEntities hasEnlistedChangingEntities) {

        if(!canDispatch()) {
            return;
        }
        
        val changingEntities = hasEnlistedChangingEntities.getChangingEntities(clockService, userService);
        
        if(changingEntities == null) {
            return;
        }
        
        for (val changingEntitiesListener : changingEntitiesListeners) {
            changingEntitiesListener.onEntitiesChanging(changingEntities);
        }
    }
    
    // -- HELPER
    
    private boolean canDispatch() {
        return changingEntitiesListeners.isNotEmpty();
    }




}
