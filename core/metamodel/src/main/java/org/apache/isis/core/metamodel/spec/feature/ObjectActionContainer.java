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
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ActionScope;

public interface ObjectActionContainer {

    // -- ACTION LOOKUP (INHERITANCE CONSIDERED)

    /**
     * Similar to {@link #getDeclaredAction(String, ImmutableEnumSet, MixedIn)},
     * but also considering any inherited object members. (mixed-in included)
     * @param id
     * @param type
     *
     * @implSpec If not found on the current 'type' search for the 'nearest' match in super-types,
     * and if nothing found there, search the interfaces. Special care needs to be taken, as the
     * {@link ActionScope} might be redeclared when inheriting from a super-type or interface.
     */
    Optional<ObjectAction> getAction(String id, ImmutableEnumSet<ActionScope> actionScopes, MixedIn mixedIn);

    default ObjectAction getActionElseFail(
            final String id, final ImmutableEnumSet<ActionScope> actionScopes, final MixedIn mixedIn) {
        return getAction(id, actionScopes, mixedIn)
                .orElseThrow(()->_Exceptions.noSuchElement("actionId=%s scope=%s mixedIn=%s",
                        id,
                        actionScopes,
                        mixedIn.name()));
    }

    default Optional<ObjectAction> getAction(final String id, final MixedIn mixedIn) {
        return getAction(id, ActionScope.ANY, mixedIn); }
    default ObjectAction getActionElseFail(final String id, final MixedIn mixedIn) {
        return getActionElseFail(id, ActionScope.ANY, mixedIn); }

    default Optional<ObjectAction> getAction(final String id, final ImmutableEnumSet<ActionScope> actionScopes) {
        return getAction(id, actionScopes, MixedIn.INCLUDED); }
    default ObjectAction getActionElseFail(final String id, final ImmutableEnumSet<ActionScope> actionScopes) {
        return getActionElseFail(id, actionScopes, MixedIn.INCLUDED); }

    default Optional<ObjectAction> getAction(final String id) {
        return getAction(id, ActionScope.ANY, MixedIn.INCLUDED); }
    default ObjectAction getActionElseFail(final String id) {
        return getActionElseFail(id, ActionScope.ANY, MixedIn.INCLUDED); }


    // -- ACTION LOOKUP, DECLARED ACTIONS (NO INHERITANCE CONSIDERED)

    /**
     * Get the action object represented by the specified identity string. (mixed-in included)
     * <p>
     * The identity string can be either fully specified with parameters (as per
     * {@link Identifier#getMemberNameAndParameterClassNamesIdentityString()} or in abbreviated form (
     * {@link Identifier#getMemberLogicalName()}).
     *
     * @see #getDeclaredAction(String, MixedIn)
     */
    Optional<ObjectAction> getDeclaredAction(String id, ImmutableEnumSet<ActionScope> actionScopes, MixedIn mixedIn);

    /**
     * Shortcut to {@link #getDeclaredAction(String, ImmutableEnumSet, MixedIn)} with {@code ActionType = null},
     * meaning where action type is <i>any</i>.
     * @see #getDeclaredAction(String, ImmutableEnumSet, MixedIn)
     */
    default Optional<ObjectAction> getDeclaredAction(final String id, final MixedIn mixedIn) {
        return getDeclaredAction(id, ActionScope.ANY, mixedIn);}
    default Optional<ObjectAction> getDeclaredAction(final String id, final ImmutableEnumSet<ActionScope> actionScopes) {
        return getDeclaredAction(id, actionScopes, MixedIn.INCLUDED);}
    default Optional<ObjectAction> getDeclaredAction(final String id) {
        return getDeclaredAction(id, ActionScope.ANY, MixedIn.INCLUDED);}

    // -- ACTION STREAM (WITH INHERITANCE)

    /**
     * Returns a Stream of all actions of given {@code actionTypes}, with inheritance considered.
     * @param actionTypes
     * @param mixedIn - whether to include mixed in actions
     * @param onActionOverloaded - callback on overloaded action detected
     */
    Stream<ObjectAction> streamActions(
            ImmutableEnumSet<ActionScope> actionTypes,
            MixedIn mixedIn,
            Consumer<ObjectAction> onActionOverloaded);

    /**
     * Returns a Stream of all actions of given {@code actionTypes}, with inheritance considered.
     * @param actionTypes
     * @param mixedIn - whether to include mixed in actions
     */
    default Stream<ObjectAction> streamActions(
            final ImmutableEnumSet<ActionScope> actionTypes,
            final MixedIn mixedIn) {
        return streamActions(actionTypes, mixedIn, __->{});
    }

    /**
     * Returns a Stream of all actions of given {@code actionType}, with inheritance considered.
     * @param actionType
     * @param mixedIn - whether to include mixed in actions
     */
    default Stream<ObjectAction> streamActions(final ActionScope actionType, final MixedIn mixedIn) {
        return streamActions(ImmutableEnumSet.of(actionType), mixedIn);
    }

    /**
     * Returns a Stream of all actions of any type, with inheritance considered.
     * @param mixedIn - whether to include mixed in actions
     */
    default Stream<ObjectAction> streamAnyActions(final MixedIn mixedIn) {
        return streamActions(ActionScope.ANY, mixedIn);
    }

    /**
     * Returns a Stream of all actions enabled for the current runtime environment,
     * with inheritance considered.
     * @param mixedIn - whether to include mixed in actions
     */
    Stream<ObjectAction> streamRuntimeActions(MixedIn mixedIn);

    // -- ACTION STREAM (NO INHERITANCE)

    /**
     * Returns an array of actions of the specified type, including or excluding
     * contributed actions as required.
     */
    Stream<ObjectAction> streamDeclaredActions(ImmutableEnumSet<ActionScope> actionTypes, MixedIn mixedIn);

    default Stream<ObjectAction> streamDeclaredActions(final ActionScope type, final MixedIn mixedIn) {
        return streamDeclaredActions(ImmutableEnumSet.of(type), mixedIn);
    }

    default Stream<ObjectAction> streamDeclaredActions(final MixedIn mixedIn) {
        return streamDeclaredActions(ActionScope.ANY, mixedIn);
    }

}
