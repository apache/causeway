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
package org.apache.isis.applib.services.bookmark;

import javax.annotation.Priority;
import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;

import lombok.NonNull;

/**
 * Hmm, not sure if this is really needed...  HAVE REMOVED FROM MODULE
 *
 * The responsibility and usage of IdStringifier is in the handling of PKs (either datastore-definde or application-defined)
 * into a string.  So I can't see that this would ever be called...
 */
// @Component
@Priority(PriorityPrecedence.LATE)
public class IdStringifierForBookmarkDEFUNCT extends IdStringifier.Abstract<Bookmark> {

    @Inject
    public IdStringifierForBookmarkDEFUNCT() {
        super(Bookmark.class);
    }

    public String enstring(final @NonNull Bookmark object) {
        return object.toString();
    }

    @Override
    public Bookmark destring(final @NonNull String stringified, @NonNull Class<?> targetEntityClass) {
        return Bookmark.parseElseFail(stringified);
    }


}
