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
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.valuetypes.ValueSemanticsAdapter;
import org.apache.isis.schema.common.v2.OidDto;

@Component
@Named("isis.val.OidDtoValueSemantics")
public class OidDtoValueSemantics
extends ValueSemanticsAdapter<OidDto, Bookmark, Void> {

    @Inject BookmarkValueSemantics bookmarkValueSemantics;

    @Override
    public Class<OidDto> getCorrespondingClass() {
        return OidDto.class;
    }

    @Override
    public ValueSemanticsAbstract<Bookmark> getDelegate() {
        return bookmarkValueSemantics;
    }

    @Override
    public OidDto fromDelegateValue(final Bookmark value) {
        return value!=null ? value.toOidDto() : null;
    }

    @Override
    public Bookmark toDelegateValue(final OidDto value) {
        return value!=null ? Bookmark.forOidDto(value) : null;
    }

    @Override
    public Can<OidDto> getExamples() {
        return Can.of(
                Bookmark.parseElseFail("a:b").toOidDto(),
                Bookmark.parseElseFail("c:d").toOidDto());
    }

}
