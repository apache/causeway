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

import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.core.security.authentication.standard.SimpleAuthentication;

import lombok.Getter;
import lombok.NonNull;

public abstract class AuthenticationAbstract
implements Authentication, Serializable {

    private static final long serialVersionUID = 1L;

    protected static final String DEFAULT_AUTH_VALID_CODE = "";

    // -- FIELDS

    @Getter(onMethod_ = {@Override})
    private final @NonNull InteractionContext interactionContext;

    @Getter(onMethod_ = {@Override})
    private final @NonNull String validationCode;

    // -- CONSTRUCTOR

    protected AuthenticationAbstract(
            final @NonNull InteractionContext interactionContext,
            final @NonNull String validationCode) {

        this.interactionContext = interactionContext;
        this.validationCode = validationCode;
    }

    // -- WITHERS

    /**
     * Returns a copy with given {@code interactionContext}.
     * @param interactionContext
     */
    public Authentication withInteractionContext(final @NonNull InteractionContext interactionContext) {
        return new SimpleAuthentication(interactionContext, validationCode);
    }

    // -- TO STRING, EQUALS, HASHCODE

    private static final ToString<AuthenticationAbstract> toString = ToString
            .toString("name", AuthenticationAbstract::getUserName)
            .thenToString("code", AuthenticationAbstract::getValidationCode);

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
        return isEqualsTo((AuthenticationAbstract) obj);
    }

    private boolean isEqualsTo(final AuthenticationAbstract other) {
        if(!Objects.equals(this.getType(), other.getType())) {
            return false;
        }
        if(!Objects.equals(this.getValidationCode(), other.getValidationCode())) {
            return false;
        }
        if(!Objects.equals(this.getInteractionContext(), other.getInteractionContext())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getUserName().hashCode(); // propably good enough to hash on just the user's name
    }

}
