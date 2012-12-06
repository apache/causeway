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

package org.apache.isis.viewer.html.component;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.html.PathBuilder;

public interface ComponentFactory extends PathBuilder {

    Component createAddOption(String id, String id2);

    Block createBlock(String style, String description);

    Component createBreadCrumbs(String[] names, boolean[] isLinked);

    Component createCollectionIcon(ObjectAssociation field, ObjectAdapter collection, String id);

    DebugPane createDebugPane();

    Component createEditOption(String id);

    Component createErrorMessage(Exception e, boolean isDebug);

    Form createForm(String id, String action, int step, int noOfPages, boolean b);

    Component createHeading(String string);

    Component createInlineBlock(String style, String text, String description);

    Component createCheckboxBlock(final boolean isEditable, final boolean isSet);

    Component createSubmenu(String menuName, Component[] items);

    Component createMenuItem(String actionId, String name, String description, String reasonDisabled, ActionType type, boolean hasParameters, String targetObjectId);

    Component createCollectionIcon(ObjectAdapter object, String collectionId);

    Component createObjectIcon(ObjectAdapter object, String objectId, String style);

    Component createObjectIcon(ObjectAssociation field, ObjectAdapter object, String objectId, String style);

    Page createPage();

    Component createRemoveOption(String id, String elementId, String id2);

    Component createService(String objectId, String title, String iconName);

    Table createTable(int noColumns, boolean withSelectorColumn);

    Component createUserSwap(final String name);

    /**
     * 
     * @param field
     * @param value
     *            - may be <tt>null</tt> so subclass should handle.
     * @param isEditable
     * @return
     */
    Component createParseableField(ObjectAssociation field, ObjectAdapter value, boolean isEditable);

    Component createLink(String link, String name, String description);
}
