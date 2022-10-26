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
 *
 */
package org.apache.causeway.applib.layout;

import org.apache.causeway.applib.annotation.PropertyLayout;

import lombok.experimental.UtilityClass;

/**
 * Constant identifiers of the <code>layout.xml</code> that are commonly used in layouts.
 *
 * <p>
 *     All of the mixins provided by the framework are bound to the {@link FieldSetId#METADATA metadata} field set,
 *     and so - unless explicitly specified in the object's layout to some other location, will expect that the layout
 *     includes a field set with this id.
 * </p>
 *
 * <p>
 *     The service that loads layouts is {@link org.apache.causeway.applib.services.grid.GridSystemService}, and the
 *     framework's default implementation of this service supports the notion of a fallback layout, used whenever
 *     an object has no associated layout file.  This fallback layout <i>does</i> expose a
 *     {@link FieldSetId#METADATA metadata} fieldset.  (The fallback layout itself can be found in the
 *     <code>GridFallbackLayout.xml</code> file).
 * </p>
 *
 * @since 2.x {@index}
 */
@UtilityClass
public final class LayoutConstants {

    @UtilityClass
    public static class FieldSetId {

        /**
         * The id for a field set intended to hold the properties/fields that uniquely identify the object from the
         * end-user's perspective.
         *
         * <p>
         *     Indended to bind using {@link PropertyLayout#fieldSetId()}.
         * </p>
         *
         * <p>
         *     The fallback layout places the 'identity' and 'metadata' fieldsets as tabs within a single tab group.
         * </p>
         */
        public static final String IDENTITY = "identity";

        /**
         * The id for a field set intended to hold various metadata, such as the id or version.  All of the framework
         * provided mixins are associated with this fieldset, either properties or actions.
         *
         * <p>
         *     Indended to bind using {@link PropertyLayout#fieldSetId()}.
         * </p>
         *
         * <p>
         *     The fallback layout places the 'identity' and 'metadata' fieldsets as tabs within a single tab group.
         * </p>
         */
        public static final String METADATA = "metadata";

        /**
         * The id for a field set intended to hold additional details.
         *
         * <p>
         *     Indended to bind using {@link PropertyLayout#fieldSetId()}.
         * </p>
         *
         * <p>
         *     The fallback layout places the 'details' fieldset under the above 'identity' and 'metadata' fieldsets.
         * </p>
         */
        public static final String DETAILS = "details";
    }

    @UtilityClass
    public static class FieldSetName {

        /**
         * As {@link FieldSetId#IDENTITY}, but intended to bind using the name of the field set,
         * eg {@link PropertyLayout#fieldSetName()}.
         */
        public static final String IDENTITY = "Identity";

        /**
         * As {@link FieldSetId#METADATA}, but intended to bind using the name of the field set,
         * eg {@link PropertyLayout#fieldSetName()}.
         */
        public static final String METADATA = "Metadata";

        /**
         * As {@link FieldSetId#DETAILS}, but intended to bind using the name of the field set,
         * eg {@link PropertyLayout#fieldSetName()}.
         */
        public static final String DETAILS = "Details";
    }

    @UtilityClass
    public static class PropertyId {
        /**
         * As contributed by {@link org.apache.causeway.applib.mixins.metamodel.Object_objectIdentifier} mixin.
         *
         * <p>
         *     Note that {@link org.apache.causeway.applib.CausewayModuleApplibMixins} module must be included within the
         *     application in order to enable this mixin.
         * </p>
         */
        public static final String OBJECT_IDENTIFIER = "objectIdentifier";

        /**
         * As contributed by {@link org.apache.causeway.applib.mixins.metamodel.Object_logicalTypeName} mixin.
         *
         * <p>
         *     Note that {@link org.apache.causeway.applib.CausewayModuleApplibMixins} module must be included within the
         *     application in order to enable this mixin.
         * </p>
         */
        public static final String LOGICAL_TYPE_NAME = "logicalTypeName";
    }


}
