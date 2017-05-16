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
public interface MetaModelService3 extends MetaModelService2 {

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

    @Programmatic
    Sort sortOf(final Class<?> domainType, final Mode mode);

    @Programmatic
    Sort sortOf(final Bookmark bookmark, final Mode mode);

}
