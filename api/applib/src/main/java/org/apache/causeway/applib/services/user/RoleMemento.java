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
package org.apache.causeway.applib.services.user;

import java.io.Serializable;

import javax.inject.Named;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.PropertyLayout;

import lombok.*;

/**
 * Immutable serializable value held by {@link UserMemento}.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Named(RoleMemento.LOGICAL_TYPE_NAME)
@DomainObject(
        nature = Nature.VIEW_MODEL)
@DomainObjectLayout(
        titleUiEvent = RoleMemento.TitleUiEvent.class
)
@Value
public class RoleMemento implements Serializable {

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".RoleMemento";

    public static class TitleUiEvent extends CausewayModuleApplib.TitleUiEvent<RoleMemento> {}

    private static final long serialVersionUID = -3876856609238378274L;

    /**
     * Creates a new role with the specified name. Description is left blank.
     */
    public RoleMemento(final String name) {
        this(name, null);
    }

    /**
     * Creates a new role with the specified name and description.
     */
    @Builder
    public RoleMemento(
            final @NonNull String name,
            final String description) {
        this.name = name;
        this.description = description == null ? "" : description;
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

    /**
     * Excluded from {@link #equals(Object) equality} checks.
     */
    @EqualsAndHashCode.Exclude
    @PropertyLayout(fieldSetId = "details", sequence = "1")
    @Getter
    String description;

}
