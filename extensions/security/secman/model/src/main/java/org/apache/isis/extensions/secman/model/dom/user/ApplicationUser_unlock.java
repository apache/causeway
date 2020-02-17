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
package org.apache.isis.extensions.secman.model.dom.user;

import javax.enterprise.inject.Model;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUser.UnlockDomainEvent;
import org.apache.isis.extensions.secman.api.user.ApplicationUserStatus;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = UnlockDomainEvent.class, 
        associateWith = "status",
        associateWithSequence = "1")
@ActionLayout(named="Enable") // symmetry with lock (disable)
@RequiredArgsConstructor
public class ApplicationUser_unlock {
    
    private final ApplicationUser holder;

    @Model
    public ApplicationUser act() {
        holder.setStatus(ApplicationUserStatus.ENABLED);
        return holder;
    }
    
    @Model
    public String disableAct() {
        return holder.getStatus() == ApplicationUserStatus.ENABLED ? "Status is already set to ENABLE": null;
    }
}
