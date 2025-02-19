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
package org.apache.causeway.core.metamodel.facets.members.layout.group;

import java.io.Serializable;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.commons.internal.base._Strings;

import org.jspecify.annotations.NonNull;

public record GroupIdAndName(
    /**
     * Id of a layout group (a <i>FieldSet</i> or a <i>Collection panel</i>).
     */
    @NonNull String id,
    /**
     * (Friendly) name of a layout group (a <i>FieldSet</i> or a <i>Collection panel</i>).
     */
    @NonNull String name)
implements
    Comparable<GroupIdAndName>,
    Serializable {

    @Override
    public int compareTo(final GroupIdAndName other) {
        if(other==null) return -1; // null last
        return this.id().compareTo(other.id());
    }

    // -- FACTORIES FOR ANNOTATIONS

    public static Optional<GroupIdAndName> forAction(
            final @NonNull Action action) {

        return GroupIdAndName.inferIfOneMissing(
                action.choicesFrom(),
                null);
    }

    public static Optional<GroupIdAndName> forActionLayout(
            final @NonNull ActionLayout actionLayout) {

        var explicit =  GroupIdAndName.inferIfOneMissing(
                actionLayout.fieldSetId(),
                actionLayout.fieldSetName());

        if(explicit.isPresent()) return explicit;

        return GroupIdAndName.inferIfOneMissing(
                actionLayout.associateWith(),
                null);
    }

    public static Optional<GroupIdAndName> forPropertyLayout(
            final @NonNull PropertyLayout propertyLayout) {
        return GroupIdAndName.inferIfOneMissing(
                propertyLayout.fieldSetId(),
                propertyLayout.fieldSetName());
    }

    // -- FACTORIES FOR XML LAYOUT

    public static Optional<GroupIdAndName> forPropertyLayoutData(
            final @NonNull PropertyLayoutData propertyLayoutData) {
        return GroupIdAndName.inferIfOneMissing(
                propertyLayoutData.getId(),
                propertyLayoutData.getNamed());
    }

    public static Optional<GroupIdAndName> forCollectionLayoutData(
            final @NonNull CollectionLayoutData collectionLayoutData) {
        return GroupIdAndName.inferIfOneMissing(
                collectionLayoutData.getId(),
                collectionLayoutData.getNamed());
    }

    public static Optional<GroupIdAndName> forFieldSet(
            final @NonNull FieldSet fieldSet) {
        return GroupIdAndName.inferIfOneMissing(
                fieldSet.getId(),
                fieldSet.getName());
    }

    // -- HELPER

    /**
     * if id is missing tries to infer it;<br>
     * if name is missing tries to infer it;<br>
     * if cannot reason about a usable id, returns Optional.empty()<br>
     */
    private static Optional<GroupIdAndName> inferIfOneMissing(
            final @Nullable String _id,
            final @Nullable String _name) {

        var id = nullToUnspecified(_id);
        var name = nullToUnspecified(_name);

        var isIdUnspecified = isUnspecified(id) || id.isEmpty();
        var isNameUnspecified = isUnspecified(name);
        if(isIdUnspecified
                && isNameUnspecified) {
            return Optional.empty(); // fully unspecified, don't create a LayoutGroupFacet down the line
        }
        if(isIdUnspecified) {
            var inferredId = inferIdFromName(name);
            if(inferredId.isEmpty()) {
                return Optional.empty(); // cannot infer a usable id, so don't create a LayoutGroupFacet down the line
            }
            return Optional.of(new GroupIdAndName(inferredId, name));
        } else if(isNameUnspecified) {
            var inferredName = inferNameFromId(id);
            return Optional.of(new GroupIdAndName(id, inferredName));
        }
        return Optional.of(new GroupIdAndName(id, name));
    }

    /**
     * @implNote this is a copy of the original logic from GridSystemServiceBS
     */
    private static @NonNull String inferIdFromName(final @NonNull String name) {
        if(name.isEmpty()) {
            return name;
        }
        final char c = name.charAt(0);
        return Character.toLowerCase(c) + name.substring(1).replaceAll("\\s+", "");
    }

    /**
     * @implNote could potentially be improved to work similar as the title service
     */
    private static @NonNull String inferNameFromId(final @NonNull String id) {
        return _Strings.asNaturalName.apply(id);
    }

    /**
     * Corresponds to the defaults set in {@link ActionLayout#fieldSetId()} etc.
     */
    private static boolean isUnspecified(final @NonNull String idOrName) {
        return "__infer".equals(idOrName);
    }

    /**
     * Corresponds to the defaults set in {@link ActionLayout#fieldSetId()} etc.
     */
    private static String nullToUnspecified(final @Nullable String idOrName) {
        return idOrName==null
                    ? "__infer"
                    : idOrName;

    }

}
