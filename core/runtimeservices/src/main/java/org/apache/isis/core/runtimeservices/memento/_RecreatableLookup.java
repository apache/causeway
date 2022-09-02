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
package org.apache.isis.core.runtimeservices.memento;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.object.ManagedObjects;

import lombok.extern.log4j.Log4j2;

@Log4j2
class _RecreatableLookup implements _Recreatable{

    @Override
    public @Nullable ManagedObject recreateObject(
            final _ObjectMemento memento,
            final MetaModelContext mmc) {

        if(_NullSafe.isEmpty(memento.stringifiedBookmark)) {
            throw _Exceptions.illegalArgument(
                    "need an id to lookup an object, got logical-type %s", memento.logicalType);
        }

        final Bookmark bookmark = Bookmark.parseElseFail(memento.stringifiedBookmark);

        try {

            log.debug("lookup by oid [{}]", bookmark);
            return mmc.loadObject(bookmark).orElse(null);

        } finally {
            // possibly out-dated insight ...
            // a side-effect of AdapterManager#adapterFor(...) is that it will update the oid
            // with the correct version, even when there is a concurrency exception
            // we copy this updated oid string into our memento so that, if we retry,
            // we will succeed second time around

            memento.stringifiedBookmark = bookmark.stringify();
        }
    }

    @Override
    public void resetVersion(
            final _ObjectMemento memento,
            final MetaModelContext mmc) {

        //XXX REVIEW: this may be redundant because recreateAdapter also guarantees the version will be reset.
        ManagedObject adapter = recreateObject(memento, mmc);

        memento.stringifiedBookmark = ManagedObjects.stringifyElseFail(adapter);
    }

    @Override
    public Bookmark asPseudoBookmark(final _ObjectMemento memento) {
        return memento.asBookmark();
    }

    @Override
    public boolean equals(final _ObjectMemento oam, final _ObjectMemento other) {
        return other.recreateStrategy == RecreateStrategy.LOOKUP
                && oam.stringifiedBookmark.equals(other.stringifiedBookmark);
    }

    @Override
    public int hashCode(final _ObjectMemento oam) {
        return oam.stringifiedBookmark.hashCode();
    }
}
