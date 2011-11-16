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
package org.apache.isis.viewer.json.viewer.resources.capabilities;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.JsonApplication;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkFollower;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;

public class CapabilitiesReprRenderer extends ReprRendererAbstract<CapabilitiesReprRenderer, Void> {

    public static class Factory extends ReprRendererFactoryAbstract {
        public Factory() {
            super(RepresentationType.CAPABILITIES);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(ResourceContext resourceContext, LinkFollower linkFollower, JsonRepresentation representation) {
            return new CapabilitiesReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }
    
    private CapabilitiesReprRenderer(ResourceContext resourceContext, LinkFollower linkFollower, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    @Override
    public CapabilitiesReprRenderer with(Void t) {
        return this;
    }

    @Override
    public JsonRepresentation render() {

        if(includesSelf) {
            withSelf("capabilities/");
        }

        putCapabilities();
        putExtensions();
        
        return representation;
    }

    private void putCapabilities() {
        JsonRepresentation capabilities = JsonRepresentation.newMap();

        capabilities.mapPut("specVersion", JsonApplication.SPEC_VERSION);
        capabilities.mapPut("concurrencyChecking", "no");
        capabilities.mapPut("transientObjects", "yes");
        capabilities.mapPut("deleteObjects", "no");
        capabilities.mapPut("simpleArguments", "no");
        capabilities.mapPut("partialArguments", "no");
        capabilities.mapPut("followLinks", "yes");
        capabilities.mapPut("validateOnly", "no");
        capabilities.mapPut("pagination", "no");
        capabilities.mapPut("sorting", "no");
        capabilities.mapPut("domainModel", "rich");

        representation.mapPut("capabilities", capabilities);
    }
    
    private void putExtensions() {
        representation.mapPut("extensions", JsonRepresentation.newMap());
    }

}