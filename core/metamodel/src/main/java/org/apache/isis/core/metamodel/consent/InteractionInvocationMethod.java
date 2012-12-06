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

package org.apache.isis.core.metamodel.consent;

import org.apache.isis.core.metamodel.interactions.InteractionContext;

/**
 * Whether an {@link InteractionContext} was invoked by the user, or is
 * programmatic.
 */
public enum InteractionInvocationMethod {

    BY_USER("By user"), PROGRAMMATIC("Programmatic");

    private final String description;

    private InteractionInvocationMethod(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
