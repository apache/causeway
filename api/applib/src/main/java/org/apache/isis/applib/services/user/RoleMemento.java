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

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.PropertyLayout;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

/**
 * Immutable serializable value held by {@link UserMemento}.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@DomainObject(
        nature = Nature.VIEW_MODEL,
        logicalTypeName = RoleMemento.LOGICAL_TYPE_NAME
)
@DomainObjectLayout(
        titleUiEvent = RoleMemento.TitleUiEvent.class
)
@Value
public class RoleMemento implements Serializable {

    public static class TitleUiEvent extends IsisModuleApplib.TitleUiEvent<RoleMemento> {}

    public static final String LOGICAL_TYPE_NAME = IsisModuleApplib.NAMESPACE + ".RoleMemento";

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
    public RoleMemento(
            final @NonNull String name,
            final @NonNull String description) {
        this.name = name;
        this.description = description;
    }

    public static class UiSubscriber {
        @Order(PriorityPrecedence.LATE)
        @EventListener(RoleMemento.TitleUiEvent.class)
        public void on(final RoleMemento.TitleUiEvent ev) {
            val roleMemento = ev.getSource();
            assert roleMemento != null;
            ev.setTitle(roleMemento.getName());
        }
    }

    @PropertyLayout(fieldSetId = "identity", sequence = "1")
    @Getter
    String name;

    @PropertyLayout(fieldSetId = "details", sequence = "1")
    @Getter
    String description;

}
