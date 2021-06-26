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
package org.apache.isis.viewer.restfulobjects.viewer.resources;

import java.io.InputStream;
import java.util.Properties;

import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.isis.viewer.restfulobjects.rendering.ReprRendererAbstract;
import org.apache.isis.viewer.restfulobjects.viewer.jaxrsapp.RestfulObjectsSpec;

public class VersionReprRenderer
extends ReprRendererAbstract<Void> {

    private static final String META_INF_POM_PROPERTIES = "/META-INF/maven/org.apache.isis.viewer/isis-viewer-restfulobjects-server/pom.properties";

    VersionReprRenderer(final IResourceContext resourceContext, final LinkFollowSpecs linkFollower, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.VERSION, representation);
    }

    @Override
    public VersionReprRenderer with(final Void t) {
        return this;
    }

    @Override
    public JsonRepresentation render() {

        if (includesSelf) {
            addLinkToSelf();
            addLinkToUp();
        }

        representation.mapPut("specVersion", RestfulObjectsSpec.SPEC_VERSION);
        representation.mapPut("implVersion", versionFromManifest());

        putOptionalCapabilities();
        putExtensions();

        return representation;
    }

    private void addLinkToSelf() {
        final JsonRepresentation link = LinkBuilder.newBuilder(getResourceContext(), Rel.SELF.getName(), RepresentationType.VERSION, "version").build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final VersionReprRenderer renderer = new VersionReprRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            link.mapPut("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinkToUp() {
        final JsonRepresentation link = LinkBuilder.newBuilder(resourceContext, Rel.UP.getName(), RepresentationType.HOME_PAGE, "").build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final HomePageReprRenderer renderer = new HomePageReprRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            link.mapPut("value", renderer.render());
        }
        getLinks().arrayAdd(link);
    }

    private static String versionFromManifest() {
        try {
            final InputStream resource = _Resources.load(VersionReprRenderer.class, META_INF_POM_PROPERTIES);
            Properties p = new Properties();
            p.load(resource);
            return p.getProperty("version");
        } catch (final Exception ex) {
            return "UNKNOWN";
        }
    }

    private void putOptionalCapabilities() {
        final JsonRepresentation optionalCapabilities = JsonRepresentation.newMap();

        optionalCapabilities.mapPut("blobsClobs", "yes");
        optionalCapabilities.mapPut("deleteObjects", "yes");
        optionalCapabilities.mapPut("domainModel", "formal");
        optionalCapabilities.mapPut("validateOnly", "yes");
        optionalCapabilities.mapPut("protoPersistentObjects", "yes");

        representation.mapPut("optionalCapabilities", optionalCapabilities);
    }

    private void putExtensions() {
        representation.mapPut("extensions", JsonRepresentation.newMap());
    }

}