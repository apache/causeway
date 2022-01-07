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
package org.apache.isis.extensions.commandreplay.secondary.mixins;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.Publishing;
import org.apache.isis.applib.annotations.RestrictTo;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.exceptions.RecoverableException;
import org.apache.isis.applib.mixins.layout.LayoutMixinConstants;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.extensions.commandreplay.secondary.IsisModuleExtCommandReplaySecondary;
import org.apache.isis.extensions.commandreplay.secondary.config.SecondaryConfig;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * @since 2.0 {@index}
 */
@Action(
        domainEvent = Object_openOnPrimary.ActionDomainEvent.class,
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING
)
@ActionLayout(
        cssClassFa = "fa-external-link-alt",
        position = ActionLayout.Position.PANEL_DROPDOWN,
        associateWith = LayoutMixinConstants.METADATA_LAYOUT_GROUPNAME,
        sequence = "750.2"
)
@RequiredArgsConstructor
public class Object_openOnPrimary {

    public static class ActionDomainEvent
            extends IsisModuleExtCommandReplaySecondary.ActionDomainEvent<Object_openOnPrimary> { }

    final Object object;

    public URL act() {
        val baseUrlPrefix = lookupBaseUrlPrefix();
        val urlSuffix = bookmarkService.bookmarkForElseFail(object).toString();

        try {
            return new URL(baseUrlPrefix + urlSuffix);
        } catch (MalformedURLException e) {
            throw new RecoverableException(e);
        }
    }
    public boolean hideAct() {
        return !secondaryConfig.isConfigured();
    }

    private String lookupBaseUrlPrefix() {
        return secondaryConfig.getPrimaryBaseUrlWicket() + "entity/";
    }

    @Inject SecondaryConfig secondaryConfig;
    @Inject BookmarkService bookmarkService;

}
