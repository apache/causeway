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

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
class _RecreatableLookup implements _Recreatable{

    @Override
    public @Nullable ManagedObject recreateObject(
            final ObjectMementoForScalar memento,
            final MetaModelContext mmc) {

        if(memento.bookmark==null) {
            throw _Exceptions.illegalArgument(
                    "need an id to lookup an object, got logical-type %s", memento.logicalType);
        }

        val bookmark = memento.bookmark;

        log.debug("lookup by {}", bookmark);
        return mmc.getObjectManager().loadObjectElseFail(bookmark);
    }

    @Override
    public boolean equals(final ObjectMementoForScalar oam, final ObjectMementoForScalar other) {
        return other.recreateStrategy == RecreateStrategy.LOOKUP
                && oam.bookmark.equals(other.bookmark);
    }

    @Override
    public int hashCode(final ObjectMementoForScalar oam) {
        return oam.bookmark.hashCode();
    }
}
