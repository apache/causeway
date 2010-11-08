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


package org.apache.isis.remoting.data.common;

import java.io.IOException;
import java.io.Serializable;

import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.encoding.DataInputExtended;
import org.apache.isis.core.metamodel.encoding.DataInputStreamExtended;
import org.apache.isis.core.metamodel.encoding.DataOutputExtended;
import org.apache.isis.core.metamodel.encoding.Encodable;

public class IdentityDataImpl implements IdentityData, Encodable {
	
    private static final long serialVersionUID = 1L;
    private final Oid oid;
    private final String type;
    private final Version version;

    public IdentityDataImpl(final String type, final Oid oid, final Version version) {
        this.type = type;
        this.oid = oid;
        this.version = version;
        initialized();
    }

    public IdentityDataImpl(final DataInputExtended input) throws IOException {
        this.type = input.readUTF();
        this.oid = input.readEncodable(Oid.class);
        this.version = input.readEncodable(Version.class);
        initialized();
    }

    public void encode(final DataOutputExtended output) throws IOException {
        output.writeUTF(type);
        output.writeEncodable(oid);
        output.writeEncodable(version);
    }

	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////

    
    public Oid getOid() {
        return oid;
    }

    public String getType() {
        return type;
    }

    public Version getVersion() {
        return version;
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("type", type);
        str.append("oid", oid);
        str.append("version", version);
        return str.toString();
    }
}
