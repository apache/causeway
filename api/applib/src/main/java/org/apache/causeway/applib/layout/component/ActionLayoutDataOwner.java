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

/**
 * @since 1.x {@index}
 */
public interface ActionLayoutDataOwner extends Owner {

    List<ActionLayoutData> getActions();
    void setActions(List<ActionLayoutData> actions);

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
         * But panel related positioning has NO meaning.
         */
        HAS_ORIENTATION;
    }

    PositioningContext positioningContext();

}
