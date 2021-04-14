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
package org.apache.isis.applib.mixins.security;

import org.apache.isis.applib.mixins.updates.OnUpdatedBy;

/**
 * Allows domain objects that were created, updated or are otherwise associated
 * with a named user to act as a mixee in order that other modules may
 * contribute behaviour.
 *
 * <p>
 *     The {@link OnUpdatedBy}
 *     interface is for entities to be automatically updated by the
 *     framework when persisted.
 * </p>
 *
 * @see OnUpdatedBy
 *
 * @since 2.0 {@index}
 */
public interface HasUsername {

    /**
     * The user that created, updated or is otherwise associated with this
     * object.
     */
    String getUsername();

}
