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

import java.util.Optional;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.layout.resource.LayoutResource;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.services.grid.GridService.LayoutKey;
import org.apache.causeway.applib.services.layout.LayoutService;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;

import lombok.RequiredArgsConstructor;

/**
 * Allows uploading of layout resources, that overrule the default layout resource lookup.
 *
 * @since 4.0 {@index}
 */
@Action(
		domainEvent = Object_patchLayout.ActionDomainEvent.class,
		semantics = SemanticsOf.IDEMPOTENT,
		commandPublishing = Publishing.DISABLED,
        executionPublishing = Publishing.DISABLED,
		restrictTo = RestrictTo.PROTOTYPING)
@ActionLayout(
        cssClassFa = "solid file-arrow-up",
		describedAs = "Uploads layout XML, to be stored in memory for this object type and current layout name. "
				+ "It overrules the default layout resource lookup. "
				+ "On application restart this information is lost.",
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        position = ActionLayout.Position.PANEL_DROPDOWN,
        sequence = "700.1.1")
// framework provided domain objects and mixins should explicitly specify their introspection policy
@DomainObject(nature=Nature.MIXIN, mixinMethod = "act", introspection = Introspection.ANNOTATION_REQUIRED)
@RequiredArgsConstructor
public class Object_patchLayout {

    public static class ActionDomainEvent
    extends org.apache.causeway.applib.CausewayModuleApplib.ActionDomainEvent<Object_patchLayout> {}

    @Inject LayoutService layoutService;
    @Inject GridService gridService;

    private final Object mixee;

    @MemberSupport public Object act(
            @ParameterLayout(multiLine = 20)
            final String layoutXml) {
    	layoutKey()
    		.ifPresent(layoutKey->{
    			gridService.addPatchedLayout(
    					layoutKey, 
    					new LayoutResource(layoutKey.resourceName(CommonMimeType.XML), CommonMimeType.XML, layoutXml));		
    		});
        return mixee;
    }

    @MemberSupport public boolean hideAct() {
        return layoutKey().isEmpty();
    }
    
    // -- HELPER
    
    private Optional<LayoutKey> layoutKey() {
    	return layoutService.layoutKey(mixee);
    }

}
