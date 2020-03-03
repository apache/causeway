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

package org.apache.isis.applib.security;

import org.apache.isis.applib.annotation.MemberOrder;

import lombok.Getter;

// tag::refguide[]
public final class RoleMemento {

    // end::refguide[]
    /**
     * Creates a new role with the specified name. Description is left blank.
     */
    public RoleMemento(final String name) {
        this(name, "");
    }

    /**
     * Creates a new role with the specified name and description.
     */
    public RoleMemento(final String name, final String description) {
        if (name == null) {
            throw new IllegalArgumentException("Name not specified");
        }
        this.name = name;
        if (description == null) {
            throw new IllegalArgumentException("Description not specified");
        }
        this.description = description;
    }

    // tag::refguide[]
    public String title() {
        return name;
    }

    @MemberOrder(sequence = "1.1")
    @Getter
    private final String name;

    @MemberOrder(sequence = "1.2")
    @Getter
    private final String description;

}
// end::refguide[]
