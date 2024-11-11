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
package org.apache.causeway.applib.id;

import java.io.Serializable;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Named;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._ClassCache;

import lombok.NonNull;

/**
 * A generalization of Java's class type to also hold a logical name, which can be supplied lazily.
 * <p>
 * Equality is driven by the corresponding class exclusively, meaning the logical name is ignored
 * in order to not cause any side-effects on logical name memoization eg. it happening too early.
 * <p>
 * Meta-model validators will take care, that there is no logical name ambiguity:
 * There cannot be any LogicalTypes sharing the same corresponding class while having different
 * logical names.
 *
 * @apiNote thread-safe and serializable
 * @since 2.0 {@index}
 */
public record LogicalType(
        /**
         * the application context unique type name
         */
        @org.springframework.lang.NonNull String logicalName,
        /**
         * Type (that is, the {@link Class} this identifier represents).
         */
        @org.springframework.lang.NonNull Class<?> correspondingClass)
implements
    Comparable<LogicalType>,
    Serializable {

    // -- FACTORIES

    /**
     * Returns a new TypeIdentifier based on the corresponding class
     * and (ahead of time) known {@code logicalName}.
     */
    public static LogicalType eager(
            final Class<?> correspondingClass,
            final String logicalName) {
        return new LogicalType(logicalName, correspondingClass);
    }

    /**
     * Use the corresponding class's fully qualified name for the {@code logicalName}.
     * Most likely used in testing scenarios.
     */
    public static LogicalType fqcn(
            final Class<?> correspondingClass) {
        return eager(correspondingClass, correspondingClass.getName());
    }

    public static LogicalType infer(
            final Class<?> correspondingClass) {
        return eager(correspondingClass, _ClassCache.getInstance().getLogicalName(correspondingClass));
    }

    // -- CANONICAL CONSTRUTORS

    public LogicalType(
            final String logicalName,
            final Class<?> correspondingClass) {

        //[CAUSEWAY-3687] would allow CGLIB proxies to be added, but we decided to not allow this for UI contributing beans
        //this.correspondingClass = ClassUtils.getUserClass(correspondingClass);
        this.correspondingClass = requireNonNull(correspondingClass);
        this.logicalName = requireNonEmpty(logicalName);
    }
    
    /**
     * @deprecated use {@link #correspondingClass()}
     */
    @Deprecated
    public Class<?> getCorrespondingClass1() {
        return correspondingClass();
    }
    
    /**
     * Canonical name of the corresponding class.
     */
    public String getClassName() {
        return _Strings.nonEmpty(correspondingClass().getCanonicalName())
                .orElse("inner");
    }

    /**
     * Returns the logical-type-name (unique amongst non-abstract classes).
     * <p>
     * This will typically be the value of the {@link Named#value()} annotation attribute.
     * If none has been specified then will default to the fully qualified class name (with
     * {@link ClassSubstitutorRegistry class name substituted} if necessary to allow for runtime
     * bytecode enhancement.
     *
     * <p>
     * The {@link ObjectSpecification} can be retrieved using
     * {@link SpecificationLoader#specForLogicalTypeName(String)}} passing the logical-type-name as argument.
     *
     * @see ClassSubstitutorRegistry
     * @see ObjectTypeFacet
     * @see ObjectSpecification
     * @see SpecificationLoader
     * @deprecated use {@link #logicalName()}
     */
    @SuppressWarnings("javadoc")
    @Deprecated
    public String getLogicalTypeName1() {
        return logicalName;
    }

    /**
     * The logical type name consists of 2 parts, the <i>namespace</i> and the <i>logical simple name</i>.
     * <p>
     * Returns the <i>logical simple name</i> part.
     * @implNote the result is not memoized, to keep it simple
     */
    public String getLogicalTypeSimpleName() {
        final int lastDot = logicalName.lastIndexOf('.');
        return lastDot >= 0
            ? logicalName.substring(lastDot + 1)
            : logicalName;
    }

    /**
     * The logical type name consists of 2 parts, the <i>namespace</i> and the <i>logical simple name</i>.
     * <p>
     * Returns the <i>namespace</i> part.
     * @implNote the result is not memoized, to keep it simple
     */
    public String getNamespace() {
        final int lastDot = logicalName.lastIndexOf('.');
        return lastDot >= 0
            ? logicalName.substring(0, lastDot)
            : "";
    }

    /**
     * The logical type name consists of 2 parts, the <i>namespace</i> and the <i>logical simple name</i>.
     * Returns a concatenation of <i>namespace</i>, {@code delimiter} and the <i>logical simple name</i>,
     * whereas in the absence of a <i>namespace</i> returns a concatenation of {@code root} and the
     * <i>logical simple name</i>.
     * @param root
     * @param delimiter
     */
    public String getLogicalTypeNameFormatted(
            final @NonNull String root,
            final @NonNull String delimiter) {
        final int lastDot = logicalName.lastIndexOf('.');
        if(lastDot > 0) {
            var namespace = logicalName.substring(0, lastDot);
            var simpleTypeName = logicalName.substring(lastDot + 1);
            return namespace + delimiter + simpleTypeName;
        } else {
            return root + logicalName;
        }
    }

    // -- OBJECT CONTRACT

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LogicalType) {
            return isEqualTo((LogicalType) obj);
        }
        return false;
    }

    public boolean isEqualTo(final @Nullable LogicalType other) {
        if(other==null) {
            return false;
        }
        return Objects.equals(this.correspondingClass, other.correspondingClass);
    }

    @Override
    public int hashCode() {
        return correspondingClass.hashCode();
    }

    @Override
    public int compareTo(final @Nullable LogicalType other) {
        var otherClassName = other!=null
                ? other.correspondingClass().getCanonicalName()
                : null;
        return _Strings.compareNullsFirst(correspondingClass.getCanonicalName(), otherClassName);
    }

    // -- HELPER

    private String requireNonEmpty(final String logicalName) {
        if(_Strings.isEmpty(logicalName)) {
            throw _Exceptions.illegalArgument("logical name for type %s cannot be empty",
                    correspondingClass.getName());
        }
        return logicalName;
    }

}
