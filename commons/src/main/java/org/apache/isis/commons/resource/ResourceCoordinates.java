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
package org.apache.isis.commons.resource;

import java.io.File;
import java.util.Comparator;

import javax.annotation.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

/**
 * @since 2.0 {@index}
 */
@Value @Builder
public class ResourceCoordinates
implements Comparable<ResourceCoordinates> {

    public static ResourceCoordinates fromFile(final @NonNull File file) {
        val parts = _Lists.<String>newArrayList();
        File next = file;
        while(next!=null) {
            if(_Strings.isNotEmpty(next.getName())) {
                parts.add(next.getName());
            }
            next = next.getParentFile();
        }

        val nameRef = _Refs.stringRef(file.getName());

        //XXX lombok issue, cannot use val
        final String simpleName = nameRef.cutAtLastIndexOfAndDrop(".");
        final String fileNameExtension = nameRef.getValue();

        return ResourceCoordinates.builder()
             // could semantically mean the mount-point, but we don't have that info in a file instance
             .location(Can.empty())
             // just the filename without extension
             .simpleName(simpleName)
             .friendlyName(simpleName)
             .name(_Strings.isEmpty(fileNameExtension)
                     ? Can.ofSingleton(simpleName)
                     : Can.of(simpleName, fileNameExtension))
             .nameAsString(file.getName())
             .namespace(Can.ofStream(parts.stream().skip(1)).reverse())
             .build();
    }

    /**
     * multi-part top level location specifier
     * like eg. adoc include {@code component:module:page$}
     * or a file-system location {@code ~/my-projects/my-project},
     * (whether relative or absolute is not specified)
     */
    private final @NonNull Can<String> location;

    /**
     * multi-part (location relative) path like eg. {@code org.apache.isis}
     */
    private final @NonNull Can<String> namespace;

    /**
     * multi-part name like eg. a filename {@code docs.adoc},
     * or a nested class name {@code A$B},
     */
    private final @NonNull Can<String> name;

    /**
     * String representation of {@link #getName()};
     */
    private final @NonNull String nameAsString;

    /**
     * usually part of the multi-part name;
     * eg. a filename without extension,
     * or a java class simple name when nested
     */
    private final @NonNull String simpleName;

    /**
     * ever only used for display, presumably human readable;
     * other than that not specified
     */
    private final @NonNull String friendlyName;

    private final static Comparator<ResourceCoordinates> comparator =
            Comparator.comparing(ResourceCoordinates::getLocation)
           .thenComparing(ResourceCoordinates::getNamespace)
           .thenComparing(ResourceCoordinates::getName);

    @Override
    public int compareTo(final @Nullable ResourceCoordinates other) {
     // when returning
        // -1 ... this is before other
        // +1 ... this is after other
        if(other==null) {
            return 1; // nulls first
        }
        return comparator.compare(this, other);
    }

}
