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

package org.apache.isis.core.security.authentication.standard;

import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.core.security.authentication.AuthenticationSessionAbstract;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class SimpleSession extends AuthenticationSessionAbstract {

    private static final long serialVersionUID = 1L;
    
    // -- FACTORIES
    
    public static SimpleSession of( 
            final @NonNull UserMemento user,
            final @NonNull String validationCode) {
        return new SimpleSession(VirtualClock.system(), user, validationCode);
    }
    
    public static SimpleSession of(
            final @NonNull VirtualClock clock, 
            final @NonNull UserMemento user,
            final @NonNull String validationCode) {
        return new SimpleSession(clock, user, validationCode);
    }
    
    public static SimpleSession validOf( 
            final @NonNull UserMemento user) {
        return of(user, AuthenticationSessionAbstract.DEFAULT_AUTH_VALID_CODE);
    }
    
    public static SimpleSession validOf(
            final @NonNull VirtualClock clock, 
            final @NonNull UserMemento user) {
        return of(clock, user, AuthenticationSessionAbstract.DEFAULT_AUTH_VALID_CODE);
    }
    
    // -- CONSTRUCTOR
    
    public SimpleSession(
            final @NonNull VirtualClock clock, 
            final @NonNull UserMemento user, 
            final @NonNull String validationCode) {
        super(clock, user, validationCode);
    }

    @Getter @Setter
    private Type type = Type.DEFAULT;


    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final SimpleSession other = (SimpleSession) obj;
        return equals(other);
    }

    public boolean equals(final SimpleSession other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        return getUserName().equals(other.getUserName());
    }

    @Override
    public int hashCode() {
        return getUserName().hashCode();
    }

}
