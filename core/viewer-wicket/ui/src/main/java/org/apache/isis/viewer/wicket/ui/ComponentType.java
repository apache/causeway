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

package org.apache.isis.viewer.wicket.ui;

import org.apache.wicket.Component;

import org.apache.isis.core.commons.lang.StringExtensions;

/**
 * Enumerates the different types of {@link Component}s that can be constructed
 * using {@link ComponentFactory}.
 * 
 * <p>
 * Some are fine-grained (such as {@link ComponentType#SCALAR_NAME_AND_VALUE}, a
 * panel to represent a single scalar property or parameter), but others are
 * somewhat larger (such as {@link ComponentType#ENTITY}, representing an
 * entity, with its actions, properties and collections).
 */
public enum ComponentType {

    
    /**
     * About page text.
     */
    ABOUT,
    /**
     * Welcome page text.
     */
    WELCOME,
    /**
     * List of services and their actions.
     * 
     * <p>
     * Could be rendered using a JavaScript or DHTML menu, an accordion, or a
     * tree view.
     */
    SERVICE_ACTIONS,
    /**
     * A single domain entity.
     */
    ENTITY,
    /**
     * Icon and title for a single entity.
     */
    ENTITY_ICON_AND_TITLE,
    /**
     * Icon, title and a copy link for a single entity.
     */
    ENTITY_ICON_TITLE_AND_COPYLINK,
    /**
     * Title, icon and action list for a single entity.
     */
    ENTITY_SUMMARY,
    /**
     * The set of properties for a single entity.
     */
    ENTITY_PROPERTIES,
    /**
     * The set of collections of a single entity, designed to be standalone outside of a form.
     * 
     * <p>
     * compare with {@value #ENTITY_COLLECTIONS}.
     */
    ENTITY_COLLECTIONS,
    /**
     * A single standalone value, as might be returned from an action.
     */
    VALUE,
    /**
     * The name and value of a single property or parameter, ie a scalar.
     */
    SCALAR_NAME_AND_VALUE,
    /**
     * The name and contents of a single collection of an entity;
     * {@link Component}s are expected to use {@link #COLLECTION_CONTENTS} to
     * actually render the contents.
     */
    COLLECTION_NAME_AND_CONTENTS,
    /**
     * The parameter form (dialog box) of an action.
     */
    PARAMETERS,
    /**
     * Info details for an action, eg to display the target, a resubmit button,
     * any description or help text, and so on.
     */
    ACTION_INFO,
    /**
     * Used to display the parameters of an action.
     */
    ACTION_PROMPT,
    /**
     * Top-level component for rendering a standalone collection (ie as returned by
     * an action).
     */
    STANDALONE_COLLECTION,
    /**
     * Bookmarked link to invoke an action
     */
    ACTION_LINK,
    /**
     * A collection of entities (the value of)
     */
    COLLECTION_CONTENTS,
    /**
     * A link to an entity.
     */
    ENTITY_LINK,
    /**
     * A collection of entities, from an action, but none returned.
     */
    EMPTY_COLLECTION,
    /**
     * A void result from an action.
     */
    VOID_RETURN,
    /**
     * A list of {@link IsisModel}s, rendered as a list of links.
     */
    BOOKMARKED_PAGES, 
    /**
     * Place holder for a component used to represent an unknown model;
     * not used for matching, since the {@link ComponentFactory} implementation
     * acts as a fallback whenever a more suitable factory cannot be located.
     */
    UNKNOWN,

    /**
     * The header (navigation bar) of the page
     */
    HEADER,

    /**
     * The footer of the page
     */
    FOOTER;

    /**
     * Returns the {@link #name()} formatted as
     * {@link org.apache.isis.core.commons.lang.StringExtensions#toCamelCase(String) case}.
     * 
     * <p>
     * For example, <tt>OBJECT_EDIT</tt> becomes <tt>objectEdit</tt>.
     */
    @Override
    public String toString() {
        return getWicketId();
    }

    public String getWicketId() {
        return StringExtensions.toCamelCase(name());
    }

    public static ComponentType lookup(final String id) {
        for (final ComponentType componentType : values()) {
            if (componentType.getWicketId().equals(id)) {
                return componentType;
            }
        }
        return null;
    }

}
