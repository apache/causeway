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

import org.springframework.lang.Nullable;

/**
 * Creates compact, bounded-cardinality contextual names for semantic observations.
 *
 * @since 2.2
 */
public final class CausewayObservationNaming {

    public static final int MAX_CONTEXTUAL_NAME_LENGTH = 50;
    public static final int MAX_APPLICATION_SPAN_SUFFIX_LENGTH = 46;

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

    /**
     * Builds an application span name while preserving the complete suffix.
     */
    public static String forApplicationSpan(
            final @Nullable String logicalMemberIdentifier,
            final String suffix) {
        final String validatedSuffix = requireApplicationSpanSuffix(suffix);
        final String identifier = logicalMemberIdentifier != null
                ? requireText(logicalMemberIdentifier, "logicalMemberIdentifier")
                : "app";
        final String fullName = identifier + " " + validatedSuffix;
        if(fullName.length() <= MAX_CONTEXTUAL_NAME_LENGTH) {
            return fullName;
        }

        final String compactIdentifier = compactLogicalMemberIdentifier(identifier);
        final String compactName = compactIdentifier + " " + validatedSuffix;
        if(compactName.length() <= MAX_CONTEXTUAL_NAME_LENGTH) {
            return compactName;
        }

        final int identifierLength = MAX_CONTEXTUAL_NAME_LENGTH
                - 1
                - validatedSuffix.length();
        return compactIdentifier.substring(0, identifierLength)
                + " "
                + validatedSuffix;
    }

    public static String forRenderRegion(
            final String region,
            final String identifier) {
        return forRegion("render", region, identifier);
    }

    public static String forRegion(
            final String operation,
            final String region,
            final String identifier) {
        return bounded(requireText(operation, "operation")
                + " " + requireText(region, "region")
                + " " + regionIdentifier(identifier));
    }

    public static String bounded(final String contextualName) {
        final String name = requireText(contextualName, "contextualName");
        return name.length() <= MAX_CONTEXTUAL_NAME_LENGTH
                ? name
                : name.substring(0, MAX_CONTEXTUAL_NAME_LENGTH);
    }

    private static String compactLogicalMemberIdentifier(final String identifier) {
        final int memberSeparator = identifier.indexOf('#');
        if(memberSeparator < 1 || memberSeparator + 1 >= identifier.length()) {
            return identifier;
        }
        return simpleTypeName(identifier.substring(0, memberSeparator))
                + identifier.substring(memberSeparator);
    }

    private static String requireApplicationSpanSuffix(final String suffix) {
        final String validated = requireText(suffix, "suffix");
        if(validated.length() > MAX_APPLICATION_SPAN_SUFFIX_LENGTH) {
            throw new IllegalArgumentException(String.format(
                    "suffix must be at most %d characters",
                    MAX_APPLICATION_SPAN_SUFFIX_LENGTH));
        }
        return validated;
    }

    private static String regionIdentifier(final String identifier) {
        final String canonicalIdentifier = requireText(identifier, "identifier");
        if("<default>".equals(canonicalIdentifier)) {
            return "default";
        }
        final int memberSeparator = canonicalIdentifier.lastIndexOf('#');
        final String memberName = memberSeparator >= 0
                ? canonicalIdentifier.substring(memberSeparator + 1)
                : canonicalIdentifier;
        final int parameterSeparator = memberName.indexOf('(');
        return parameterSeparator >= 0
                ? memberName.substring(0, parameterSeparator)
                : memberName;
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
