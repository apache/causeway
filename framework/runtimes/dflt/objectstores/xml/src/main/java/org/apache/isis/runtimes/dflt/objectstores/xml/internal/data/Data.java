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

package org.apache.isis.runtimes.dflt.objectstores.xml.internal.data;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.objectstores.xml.internal.version.FileVersion;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;

public abstract class Data {
    private final ObjectSpecification noSpec;
    private final SerialOid oid;
    private final FileVersion version;

    Data(final ObjectSpecification noSpec, final SerialOid oid, final FileVersion version) {
        this.noSpec = noSpec;
        this.oid = oid;
        this.version = version;
    }

    public SerialOid getOid() {
        return oid;
    }

    public FileVersion getVersion() {
        return version;
    }

    public ObjectSpecification getSpecification() {
        return noSpec;
    }

    public String getTypeName() {
        return noSpec.getFullIdentifier();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Data) {
            return ((Data) obj).getTypeName().equals(getTypeName()) && ((Data) obj).oid.equals(oid);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 37 * h + getTypeName().hashCode();
        h = 37 * h + oid.hashCode();
        return h;
    }

}
