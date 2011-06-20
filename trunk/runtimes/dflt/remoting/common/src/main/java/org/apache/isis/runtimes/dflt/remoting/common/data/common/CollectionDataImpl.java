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

package org.apache.isis.runtimes.dflt.remoting.common.data.common;

import java.io.IOException;
import java.io.Serializable;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.encoding.Encodable;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;

public class CollectionDataImpl implements CollectionData, Encodable, Serializable {

    private static final long serialVersionUID = 1L;
    private final ReferenceData elements[];
    private final Oid oid;
    private final String collectionType;
    private final Version version;
    private final boolean hasAllElements;
    private final String elementType;

    public CollectionDataImpl(final Oid oid, final String collectionType, final String elementType,
        final ReferenceData[] elements, final boolean hasAllElements, final Version version) {
        this.collectionType = collectionType;
        this.elementType = elementType;
        this.oid = oid;
        this.version = version;
        this.hasAllElements = hasAllElements;
        this.elements = elements;
        initialized();
    }

    public CollectionDataImpl(final DataInputExtended input) throws IOException {
        this.collectionType = input.readUTF();
        this.elementType = input.readUTF();
        this.oid = input.readEncodable(Oid.class);
        this.version = input.readEncodable(Version.class);
        this.hasAllElements = input.readBoolean();
        this.elements = input.readEncodables(ReferenceData.class);
        initialized();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeUTF(collectionType);
        output.writeUTF(elementType);
        output.writeEncodable(oid);
        output.writeEncodable(version);
        output.writeBoolean(hasAllElements);
        output.writeEncodables(elements);
    }

    private void initialized() {
        // nothing to do
    }

    // ///////////////////////////////////////////////////////
    //
    // ///////////////////////////////////////////////////////

    @Override
    public ReferenceData[] getElements() {
        return elements;
    }

    @Override
    public String getElementype() {
        return elementType;
    }

    @Override
    public Oid getOid() {
        return oid;
    }

    @Override
    public String getType() {
        return collectionType;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public boolean hasAllElements() {
        return hasAllElements;
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("collection type", collectionType);
        str.append("element type", elementType);
        str.append("oid", oid);
        str.append("version", version);
        str.append(",elements=");
        for (int i = 0; elements != null && i < elements.length; i++) {
            if (i > 0) {
                str.append(";");
            }
            if (elements[i] == null) {
                str.append("null");
            } else {
                final String name = elements[i].getClass().getName();
                str.append(name.substring(name.lastIndexOf('.') + 1));
            }
        }
        return str.toString();
    }
}
