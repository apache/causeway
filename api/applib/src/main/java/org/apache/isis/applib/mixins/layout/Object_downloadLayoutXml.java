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
package org.apache.isis.applib.mixins.layout;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.ParameterLayout;
import org.apache.isis.applib.annotations.Publishing;
import org.apache.isis.applib.annotations.RestrictTo;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.mixins.dto.DtoMixinConstants;
import org.apache.isis.applib.services.layout.LayoutService;
import org.apache.isis.applib.services.layout.Style;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Provides the ability to download the layout XML for any domain
 * entity or view model.
 *
 * @since 1.x {@index}
 */
@Action(
        domainEvent = Object_downloadLayoutXml.ActionDomainEvent.class,
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING
)
@ActionLayout(
        cssClassFa = "fa-download",
        position = ActionLayout.Position.PANEL_DROPDOWN,
        associateWith = LayoutMixinConstants.METADATA_LAYOUT_GROUPNAME,
        sequence = "700.1"
)
//mixin's don't need a logicalTypeName
@RequiredArgsConstructor
public class Object_downloadLayoutXml {

    public static class ActionDomainEvent
    extends org.apache.isis.applib.IsisModuleApplib.ActionDomainEvent<Object_downloadLayoutXml> {}

    private final Object holder;

    @MemberSupport public Object act(
            @ParameterLayout(
                    named = DtoMixinConstants.FILENAME_PROPERTY_NAME,
                    describedAs = DtoMixinConstants.FILENAME_PROPERTY_DESCRIPTION)
            final String fileName,
            final Style style) {

        val xmlString = layoutService.toXml(holder.getClass(), style);
        return  Clob.of(fileName, CommonMimeType.XML, xmlString);
    }

    /**
     * Defaults to the (simple) name of the domain object's class, with a <code>.layout</code> suffix
     */
    @MemberSupport public String default0Act() {
        return holder.getClass().getSimpleName() + ".layout";
    }

    /**
     * Default style is {@link Style#NORMALIZED}.
     */
    @MemberSupport public Style default1Act() {
        return Style.NORMALIZED;
    }

    @Inject LayoutService layoutService;

}
