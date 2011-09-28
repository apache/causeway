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

import java.util.Collection;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.AbstractReprBuilder;

public class DomainObjectListReprBuilder extends AbstractReprBuilder<DomainObjectListReprBuilder> {

    private ObjectAdapterLinkToBuilder objectAdapterLinkToBuilder;

    public static DomainObjectListReprBuilder newBuilder(ResourceContext resourceContext, JsonRepresentation representation) {
        return new DomainObjectListReprBuilder(resourceContext, representation);
    }

    public static DomainObjectListReprBuilder newBuilder(ResourceContext resourceContext) {
        return new DomainObjectListReprBuilder(resourceContext, JsonRepresentation.newMap());
    }

    private DomainObjectListReprBuilder(ResourceContext resourceContext, JsonRepresentation representation) {
        super(resourceContext, representation);
        usingLinkToBuilder(new DomainObjectLinkToBuilder());
    }
    
    public DomainObjectListReprBuilder usingLinkToBuilder(ObjectAdapterLinkToBuilder objectAdapterLinkToBuilder) {
        this.objectAdapterLinkToBuilder = objectAdapterLinkToBuilder.usingResourceContext(resourceContext);
        return this;
    }

    public DomainObjectListReprBuilder withAdapters(Collection<ObjectAdapter> objectAdapters) {
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