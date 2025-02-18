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
package org.apache.causeway.applib.mixins.rest;

import java.util.Optional;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.value.LocalResourcePath;

import lombok.RequiredArgsConstructor;

/**
 * Provides the ability to navigate to the corresponding URL of this domain
 * object in the REST API provided by the <i>Restful Objects</i> viewer.
 *
 * @since 1.x {@index}
 */
@Action(
        commandPublishing = Publishing.DISABLED,
        domainEvent = Object_openRestApi.ActionDomainEvent.class,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.SAFE
)
@ActionLayout(
        cssClassFa = "fa-external-link-alt",
        describedAs = "Opens up a view of this object as represented in the (Restful Objects) REST API",
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        position = ActionLayout.Position.PANEL_DROPDOWN,
        sequence = "750.1"
)
@RequiredArgsConstructor
public class Object_openRestApi {

    public static class ActionDomainEvent
    extends org.apache.causeway.applib.CausewayModuleApplib.ActionDomainEvent<Object_openRestApi> {}

    private final Object holder;

    @MemberSupport public LocalResourcePath act() {
        var bookmark = bookmarkService.bookmarkForElseFail(holder);
        var logicalTypeName = bookmark.logicalTypeName();
        var objId = bookmark.identifier();

        var restfulPathIfAny = restfulPathProvider.getRestfulPath();

        final String format = restfulPathIfAny
                .map(path -> String.format("%s/objects/%s/%s", path, logicalTypeName, objId))
                .orElseGet(() -> String.format("/objects/%s/%s", logicalTypeName, objId));
        return new LocalResourcePath(format);
    }

    public interface RestfulPathProvider {
        Optional<String> getRestfulPath();
    }

    @Inject BookmarkService bookmarkService;
    @Inject RestfulPathProvider restfulPathProvider;

}
