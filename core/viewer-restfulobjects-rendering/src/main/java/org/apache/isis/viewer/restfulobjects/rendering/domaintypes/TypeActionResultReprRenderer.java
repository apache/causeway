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
package org.apache.isis.viewer.restfulobjects.rendering.domaintypes;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;
import org.apache.isis.viewer.restfulobjects.rendering.ReprRendererAbstract;

public class TypeActionResultReprRenderer extends ReprRendererAbstract<TypeActionResultReprRenderer, ObjectSpecification> {

    private ObjectSpecification objectSpecification;
    private LinkRepresentation selfLink;
    private Object value;

    public TypeActionResultReprRenderer(final RendererContext resourceContext, final LinkFollowSpecs linkFollower, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.TYPE_ACTION_RESULT, representation);
    }

    @Override
    public TypeActionResultReprRenderer with(final ObjectSpecification objectSpecification) {
        this.objectSpecification = objectSpecification;
        return cast(this);
    }

    public TypeActionResultReprRenderer withValue(final Object value) {
        this.value = value;
        return this;
    }

    public TypeActionResultReprRenderer withSelf(final JsonRepresentation link) {
        return withLink(Rel.SELF, link);
    }

    @Override
    public JsonRepresentation render() {
        if (includesSelf && selfLink != null) {
            getLinks().arrayAdd(selfLink);
        }

        if (value != null) {
            representation.mapPut("value", value);
        }
        getExtensions();

        return representation;
    }

    protected void putExtensionsIfService() {
        getExtensions().mapPut("isService", objectSpecification.isManagedBean());
    }

}