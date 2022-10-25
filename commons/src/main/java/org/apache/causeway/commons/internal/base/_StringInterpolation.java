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
package org.apache.causeway.commons.internal.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings.KeyValuePair;

import static org.apache.causeway.commons.internal.base._NullSafe.stream;

import lombok.NonNull;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * String interpolation support. Yet use only for small input!
 * Not optimized for large input, with many variables.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _StringInterpolation {

    final Map<String, String> variables;

    public _StringInterpolation(final @NonNull KeyValuePair ... kvPairs) {
        this.variables = new HashMap<>();
        stream(kvPairs)
        .forEach(kvPair->variables.put(kvPair.getKey(), kvPair.getValue()));
    }

    public _StringInterpolation(final @NonNull Map<String, String> variables) {
        this.variables = variables;
    }

    /**
     * Returns a String from given template, with all its variables replaced by their value.
     */
    public String applyTo(final @Nullable String template) {

        if(template == null) return null;

        var acc = template;

        for ( Entry<String, String> entry : variables.entrySet()) {
            final String placeholderLiteral = "${" + entry.getKey() + "}";
            final String placeholderValue = entry.getValue();

            acc = acc.replace(placeholderLiteral, placeholderValue);
        }

        return acc;
    }

    public Can<String> applyTo(final @Nullable Can<String> lines) {
        if(lines == null) return Can.empty();

        return lines.map(this::applyTo);
    }

}
