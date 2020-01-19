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

package org.apache.isis.core.metamodel.adapter.oid;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.schema.common.v2.OidDto;

final class Oid_Value implements RootOid {

    private final static long serialVersionUID = 2L;

    static final Oid_Value INSTANCE = new Oid_Value();

    @Override
    public String enString() {
        return null;
    }

    @Override
    public boolean isValue() { 
        return true; 
    }

    @Override
    public ObjectSpecId getObjectSpecId() {
        return null;
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public String toString() {
        return "VALUE_OID";
    }

    @Override
    public boolean equals(Object obj) {
        return obj == INSTANCE;
    }
    
    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public Bookmark asBookmark() {
        return null;
    }

    @Override
    public OidDto asOidDto() {
        return null;
    }

}
