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

package org.apache.isis.objectstore.xml.internal.data;

import com.google.common.base.Objects;

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;

public abstract class Data {
    
    private final RootOid oid;
    private final Version version;

    Data(final RootOid oid, final Version version) {
        this.oid = oid;
        this.version = version;
    }

    public RootOid getRootOid() {
        return oid;
    }

    public Version getVersion() {
        return version;
    }

    public ObjectSpecification getSpecification(SpecificationLoader specificationLookup) {
        final ObjectSpecId objectSpecId = oid.getObjectSpecId();
        return specificationLookup.lookupBySpecId(objectSpecId);
    }

    public ObjectSpecId getObjectSpecId() {
        return getRootOid().getObjectSpecId();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Data) {
            final Data data = (Data) obj;
            return Objects.equal(data.getObjectSpecId(), getObjectSpecId()) && Objects.equal(data.oid, oid);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 37 * h + getObjectSpecId().hashCode();
        h = 37 * h + oid.hashCode();
        return h;
    }

}
