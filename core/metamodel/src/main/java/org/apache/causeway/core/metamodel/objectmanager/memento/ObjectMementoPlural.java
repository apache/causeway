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
package org.apache.causeway.core.metamodel.objectmanager.memento;

import java.util.ArrayList;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

/**
 * @since 2.0
 */
@Value(staticConstructor = "of")
public final class ObjectMementoPlural implements ObjectMemento {

    private static final long serialVersionUID = 1L;

    private final ArrayList<ObjectMemento> container;

    @Getter(onMethod_ = {@Override})
    @NonNull private final LogicalType logicalType;

    @Getter
    @NonNull private final Identifier featureId;

    public static ObjectMementoPlural of(
            final ObjectFeature objectFeature,
            final ArrayList<ObjectMemento> container) {
        return of(container, objectFeature.getElementType().getLogicalType(), objectFeature.getFeatureIdentifier());
    }

    @Override
    public String getTitle() {
        throw _Exceptions.notImplemented(); // please unwrap at call-site
    }

    @Override
    public Bookmark getBookmark() {
        throw _Exceptions.notImplemented(); // please unwrap at call-site
    }

    public ArrayList<ObjectMemento> unwrapList() {
        return getContainer();
    }

}
