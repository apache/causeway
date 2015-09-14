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

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public interface ObjectActionContainer {

    /**
     * Returns the action of the specified type with the specified signature.
     */
    ObjectAction getObjectAction(ActionType type, String id, List<ObjectSpecification> parameters);

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
    ObjectAction getObjectAction(ActionType type, String id);

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
    ObjectAction getObjectAction(String id);

    /**
     * Returns an array of actions of the specified type, including or excluding
     * contributed actions as required.
     */
    List<ObjectAction> getObjectActions(ActionType type, Contributed contributee, Filter<ObjectAction> filter);

    List<ObjectAction> getObjectActions(List<ActionType> types, Contributed contributee, Filter<ObjectAction> filter);

    List<ObjectAction> getObjectActions(Contributed contributee);



}
