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

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Data;

final class ObjectMemorizer_builtinHandlers {


    //TODO if we ever manage to make sense of the ObjectMementoServiceWicket

    @Data
    public static class MemorizeSerializable implements ObjectMemorizer.Handler {

        private MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(final ObjectSpecification spec) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Object deserialize(final ObjectSpecification spec, final ObjectMemento memento) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ObjectMemento serialize(final ManagedObject object) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Data
    public static class MemorizeOther implements ObjectMemorizer.Handler {

        private MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(final ObjectSpecification spec) {
            return true; // the last handler in the chain
        }

        @Override
        public ObjectMemento serialize(final ManagedObject object) {
            throw _Exceptions.illegalArgument(
                    "None of the registered ObjectMemorizers knows how to serialize this object. "
                    + "(when serializing pojo as held by ManagedObject %s)",
                        object);
        }

        @Override
        public Object deserialize(final ObjectSpecification spec, final ObjectMemento memento) {
            throw _Exceptions.illegalArgument(
                    "None of the registered ObjectMemorizers knows how to de-serialize "
                    + "an object having ObjectSpecification %s",
                        spec);
        }

    }

}
