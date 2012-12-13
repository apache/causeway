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
package org.apache.isis.objectstore.jdo.datanucleus.valuetypes;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.core.progmodel.facets.value.blobs.BlobValueSemanticsProvider;
import org.datanucleus.store.types.converters.TypeConverter;

public class IsisBlobConverter implements TypeConverter<Blob, String>{

    private static final long serialVersionUID = 1L;
    private EncoderDecoder<Blob> encoderDecoder;

    public IsisBlobConverter() {
        encoderDecoder = new BlobValueSemanticsProvider().getEncoderDecoder();
    }
    
    @Override
    public String toDatastoreType(Blob memberValue) {
        return encoderDecoder.toEncodedString(memberValue);
    }

    @Override
    public Blob toMemberType(String datastoreValue) {
        return encoderDecoder.fromEncodedString(datastoreValue);
    }

}
