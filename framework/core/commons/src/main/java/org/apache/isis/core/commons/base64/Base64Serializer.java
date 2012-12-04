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
package org.apache.isis.core.commons.base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;

public class Base64Serializer {

    public static class Exception extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public Exception(java.lang.Exception e) {
            super(e);
        }
    }

    public static Object fromString( String s ) {
        final byte [] data = Base64.decodeBase64( s );
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(  data ) );
            return ois.readObject();
        } catch (IOException e) {
            throw new Base64Serializer.Exception(e);
        } catch (ClassNotFoundException e) {
            throw new Base64Serializer.Exception(e);
        } finally {
            try {
                if(ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                throw new Base64Serializer.Exception(e);
            }
        }
    }

    public static String toString( Serializable serializable ) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream( baos );
            oos.writeObject( serializable );
            return new String( Base64.encodeBase64( baos.toByteArray() ) );
        } catch (IOException e) {
            throw new Base64Serializer.Exception(e);
        } finally {
            try {
                if(oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
                throw new Base64Serializer.Exception(e);
            }
        }
    }
}
