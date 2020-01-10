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
package org.apache.isis.viewer.restfulobjects.applib.domainobjects;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents the members within the {@link DomainObjectRepresentation}'s <tt>members</tt>
 * map.
 *
 * <p>
 * Do not confuse with the subclasses of {@link AbstractObjectMemberRepresentation}, which
 * are standalone representations in their own right.
 */
public class DomainObjectMemberRepresentation extends DomainRepresentation  {

    public DomainObjectMemberRepresentation(final JsonNode jsonNode) {
        super(jsonNode);
    }

    /**
     * Whether this is a <tt>property</tt>, <tt>collection</tt> or <tt>action</tt>.
     */
    public String getMemberType() {
        return getString("memberType");
    }

    /**
     * The reason this member cannot be modified (if property or collection) or
     * invoked (if action).
     *
     * <p>
     * If null, then is not disabled.
     */
    public String getDisabledReason() {
        return getString("disabledReason");
    }

    public String getFormat() {
        return getString("format");
    }

    /**
     * Isis-specific extension; not part of the RO API;
     */
    public String getXIsisFormat() {
        return getString("extensions.x-isis-format");
    }

}
