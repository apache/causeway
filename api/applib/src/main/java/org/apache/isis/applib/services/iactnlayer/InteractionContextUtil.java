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
package org.apache.isis.applib.services.iactnlayer;

import org.apache.isis.applib.services.user.UserMemento;

import lombok.experimental.UtilityClass;


@UtilityClass
public class InteractionContextUtil{

    /**
     * For internal usage, not formal API.
     *
     * <p>
     *     Instead, use {@link InteractionContext#withUser(UserMemento)}, which honours the value semantics of this class.
     * </p>
     */
    public static void replaceUserIn(InteractionContext interactionContext, UserMemento userMemento) {
        interactionContext.replaceUser(userMemento);
    }

}
