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
package org.apache.causeway.valuetypes.asciidoc.builder.xref;

import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Lazy;

import lombok.Builder;

// model for
// xref:refguide:applib:index/services/xmlsnapshot/XmlSnapshotService~Snapshot.adoc[XmlSnapshotService.Snapshot]
// location: system, generated
// namespace: index, applib, services, xmlsnapshot
// canonicalName: XmlSnapshotService~Snapshot.adoc
// friendlyName: XmlSnapshotService.Snapshot
@Builder
public record Xref(
        @NonNull Can<String> location,
        @NonNull Can<String> namespace,
        @NonNull String canonicalName,
        @NonNull String friendlyName,  // as used for display
        @NonNull _Lazy<String> xrefLazy // full xref string
        ) {

    public Xref(
            final Can<String> location,
            final Can<String> namespace,
            final String canonicalName,
            final String friendlyName,
            final _Lazy<String> xrefLazy) {
        this.location = location;
        this.namespace = namespace;
        this.canonicalName = canonicalName;
        this.friendlyName = friendlyName;
        this.xrefLazy = _Lazy.threadSafe(()->evalXref());
    }

    public String xref() {
        return xrefLazy().get();
    }

    private String evalXref() {
        var sb = new StringBuilder();

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
