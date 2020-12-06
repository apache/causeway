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
package org.apache.isis.applib.services.user;

import org.apache.isis.applib.services.iactn.ExecutionContext;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import javax.annotation.Nullable;
import java.util.Optional;

// tag::refguide[]
public interface UserService {

    // -- INTERFACE
    
    // end::refguide[]
    /**
     * Optionally gets the details about the current user, 
     * based on whether an {@link ExecutionContext} can be found with the current thread's context.
     */
    // tag::refguide[]
    Optional<UserMemento> currentUser();    // <.>

    /**
     * Gets the details about the current user.
     * @apiNote for backward compatibility
     */
    @Nullable
    default UserMemento getUser() {
        return currentUser().orElse(null);
    }
    // end::refguide[]
    
    // -- UTILITIES
    
    /**
     * Gets the details about the current user.
     * @throws IllegalStateException if no {@link ExecutionContext} can be found with the current thread's context.
     */
    // tag::refguide[]
    default UserMemento currentUserElseFail() {              // <.>
        // end::refguide[]
        return currentUser()
                .orElseThrow(()->_Exceptions.illegalState("Current thread has no ExecutionContext."));
    }
    
    // end::refguide[]
    /**
     * Optionally gets the the current user's name, 
     * based on whether an {@link ExecutionContext} can be found with the current thread's context.
     */
    // tag::refguide[]
    default Optional<String> currentUserName() {    // <.>
        return currentUser()
                .map(UserMemento::getName);
    }
    
    default String currentUserNameElseNobody() {    // <.>
        return currentUserName()
                .orElse("Nobody");
    }

}
// end::refguide[]
