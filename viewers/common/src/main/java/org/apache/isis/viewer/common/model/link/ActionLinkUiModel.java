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
package org.apache.isis.viewer.common.model.link;

import java.util.List;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.HasUiComponent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionLinkUiModel<T> implements HasUiComponent<T> {

    public enum Parameters {
        NO_PARAMETERS,
        TAKES_PARAMETERS;

        public static Parameters fromParameterCount(final int parameterCount) {
            return parameterCount > 0? TAKES_PARAMETERS: NO_PARAMETERS;
        }

        public boolean isNoParameters() {
            return this == NO_PARAMETERS;
        }
    }
    
    @Getter private final String label;
    @Getter private final String descriptionIfAny;
    @Getter private final boolean blobOrClob;
    @Getter private final boolean prototype;
    @Getter private final String actionIdentifier;
    @Getter private final String cssClass;
    @Getter private final String cssClassFa;
    @Getter private final CssClassFaPosition cssClassFaPosition;
    @Getter private final ActionLayout.Position position;
    @Getter private final SemanticsOf semantics;
    @Getter private final PromptStyle promptStyle;
    @Getter private final Parameters parameters;
    
    @Getter(onMethod = @__(@Override)) @Setter private T uiComponent;
    
    public static <T> ActionLinkUiModel<T> of(
            final Class<T> uiComponentType,
            final ManagedObject actionHolder,
            final ObjectAction objectAction,
            final boolean blobOrClob) {
        return new ActionLinkUiModel<T>(actionHolder, objectAction, blobOrClob);
    };
    
    protected ActionLinkUiModel(
            final ManagedObject actionHolder,
            final ObjectAction objectAction,
            final boolean blobOrClob) {
        this(   ObjectAction.Util.nameFor(objectAction),
                ObjectAction.Util.descriptionOf(objectAction),
                blobOrClob, 
                objectAction.isPrototype(),
                ObjectAction.Util.actionIdentifierFor(objectAction),
                ObjectAction.Util.cssClassFor(objectAction, actionHolder), 
                ObjectAction.Util.cssClassFaFor(objectAction), 
                ObjectAction.Util.cssClassFaPositionFor(objectAction), 
                ObjectAction.Util.actionLayoutPositionOf(objectAction),
                objectAction.getSemantics(),
                ObjectAction.Util.promptStyleFor(objectAction),
                Parameters.fromParameterCount(objectAction.getParameterCount()));
    }
    
    public static <T extends ActionLinkUiModel<?>> List<T> positioned(
            final List<T> entityActionLinks,
            final ActionLayout.Position position) {
        
        return _Lists.filter(entityActionLinks, linkAndLabel -> linkAndLabel.getPosition() == position);
    }
    
}
