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
package org.apache.isis.viewer.common.model.components;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.core.metamodel.commons.StringExtensions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumerates the different types of UI <i>Components</i> that can be constructed
 * using component factories.
 *
 * <p>
 * Some are fine-grained (such as {@link ComponentType#SCALAR_NAME_AND_VALUE}, a
 * panel to represent a single scalar property or parameter), but others are
 * somewhat larger (such as {@link ComponentType#ENTITY}, representing an
 * entity, with its actions, properties and collections).
 */
@RequiredArgsConstructor
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
     * A single &quot;parented&quot; collection of an entity.
     *
     * <p>
     *     Note that the default implementation is actually a wrapper that provides a selector over all available
     *     individual representations of the entity by way of {@link #COLLECTION_CONTENTS} component type.  The
     *     framework provides a number of implementations of this lower-level component: as a table, as
     *     collapsed/hidden, as a summary.  Any additional representations that are found that can render the
     *     collection (eg map, calendar) are added to the selector.
     * </p>
     */
    ENTITY_COLLECTION,
    /**
     * A single standalone value, as might be returned from an action.
     */
    VALUE,
    /**
     * The name and value of a single property or parameter, ie a scalar.
     */
    SCALAR_NAME_AND_VALUE,
    /**
     * The parameter form (dialog box) of an action.
     */
    PARAMETERS,
    /**
     * The edit form (property value and buttons) of an property.
     */
    PROPERTY_EDIT_FORM,
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
     * Used to display a single property for editing.
     */
    PROPERTY_EDIT_PROMPT,
    /**
     * Top-level component for rendering a standalone collection (ie as returned by
     * an action).
     */
    STANDALONE_COLLECTION,
    /**
     * A collection of entities (the value of)
     */
    COLLECTION_CONTENTS,
    /**
     * A collection of entities (the value of)
     */
    COLLECTION_CONTENTS_EXPORT(Optionality.OPTIONAL),
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
     * A list of models, rendered as a list of links.
     */
    BOOKMARKED_PAGES,
    /**
     * Place holder for a component used to represent an unknown model;
     * not used for matching,
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

    private ComponentType() {
        this.optionality = Optionality.MANDATORY;
    }

    @Override
    public String toString() {
        return getId();
    }

    /**
     * Returns the {@link #name()} formatted as
     * {@link StringExtensions#toCamelCase(String) case}.
     *
     * <p>
     * For example, <tt>OBJECT_EDIT</tt> becomes <tt>objectEdit</tt>.
     */
    public String getId() {
        return StringExtensions.toCamelCase(name());
    }

    /**
     * Whether there must be a ComponentFactory for this type.
     */
    @Getter
    private final Optionality optionality;

    @Nullable
    public static ComponentType lookup(final String id) {
        for (final ComponentType componentType : values()) {
            if (componentType.getId().equals(id)) {
                return componentType;
            }
        }
        return null;
    }



}
