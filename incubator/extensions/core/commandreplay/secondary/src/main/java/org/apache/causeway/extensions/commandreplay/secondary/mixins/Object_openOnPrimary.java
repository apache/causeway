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
package org.apache.causeway.extensions.commandreplay.secondary.mixins;

import java.net.MalformedURLException;
import java.net.URL;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.extensions.commandreplay.secondary.CausewayModuleExtCommandReplaySecondary;
import org.apache.causeway.extensions.commandreplay.secondary.config.SecondaryConfig;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@Action(
        commandPublishing = Publishing.DISABLED,
        domainEvent = Object_openOnPrimary.ActionDomainEvent.class,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.SAFE
)
@ActionLayout(
        cssClassFa = "fa-external-link-alt",
        position = ActionLayout.Position.PANEL_DROPDOWN,
        associateWith = LayoutConstants.FieldSetId.METADATA,
        sequence = "750.2"
)
@RequiredArgsConstructor
public class Object_openOnPrimary {

    public static class ActionDomainEvent
            extends CausewayModuleExtCommandReplaySecondary.ActionDomainEvent<Object_openOnPrimary> { }

    final Object object;

    @MemberSupport
    public URL act() {
        var baseUrlPrefix = lookupBaseUrlPrefix();
        var urlSuffix = bookmarkService.bookmarkForElseFail(object).toString();

        try {
            return new URL(baseUrlPrefix + urlSuffix);
        } catch (MalformedURLException e) {
            throw new RecoverableException(e);
        }
    }
    @MemberSupport public boolean hideAct() {
        return !secondaryConfig.isConfigured();
    }

    private String lookupBaseUrlPrefix() {
        return secondaryConfig.getPrimaryBaseUrlWicket() + "entity/";
    }

    @Inject SecondaryConfig secondaryConfig;
    @Inject BookmarkService bookmarkService;

}
