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
package org.apache.isis.core.security.authentication;

import java.io.Serializable;
import java.util.Objects;

import org.apache.isis.applib.services.iactn.ExecutionContext;
import org.apache.isis.applib.util.ToString;

import lombok.Getter;
import lombok.NonNull;

public abstract class AuthenticationSessionAbstract 
implements AuthenticationSession, Serializable {

    private static final long serialVersionUID = 1L;

    protected static final String DEFAULT_AUTH_VALID_CODE = "";
    
    // -- Constructor, fields

    @Getter(onMethod_ = {@Override})  
    private final @NonNull ExecutionContext executionContext;
    
    @Getter(onMethod_ = {@Override})
    private final @NonNull String validationCode;

    @Getter(onMethod_ = {@Override})
    private final @NonNull MessageBroker messageBroker;
    
    public AuthenticationSessionAbstract(
            @NonNull final ExecutionContext executionEnvironment,
            @NonNull final String validationCode) {

        this.executionContext = executionEnvironment;
        this.validationCode = validationCode;
        this.messageBroker = new MessageBroker();
        // nothing to do
    }

    // -- TO STRING, EQUALS, HASHCODE

    private static final ToString<AuthenticationSessionAbstract> toString = ToString
            .toString("name", AuthenticationSessionAbstract::getUserName)
            .thenToString("code", AuthenticationSessionAbstract::getValidationCode);

    @Override
    public String toString() {
        return toString.toString(this);
    }
    
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
        return isEqualsTo((AuthenticationSessionAbstract) obj);
    }

    private boolean isEqualsTo(final AuthenticationSessionAbstract other) {
        if(!Objects.equals(this.getValidationCode(), other.getValidationCode())) {
            return false;
        }
        if(!Objects.equals(this.getExecutionContext(), other.getExecutionContext())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getUserName().hashCode(); // its good enough to hash on just the user's name
    }

}
