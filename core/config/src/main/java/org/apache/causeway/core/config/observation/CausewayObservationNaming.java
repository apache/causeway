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
package org.apache.causeway.core.config.observation;

import java.util.Objects;

/**
 * Creates compact, bounded-cardinality contextual names for semantic observations.
 *
 * @since 2.2
 */
public final class CausewayObservationNaming {

    private CausewayObservationNaming() {}

    public static String forType(
            final String operation,
            final String logicalTypeName) {
        return token(operation) + " " + token(simpleTypeName(logicalTypeName));
    }

    public static String forMember(
            final String operation,
            final String logicalTypeName,
            final String memberLogicalName) {
        return token(operation)
                + " " + token(memberLogicalName)
                + " on " + token(simpleTypeName(logicalTypeName));
    }

    private static String simpleTypeName(final String logicalTypeName) {
        final String typeName = requireText(logicalTypeName, "logicalTypeName");
        int separator = -1;
        for (int i = 0; i < typeName.length(); i++) {
            switch (typeName.charAt(i)) {
                case '.':
                case '/':
                case '\\':
                case ':':
                case '$':
                    separator = i;
                    break;
                default:
                    break;
            }
        }
        return separator >= 0 && separator + 1 < typeName.length()
                ? typeName.substring(separator + 1)
                : typeName;
    }

    private static String token(final String value) {
        final String text = requireText(value, "contextual name token");
        final StringBuilder normalized = new StringBuilder(text.length());
        boolean separatorPending = false;
        char previous = 0;
        for (int i = 0; i < text.length(); i++) {
            final char current = text.charAt(i);
            if(Character.isLetterOrDigit(current)) {
                if(normalized.length() > 0
                        && (separatorPending
                                || Character.isUpperCase(current)
                                        && (Character.isLowerCase(previous)
                                                || Character.isDigit(previous)))) {
                    appendSeparator(normalized);
                }
                normalized.append(Character.toLowerCase(current));
                separatorPending = false;
                previous = current;
            } else {
                separatorPending = normalized.length() > 0;
                previous = 0;
            }
        }
        if(normalized.length() == 0) {
            throw new IllegalArgumentException("contextual name token must contain a letter or digit");
        }
        return normalized.toString();
    }

    private static void appendSeparator(final StringBuilder normalized) {
        if(normalized.charAt(normalized.length() - 1) != '-') {
            normalized.append('-');
        }
    }

    private static String requireText(final String value, final String name) {
        Objects.requireNonNull(value, name);
        if(value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
