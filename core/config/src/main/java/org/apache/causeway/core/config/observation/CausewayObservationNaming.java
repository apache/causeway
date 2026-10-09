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
 * Creates compact contextual names with a bounded display length for semantic observations.
 *
 * @since 4.0.0
 */
public final class CausewayObservationNaming {

    public static final int MAX_CONTEXTUAL_NAME_LENGTH = 50;

    private CausewayObservationNaming() {}

    public static String forType(
            final String operation,
            final String logicalTypeName) {
        final String prefix = requireText(operation, "operation") + " ";
        final String typeName = requireText(logicalTypeName, "logicalTypeName");
        final String fullName = prefix + typeName;
        return fullName.length() <= MAX_CONTEXTUAL_NAME_LENGTH
                ? fullName
                : bounded(prefix + simpleTypeName(typeName));
    }

    public static String forMember(
            final String operation,
            final String logicalTypeName,
            final String memberLogicalName) {
        final String typeName = requireText(logicalTypeName, "logicalTypeName");
        final String memberName = requireText(memberLogicalName, "memberLogicalName");
        return forLogicalMember(operation, typeName + "#" + memberName);
    }

    public static String forLogicalMember(
            final String operation,
            final String logicalMemberIdentifier) {
        final String prefix = requireText(operation, "operation") + " ";
        final String identifier = requireText(
                logicalMemberIdentifier, "logicalMemberIdentifier");
        final String fullName = prefix + identifier;
        if(fullName.length() <= MAX_CONTEXTUAL_NAME_LENGTH) {
            return fullName;
        }

        final int memberSeparator = identifier.indexOf('#');
        if(memberSeparator < 1 || memberSeparator + 1 >= identifier.length()) {
            return bounded(fullName);
        }
        final String typeName = identifier.substring(0, memberSeparator);
        final String memberName = identifier.substring(memberSeparator + 1);
        return bounded(prefix + simpleTypeName(typeName) + "#" + memberName);
    }

    /** Formats a render region without including action signatures in its display name. */
    public static String forRenderRegion(final String region, final String identifier) {
        final String id = requireText(identifier, "identifier");
        final int parameters = id.indexOf('(');
        return forLogicalMember("render " + requireText(region, "region"),
                parameters >= 0 ? id.substring(0, parameters) : id.replace("<default>", "default"));
    }

    public static String bounded(final String contextualName) {
        final String name = requireText(contextualName, "contextualName");
        return name.length() <= MAX_CONTEXTUAL_NAME_LENGTH
                ? name
                : name.substring(0, MAX_CONTEXTUAL_NAME_LENGTH);
    }

    private static String simpleTypeName(final String logicalTypeName) {
        int separator = -1;
        for (int i = 0; i < logicalTypeName.length(); i++) {
            switch (logicalTypeName.charAt(i)) {
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
        return separator >= 0 && separator + 1 < logicalTypeName.length()
                ? logicalTypeName.substring(separator + 1)
                : logicalTypeName;
    }

    private static String requireText(final String value, final String name) {
        Objects.requireNonNull(value, name);
        if(value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
