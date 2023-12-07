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
package org.apache.causeway.applib.layout.component;

import java.util.List;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.commons.internal.collections._Lists;

/**
 * @since 1.x {@index}
 */
public interface ActionLayoutDataOwner extends Owner {

    List<ActionLayoutData> getActions();
    void setActions(List<ActionLayoutData> actions);

    default void addAction(final ActionLayoutData actionLayoutData) {
        if(getActions() == null) {
            setActions(_Lists.newArrayList());
        }
        actionLayoutData.setOwner(this);
        getActions().add(actionLayoutData);
    }

    public enum PositioningContext {
        /**
         * Positioning has NO meaning in this context, eg. <i>DomainObject</i> or <i>Collection</i>.
         */
        HAS_NONE,
        /**
         * This context provides a panel, eg. field-sets. But orientation has NO meaning.
         */
        HAS_PANEL,
        /**
         * In this context orientation has meaning, eg. detailed <i>Property</i> rendering.
         * But panel related positioning also has meaning.
         */
        HAS_ORIENTATION;

        //TODO[CAUSEWAY-3655] update java-doc
        /**
         * In a HAS_PANEL context, action's position is normalized to either
         * {@link org.apache.causeway.applib.annotation.ActionLayout.Position#PANEL_DROPDOWN} (default) or
         * {@link org.apache.causeway.applib.annotation.ActionLayout.Position#PANEL}.
         * <p>
         * In a HAS_ORIENTATION context, action's position is normalized to either
         * {@link org.apache.causeway.applib.annotation.ActionLayout.Position#BELOW} (default) or
         * {@link org.apache.causeway.applib.annotation.ActionLayout.Position#RIGHT}.
         * <p>
         * In a HAS_NONE context, action's position is without meaning, hence returning {@link Optional#empty()}.
         */
        public Optional<ActionLayout.Position> normalizePosition(final @Nullable ActionLayout.Position position) {
            switch (this) {
            case HAS_PANEL:
                if(ActionLayout.Position.isNullOrNotSpecified(position)
                    || ActionLayout.Position.isBelow(position)
                    || ActionLayout.Position.isRight(position)) {
                        return Optional.of(ActionLayout.Position.PANEL_DROPDOWN);
                }
                return Optional.of(position); // keep as is
            case HAS_ORIENTATION:
                if(ActionLayout.Position.isNullOrNotSpecified(position)
//                    || ActionLayout.Position.isPanelDropdown(position)
//                    || ActionLayout.Position.isPanel(position)
                        ) {
                        return Optional.of(ActionLayout.Position.BELOW);
                }
                return Optional.of(position); // keep as is
            case HAS_NONE:
            default:
                // positioning has no meaning in this context
                return Optional.empty();
            }
        }

    }

    PositioningContext positioningContext();

}
