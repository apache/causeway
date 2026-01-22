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
package org.apache.causeway.viewer.wicket.model.models;

import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.commons.model.action.UiActionForm;

import lombok.val;

public interface ActionModel
extends UiActionForm, FormExecutorContext, BookmarkableModel, IModel<ManagedObject> {

    /**
     * If underlying action, originates from an action-column, it has special page redirect semantics:
     * <ul>
     * <li>if action return is void or matches the originating table/collection's element-type, then just RELOAD page</li>
     * <li>otherwise open action result page in NEW browser tab</li>
     * </ul>
     * @since CAUSEWAY-3815
     */
    public enum ColumnActionModifier {
        /**
         * don't interfere with the default action result route
         */
        NONE,
        /**
         * reload current page, irrespective of the action result
         */
        FORCE_STAY_ON_PAGE,
        /**
         * open the action result in a new (blank) browser tab or window
         */
        FORCE_NEW_BROWSER_WINDOW;
        public boolean isNone() { return this == NONE; }
        public boolean isForceStayOnPage() { return this == FORCE_STAY_ON_PAGE; }
        public boolean isForceNewBrowserWindow() { return this == FORCE_NEW_BROWSER_WINDOW; }
    }
	
    /** Resets arguments to their fixed point default values
     * @see ActionInteractionHead#defaults(org.apache.causeway.core.metamodel.interactions.managed.ManagedAction)
     */
    void clearArguments();

    ManagedObject executeActionAndReturnResult();
    Can<ManagedObject> snapshotArgs();

    @Override
    default PromptStyle getPromptStyle() {
        val promptStyle = getAction().getPromptStyle();
        return promptStyle;
    }

}