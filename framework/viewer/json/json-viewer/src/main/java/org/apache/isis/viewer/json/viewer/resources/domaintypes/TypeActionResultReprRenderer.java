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
package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.links.LinkRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkFollower;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;

public class TypeActionResultReprRenderer extends ReprRendererAbstract<TypeActionResultReprRenderer, ObjectSpecification> {

    public static class Factory extends ReprRendererFactoryAbstract {
        public Factory() {
            super(RepresentationType.TYPE_ACTION_RESULT);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final JsonRepresentation representation) {
            return new TypeActionResultReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    private ObjectSpecification objectSpecification;
    private LinkRepresentation selfLink;
    private Object value;

    public TypeActionResultReprRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final RepresentationType representationType, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
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
        getExtensions().mapPut("isService", objectSpecification.isService());
    }

}