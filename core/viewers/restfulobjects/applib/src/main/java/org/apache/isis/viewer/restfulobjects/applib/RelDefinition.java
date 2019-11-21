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
package org.apache.isis.viewer.restfulobjects.applib;

/**
 * Enumerates the organization that defined a {@link Rel}.
 */
public enum RelDefinition {

    /**
     * {@link Rel} defined by IANA.
     */
    IANA(null, false),
    /**
     * {@link Rel} defined by the Restful Objects spec.
     */
    RO_SPEC("urn:org.restfulobjects:rels/", true),
    /**
     * Proprietary rel defined by implementation.
     */
    IMPL("urn:org.apache.isis.restfulobjects:rels/", true);

    private final String relPrefix;
    private final boolean addParams;

    private RelDefinition(String relPrefix, boolean canAddParams) {
        this.relPrefix = relPrefix;
        this.addParams = canAddParams;
    }

    public String nameOf(String relSuffix) {
        return (relPrefix != null? relPrefix:"") +relSuffix;
    }
    public boolean canAddParams() {
        return addParams;
    }
}
