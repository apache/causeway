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

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.causeway.viewer.restfulobjects.rendering.ReprRendererAbstract;

public abstract class AbstractTypeFeatureReprRenderer<T extends ObjectFeature>
extends ReprRendererAbstract<ParentSpecAndFeature<T>> {

    protected ObjectSpecification objectSpecification;
    protected T objectFeature;

    public AbstractTypeFeatureReprRenderer(
            final IResourceContext resourceContext,
            final LinkFollowSpecs linkFollower,
            final RepresentationType representationType,
            final JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    public ObjectSpecification getParentSpecification() {
        return objectSpecification;
    }

    public T getObjectFeature() {
        return objectFeature;
    }

    @Override
    public AbstractTypeFeatureReprRenderer<T> with(final ParentSpecAndFeature<T> specAndFeature) {
        objectSpecification = specAndFeature.getParentSpec();
        objectFeature = specAndFeature.getObjectFeature();

        return this;
    }

    @Override
    public JsonRepresentation render() {

        addLinkSelfIfRequired();
        addLinkUpToParent();

        addPropertiesSpecificToFeature();

        addLinksSpecificToFeature();
        putExtensionsSpecificToFeature();

        return representation;
    }

    /**
     * Optional hook method.
     */
    protected void addPropertiesSpecificToFeature() {
    }

    /**
     * Mandatory hook method.
     */
    protected abstract void addLinkSelfIfRequired();

    /**
     * Mandatory hook method.
     */
    protected abstract void addLinkUpToParent();

    /**
     * Optional hook method.
     */
    protected void addLinksSpecificToFeature() {
    }

    /**
     * Mandatory hook method.
     */
    protected abstract void putExtensionsSpecificToFeature();

    protected void putExtensionsName() {
        getObjectFeature()
        .getStaticOrCanonicalFriendlyName()
        .accept(
                staticForm->{
                    getExtensions().mapPutString("friendlyName", staticForm);
                    getExtensions().mapPutString("friendlyNameForm", "static");
                },
                canonicalForm->{
                    getExtensions().mapPutString("friendlyName", canonicalForm);
                    getExtensions().mapPutString("friendlyNameForm", "canonical");
                });
    }

    protected void putExtensionsDescriptionIfAvailable() {
        getObjectFeature()
        .getStaticOrCanonicalDescription()
        .ifPresent(description->{
            description.accept(
                    staticForm->{
                        if(staticForm.isEmpty()) return;
                        getExtensions().mapPutString("description", staticForm);
                        getExtensions().mapPutString("descriptionForm", "static");
                    },
                    canonicalForm->{
                        if(canonicalForm.isEmpty()) return;
                        getExtensions().mapPutString("description", canonicalForm);
                        getExtensions().mapPutString("descriptionForm", "canonical");
                    });
        });

    }

}