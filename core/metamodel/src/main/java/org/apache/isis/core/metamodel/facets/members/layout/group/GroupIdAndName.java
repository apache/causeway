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
package org.apache.isis.core.metamodel.facets.members.layout.group;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.commons.internal.base._Strings;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value(staticConstructor = "of")
public class GroupIdAndName 
implements
    Comparable<GroupIdAndName>,
    Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Id of a layout group.
     */
    private final @NonNull String id;
    
    /**
     * (Friendly) name of a layout group.
     */
    private final @NonNull String name;

    @Override
    public int compareTo(GroupIdAndName other) {
        if(other==null) {
            return -1; // null last
        }
        return this.getId().compareTo(other.getId());
    }
    
    // -- FACTORIES FOR ANNOTATIONS

    public static @Nullable GroupIdAndName forActionLayout(
            final @NonNull ActionLayout actionLayout) {
        return GroupIdAndName.inferIfOneMissing(
                actionLayout.fieldSetId(), 
                actionLayout.fieldSetName());
    }

    public static @Nullable GroupIdAndName forPropertyLayout(
            final @NonNull PropertyLayout propertyLayout) {
        return GroupIdAndName.inferIfOneMissing(
                propertyLayout.fieldSetId(), 
                propertyLayout.fieldSetName());
    }
    
    // -- FACTORIES FOR XML LAYOUT

    public static @Nullable GroupIdAndName forPropertyLayoutData(
            final @NonNull PropertyLayoutData propertyLayoutData) {
        return GroupIdAndName.inferIfOneMissing(
                propertyLayoutData.getId(), 
                propertyLayoutData.getNamed());
    }
    
    public static @Nullable GroupIdAndName forCollectionLayoutData(
            final @NonNull CollectionLayoutData collectionLayoutData) {
        return GroupIdAndName.inferIfOneMissing(
                collectionLayoutData.getId(), 
                collectionLayoutData.getNamed());
    }
    
    public static @Nullable GroupIdAndName forFieldSet(
            final @NonNull FieldSet fieldSet) {
        return GroupIdAndName.inferIfOneMissing(
                fieldSet.getId(), 
                fieldSet.getName());
    }
    
    // -- HELPER
    
    private static @Nullable GroupIdAndName inferIfOneMissing(
            final @Nullable String _id, 
            final @Nullable String _name) {
        
        val id = nullToUnspecified(_id);
        val name = nullToUnspecified(_name);
        
        val isIdUnspecified = isUnspecified(id) || id.isEmpty();
        val isNameUnspecified = isUnspecified(name);
        if(isIdUnspecified
                && isNameUnspecified) {
            return null; // fully unspecified, don't create a LayoutGroupFacet down the line
        }
        if(isIdUnspecified) {
            val inferredId = inferIdFromName(name);
            if(inferredId.isEmpty()) {
                return null; // cannot infer a usable id, so don't create a LayoutGroupFacet down the line
            }
            return GroupIdAndName.of(inferIdFromName(name), name);
        } else if(isNameUnspecified) {
            val inferredName = inferNameFromId(id);
            return GroupIdAndName.of(id, inferredName);
        }
        return GroupIdAndName.of(id, name);
    }
    
    // note: this is a copy of the original logic from GridSystemServiceBS3
    private static @NonNull String inferIdFromName(final @NonNull String name) {
        if(name.isEmpty()) {
            return name;
        }
        final char c = name.charAt(0);
        return Character.toLowerCase(c) + name.substring(1).replaceAll("\\s+", "");
    }
    
    // note: could potentially be improved to work similar as the title service
    private static @NonNull String inferNameFromId(final @NonNull String id) {
        return _Strings.asNaturalName2.apply(id);
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
