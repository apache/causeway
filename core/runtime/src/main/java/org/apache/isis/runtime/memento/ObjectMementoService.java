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
package org.apache.isis.runtime.memento;

import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;

import lombok.val;

/**
 * @since 2.0
 * 
 *
 */
public interface ObjectMementoService {

    ObjectMemento mementoForRootOid(RootOid rootOid);

    ObjectMemento mementoForAdapter(ManagedObject adapter);

    ObjectMemento mementoForPojo(Object pojo);
    
    default ObjectMemento mementoForPojos(Iterable<Object> iterablePojos, ObjectSpecId specId) {
        val listOfMementos = _NullSafe.stream(iterablePojos)
                .map(pojo->mementoForPojo(pojo))
                .collect(Collectors.toList());
        val memento =
                ObjectMemento.wrapMementoList(listOfMementos, specId);
        return memento;
    }

    ManagedObject reconstructObject(ObjectMemento memento);
    

}
