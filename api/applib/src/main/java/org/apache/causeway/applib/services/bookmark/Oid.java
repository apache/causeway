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
package org.apache.causeway.applib.services.bookmark;

import java.io.Serializable;

/**
 * @since 1.x revised for 2.0 {@index}
 */
public interface Oid extends Serializable {

    static final String SEPARATOR = ":";

    /**
     * Logical-type-name of the domain object this Oid is representing.
     * (aka. object-type)
     */
    String getLogicalTypeName();

    /**
     * Stringified version of the ID of the domain object instance this Oid is representing.
     */
    String getIdentifier();

    /**
     * The canonical form of the {@link Bookmark}, that is
     * {@link #getLogicalTypeName() logical-type-name}{@value #SEPARATOR}{@link #getIdentifier() identifier}.
     */
    String stringify();

}
