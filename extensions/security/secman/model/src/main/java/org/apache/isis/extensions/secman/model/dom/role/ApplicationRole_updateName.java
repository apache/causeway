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
package org.apache.isis.extensions.secman.model.dom.role;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.ApplicationRole.UpdateNameDomainEvent;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = UpdateNameDomainEvent.class, 
        associateWith = "name")
@ActionLayout(sequence = "1")
@RequiredArgsConstructor
public class ApplicationRole_updateName {
    
    private final ApplicationRole target;
    
    public ApplicationRole act(
            @Parameter(maxLength = ApplicationRole.MAX_LENGTH_NAME) 
            @ParameterLayout(named="Name", typicalLength = ApplicationRole.TYPICAL_LENGTH_NAME)
            final String name) {
        
        target.setName(name);
        return target;
    }

    public String default0Act() {
        return target.getName();
    }

}
