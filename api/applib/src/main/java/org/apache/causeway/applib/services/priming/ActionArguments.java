/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.applib.services.priming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable shallow positional snapshot of effective action arguments.
 * Array-valued parameters occupy one position and are never flattened.
 * @since 4.0 {@index}
 */
public final class ActionArguments {
    public static ActionArguments of(final List<?> arguments) {
        return new ActionArguments(Objects.requireNonNull(arguments, "arguments"));
    }

    private final List<Object> arguments;
    private ActionArguments(final List<?> arguments) {
        this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
    }
    public int size() { return arguments.size(); }
    public Object get(final int index) { return arguments.get(index); }

    /** Validates the required type when the positional value is non-null. */
    public <T> T get(final int index, final Class<T> requiredType) {
        Objects.requireNonNull(requiredType, "requiredType");
        final var argument = get(index);
        if (argument != null && !requiredType.isInstance(argument)) {
            throw new IllegalArgumentException("Action argument at index " + index + " has type "
                    + argument.getClass().getName() + ", expected " + requiredType.getName());
        }
        return requiredType.cast(argument);
    }
    public List<Object> asList() { return arguments; }
}
