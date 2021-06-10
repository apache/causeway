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
package org.apache.isis.applib.services.sudo;

import org.apache.isis.applib.services.iactnlayer.InteractionContext;

import lombok.NonNull;

/**
 * Allows the {@link SudoService} to notify other services/components that
 * the effective user has been changed.
 *
 * <p>
 * The subscribing domain service need only implement this interface,
 * there is no need to explicitly register as a subscriber.
 * </p>
 *
 * @since 2.0
 */
public interface SudoServiceListener {

    /**
     * @param before
     * @param after
     */
    void beforeCall(@NonNull InteractionContext before, @NonNull InteractionContext after);

    void afterCall(@NonNull InteractionContext before, @NonNull InteractionContext after);
}
