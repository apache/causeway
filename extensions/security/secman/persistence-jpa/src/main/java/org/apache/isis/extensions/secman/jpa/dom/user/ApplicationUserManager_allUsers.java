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
package org.apache.isis.extensions.secman.jpa.dom.user;

import java.util.Collection;

import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.extensions.secman.model.dom.user.ApplicationUserManager;

import lombok.RequiredArgsConstructor;

@org.apache.isis.applib.annotation.Collection
@RequiredArgsConstructor
public class ApplicationUserManager_allUsers
extends org.apache.isis.extensions.secman.model.dom.user.ApplicationUserManager_allUsers<ApplicationUser>{
    
    @SuppressWarnings("unused")
    private final ApplicationUserManager target;
    
    @MemberSupport
    public Collection<ApplicationUser> coll() {
        return super.doColl();        
    }

}
