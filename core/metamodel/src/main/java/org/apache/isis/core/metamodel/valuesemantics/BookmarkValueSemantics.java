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
package org.apache.isis.core.metamodel.valuesemantics;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.core.metamodel.valuetypes.ValueSemanticsAdapter;
import org.apache.isis.schema.common.v2.OidDto;

@Component
@Named("isis.val.BookmarkValueSemantics")
public class BookmarkValueSemantics
extends ValueSemanticsAdapter<Bookmark, OidDto, Void> {

    @Inject OidDtoValueSemantics oidDtoValueSemantics;

    @Override
    public Class<Bookmark> getCorrespondingClass() {
        return Bookmark.class;
    }

    @Override
    public ValueSemanticsAbstract<OidDto> getDelegate() {
        return oidDtoValueSemantics;
    }

    @Override
    public Bookmark fromDelegateValue(final OidDto value) {
        return value!=null ? Bookmark.forOidDto(value) : null;
    }

    @Override
    public OidDto toDelegateValue(final Bookmark value) {
        return value!=null ? value.toOidDto() : null;
    }

}
