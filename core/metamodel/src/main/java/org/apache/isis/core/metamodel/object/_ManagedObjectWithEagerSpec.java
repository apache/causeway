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
package org.apache.isis.core.metamodel.object;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;

//@Value
//@RequiredArgsConstructor(staticName="of", access = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName="of", access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "pojo", callSuper = false)
@ToString(of = {"specification", "pojo"}) //ISIS-2317 make sure toString() is without side-effects
@Getter
final class _ManagedObjectWithEagerSpec
extends _ManagedObjectWithBookmark {

    public static ManagedObject identified(
            final @NonNull  ObjectSpecification spec,
            final @Nullable Object pojo,
            final @NonNull  Bookmark bookmark) {

        if(pojo!=null) {
            _Assert.assertFalse(_Collections.isCollectionOrArrayOrCanType(pojo.getClass()));
        }

        val managedObject = _ManagedObjectWithEagerSpec.of(spec, pojo);
        managedObject.bookmarkLazy.set(Optional.of(bookmark));
        return managedObject;
    }

    @NonNull private final ObjectSpecification specification;
    @Nullable private /*final*/ Object pojo;

    @Override
    public void replacePojo(final UnaryOperator<Object> replacer) {
        pojo = replacer.apply(pojo);
        assertSpecIsInSyncWithPojo();
    }

}