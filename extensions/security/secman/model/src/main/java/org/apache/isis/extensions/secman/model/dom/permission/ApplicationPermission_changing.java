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
package org.apache.isis.extensions.secman.model.dom.permission;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission.ChangingDomainEvent;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;

import lombok.RequiredArgsConstructor;

@Action(domainEvent = ChangingDomainEvent.class, associateWith = "mode")
@RequiredArgsConstructor
public class ApplicationPermission_changing {

    private final ApplicationPermission holder;

    //@MemberOrder(name = "Mode", sequence = "2")
    public ApplicationPermission act() {
        holder.setMode(ApplicationPermissionMode.CHANGING);
        return holder;
    }
    
    public String disableAct() {
        return holder.getMode() == ApplicationPermissionMode.CHANGING ? "Mode is already set to CHANGING": null;
    }
    

}
