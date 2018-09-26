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

import java.util.List;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.AppManifest2;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.CommandDtoProcessor;

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
     */
    @Programmatic
    List<DomainMember> export();

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

}
