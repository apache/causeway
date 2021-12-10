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
package org.apache.isis.applib;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.isis.applib.id.HasLogicalType;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.i18n.HasTranslationContext;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.reflection._Reflect;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * Combines {@link LogicalType} and member identification (from properties, collections or actions),
 * to a fully qualified <i>feature</i> identifier.
 * <p>
 * For {@link Identifier}(s) of type {@link Identifier.Type#CLASS} member information is
 * left empty.
 *
 * @since 1.x revised for 2.0 {@index}
 * @see LogicalType
 */
public class Identifier
implements
    Comparable<Identifier>,
    HasLogicalType,
    HasTranslationContext,
    Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * What type of feature this identifies.
     */
    public static enum Type {
        /**
         * A <i>Value-type</i> or <i>Domain Object</i>.
         */
        CLASS,
        /**
         * <i>Action</i> either declared or mixed in.
         */
        ACTION,
        /**
         * <i>Action Parameter</i>, also has a non-negative {@link #parameterIndex}.
         */
        ACTION_PARAMETER,
        /**
         * <i>Association</i> (of any cardinality), either declared or mixed in.
         */
        PROPERTY_OR_COLLECTION
        ;
        public boolean isClass() { return this == CLASS; }
        public boolean isAction() { return this == ACTION; }
        public boolean isActionParameter() { return this == ACTION_PARAMETER; }
        public boolean isPropertyOrCollection() { return this == PROPERTY_OR_COLLECTION;}
    }

    // -- FACTORY METHODS

    public static Identifier classIdentifier(final LogicalType typeIdentifier) {
        return new Identifier(typeIdentifier, "", Can.empty(), Type.CLASS);
    }

    public static Identifier propertyOrCollectionIdentifier(
            final LogicalType typeIdentifier,
            final String propertyOrCollectionName) {
        return new Identifier(typeIdentifier, propertyOrCollectionName, Can.empty(),
                Type.PROPERTY_OR_COLLECTION);
    }

    /** for reporting orphaned methods */
    public static Identifier methodIdentifier(
            final LogicalType typeIdentifier,
            final Method method) {
        return actionIdentifier(typeIdentifier, _Reflect.methodToShortString(method), method.getParameterTypes());
    }

    public static Identifier actionIdentifier(
            final LogicalType typeIdentifier,
            final String actionName,
            final Class<?>... parameterClasses) {
        return actionIdentifier(typeIdentifier, actionName, classNamesOf(parameterClasses));
    }

    public static Identifier actionIdentifier(
            final LogicalType typeIdentifier,
            final String actionName,
            final Can<String> parameterClassNames) {
        return new Identifier(typeIdentifier, actionName, parameterClassNames, Type.ACTION);
    }

    // -- INSTANCE FIELDS

    @Getter(onMethod_ = {@Override}) private final LogicalType logicalType;

    @Getter private final String className;

    @Getter private final String memberLogicalName;

    /**
     * Optional. Used for <i>Action Parameters</i>, otherwise {@code -1}.
     */
    @Getter private final int parameterIndex;

    @Getter private final Can<String> memberParameterClassNames;

    @Getter private final Type type;

    /**
     * Fully qualified Identity String. (class-name + member-logical-name + param-class-names)
     */
    @Getter private final String fullIdentityString;

    /**
     * Member Identity String (class omitted), including parameters if any.
     */
    @Getter private final String memberNameAndParameterClassNamesIdentityString;

    /**
     * Context to be used for i18n translation.
     * @see TranslationService
     */
    @Getter(onMethod_ = {@Override}) private final TranslationContext translationContext;

    // -- CONSTRUCTION

    private Identifier(
            final LogicalType logicalType,
            final String memberLogicalName,
            final Can<String> memberParameterClassNames,
            final Type type) {
        this(logicalType, memberLogicalName, memberParameterClassNames, type, -1);
    }

    private Identifier(
            final LogicalType logicalType,
            final String memberLogicalName,
            final Can<String> memberParameterClassNames,
            final Type type,
            final int parameterIndex) {

        this.logicalType = logicalType;
        this.className = logicalType.getClassName();
        this.memberLogicalName = memberLogicalName;
        this.memberParameterClassNames = memberParameterClassNames;
        this.type = type;
        this.parameterIndex = parameterIndex;

        this.memberNameAndParameterClassNamesIdentityString =
                memberLogicalName + (type.isAction()
                        ? "(" + memberParameterClassNames.stream().collect(Collectors.joining(",")) + ")"
                        : "");

        this.translationContext = TranslationContext.ofName(
                className + "#" + memberLogicalName + (type.isAction() ? "()" : ""));

        this.fullIdentityString = _Strings.isEmpty(memberLogicalName)
                ? className
                : className + "#" + memberNameAndParameterClassNamesIdentityString;
    }

    // -- WITHERS

    public Identifier withParameterIndex(final int parameterIndex) {
        return new Identifier(
                logicalType, memberLogicalName, memberParameterClassNames, Type.ACTION_PARAMETER, parameterIndex);
    }

    // -- LOGICAL ID

    public String getLogicalIdentityString(final @NonNull String delimiter) {
        return getLogicalTypeName()
                + delimiter
                + memberNameAndParameterClassNamesIdentityString;
    }

    // -- NATURAL NAMES

    public String getClassNaturalName() {
        val className = getClassName();
        val isolatedName = className.substring(className.lastIndexOf('.') + 1);
        return naturalName(isolatedName);
    }

    public String getMemberNaturalName() {
        return naturalName(memberLogicalName);
    }

    public Can<String> getMemberParameterClassNaturalNames() {
        return naturalNames(memberParameterClassNames);
    }

    // -- OBJECT CONTRACT

    @Override
    public int compareTo(final Identifier other) {
        return toString().compareTo(other.toString());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Identifier) {
            return isEqualTo((Identifier) obj);
        }
        return false;
    }

    public boolean isEqualTo(final Identifier other) {
        return Objects.equals(this.className, other.className)
                && Objects.equals(this.memberLogicalName, other.memberLogicalName)
                && this.memberParameterClassNames.equals(other.memberParameterClassNames);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return fullIdentityString;
    }

    // -- HELPER

    private static Can<String> classNamesOf(final Class<?>[] parameterClasses) {
        return Can.ofArray(parameterClasses)
        .map(Class::getName);
    }

    private static final char SPACE = ' ';

    /*
     * Returns a word spaced version of the specified name, so there are spaces
     * between the words, where each word starts with a capital letter. E.g.,
     * "NextAvailableDate" is returned as "Next Available Date".
     */
    private static String naturalName(final String name) {
        final int length = name.length();

        if (length <= 1) {
            return name.toUpperCase();// ensure first character is upper case
        }

        final StringBuffer naturalName = new StringBuffer(length);

        char previousCharacter;
        char character = Character.toUpperCase(name.charAt(0));// ensure first
        // character is
        // upper case
        naturalName.append(character);
        char nextCharacter = name.charAt(1);

        for (int pos = 2; pos < length; pos++) {
            previousCharacter = character;
            character = nextCharacter;
            nextCharacter = name.charAt(pos);

            if (previousCharacter != SPACE) {
                if (Character.isUpperCase(character)
                        && !Character.isUpperCase(previousCharacter)) {
                    naturalName.append(SPACE);
                }
                if (Character.isUpperCase(character)
                        && Character.isLowerCase(nextCharacter)
                        && Character.isUpperCase(previousCharacter)) {
                    naturalName.append(SPACE);
                }
                if (Character.isDigit(character)
                        && !Character.isDigit(previousCharacter)) {
                    naturalName.append(SPACE);
                }
            }
            naturalName.append(character);
        }
        naturalName.append(nextCharacter);
        return naturalName.toString();
    }

    private static Can<String> naturalNames(final Can<String> names) {
        return names.map(Identifier::naturalName);
    }




}

