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
package org.apache.causeway.applib.services.priming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable positional arguments supplied to an {@link ActionPrimer}.
 *
 * <p>
 * An array-valued action parameter occupies one position and is not flattened.
 * </p>
 *
 * @since 2.2 {@index}
 */
public final class ActionArguments {

    public static ActionArguments of(final List<?> arguments) {
        Objects.requireNonNull(arguments, "arguments");
        return new ActionArguments(arguments);
    }

    private final List<Object> arguments;

    private ActionArguments(final List<?> arguments) {
        this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
    }

    public int size() {
        return arguments.size();
    }

    public Object get(final int index) {
        return arguments.get(index);
    }

    /**
     * Returns the argument at {@code index}, validating its type when non-null.
     *
     * @throws IllegalArgumentException if the non-null value is not compatible
     *         with {@code requiredType}
     */
    public <T> T get(final int index, final Class<T> requiredType) {
        Objects.requireNonNull(requiredType, "requiredType");
        final Object argument = get(index);
        if(argument != null && !requiredType.isInstance(argument)) {
            throw new IllegalArgumentException(String.format(
                    "Action argument at index %d has type %s, expected %s",
                    index,
                    argument.getClass().getName(),
                    requiredType.getName()));
        }
        return requiredType.cast(argument);
    }

    public List<Object> asList() {
        return arguments;
    }
}
