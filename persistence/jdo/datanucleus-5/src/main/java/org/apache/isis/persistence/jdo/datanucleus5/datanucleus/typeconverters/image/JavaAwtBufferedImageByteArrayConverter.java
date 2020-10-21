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
package org.apache.isis.persistence.jdo.datanucleus5.datanucleus.typeconverters.image;

import java.awt.image.BufferedImage;

import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.store.types.converters.TypeConverter;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.image._Images;

public class JavaAwtBufferedImageByteArrayConverter implements TypeConverter<BufferedImage, byte[]> {

    // generated 
    private static final long serialVersionUID = 5481131533536472276L;

    @Override
    public byte[] toDatastoreType(BufferedImage memberValue) {
        if (memberValue == null) {
            return null;
        }
        try {
            return _Images.toBytes(memberValue);
        } catch (Exception e) {
            throw new NucleusDataStoreException("Error serialising object of type BufferedImage to byte array", e);
        }
    }

    @Override
    public BufferedImage toMemberType(byte[] datastoreValue) {
        if (_NullSafe.size(datastoreValue) == 0) {
            return null;
        }
        try {
            return _Images.fromBytes(datastoreValue);
        } catch (Exception e) {
            throw new NucleusDataStoreException("Error deserialising image datastoreValue", e);
        }
    }

}
