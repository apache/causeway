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
package org.apache.causeway.applib.mixins.layout;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.mixins.dto.DtoMixinConstants;
import org.apache.causeway.applib.services.layout.LayoutExportStyle;
import org.apache.causeway.applib.services.layout.LayoutService;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;

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
        describedAs = "Downloads the Xxx.layout.xml layout file effective/inferred for this object",
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        position = ActionLayout.Position.PANEL_DROPDOWN,
        sequence = "700.1"
)
//mixin's don't need a logicalTypeName
@RequiredArgsConstructor
public class Object_downloadLayoutXml {

    public static class ActionDomainEvent
    extends org.apache.causeway.applib.CausewayModuleApplib.ActionDomainEvent<Object_downloadLayoutXml> {}

    private final Object holder;

    @MemberSupport public Object act(
            @ParameterLayout(
                    named = DtoMixinConstants.FILENAME_PROPERTY_NAME,
                    describedAs = DtoMixinConstants.FILENAME_PROPERTY_DESCRIPTION)
            final String fileName,
            final LayoutExportStyle style) {

        val xmlString = layoutService.toXml(holder.getClass(), style);
        return Clob.of(fileName, CommonMimeType.XML, xmlString);
    }

    /**
     * Defaults to the (simple) name of the domain object's class, with a <code>.layout</code> suffix
     */
    @MemberSupport public String default0Act() {
        return holder.getClass().getSimpleName() + ".layout";
    }

    /**
     * Default style is {@link LayoutExportStyle#MINIMAL}.
     */
    @MemberSupport public LayoutExportStyle default1Act() {
        return LayoutExportStyle.defaults();
    }

    @Inject LayoutService layoutService;

}
