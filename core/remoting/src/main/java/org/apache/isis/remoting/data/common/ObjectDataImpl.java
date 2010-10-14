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

import org.apache.isis.commons.lang.ToString;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.version.Version;
import org.apache.isis.metamodel.encoding.DataInputExtended;
import org.apache.isis.metamodel.encoding.DataInputStreamExtended;
import org.apache.isis.metamodel.encoding.DataOutputExtended;
import org.apache.isis.metamodel.encoding.Encodable;
import org.apache.isis.remoting.data.Data;

public class ObjectDataImpl implements ObjectData, Encodable {
	
    private static final long serialVersionUID = 1L;
    private final Oid oid;
    private final boolean isResolved;
    private final String type;
    private final Version version;
    private Data fieldContent[];

    public ObjectDataImpl(
    		final Oid oid, final String type, final boolean resolved, final Version version) {
        this.type = type;
        this.oid = oid;
        this.version = version;
        this.isResolved = resolved;
        // fieldContent initially null
        initialized();
    }

    public ObjectDataImpl(final DataInputExtended input) throws IOException {
        this.type = input.readUTF();
        this.oid = input.readEncodable(Oid.class);
        this.version = input.readEncodable(Version.class);
        this.isResolved = input.readBoolean();
        this.fieldContent = input.readEncodables(Data.class);
        initialized();
    }

    public void encode(final DataOutputExtended output) throws IOException {
        output.writeUTF(type);
        output.writeEncodable(oid);
        output.writeEncodable(version);
        output.writeBoolean(isResolved);
        output.writeEncodables(fieldContent);
    }

	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////
    

    public Data[] getFieldContent() {
        return fieldContent;
    }

    public Oid getOid() {
        return oid;
    }

    public String getType() {
        return type;
    }

    public Version getVersion() {
        return version;
    }

    public boolean hasCompleteData() {
        return isResolved;
    }

    public void setFieldContent(final Data[] fieldContent) {
        this.fieldContent = fieldContent;
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("type", type);
        str.append("oid", oid);
        str.append("version", version);
        str.append(",fields=");
        for (int i = 0; fieldContent != null && i < fieldContent.length; i++) {
            if (i > 0) {
                str.append(";");
            }
            if (fieldContent[i] == null) {
                str.append("null");
            } else {
                final String name = fieldContent[i].getClass().getName();
                str.append(name.substring(name.lastIndexOf('.') + 1));
            }
        }
        return str.toString();
    }
}
