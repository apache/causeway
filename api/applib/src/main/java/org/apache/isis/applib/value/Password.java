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
import java.util.Objects;

import org.apache.isis.applib.annotation.Value;

@Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.value.password.PasswordValueSemanticsProvider")
@lombok.Value
public class Password implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final String STARS = "********************";
    
    private final String password;

    public boolean checkPassword(final String password) {
        return Objects.equals(this.password, password);
    }

    @Override
    public String toString() {
        if (password == null) {
            return "";
        }
        return STARS;
    }
}
