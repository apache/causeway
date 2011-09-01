/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.json.viewer.resources.objectlist;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.RepContext;
import org.apache.isis.viewer.json.viewer.representations.LinkRepBuilder;
import org.apache.isis.viewer.json.viewer.representations.RepresentationBuilder;
import org.apache.isis.viewer.json.viewer.representations.WellKnownType;

public class DomainObjectListRepBuilder extends RepresentationBuilder {

    public static DomainObjectListRepBuilder newBuilder(RepContext repContext, ObjectSpecification objectSpec, List<ObjectAdapter> objectAdapters) {
        return newBuilder(repContext, WellKnownType.canonical(objectSpec.getFullIdentifier()), objectAdapters);
    }

    public static DomainObjectListRepBuilder newBuilder(RepContext repContext, String typeName, List<ObjectAdapter> objectAdapters) {
        return new DomainObjectListRepBuilder(repContext, typeName, objectAdapters);
    }

    private final List<ObjectAdapter> objectAdapters;
    private String typeName;
    
    DomainObjectListRepBuilder(RepContext repContext, String typeName, List<ObjectAdapter> objectAdapters) {
        super(repContext);
        this.objectAdapters = objectAdapters;
        this.typeName = typeName;
    }
    
    public JsonRepresentation build() {
        JsonRepresentation linkToRepresentationType = LinkRepBuilder.newBuilder(repContext, "representationType", "representationTypes/list:" + typeName).build();
        representation.put("representationType", linkToRepresentationType);

        JsonRepresentation list = JsonRepresentation.newArray();
        for(ObjectAdapter adapter: objectAdapters) {
            JsonRepresentation linkToObject = buildLinkTo(adapter);
            list.add(linkToObject);
        }
        representation.put("value", list);
        representation.put("links", JsonRepresentation.newArray());
        representation.put("metadata", JsonRepresentation.newMap());

        return representation;
    }

    protected JsonRepresentation buildLinkTo(ObjectAdapter adapter) {
        return LinkRepBuilder.newObjectBuilder(repContext, adapter, getOidStringifier()).build();
    }


}