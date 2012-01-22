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

package org.apache.isis.viewer.dnd.view;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.dnd.drawing.Image;

public interface Content {

    /**
     * Determines if the specified content can be drop on this content.
     */
    Consent canDrop(Content sourceContent);

    /**
     * Allows this content to add menu options to the set of menu options the
     * user will see for this content.
     * 
     * @see #viewMenuOptions(UserActionSet)
     */
    void contentMenuOptions(UserActionSet options);

    void debugDetails(DebugBuilder debug);

    /**
     * Implements the response to the dropping of the specified content onto
     * this content.
     */
    ObjectAdapter drop(Content sourceContent);

    String getDescription();

    String getHelp();

    /**
     * The name of the icon to use to respresent the object represented by this
     * content.
     */
    String getIconName();

    /**
     * The icon to use to respresent the object represented by this content.
     */
    Image getIconPicture(int iconHeight);

    String getId();

    /**
     * The object represented by this content.
     */
    ObjectAdapter getAdapter();

    ObjectAdapter[] getOptions();

    /**
     * The specification of the object represented by this content.
     */
    ObjectSpecification getSpecification();

    /**
     * Returns true if this content represents a CollectionAdapter.
     */
    boolean isCollection();

    /**
     * Returns true if this content represents a ObjectAdapter.
     */
    boolean isObject();

    /**
     * Returns true if the object represented by this content can be persisted.
     */
    boolean isPersistable();

    boolean isOptionEnabled();

    /**
     * Returns true if the object represented by this content is transient; has
     * not been persisted yet.
     */
    boolean isTransient();

    boolean isTextParseable();

    String title();

    /**
     * Allows this content to add menu options to the set of menu options the
     * user will see for this view.
     * 
     * @see #contentMenuOptions(UserActionSet)
     */
    void viewMenuOptions(UserActionSet options);

    String windowTitle();
}
