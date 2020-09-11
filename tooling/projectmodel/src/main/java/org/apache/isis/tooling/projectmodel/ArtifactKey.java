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
package org.apache.isis.tooling.projectmodel;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.maven.artifact.versioning.ComparableVersion;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class ArtifactKey implements Comparable<ArtifactKey> {

    @NonNull private final String groupId;
    @NonNull private final String artifactId;
    @NonNull private final String type;
    @NonNull private final String version;
    
    private final AtomicReference<ComparableVersion> comparableVersion = new AtomicReference<ComparableVersion>();
    
    @Override
    public String toString() {
        return String.format("%s:%s:%s:%s", groupId, artifactId, type, version); 
    }

    // -- COMPARATOR
    
    private final static Comparator<ArtifactKey> comparator = Comparator
            .comparing(ArtifactKey::getGroupId)
            .thenComparing(ArtifactKey::getArtifactId)
            .thenComparing(ArtifactKey::getType)
            .thenComparing(ArtifactKey::getComparableVersion);
    
    private ComparableVersion getComparableVersion() {
        if(comparableVersion.get()==null) {
            comparableVersion.set(new ComparableVersion(getVersion()));
        }
        return comparableVersion.get();
    }

    @Override
    public int compareTo(ArtifactKey o) {
        return comparator.compare(this, o);
    }
    
}
