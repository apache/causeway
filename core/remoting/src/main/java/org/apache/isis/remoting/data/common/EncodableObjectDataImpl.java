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
import org.apache.isis.metamodel.encoding.DataInputExtended;
import org.apache.isis.metamodel.encoding.DataInputStreamExtended;
import org.apache.isis.metamodel.encoding.DataOutputExtended;
import org.apache.isis.metamodel.encoding.Encodable;

public class EncodableObjectDataImpl implements EncodableObjectData, Encodable, Serializable {
	
    private static final long serialVersionUID = 1L;
    private final String type;
    private final String objectAsString;

    public EncodableObjectDataImpl(final String type, final String objectAsString) {
        this.type = type == null ? null : type.equals(String.class.getName()) ? "s" : type;
        this.objectAsString = objectAsString;
        initialized();
    }

    public EncodableObjectDataImpl(final DataInputExtended input) throws IOException {
        this.type = input.readUTF();
        this.objectAsString = input.readUTF();
        initialized();
    }

    public void encode(final DataOutputExtended output) throws IOException {
        output.writeUTF(type);
        output.writeUTF(objectAsString);
    }

	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////
    

    public String getEncodedObjectData() {
        return objectAsString;
    }

    public String getType() {
        return type.equals("s") ? String.class.getName() : type;
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("type", type);
        str.append("value", objectAsString);
        return str.toString();
    }

}
