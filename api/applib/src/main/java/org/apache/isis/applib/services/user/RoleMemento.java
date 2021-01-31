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

import java.io.Serializable;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberOrder;

import lombok.Getter;
import lombok.Value;

/**
 * Immutable serializable value held by {@link UserMemento}.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@DomainObject(objectType = "isis.applib.RoleMemento")
@Value
public final class RoleMemento implements Serializable {

    private static final long serialVersionUID = -3876856609238378274L;

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
