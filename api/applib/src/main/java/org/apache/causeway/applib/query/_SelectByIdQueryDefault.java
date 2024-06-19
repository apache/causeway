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
package org.apache.causeway.applib.query;

import org.apache.causeway.commons.collections.Can;

import lombok.Getter;
import lombok.NonNull;

final class _SelectByIdQueryDefault<T>
extends _QueryAbstract<T>
implements SelectByIdQuery<T> {

    private static final long serialVersionUID = 1L;

    @Getter
    private final @NonNull Can<String> idsStringified;

    _SelectByIdQueryDefault(
            final @NonNull Class<T> type,
            final @NonNull Can<String> idsStringified,
            final @NonNull QueryRange range) {
        super(type, range);
        this.idsStringified = idsStringified;
    }

    @Override
    public String getDescription() {
        return getResultType().getName() + " (select by id)";
    }

    // -- WITHERS

    @Override
    public _SelectByIdQueryDefault<T> withRange(final @NonNull QueryRange range) {
        return new _SelectByIdQueryDefault<>(getResultType(), idsStringified, range);
    }

}
