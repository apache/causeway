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

package org.apache.isis.applib.value;

import java.io.Serializable;

import org.apache.isis.applib.annotation.Value;

@Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.value.password.PasswordValueSemanticsProvider")
public class Password implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String STARS = "********************";
    private final String password;

    public Password(final String password) {
        this.password = password;
    }

    public boolean checkPassword(final String password) {
        return this.password.equals(password);
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        return other.getClass() == this.getClass() && equals((Password) other);
    }

    public boolean equals(final Password other) {
        final String otherPassword = other.getPassword();
        if (getPassword() == null && otherPassword == null) {
            return true;
        }
        if (getPassword() == null || otherPassword == null) {
            return false;
        }
        return getPassword().equals(otherPassword);
    }

    @Override
    public int hashCode() {
        return password != null ? password.hashCode() : 0;
    }

    @Override
    public String toString() {
        if (password == null) {
            return "";
        }
        return STARS.substring(0, Math.min(STARS.length(), password.length()));
    }
}
