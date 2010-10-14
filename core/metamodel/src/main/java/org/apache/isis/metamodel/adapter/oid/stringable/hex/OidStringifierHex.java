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


package org.apache.isis.metamodel.adapter.oid.stringable.hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.metamodel.encoding.DataInputStreamExtended;
import org.apache.isis.metamodel.encoding.DataOutputStreamExtended;


public class OidStringifierHex implements OidStringifier {

    public String enString(final Oid oid) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStreamExtended outputImpl = new DataOutputStreamExtended(baos);
        try {
			outputImpl.writeEncodable(oid);
			final byte[] byteArray = baos.toByteArray();
			return new String(Hex.encodeHex(byteArray));
		} catch (IOException e) {
			throw new IsisException("Failed to write object", e);
		}
    }

    public Oid deString(final String oidStr) {
        final char[] oidCharArray = oidStr.toCharArray();
        byte[] oidBytes;
        try {
            oidBytes = Hex.decodeHex(oidCharArray);
            final ByteArrayInputStream bais = new ByteArrayInputStream(oidBytes);
            final DataInputStreamExtended inputImpl = new DataInputStreamExtended(bais);
            return inputImpl.readEncodable(Oid.class);
        } catch (IOException ex) {
        	throw new IsisException("Failed to read object", ex);
		} catch (DecoderException ex) {
			throw new IsisException("Failed to hex decode object", ex);
		}
    }

}
