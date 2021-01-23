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

import javax.annotation.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ActionType;

public interface ObjectActionContainer {

    // -- ACTION LOOKUP (INHERITANCE CONSIDERED)
    
    /**
     * Same as {@link #getDeclaredAction(String, ActionType)}, but also considering any inherited object members.
     * @param id
     * @param type
     * 
     * @implSpec If not found on the current 'type' search for the 'nearest' match in super-types, 
     * and if nothing found there, search the interfaces. Special care needs to be taken, as the
     * {@link ActionType} might be redeclared when inheriting from a super-type or interface.  
     */
    Optional<ObjectAction> getAction(String id, @Nullable ActionType type);
    
    default ObjectAction getActionElseFail(String id, @Nullable ActionType type) {
        return getAction(id, type)
                .orElseThrow(()->_Exceptions.noSuchElement("id=%s type=%s", 
                        id, 
                        type==null ? "any" : type.name()));
    }


    default Optional<ObjectAction> getAction(String id) {
        return getAction(id, null);
    }
    
    default ObjectAction getActionElseFail(String id) {
        return getActionElseFail(id, null);
    }
    
    
    // -- ACTION LOOKUP, DECLARED ACTIONS (NO INHERITANCE CONSIDERED)
    
    /**
     * Get the action object represented by the specified identity string.
     * <p>
     * The identity string can be either fully specified with parameters (as per
     * {@link Identifier#toNameParmsIdentityString()} or in abbreviated form (
     * {@link Identifier#toNameIdentityString()}).
     *
     * @see #getDeclaredAction(String)
     */
    Optional<ObjectAction> getDeclaredAction(String id, @Nullable ActionType type);

    /**
     * Shortcut to {@link #getDeclaredAction(String, null)}, meaning where action type is <i>any</i>.
     * @see #getDeclaredAction(String, ActionType)
     */
    default Optional<ObjectAction> getDeclaredAction(String id) {
        return getDeclaredAction(id, null);
    }

    // -- ACTION STREAM (W/ INHERITANCE)
    
    Stream<ObjectAction> streamActions(ImmutableEnumSet<ActionType> types, MixedIn contributed);
    
    default Stream<ObjectAction> streamActions(ActionType type, MixedIn contributed) {
        return streamActions(ImmutableEnumSet.of(type), contributed);
    }
    
    default Stream<ObjectAction> streamActions(MixedIn contributed) {
        return streamActions(ActionType.ANY, contributed);
    }
    
    // -- ACTION STREAM (NO INHERITANCE)

    /**
     * Returns an array of actions of the specified type, including or excluding
     * contributed actions as required.
     */
    Stream<ObjectAction> streamDeclaredActions(ImmutableEnumSet<ActionType> types, MixedIn contributed);

    default Stream<ObjectAction> streamDeclaredActions(ActionType type, MixedIn contributed) {
        return streamDeclaredActions(ImmutableEnumSet.of(type), contributed);
    }
    
    default Stream<ObjectAction> streamDeclaredActions(MixedIn contributed) {
        return streamDeclaredActions(ActionType.ANY, contributed);
    }

    
}
