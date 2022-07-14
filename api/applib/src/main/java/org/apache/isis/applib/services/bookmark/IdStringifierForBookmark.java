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

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.Priority;
import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.graph.tree.TreeState;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.memento._Mementos;

@Component
@Priority(PriorityPrecedence.LATE)
public class IdStringifierForBookmark extends IdStringifier.Abstract<Bookmark> {

    @Inject
    public IdStringifierForBookmark() {
        super(Bookmark.class, null);
    }

    public String stringify(final Bookmark object) {
        return object.toString();
    }

    @Override
    public Bookmark parse(final String stringified, Class<?> owningEntityType) {
        return Bookmark.parseElseFail(stringified);
    }


}
