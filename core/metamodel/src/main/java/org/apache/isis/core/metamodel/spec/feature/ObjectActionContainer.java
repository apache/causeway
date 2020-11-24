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

package org.apache.isis.core.metamodel.spec.feature;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

public interface ObjectActionContainer {

    /**
     * Returns the action of the specified type with the specified signature.
     */
    Optional<ObjectAction> getObjectAction(ActionType type, String id, Can<ObjectSpecification> parameters);

    /**
     * Get the action object represented by the specified identity string.
     *
     * <p>
     * The identity string can be either fully specified with parameters (as per
     * {@link Identifier#toNameParmsIdentityString()} or in abbreviated form (
     * {@link Identifier#toNameIdentityString()}).
     *
     * @see #getObjectAction(String)
     */
    Optional<ObjectAction> getObjectAction(ActionType type, String id);
    
    default ObjectAction getObjectActionElseFail(ActionType type, String id) {
        return getObjectAction(type, id)
                .orElseThrow(()->_Exceptions.noSuchElement("type=%s id=%s", type, id));  
    }
    

    /**
     * Get the action object represented by the specified identity string,
     * irrespective of {@link ActionType}.
     *
     * <p>
     * The identity string can be either fully specified with parameters (as per
     * {@link Identifier#toNameParmsIdentityString()} or in abbreviated form (
     * {@link Identifier#toNameIdentityString()}).
     *
     * @see #getObjectAction(ActionType, String)
     */
    Optional<ObjectAction> getObjectAction(String id);
    
    default ObjectAction getObjectActionElseFail(String id) {
        return getObjectAction(id)
                .orElseThrow(()->_Exceptions.noSuchElement("id=%s", id));  
    }

    default Stream<ObjectAction> streamObjectActions(MixedIn contributed) {
        return streamObjectActions(ActionType.ALL, contributed);
    }

    /**
     * Returns an array of actions of the specified type, including or excluding
     * contributed actions as required.
     */
    Stream<ObjectAction> streamObjectActions(ActionType type, MixedIn contributee);

    default Stream<ObjectAction> streamObjectActions(ImmutableEnumSet<ActionType> types, MixedIn contributee) {
        return stream(types)
                .flatMap(type->streamObjectActions(type, contributee));
    }


}
