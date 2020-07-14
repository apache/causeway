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
package org.apache.isis.core.metamodel.objectmanager.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.isis.core.commons.internal.base._Bytes;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
final class ObjectSerializer_builtinHandlers {

    @Data
    public static class SerializeSerializable implements ObjectSerializer.Handler {
        
        private MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(ObjectSpecification spec) {
            return spec.isSerializable();
        }

        @SneakyThrows
        @Override
        public byte[] serialize(ManagedObject object) {
            val bos = new ByteArrayOutputStream(16*1024); // 16k initial buffer size
            try(val oos = new ObjectOutputStream(bos)){
                oos.writeObject(object.getPojo());
                oos.flush();
                return _Bytes.compress(bos.toByteArray());
            } 
        }
        
        @SneakyThrows
        @Override
        public Object deserialize(ObjectSpecification spec, byte[] serializedObjectBytes) {
            val pojoType = spec.getCorrespondingClass();
            return unmarshall(pojoType, serializedObjectBytes);
        }
        
        private <T> T unmarshall(Class<T> type, byte[] input) throws IOException, ClassNotFoundException {
            try(val bis = new ByteArrayInputStream(_Bytes.decompress(input))){
                val ois = new ObjectInputStream(bis);
                @SuppressWarnings("unchecked")
                val t = (T) ois.readObject();
                return t;
            }
        }
        
    }
    
    @Data
    public static class SerializeOther implements ObjectSerializer.Handler {
        
        private MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(ObjectSpecification spec) {
            return true; // the last handler in the chain
        }
        
        @Override
        public byte[] serialize(ManagedObject object) {
            throw _Exceptions.illegalArgument(
                    "None of the registered ObjectSerializers knows how to serialize this object. "
                    + "(when serializing pojo as held by ManagedObject %s)", 
                        object);
        }

        @Override
        public Object deserialize(ObjectSpecification spec, byte[] serializedObjectBytes) {
            throw _Exceptions.illegalArgument(
                    "None of the registered ObjectSerializers knows how to de-serialize "
                    + "an object having ObjectSpecification %s", 
                        spec);
        }

    }



}
