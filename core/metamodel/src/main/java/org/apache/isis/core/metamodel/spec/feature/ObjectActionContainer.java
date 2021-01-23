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

    // -- ACTION LOOKUP
    
    /**
     * Get the action object represented by the specified identity string.
     * <p>
     * The identity string can be either fully specified with parameters (as per
     * {@link Identifier#toNameParmsIdentityString()} or in abbreviated form (
     * {@link Identifier#toNameIdentityString()}).
     *
     * @see #getObjectAction(String)
     */
    Optional<ObjectAction> getObjectAction(String id, @Nullable ActionType type);
    
    default ObjectAction getObjectActionElseFail(String id, @Nullable ActionType type) {
        return getObjectAction(id, type)
                .orElseThrow(()->_Exceptions.noSuchElement("id=%s type=%s", 
                        id, 
                        type==null ? "any" : type.name()));  
    }

    /**
     * Shortcut to {@link #getObjectAction(String, null)}, meaning where action type is <i>any</i>.
     * @see #getObjectAction(String, ActionType)
     */
    default Optional<ObjectAction> getObjectAction(String id) {
        return getObjectAction(id, null);
    }
    
    default ObjectAction getObjectActionElseFail(String id) {
        return getObjectAction(id)
                .orElseThrow(()->_Exceptions.noSuchElement("id=%s", id));  
    }

    // -- ACTION STREAM

    /**
     * Returns an array of actions of the specified type, including or excluding
     * contributed actions as required.
     */
    Stream<ObjectAction> streamObjectActions(ImmutableEnumSet<ActionType> types, MixedIn contributed);

    default Stream<ObjectAction> streamObjectActions(ActionType type, MixedIn contributed) {
        return streamObjectActions(ImmutableEnumSet.of(type), contributed);
    }
    
    default Stream<ObjectAction> streamObjectActions(MixedIn contributed) {
        return streamObjectActions(ActionType.ANY, contributed);
    }
}
