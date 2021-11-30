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
package org.apache.isis.core.metamodel.objectmanager.memento;

import java.util.List;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.handler.ChainOfResponsibility;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

public interface ObjectMemorizer {

    /**
     * Does both, serialize or deserialize, depending on the request's type.
     * @apiNote Rather use the more convenient specialized variants
     * {@link #serialize(ManagedObject)} and {@link #deserialize(ObjectSpecification, ObjectMemento)}
     * @param request
     */
    BiForm serializeObject(BiForm request);

    default ManagedObject deserialize(final ObjectSpecification spec, final ObjectMemento memento) {
        val request = BiForm.deSerializationRequest(SerializedObject.of(spec, memento));
        val response = serializeObject(request);
        return response.getObject();
    }

    default ObjectMemento serialize(final ManagedObject object) {
        val request = BiForm.serializationRequest(object);
        val response = serializeObject(request);
        return response.getSerializedObject().getMemento();
    }

    @Value(staticConstructor = "of")
    static class SerializedObject {
        @NonNull private ObjectSpecification specification;
        @NonNull private ObjectMemento memento;
    }

    // -- BOTH, REQUEST AND RESPONSE OBJECT FOR THE HANDLER

    @Value(staticConstructor = "of")
    static class BiForm {
        @Nullable private ManagedObject object;
        @Nullable private SerializedObject serializedObject;
        public boolean isSerialized() { return serializedObject!=null; }
        public boolean isDeserialized() { return object!=null; }
        public static BiForm serializationRequest(final ManagedObject object) {
            return of(object, null);
        }
        public static BiForm deSerializationRequest(final SerializedObject serializedObject) {
            return of(null, serializedObject);
        }
        public static BiForm serializationResponse(final SerializedObject serializedObject) {
            return of(null, serializedObject);
        }
        public static BiForm deSerializationResponse(final ManagedObject object) {
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

        @Override
        default boolean isHandling(final BiForm request) {
            val spec = request.getSpecification();
            return isHandling(spec);
        }

        @Override
        default BiForm handle(final BiForm request) {
            val spec = request.getSpecification();
            if(request.isSerialized()) {
                val memento = request.getSerializedObject().getMemento();
                return BiForm.deSerializationResponse(deserialize(spec, memento));
            } else {
                val memento = serialize(request.getObject());
                return BiForm.serializationResponse(SerializedObject.of(spec, memento));
            }
        }

        boolean isHandling(ObjectSpecification spec);
        ManagedObject deserialize(ObjectSpecification spec, ObjectMemento memento);
        ObjectMemento serialize(ManagedObject object);
    }

    // -- FACTORY

    public static ObjectMemorizer createDefault(final MetaModelContext metaModelContext) {

        val serviceInjector = metaModelContext.getServiceInjector();
        val chainOfHandlers = List.<ObjectMemorizer.Handler>of(
                new ObjectMemorizer_builtinHandlers.MemorizeViaObjectMementoService(),
                new ObjectMemorizer_builtinHandlers.MemorizeOther()
                );

        if(metaModelContext instanceof MetaModelContext_forTesting) {
            ((MetaModelContext_forTesting)(metaModelContext))
            .registerPostconstruct(()->chainOfHandlers.forEach(serviceInjector::injectServicesInto));
        } else {
            chainOfHandlers.forEach(serviceInjector::injectServicesInto);
        }

        return request -> ChainOfResponsibility.of(chainOfHandlers)
                .handle(request);
    }

}
