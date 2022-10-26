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
package org.apache.causeway.viewer.restfulobjects.rendering.domaintypes;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.causeway.viewer.restfulobjects.rendering.ReprRendererAbstract;

public class TypeListReprRenderer
extends ReprRendererAbstract<Can<ObjectSpecification>> {

    private Can<ObjectSpecification> specifications;

    public TypeListReprRenderer(final IResourceContext resourceContext, final LinkFollowSpecs linkFollower, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.TYPE_LIST, representation);
    }

    @Override
    public TypeListReprRenderer with(final Can<ObjectSpecification> specifications) {
        this.specifications = specifications;
        return this;
    }

    @Override
    public JsonRepresentation render() {

        // self
        if (includesSelf) {
            withLink(Rel.SELF, "domain-types");
        }

        final JsonRepresentation specList = JsonRepresentation.newArray();
        for (final ObjectSpecification objectSpec : specifications) {
            final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.DOMAIN_TYPE.getName(), RepresentationType.DOMAIN_TYPE, "domain-types/%s", objectSpec.getLogicalTypeName());
            specList.arrayAdd(linkBuilder.build());
        }

        representation.mapPutJsonRepresentation("values", specList);

        getExtensions(); // empty

        return representation;
    }

}