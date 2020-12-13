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

package org.apache.isis.applib.annotation;

import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.util.Enums;

/**
 * Represents the location in the user interface where a class member is to be rendered.
 *
 * <p>
 * Used to control visibility (eg using the {@link Hidden} annotation) and enablement
 * (eg using the {@link Disabled} annotation) in different regions of the user interface.
 *
 * <p>
 * The application programmer may use any of the values of this enum.  Some represent
 * concrete locations (eg {@link #OBJECT_FORMS}, {@link #PARENTED_TABLES}), whereas some
 * represent a combination of locations (eg {@link #ALL_TABLES}, {@link #ANYWHERE}).
 *
 * <h4>Framework Implementation Notes</h4>
 * <p>
 * This enum is also used internally within the framework.  When rendering an element,
 * the framework developer should only use those values that represent concrete locations.
 * @since 1.x {@index}
 */
// tag::refguide[]
@XmlType(
        namespace = "http://isis.apache.org/applib/layout/component"
        )
public enum Where {

    // end::refguide[]
    /**
     * The member should be disabled/hidden everywhere.
     *
     * <p>
     * Synonym for {@link #ANYWHERE}.
     */
    // tag::refguide[]
    EVERYWHERE {
        // end::refguide[]

        @Override
        public boolean includes(Where context) {
            return true;
        }

        // tag::refguide[]
        // ...
    },

    // end::refguide[]
    /**
     * The member should be disabled/hidden everywhere.
     *
     * <p>
     * Synonym for {@link #EVERYWHERE}.
     */
    // tag::refguide[]
    ANYWHERE {
        // end::refguide[]

        @Override
        public boolean includes(Where context) {
            return true;
        }

        // tag::refguide[]
        // ...
    },

    // end::refguide[]
    /**
     * The member should be disabled/hidden when displayed within an object form.
     *
     * <p>
     * For most viewers, this applies to property and collection members, not actions.
     */
    // tag::refguide[]
    OBJECT_FORMS,

    // end::refguide[]
    /**
     * The member should be disabled/hidden when displayed as a column of a table
     * within parent object's collection, and references that parent.
     *
     * <p>
     * For most (all?) viewers, this will have meaning only if applied to a property member.
     */
    // tag::refguide[]
    REFERENCES_PARENT,

    // end::refguide[]
    /**
     * The member should be disabled/hidden when displayed as a column of a table within
     * a parent object's collection.
     *
     * <p>
     * For most (all?) viewers, this will have meaning only if applied to a property member.
     */
    // tag::refguide[]
    PARENTED_TABLES ,

    // end::refguide[]
    /**
     * The member should be disabled/hidden when displayed as a column of a table showing a standalone list
     * of objects, for example as returned by a repository query.
     *
     * <p>
     * For most (all?) viewers, this will have meaning only if applied to a property member.
     */
    // tag::refguide[]
    STANDALONE_TABLES,

    // end::refguide[]
    /**
     * The member should be disabled/hidden when displayed as a column of a table, either an object's
     * collection or a standalone list.
     *
     * <p>
     * This combines {@link #PARENTED_TABLES} and {@link #STANDALONE_TABLES}.
     */
    // tag::refguide[]
    ALL_TABLES {
        // end::refguide[]

        @Override
        public boolean includes(Where context) {
            return context == this || context == PARENTED_TABLES || context == STANDALONE_TABLES;
        }

        // tag::refguide[]
        // ...
    },

    // end::refguide[]
    /**
     * The member should be disabled/hidden except when displayed as a column of a standalone table.
     *
     * <p>
     * This is the inverse of {@link #STANDALONE_TABLES}.
     */
    // tag::refguide[]
    ALL_EXCEPT_STANDALONE_TABLES {
        // end::refguide[]

        @Override
        public boolean includes(Where context) {
            return context != STANDALONE_TABLES;
        }

        // tag::refguide[]
        // ...
    },

    // end::refguide[]
    /**
     * To act as an override if a member would normally be hidden as a result of some other convention.
     *
     * <p>
     * For example, if a property is annotated with <tt>@Title</tt>, then normally this should be hidden
     * from all tables.  Additionally annotating with <tt>@Hidden(where=Where.NOWHERE)</tt> overrides this.
     */
    // tag::refguide[]
    NOWHERE {
        // end::refguide[]

        @Override
        public boolean includes(Where context) {
            return false;
        }

        // tag::refguide[]
        // ...
    },

    // end::refguide[]
    /**
     * Acts as the default no-op value for {@link PropertyLayout#hidden()}, {@link CollectionLayout#hidden()} and {@link ActionLayout#hidden()}.
     */
    // tag::refguide[]
    NOT_SPECIFIED {
        // end::refguide[]

        @Override
        public boolean includes(Where context) {
            return false;
        }

        // tag::refguide[]
        // ...
    };

    // end::refguide[]
    public String getFriendlyName() {
        return Enums.getFriendlyNameOf(this);
    }

    public boolean inParentedTable() {
        return this == PARENTED_TABLES || this == ALL_TABLES;
    }

    public boolean inStandaloneTable() {
        return this == STANDALONE_TABLES || this == ALL_TABLES;
    }

    /**
     * Whether this <tt>Where</tt> is a superset of the context <tt>Where</tt> provided.
     *
     * <p>
     * For example, {@link #ALL_TABLES} includes {@link #STANDALONE_TABLES}; {@link #ANYWHERE} includes all others.
     */
    public boolean includes(Where context) {
        return context == this;
    }

    // tag::refguide[]

}
// end::refguide[]
