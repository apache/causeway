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

import javax.annotation.Nullable;

import org.apache.isis.commons.handler.ChainOfResponsibility;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

public interface ObjectSerializer {

    /**
     * Does both, serialize or deserialize, depending on the request's type.
     * @apiNote Rather use the more convenient specialized variants
     * {@link #serialize(ManagedObject)} and {@link #deserialize(ObjectSpecification, byte[])}
     * @param request
     */
    BiForm serializeObject(BiForm request);
    
    default ManagedObject deserialize(ObjectSpecification spec, byte[] serializedObjectBytes) {
        val request = BiForm.deSerializationRequest(SerializedObject.of(spec, serializedObjectBytes));
        val response = serializeObject(request);
        return response.getObject();
    }
    
    default byte[] serialize(ManagedObject object) {
        val request = BiForm.serializationRequest(object);
        val response = serializeObject(request);
        return response.getSerializedObject().getSerializedObjectBytes();
    }

    @Value(staticConstructor = "of")
    static class SerializedObject {
        @NonNull private ObjectSpecification specification;
        @NonNull private byte[] serializedObjectBytes;
    }
    
    // -- BOTH, REQUEST AND RESPONSE OBJECT FOR THE HANDLER
    
    @Value(staticConstructor = "of")
    static class BiForm {
        @Nullable private ManagedObject object;
        @Nullable private SerializedObject serializedObject;
        public boolean isSerialized() { return serializedObject!=null; }
        public boolean isDeserialized() { return object!=null; }
        public static BiForm serializationRequest(ManagedObject object) {
            return of(object, null);
        }
        public static BiForm deSerializationRequest(SerializedObject serializedObject) {
            return of(null, serializedObject);
        }
        public static BiForm serializationResponse(SerializedObject serializedObject) {
            return of(null, serializedObject);
        }
        public static BiForm deSerializationResponse(ManagedObject object) {
            return of(object, null);
        }
        public ObjectSpecification getSpecification() {
            return isDeserialized()
                    ? object.getSpecification()
                    : serializedObject.getSpecification();
        }
    }

    // -- HANDLER

    static interface Handler
    extends ChainOfResponsibility.Handler<BiForm, BiForm> {
        
        default boolean isHandling(BiForm request) {
            val spec = request.getSpecification();
            return isHandling(spec);
        }
        
        default BiForm handle(BiForm request) {
            val spec = request.getSpecification();
            if(request.isSerialized()) {
                val serializedObjectBytes = request.getSerializedObject().getSerializedObjectBytes();
                return BiForm.deSerializationResponse(ManagedObject.of(spec, deserialize(spec, serializedObjectBytes)));
            } else {
                val serializedObjectBytes = serialize(request.getObject());
                return BiForm.serializationResponse(SerializedObject.of(spec, serializedObjectBytes));
            }
        }
        
        boolean isHandling(ObjectSpecification spec);
        Object deserialize(ObjectSpecification spec, byte[] serializedObjectBytes);
        byte[] serialize(ManagedObject object);
    }

    // -- FACTORY

    public static ObjectSerializer createDefault(MetaModelContext metaModelContext) {

        val chainOfHandlers = _Lists.<ObjectSerializer.Handler>of(
                new ObjectSerializer_builtinHandlers.SerializeSerializable(),
                new ObjectSerializer_builtinHandlers.SerializeOther()
                );

        val chainOfRespo = ChainOfResponsibility.of(chainOfHandlers);

        return request -> chainOfRespo.handle(request).orElse(null);

    }

}
