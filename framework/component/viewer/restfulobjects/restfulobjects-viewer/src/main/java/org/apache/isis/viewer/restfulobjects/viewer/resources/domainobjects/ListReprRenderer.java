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
package org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects;

import java.util.Collection;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.links.Rel;
import org.apache.isis.viewer.restfulobjects.viewer.ResourceContext;
import org.apache.isis.viewer.restfulobjects.viewer.representations.LinkFollower;
import org.apache.isis.viewer.restfulobjects.viewer.representations.RendererFactory;
import org.apache.isis.viewer.restfulobjects.viewer.representations.RendererFactoryRegistry;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRendererFactoryAbstract;

public class ListReprRenderer extends ReprRendererAbstract<ListReprRenderer, Collection<ObjectAdapter>> {

    public static class Factory extends ReprRendererFactoryAbstract {
        public Factory() {
            super(RepresentationType.LIST);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final JsonRepresentation representation) {
            return new ListReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    private ObjectAdapterLinkTo linkTo;
    private Collection<ObjectAdapter> objectAdapters;
    private ObjectSpecification elementType;
    private ObjectSpecification returnType;

    private ListReprRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final RepresentationType representationType, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
        usingLinkToBuilder(new DomainObjectLinkTo());
    }

    public ListReprRenderer usingLinkToBuilder(final ObjectAdapterLinkTo objectAdapterLinkToBuilder) {
        this.linkTo = objectAdapterLinkToBuilder.usingResourceContext(resourceContext);
        return this;
    }

    @Override
    public ListReprRenderer with(final Collection<ObjectAdapter> objectAdapters) {
        this.objectAdapters = objectAdapters;
        return this;
    }

    public ListReprRenderer withReturnType(final ObjectSpecification returnType) {
        this.returnType = returnType;
        return this;
    }

    public ListReprRenderer withElementType(final ObjectSpecification elementType) {
        this.elementType = elementType;
        return this;
    }

    @Override
    public JsonRepresentation render() {
        addValue();

        addLink(Rel.RETURN_TYPE, returnType);
        addLink(Rel.ELEMENT_TYPE, elementType);

        getExtensions();

        return representation;
    }

    private void addValue() {
        if (objectAdapters == null) {
            return;
        }

        final JsonRepresentation values = JsonRepresentation.newArray();
        final LinkFollower linkFollower = getLinkFollower().follow("value");

        for (final ObjectAdapter adapter : objectAdapters) {
            if (adapter.getSpecification().isHidden()) {
                continue;
            }
            final JsonRepresentation linkToObject = linkTo.with(adapter).builder().build();
            values.arrayAdd(linkToObject);

            if (linkFollower.matches(linkToObject)) {
                final RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.DOMAIN_OBJECT);
                final DomainObjectReprRenderer renderer = (DomainObjectReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
                final JsonRepresentation domainObject = renderer.with(adapter).render();
                linkToObject.mapPut("value", domainObject);
            }
        }
        representation.mapPut("value", values);
    }

}