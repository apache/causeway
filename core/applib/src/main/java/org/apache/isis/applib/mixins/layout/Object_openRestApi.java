/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.mixins.layout;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.swagger.SwaggerService;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.commons.internal.resources._Resources;

import lombok.RequiredArgsConstructor;

@Mixin(method="act") @RequiredArgsConstructor
public class Object_openRestApi {

    private final Object holder;

    public static class ActionDomainEvent
    extends org.apache.isis.applib.IsisApplibModule.ActionDomainEvent<Object_openRestApi> {
        private static final long serialVersionUID = 1L;
    }

    @Action(
            domainEvent = ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            contributed = Contributed.AS_ACTION,
            cssClassFa = "fa-external-link",
            position = ActionLayout.Position.PANEL_DROPDOWN
            )
    @MemberOrder(name = "datanucleusIdLong", sequence = "750.1")
    public LocalResourcePath act() {
        Bookmark bookmark = bookmarkService.bookmarkFor(holder);

        return new LocalResourcePath(String.format(
                "/%s/objects/%s/%s",
                _Resources.getRestfulPathIfAny(),
                bookmark.getObjectType(),
                bookmark.getIdentifier()));
    }

    @Inject BookmarkService bookmarkService;
    @Inject SwaggerService swaggerService;

}
