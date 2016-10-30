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

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;

/**
 * This service provides a formal API into Isis' metamodel.
 */
public interface MetaModelService2 extends MetaModelService {

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

    /**
     * @deprecated - use {@link MetaModelService3#sortOf(Class, org.apache.isis.applib.services.metamodel.MetaModelService3.Mode)}
     */
    @Deprecated
    @Programmatic
    Sort sortOf(final Class<?> domainType);

    /**
     * @deprecated - use {@link MetaModelService3#sortOf(Bookmark, org.apache.isis.applib.services.metamodel.MetaModelService3.Mode)}
     */
    @Deprecated
    @Programmatic
    Sort sortOf(final Bookmark bookmark);

}
