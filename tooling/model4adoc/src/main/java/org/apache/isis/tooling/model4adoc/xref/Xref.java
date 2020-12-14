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
package org.apache.isis.tooling.model4adoc.xref;

import java.util.stream.Collectors;

import org.apache.isis.commons.collections.Can;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value @Builder
public final class Xref {
    
    // model for
    // xref:system:generated:index/applib/services/xmlsnapshot/XmlSnapshotService~Snapshot.adoc[XmlSnapshotService.Snapshot]
    // location: system, generated
    // namespace: index, applib, services, xmlsnapshot
    // canonicalName: XmlSnapshotService~Snapshot.adoc
    // friendlyName: XmlSnapshotService.Snapshot
    
    private final @NonNull Can<String> location; 
    private final @NonNull Can<String> namespace; 
    private final @NonNull String canonicalName;
    private final @NonNull String friendlyName;  // as used for display
    
    @Getter(lazy = true)
    private final @NonNull String xref = xref(); // full xref string

    private String xref() {
        val sb = new StringBuilder();
        
        sb.append("xref:")
        .append(location.stream().collect(Collectors.joining(":")))
        .append(":");
        
        namespace.stream().forEach(part->sb.append(part).append("/"));
        
        sb.append(canonicalName)
        .append("[")
        .append(friendlyName)
        .append("]");
        
        return sb.toString();
    }
     
    
}
