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
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.mixins.MixinConstants;
import org.apache.isis.applib.services.layout.LayoutService;
import org.apache.isis.applib.value.BlobClobFactory;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Mixin(method="act") 
@RequiredArgsConstructor
public class Object_downloadLayoutXml {

    private final Object holder;

    public static class ActionDomainEvent
    extends org.apache.isis.applib.IsisApplibModule.ActionDomainEvent<Object_downloadLayoutXml> {
        private static final long serialVersionUID = 1L;
    }

    @Action(
            domainEvent = ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            contributed = Contributed.AS_ACTION,
            cssClassFa = "fa-download",
            position = ActionLayout.Position.PANEL_DROPDOWN
            )
    @MemberOrder(name = MixinConstants.METADATA_LAYOUT_GROUPNAME, sequence = "700.1")
    public Object act(
    		
    		// PARAM 0
    		@ParameterLayout(
            		named = MixinConstants.FILENAME_PROPERTY_NAME,
            		describedAs = MixinConstants.FILENAME_PROPERTY_DESCRIPTION)
            final String fileName,
            
            // PARAM 1
            final LayoutService.Style style) {

        val xmlString = layoutService.toXml(holder.getClass(), style);
        return BlobClobFactory.clobXml(fileName, xmlString);
    }

    // -- PARAM 0 (fileName)
    
    public String default0Act() {
        return holder.getClass().getSimpleName() + ".layout";
    }
    
    // -- PARAM 1 (style)
    
    public LayoutService.Style default1Act() {
        return LayoutService.Style.NORMALIZED;
    }
    
    // -- DEPENDENCIES

    @Inject LayoutService layoutService;

}
