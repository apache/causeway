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
package org.apache.causeway.viewer.commons.applib.services.menu.model;

import java.util.Optional;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaLayersProvider;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.util.Facets;
import org.springframework.lang.NonNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter @Accessors(fluent = true)
public class MenuAction implements MenuEntry {

        private static final long serialVersionUID = 1L;
        
		@NonNull private final Bookmark serviceBookmark;
        @NonNull private final Identifier actionId;
        @NonNull private final String name;
        @NonNull private final DecorationModel decorationModel;

    @AllArgsConstructor
    @Getter @Accessors(fluent = true)
    @Builder(access = AccessLevel.PRIVATE)
    public static class DecorationModel {
    	
    	private final boolean isPrototype;
    	private final int paramCount;
    	private final Optional<VetoReason> interactionVetoOpt;
    	private final Optional<FontAwesomeLayers> fontAwesomeLayersOpt;
    	private final Optional<String> describedAsOpt;
    	private final Optional<String> additionalCssClassOpt;
    	
        static DecorationModel of(final @NonNull ManagedAction managedAction) {
            var action = managedAction.getAction();
            return DecorationModel.builder()
                .isPrototype(action.isPrototype())
                .paramCount(action.getParameterCount())
                .interactionVetoOpt(managedAction.checkUsability()
                    .flatMap(veto->veto.getReason()))
                .fontAwesomeLayersOpt(ObjectAction.Util.cssClassFaFactoryFor(
                    managedAction.getAction(),
                    managedAction.getOwner())
                    .map(FaLayersProvider::getLayers)
                    .map(FontAwesomeLayers::emptyToBlank))
                .describedAsOpt(managedAction.getDescription())
                .additionalCssClassOpt(Facets.cssClass(action, managedAction.getOwner()))
                .build();
        }
    }

    public static MenuAction of(final @NonNull ManagedAction managedAction) {
        return new MenuAction(
                managedAction.getOwner().getBookmark().orElseThrow(),
                managedAction.getIdentifier(),
                managedAction.getFriendlyName(),
                DecorationModel.of(managedAction));
    }

    public Optional<ManagedAction> managedAction(){
        var mmc = MetaModelContext.instanceElseFail();
        var service = mmc.getObjectManager().debookmark(serviceBookmark);
        return ManagedAction.lookupAction(service, actionId.memberLogicalName(), Where.NOT_SPECIFIED);
    }

}
