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

package org.apache.isis.core.metamodel.services.classsubstitutor;

import java.io.Serializable;
import java.util.function.UnaryOperator;

import org.checkerframework.checker.nullness.qual.Nullable;

import lombok.NonNull;
import lombok.Value;

/**
 * Provides capability to translate or ignore classes.
 */
public interface ClassSubstitutor {

    /**
     * Captures 4 possible directives:
     * <ul>
     * <li>pass-through (indifferent)</li>
     * <li>never replace the class</li>
     * <li>never introspect the class</li>
     * <li>replace the class with another</li>
     * </ul>
     * @since 2.0
     */
    @Value(staticConstructor = "of")
    static class Substitution implements UnaryOperator<Class<?>>, Serializable {
        private static final long serialVersionUID = 1L;
        private static final Substitution PASSTHROUGH = Substitution.of(Type.PASSTHROUGH, null);
        private static final Substitution NEVER_REPLACE_CLASS = Substitution.of(Type.NEVER_REPLACE_CLASS, null);
        private static final Substitution NEVER_INTROSPECT_CLASS = Substitution.of(Type.NEVER_INTROSPECT_CLASS, null);

        private static enum Type {
            PASSTHROUGH,
            NEVER_REPLACE_CLASS,
            NEVER_INTROSPECT_CLASS,
            REPLACE_WITH_OTHER_CLASS,
        }

        @NonNull Type type;
        @Nullable Class<?> replacement;

        // -- FACTORIES

        /**
         * The result type to be used for a ClassSubstitutor that does not feel responsible
         * for the (operand-) class, hence acts as a pass-through when aggregating.
         */
        public static Substitution passThrough() {
            return PASSTHROUGH;
        }

        /**
         * Forces the (operand-) class never to be replaced.
         */
        public static Substitution neverReplaceClass() {
            return NEVER_REPLACE_CLASS;
        }

        public static Substitution neverIntrospect() {
            return NEVER_INTROSPECT_CLASS;
        }

        public static Substitution replaceWith(@lombok.NonNull @org.springframework.lang.NonNull Class<?> cls) {
            return of(Type.REPLACE_WITH_OTHER_CLASS, cls);
        }

        // -- PREDICATES

        /**
         *  @return whether to act as a pass-through (indifferent)
         */
        public boolean isPassThrough() {
            return type == Type.PASSTHROUGH;
        }

        /**
         * @return whether to not replace the class (do nothing)
         */
        public boolean isNeverReplace() {
            return type == Type.NEVER_REPLACE_CLASS;
        }

        /**
         * @return whether to ignore the class (never introspect)
         */
        public boolean isNeverIntrospect() {
            return type == Type.NEVER_INTROSPECT_CLASS;
        }

        /**
         * @return whether to replace the class with registered replacement
         */
        public boolean isReplace() {
            return type == Type.REPLACE_WITH_OTHER_CLASS;
        }

        // -- OPERATOR

        @Override
        public Class<?> apply(Class<?> cls) {
            if(isNeverIntrospect()) {
                return null;
            }
            return isReplace() ? getReplacement() : cls;
        }

    }

    // -- INTERFACE

    /**
     * @param cls
     * @return (non-null) Substitution for given {@code cls}
     * @since 2.0
     */
    Substitution getSubstitution(@lombok.NonNull @org.springframework.lang.NonNull Class<?> cls);


}
