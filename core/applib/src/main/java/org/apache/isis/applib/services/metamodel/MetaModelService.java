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

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.AppManifest2;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.CommandDtoProcessor;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.schema.metamodel.v1.MetamodelDto;

/**
 * This service provides a formal API into Isis' metamodel.
 *
 * <p>
 * This API is currently extremely limited, but the intention is to extend it gradually as use cases emerge.
 * </p>
 */
public interface MetaModelService {

    /**
     * Provides a reverse lookup of a domain class' object type, as defined by {@link DomainObject#objectType()} (or any other mechanism that corresponds to Isis' <code>ObjectSpecIdFacet</code>).
     */
    @Programmatic
    Class<?> fromObjectType(final String objectType);

    /**
     * Provides a lookup of a domain class' object type, as defined by {@link DomainObject#objectType()} (or any other mechanism that corresponds to Isis' <code>ObjectSpecIdFacet</code>).
     */
    @Programmatic
    String toObjectType(final Class<?> domainType);


    @Programmatic
    void rebuild(final Class<?> domainType);

    /**
     * Returns a list of representations of each of member of each domain class.
     *
     * <p>
     *     Used by {@link MetaModelServicesMenu} to return a downloadable CSV.
     * </p>
     *
     * <p>
     *     Note that {@link MetaModelService6#exportMetaModel(MetaModelService6.Config)} provides a superset of the functionality provided by this method.
     * </p>
     *
     * @see MetaModelService6
     */
    @Programmatic
    DomainModel getDomainModel();

    @Programmatic
    Sort sortOf(Class<?> domainType, Mode mode);

    @Programmatic
    Sort sortOf(Bookmark bookmark, Mode mode);

    enum Sort {
        VIEW_MODEL,
        JDO_ENTITY,
        DOMAIN_SERVICE,
        MIXIN,
        VALUE,
        COLLECTION,
        UNKNOWN;

        public boolean isDomainService() {
            return this == DOMAIN_SERVICE;
        }

        public boolean isMixin() {
            return this == MIXIN;
        }

        public boolean isViewModel() {
            return this == VIEW_MODEL;
        }

        public boolean isValue() {
            return this == VALUE;
        }

        public boolean isCollection() {
            return this == COLLECTION;
        }

        public boolean isJdoEntity() {
            return this == JDO_ENTITY;
        }

        public boolean isUnknown() {
            return this == UNKNOWN;
        }

    }

    enum Mode {
        /**
         * If the {@link #sortOf(Class, Mode) sort of} object type is unknown, then throw an exception.
         */
        STRICT,
        /**
         * If the {@link #sortOf(Class, Mode) sort of} object type is unknown, then return {@link Sort#UNKNOWN}.
         */
        RELAXED
    }

    /**
     * @return as {@link #getAppManifest()}, downcasted (else null).
     */
    @Programmatic
    AppManifest2 getAppManifest2();

    /**
     * @return the {@link AppManifest} used to bootstrap the application.
     */
    @Programmatic
    AppManifest getAppManifest();

    @Programmatic
    CommandDtoProcessor commandDtoProcessorFor(String memberIdentifier);

    public static class Config {

        private static final int IGNORE_NOOP_FACETS = 1;
        private static final int IGNORE_INTERFACES = 2;
        private static final int IGNORE_ABSTRACT_CLASSES = 4;
        private static final int IGNORE_BUILT_IN_VALUE_TYPES = 8;
        private static final int IGNORE_MIXINS = 16;

        private final int mask;
        private final List<String> packagePrefixes;

        public Config() {
            this(0, Collections.emptyList());
        }
        private Config(final int mask, final List<String> packagePrefixes) {
            this.mask = mask;
            this.packagePrefixes = Collections.unmodifiableList(packagePrefixes);
        }
        private Config(final int mask) {
            this(mask, null);
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

        public Config withPackagePrefix(final String packagePrefix) {
            final List<String> prefixes = _Lists.newArrayList(this.packagePrefixes);
            prefixes.add(packagePrefix);
            return new Config(mask, prefixes);
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

        public List<String> getPackagePrefixes() {
            return packagePrefixes;
        }

        private boolean hasFlag(final int x) {
            return (mask & x) == x;
        }

    }

    @Programmatic
    MetamodelDto exportMetaModel(final Config config);

}
