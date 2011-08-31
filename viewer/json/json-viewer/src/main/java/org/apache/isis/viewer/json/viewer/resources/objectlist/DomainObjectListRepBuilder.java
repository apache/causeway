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

public class DomainObjectListRepBuilder extends RepresentationBuilder {

    public static DomainObjectListRepBuilder newBuilder(RepContext repContext, ObjectSpecification objectSpec, List<ObjectAdapter> objectAdapter) {
        return new DomainObjectListRepBuilder(repContext, objectSpec, objectAdapter);
    }

    private final ObjectSpecification objectSpec;
    private final List<ObjectAdapter> objectAdapters;
    
    public DomainObjectListRepBuilder(RepContext repContext, ObjectSpecification objectSpec, List<ObjectAdapter> objectAdapters) {
        super(repContext);
        this.objectSpec = objectSpec;
        this.objectAdapters = objectAdapters;
    }
    
    public JsonRepresentation build() {
        representation.put("resourceType", "objectList");
        
        JsonRepresentation type = LinkRepBuilder.newTypeBuilder(repContext, objectSpec).build();
        representation.put("type", type);

        return representation;
    }


}