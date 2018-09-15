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

package org.apache.isis.core.runtime.authentication;

import java.util.Collections;
import java.util.List;

import org.apache.isis.commons.internal.collections._Lists;

public abstract class AuthenticationRequestAbstract implements AuthenticationRequest {

    private final String name;
    private final List<String> roles = _Lists.newArrayList();

    public AuthenticationRequestAbstract(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    @Override
    public void setRoles(final List<String> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

}
