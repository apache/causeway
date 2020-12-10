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
package org.apache.isis.applib.services.metamodel;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.schema.metamodel.v2.MetamodelDto;

import lombok.val;

/**
 * This service provides a formal API into Isis' metamodel.
 *
 * <p>
 * This API is currently extremely limited, but the intention is to extend it gradually as use cases emerge.
 * </p>
 * @since 2.0 {@index}
 */
public interface MetaModelService {

    /**
     * Provides a reverse lookup of a domain class' object type, as defined by {@link DomainObject#objectType()} (or any other mechanism that corresponds to Isis' <code>ObjectSpecIdFacet</code>).
     */
    Class<?> fromObjectType(final String objectType);

    /**
     * Provides a lookup of a domain class' object type, as defined by {@link DomainObject#objectType()} (or any other mechanism that corresponds to Isis' <code>ObjectSpecIdFacet</code>).
     */
    String toObjectType(final Class<?> domainType);

    void rebuild(final Class<?> domainType);

    /**
     * Returns a list of representations of each of member of each domain class.
     *
     * <p>
     *     Used by {@link MetaModelServiceMenu} to return a downloadable CSV.
     * </p>
     *
     * <p>
     *     Note that {@link MetaModelService#exportMetaModel(Config)} provides a superset of the functionality provided by this method.
     * </p>
     *
     */
    DomainModel getDomainModel();

    BeanSort sortOf(Class<?> domainType, Mode mode);

    BeanSort sortOf(Bookmark bookmark, Mode mode);

    CommandDtoProcessor commandDtoProcessorFor(
                            String logicalMemberIdentifier);

    enum Mode {
        /**
         * If the {@link #sortOf(Class, Mode) sort of} object type is unknown, then throw an exception.
         */
        STRICT,
        /**
         * If the {@link #sortOf(Class, Mode) sort of} object type is unknown, then return {@link BeanSort#UNKNOWN}.
         */
        RELAXED
    }

    class Config {
        private static final int IGNORE_NOOP_FACETS = 1;
        private static final int IGNORE_INTERFACES = 2;
        private static final int IGNORE_ABSTRACT_CLASSES = 4;
        private static final int IGNORE_BUILT_IN_VALUE_TYPES = 8;
        private static final int IGNORE_MIXINS = 16;

        private static final String WILDCARD = "*";

        private final int mask;

        private final Set<String> packagePrefixes = _Sets.newHashSet();

        public Config() {
            this(0, Collections.emptyList());
        }
        private Config(final int mask, final Collection<String> packagePrefixes) {
            this.mask = mask;
            this.packagePrefixes.addAll(packagePrefixes);
        }

        public Set<String> getPackagePrefixes() {
            return Collections.unmodifiableSet(packagePrefixes);
        }

        public Config withIgnoreNoop() {
            return newConfigWith(IGNORE_NOOP_FACETS);
        }

        public Config withIgnoreInterfaces() {
            return newConfigWith(IGNORE_INTERFACES);
        }
        public Config withIgnoreAbstractClasses() {
            return newConfigWith(IGNORE_ABSTRACT_CLASSES);
        }
        public Config withIgnoreBuiltInValueTypes() {
            return newConfigWith(IGNORE_BUILT_IN_VALUE_TYPES);
        }
        public Config withIgnoreMixins() {
            return newConfigWith(IGNORE_MIXINS);
        }

        private Config newConfigWith(final int x) {
            return new Config(mask | x, packagePrefixes);
        }

        public Config withPackagePrefixAny() {
            val newPrefixes = _Sets.<String>newHashSet();
            newPrefixes.add(WILDCARD);
            return new Config(mask, newPrefixes);
        }

        public boolean isPackagePrefixAny() {
            return packagePrefixes.contains(WILDCARD);
        }

        /**
         * Returns a new {@code Config} with given {@code packagePrefix} added to the set of
         * this {@code Config}'s packagePrefixes.
         * @param packagePrefix - prefix to be added
         */
        public Config withPackagePrefix(final String packagePrefix) {
            val newPrefixes = _Sets.newHashSet(this.packagePrefixes);
            newPrefixes.add(packagePrefix);
            return new Config(mask, newPrefixes);
        }

        public boolean isIgnoreNoop() {
            return hasFlag(IGNORE_NOOP_FACETS);
        }

        public boolean isIgnoreInterfaces() {
            return hasFlag(IGNORE_INTERFACES);
        }

        public boolean isIgnoreAbstractClasses() {
            return hasFlag(IGNORE_ABSTRACT_CLASSES);
        }
        public boolean isIgnoreBuiltInValueTypes() {
            return hasFlag(IGNORE_BUILT_IN_VALUE_TYPES);
        }
        public boolean isIgnoreMixins() {
            return hasFlag(IGNORE_MIXINS);
        }

        private boolean hasFlag(final int x) {
            return (mask & x) == x;
        }

        // ...
    }

    MetamodelDto exportMetaModel(final Config config);

}
