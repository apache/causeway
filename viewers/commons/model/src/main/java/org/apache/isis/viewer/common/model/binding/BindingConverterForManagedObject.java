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
package org.apache.isis.viewer.common.model.binding;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public final class BindingConverterForManagedObject<T>
implements BindingConverter<ManagedObject, T> {

    @Getter private final ObjectSpecification valueSpecification;

    @Override
    public ManagedObject toLeft(final T pojo) {
        return ManagedObject.of(getValueSpecification(), pojo);
    }

    @Override
    public T toRight(final ManagedObject adapter) {
        return _Casts.uncheckedCast(ManagedObjects.UnwrapUtil.single(adapter));
    }

}




