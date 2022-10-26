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
package org.apache.causeway.tooling.projectmodel;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.maven.artifact.versioning.ComparableVersion;

import org.apache.causeway.commons.internal.base._Strings;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class ArtifactCoordinates implements Comparable<ArtifactCoordinates> {

    public static final String MANAGED_VERSION = "<managed>";

    @NonNull private final String groupId;
    @NonNull private final String artifactId;
    @NonNull private final String packaging;
    @NonNull private final String version;

    private final AtomicReference<ComparableVersion> comparableVersion = new AtomicReference<ComparableVersion>();

    @Override
    public String toString() {
        return String.format("%s:%s:%s:%s", groupId, artifactId, packaging, version);
    }

    public String toStringWithGroupAndId() {
        return String.format("%s:%s", groupId, artifactId);
    }

    public String toStringWithGroupAndIdAndVersion() {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }

    public boolean isVersionResolved() {
        return _Strings.isNotEmpty(version)
                && !MANAGED_VERSION.equals(version)
                && !version.startsWith("$");
    }

    // -- COMPARATOR

    private final static Comparator<ArtifactCoordinates> comparator = Comparator
            .comparing(ArtifactCoordinates::getGroupId)
            .thenComparing(ArtifactCoordinates::getArtifactId)
            .thenComparing(ArtifactCoordinates::getPackaging)
            .thenComparing(ArtifactCoordinates::getComparableVersion);

    private ComparableVersion getComparableVersion() {
        if(comparableVersion.get()==null) {
            comparableVersion.set(new ComparableVersion(getVersion()));
        }
        return comparableVersion.get();
    }

    @Override
    public int compareTo(ArtifactCoordinates o) {
        return comparator.compare(this, o);
    }

}
