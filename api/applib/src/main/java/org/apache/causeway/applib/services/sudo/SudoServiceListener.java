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
package org.apache.causeway.applib.services.sudo;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;

import org.jspecify.annotations.NonNull;

/**
 * Allows the {@link SudoService} to notify other {@link org.springframework.stereotype.Service}s or
 * {@link org.springframework.stereotype.Component}s that the effective user has been changed.
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
