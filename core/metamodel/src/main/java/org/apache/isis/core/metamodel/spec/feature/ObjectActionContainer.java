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
import org.apache.isis.core.metamodel.spec.ObjectSpecification;


public interface ObjectActionContainer {

    /**
     * TODO: convert to relatedResourceActions
     */
    List<ObjectAction> getServiceActionsReturning(ObjectActionType... type);

    /**
     * Returns the action of the specified type with the specified signature.
     */
    ObjectAction getObjectAction(ObjectActionType type, String id, List<ObjectSpecification> parameters);

    /**
     * Get the action object represented by the specified identity string.
     * 
     * <p>
     * The identity string should be {@link Identifier#toNameParmsIdentityString()}</tt>.
     */
    ObjectAction getObjectAction(ObjectActionType type, String nameAndParmsIdentityString);

    /**
     * Returns an array of actions of the specified type(s).
     * 
     * <p>
     * If the type is <tt>null</tt>, then returns all {@link ObjectActionType#USER user},
     * {@link ObjectActionType#EXPLORATION exploration} and {@link ObjectActionType#DEBUG
     * debug} actions (but not {@link ObjectActionType#SET action sets}).
     */
    List<ObjectAction> getObjectActions(ObjectActionType... type);


}
