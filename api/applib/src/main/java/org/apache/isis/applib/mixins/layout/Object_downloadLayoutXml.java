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

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.mixins.dto.DtoMixinConstants;
import org.apache.isis.applib.services.layout.LayoutService;
import org.apache.isis.applib.services.layout.Style;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Provides a mixin to download the layout XML for any domain object.
 *
 * @since 1.x {@index}
 */
@Action(
        domainEvent = Object_downloadLayoutXml.ActionDomainEvent.class,
        semantics = SemanticsOf.SAFE,
        restrictTo = RestrictTo.PROTOTYPING
)
@ActionLayout(
        cssClassFa = "fa-download",
        position = ActionLayout.Position.PANEL_DROPDOWN
)
@RequiredArgsConstructor
public class Object_downloadLayoutXml {

    public static class ActionDomainEvent
    extends org.apache.isis.applib.IsisModuleApplib.ActionDomainEvent<Object_downloadLayoutXml> {}

    private final Object holder;

    @MemberOrder(name = LayoutMixinConstants.METADATA_LAYOUT_GROUPNAME, sequence = "700.1")
    public Object act(
            @ParameterLayout(
                    named = DtoMixinConstants.FILENAME_PROPERTY_NAME,
                    describedAs = DtoMixinConstants.FILENAME_PROPERTY_DESCRIPTION)
            final String fileName,
            final Style style) {

        val xmlString = layoutService.toXml(holder.getClass(), style);
        return  Clob.of(fileName, CommonMimeType.XML, xmlString);
    }

    public String default0Act() {
        return holder.getClass().getSimpleName() + ".layout";
    }

    public Style default1Act() {
        return Style.NORMALIZED;
    }

    @Inject LayoutService layoutService;

}
