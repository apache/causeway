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
package org.apache.isis.core.metamodel.services.command;

import java.util.UUID;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.cmd.v2.ActionDto;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.PropertyDto;

/**
 * Used to serialize a command, eg. such that it can be persisted and then executed 
 * at some later time or even at some external system.
 */
public interface CommandDtoFactory {

    /**
     * @return a DTO that represents the intention to invoke an action on a
     *         target object (or possibly many targets, for bulk actions),
     *         or to edit a property.  If an action, it be either a
     *         mixin action or a contributed action.
     */
    CommandDto asCommandDto(
            final UUID uniqueId,
            final Can<ManagedObject> targetAdapters,
            final ObjectAction objectAction,
            final Can<ManagedObject> argAdapters);

    /**
     * @return a DTO that represents the intention to edit (set or clear) a
     *         property on a target (or possibly many targets, for symmetry
     *         with actions).
     */
    CommandDto asCommandDto(
            final UUID uniqueId,
            final Can<ManagedObject> targetAdapters,
            final OneToOneAssociation association,
            final ManagedObject valueAdapterOrNull);

    void addActionArgs(
            final ObjectAction objectAction,
            final ActionDto actionDto,
            final Can<ManagedObject> argAdapters);

    void addPropertyValue(
            final OneToOneAssociation property,
            final PropertyDto propertyDto,
            final ManagedObject valueAdapter);

}
