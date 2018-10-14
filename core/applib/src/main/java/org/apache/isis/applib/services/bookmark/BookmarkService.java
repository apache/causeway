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
package org.apache.isis.applib.services.bookmark;

import org.apache.isis.applib.annotation.Programmatic;

/**
 * This service enables a serializable &quot;bookmark&quot; to be created for an entity.
 *
 * <p>
 * Because an implementation of this service (<tt>BookmarkServiceDefault</tt>) is annotated with
 * {@link org.apache.isis.applib.annotation.DomainService} and is implemented in the core.metamodel, it is
 * automatically registered and available for use; no configuration is required.
 * </p>
 */
public interface BookmarkService {

    @Programmatic
    Bookmark bookmarkFor(Object domainObject);

    @Programmatic
    Bookmark bookmarkFor(Class<?> cls, String identifier);

    @Programmatic
    Object lookup(BookmarkHolder bookmarkHolder, FieldResetPolicy fieldResetPolicy);

    @Programmatic
    Object lookup(Bookmark bookmark, FieldResetPolicy fieldResetPolicy);

    /**
     * As {@link #lookup(Bookmark, FieldResetPolicy)}, but down-casting to the specified type.
     */
    @Programmatic <T> T lookup(Bookmark bookmark, FieldResetPolicy fieldResetPolicy, Class<T> cls);

    enum FieldResetPolicy {
        /**
         * Will cause all fields of an object to be re-initialized.
         *
         * If the object is unresolved then the object's missing data should be retrieved from the persistence
         * mechanism and be used to set up the value objects and associations.
         *
         * If the object is a view model, then is ignored; the behaviour is as for {@link #DONT_REFRESH}
         *
         * @deprecated - retained for backwards compatibility with previous behaviour, but in most/all cases {@link #DONT_REFRESH} makes more sense/is less surprising.
         */
        @Deprecated
        RESET,
        /*p*
         * Required in order to recreate view models.
         */
        DONT_REFRESH
    }


}
