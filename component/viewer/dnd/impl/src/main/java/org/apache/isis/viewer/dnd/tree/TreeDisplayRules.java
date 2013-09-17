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

package org.apache.isis.viewer.dnd.tree;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.object.bounded.ChoicesFacetUtils;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.UserAction;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;

public class TreeDisplayRules {
    private static boolean showCollectionsOnly = false;

    private TreeDisplayRules() {
    }

    public static void menuOptions(final UserActionSet options) {
        // TODO fix and remove following line
        if (true) {
            return;
        }

        final UserAction option = new UserAction() {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                showCollectionsOnly = !showCollectionsOnly;
            }

            @Override
            public String getName(final View view) {
                return showCollectionsOnly ? "Show collections only" : "Show all references";
            }

            @Override
            public Consent disabled(final View view) {
                return Allow.DEFAULT;
            }

            @Override
            public String getDescription(final View view) {
                return "This option makes the system only show collections within the trees, and not single elements";
            }

            @Override
            public ActionType getType() {
                return ActionType.USER;
            }

            @Override
            public String getHelp(final View view) {
                return "";
            }
        };
        options.add(option);
    }

    public static boolean isCollectionsOnly() {
        return showCollectionsOnly;
    }

    private static boolean canDisplay(final ObjectAdapter object) {
        // TODO: rather than looking for the ChoicesFacet on the type, 
        // should look for the appropriate PropertyChoicesFacet, ActionParameterChoicesFacet or 
        // PropertyAutoCompleteFacet or ActionParameterAutoCompleteFacet
        final boolean lookupView = object != null && ChoicesFacetUtils.hasChoices(object.getSpecification());
        final boolean showNonCollections = !TreeDisplayRules.isCollectionsOnly();
        final boolean objectView = object instanceof ObjectAdapter && showNonCollections;
        final boolean collectionView = object.getSpecification().isParentedOrFreeCollection();
        return (objectView || collectionView) && !lookupView;
    }
}
