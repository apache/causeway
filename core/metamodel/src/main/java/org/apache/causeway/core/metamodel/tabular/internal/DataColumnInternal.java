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
package org.apache.causeway.core.metamodel.tabular.internal;

import java.util.Optional;

import org.apache.causeway.commons.internal.binding._Observables;
import org.apache.causeway.commons.internal.binding._Observables.LazyObservable;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.tabular.DataColumn;

import org.jspecify.annotations.NonNull;

record DataColumnInternal(
    @NonNull String columnId,
    @NonNull ObjectAssociation associationMetaModel,
    @NonNull LazyObservable<String> columnFriendlyNameObservable,
    @NonNull LazyObservable<Optional<String>> columnDescriptionObservable)
implements DataColumn {

    DataColumnInternal(final DataTableInternal parentTable, final ObjectAssociation associationMetaModel) {
        this(associationMetaModel.getId(),
            associationMetaModel,
            _Observables.lazy(associationMetaModel::getCanonicalFriendlyName),
            _Observables.lazy(associationMetaModel::getCanonicalDescription));
    }

}
