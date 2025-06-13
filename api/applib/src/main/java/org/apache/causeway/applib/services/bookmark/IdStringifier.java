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
 *
 */

package org.apache.causeway.applib.services.bookmark;

import org.jspecify.annotations.NonNull;

/**
 * SPI to convert the identifier (primary key) of an entity, of a given type (eg Integer) into a string, and
 * to convert back again into the key object used to actually look up the target entity instance;
 * supported by both JDO and JPA persistence mechanisms.
 *
 * <p>
 *     This is ultimately used by {@link BookmarkService} where we hold a persistent reference to an entity.  The
 *     resultant string also appears in URLs of the Wicket viewer and Restful Objects viewers, and in mementos eg
 *     in {@link org.apache.causeway.schema.cmd.v2.CommandDto} and {@link org.apache.causeway.schema.ixn.v2.InteractionDto}.
 * </p>
 *
 * <p>
 *     The framework provides default implementations of this SPI for JDO (data store and application identity) and
 *     for JPA. Because this is a SPI, other modules or application code can provide their own implementations.
 *     An example of such is the JPA implementation of the <code>commandlog</code> extension.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface IdStringifier<T> {

    public final static char SEPARATOR = '_';

    Class<T> getCorrespondingClass();

    /**
     * Convert the value (which will be of the same type as returned by {@link #getCorrespondingClass()}
     * into a string representation.
     *
     * @see #destring(Class, String)
     */
    String enstring(@NonNull T value);

    /**
     * Convert a string representation of the identifier (as returned by {@link #enstring(Object)}) into an object
     * that can be used to retrieve.
     *
     * @param targetEntityClass - the class of the target entity, eg <code>Customer</code>.  For both JDO and JPA,
     *          we always have this information available, and is needed (at least) by the JDO
     *          implementations of application primary keys using built-ins, eg <code>LongIdentity</code>.
     * @param stringified - as returned by {@link #enstring(Object)}
     */
    T destring(@NonNull Class<?> targetEntityClass, @NonNull String stringified);

    /**
     * Whether the non-null primary key object is valid,
     * that is, in the case of a composite, whether it is fully populated.
     * @implNote in the invalid case, the default implementation generates a stacktrace;
     * @apiNote override for performance reasons if applicable
     */
    default boolean isValid(final @NonNull T value) {
        try {
            return enstring(value)!=null;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Entity agnostic variant of {@link IdStringifier}.
     */
    interface EntityAgnostic<T> extends IdStringifier<T> {

        /**
         * Convert a string representation of the identifier (as returned by {@link #enstring(Object)}) into an object
         * that can be used to retrieve.
         *
         * @param stringified - as returned by {@link #enstring(Object)}
         */
        T destring(@NonNull String stringified);

        @Override
        default T destring(final @NonNull Class<?> targetEntityClass, final @NonNull String stringified) {
            return destring(stringified);
        }

    }

}
