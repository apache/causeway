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
package org.apache.causeway.core.metamodel.object;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.hint.HintIdProvider;

import org.jspecify.annotations.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MmHintUtils {

    public String hintId(final @Nullable ManagedObject adapter) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) return null;
        var spec = adapter.getSpecification();
        return (spec.isIdentifiable() || spec.isParented())
                && adapter.getPojo() instanceof HintIdProvider hp
             ? hp.hintId()
             : null;
    }

    public Bookmark bookmarkElseFail(final @NonNull ManagedObject adapter) {
        var hintId = hintId(adapter);
        var bookmark = ManagedObjects.bookmarkElseFail(adapter);
        return hintId != null
                    ? bookmark.withHintId(hintId)
                    : bookmark;
    }
}
