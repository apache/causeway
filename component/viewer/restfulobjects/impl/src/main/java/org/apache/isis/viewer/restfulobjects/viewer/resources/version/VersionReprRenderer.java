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
package org.apache.isis.viewer.restfulobjects.viewer.resources.version;

import java.nio.charset.Charset;

import com.google.common.io.Resources;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.viewer.RestfulObjectsApplication;
import org.apache.isis.viewer.restfulobjects.viewer.ResourceContext;
import org.apache.isis.viewer.restfulobjects.viewer.representations.LinkFollower;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRendererFactoryAbstract;

public class VersionReprRenderer extends ReprRendererAbstract<VersionReprRenderer, Void> {

    private static final String META_INF_POM_PROPERTIES = "/META-INF/maven/org.apache.isis.viewer/restfulobjects-viewer/pom.properties";

    public static class Factory extends ReprRendererFactoryAbstract {
        public Factory() {
            super(RepresentationType.VERSION);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final JsonRepresentation representation) {
            return new VersionReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    private VersionReprRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final RepresentationType representationType, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    @Override
    public VersionReprRenderer with(final Void t) {
        return this;
    }

    @Override
    public JsonRepresentation render() {

        if (includesSelf) {
            withSelf("version/");
        }

        representation.mapPut("specVersion", RestfulObjectsApplication.SPEC_VERSION);
        representation.mapPut("implVersion", versionFromManifest());

        putOptionalCapabilities();
        putExtensions();

        return representation;
    }

    private static String versionFromManifest() {
        try {
            return Resources.toString(Resources.getResource(META_INF_POM_PROPERTIES), Charset.defaultCharset());
        } catch (final Exception ex) {
            return "UNKNOWN";
        }
    }

    private void putOptionalCapabilities() {
        final JsonRepresentation optionalCapabilities = JsonRepresentation.newMap();

        optionalCapabilities.mapPut("concurrencyChecking", "no");
        optionalCapabilities.mapPut("transientObjects", "yes");
        optionalCapabilities.mapPut("deleteObjects", "no");
        optionalCapabilities.mapPut("simpleArguments", "no");
        optionalCapabilities.mapPut("partialArguments", "no");
        optionalCapabilities.mapPut("followLinks", "yes");
        optionalCapabilities.mapPut("validateOnly", "no");
        optionalCapabilities.mapPut("pagination", "no");
        optionalCapabilities.mapPut("sorting", "no");
        optionalCapabilities.mapPut("domainModel", "rich");

        representation.mapPut("optionalCapabilities", optionalCapabilities);
    }

    private void putExtensions() {
        representation.mapPut("extensions", JsonRepresentation.newMap());
    }

}