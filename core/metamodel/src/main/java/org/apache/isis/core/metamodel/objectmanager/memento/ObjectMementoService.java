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

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.PackedManagedObject;

import lombok.NonNull;

/**
 * @since 2.0
 */
public interface ObjectMementoService {

    ObjectMemento mementoForBookmark(@NonNull Bookmark bookmark);

    ObjectMemento mementoForObject(ManagedObject adapter);

    ObjectMemento mementoForObjects(PackedManagedObject adapter);

    ObjectMemento mementoForPojo(Object pojo);

    ObjectMemento mementoForPojos(Iterable<Object> iterablePojos, LogicalType logicalType);

    ManagedObject reconstructObject(ObjectMemento memento);

    ObjectMemento mementoForParameter(@NonNull ManagedObject paramAdapter);


}
