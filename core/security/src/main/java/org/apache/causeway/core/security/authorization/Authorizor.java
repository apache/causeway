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
package org.apache.causeway.core.security.authorization;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;

/**
 * Represents a mechanism to evaluate whether the current user (as represented
 * by {@link InteractionContext} can either view or invoke the domain object
 * feature (as represented by {@link Identifier}.
 *
 * @apiNote This is a framework internal class and so does not constitute a formal API.
 *
 * @since 1.x but refactored in v2 {@index}
 */
public interface Authorizor {

    /**
     * Whether the current {@link InteractionContext user} can view the
     * domain object feature (represented by {@link Identifier}).
     */
    boolean isVisible(InteractionContext authentication, Identifier identifier);

    /**
     * Whether the current {@link InteractionContext user} can invoke the
     * domain object feature (represented by {@link Identifier}).
     *
     * <p>
     *     If this methods returns <code>false</code> then the feature will be
     *     greyed out/disabled.
     * </p>
     */
    boolean isUsable(InteractionContext authentication, Identifier identifier);

}
