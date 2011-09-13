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
package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.AbstractRepresentationBuilder;

public class DomainObjectListRepBuilder extends AbstractRepresentationBuilder<DomainObjectListRepBuilder> {

    private ObjectAdapterLinkToBuilder objectAdapterLinkToBuilder;
    
    public static DomainObjectListRepBuilder newBuilder(ResourceContext resourceContext) {
        return new DomainObjectListRepBuilder(resourceContext);
    }

    private DomainObjectListRepBuilder(ResourceContext resourceContext) {
        super(resourceContext);
        usingLinkToBuilder(new DomainObjectLinkToBuilder());
    }
    
    public DomainObjectListRepBuilder usingLinkToBuilder(ObjectAdapterLinkToBuilder objectAdapterLinkToBuilder) {
        this.objectAdapterLinkToBuilder = objectAdapterLinkToBuilder.usingResourceContext(resourceContext);
        return this;
    }

    public DomainObjectListRepBuilder withAdapters(List<ObjectAdapter> objectAdapters) {
        JsonRepresentation list = JsonRepresentation.newArray();
        for(ObjectAdapter adapter: objectAdapters) {
            JsonRepresentation linkToObject = objectAdapterLinkToBuilder.with(adapter).linkToAdapter().build();
            list.arrayAdd(linkToObject);
        }
        representation.mapPut("values", list);
        return this;
    }


    public JsonRepresentation build() {
        withLinks();
        withExtensions();

        return representation;
    }


}