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
package org.apache.causeway.viewer.restfulobjects.viewer.resources;

import java.io.InputStream;
import java.util.Properties;

import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.causeway.viewer.restfulobjects.rendering.ReprRendererAbstract;
import org.apache.causeway.viewer.restfulobjects.viewer.jaxrsapp.RestfulObjectsSpec;

public class VersionReprRenderer
extends ReprRendererAbstract<Void> {

    private static final String META_INF_POM_PROPERTIES = "/META-INF/maven/org.apache.causeway.viewer/causeway-viewer-restfulobjects-server/pom.properties";

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

        representation.mapPutString("specVersion", RestfulObjectsSpec.SPEC_VERSION);
        representation.mapPutString("implVersion", versionFromManifest());

        putOptionalCapabilities();
        putExtensions();

        return representation;
    }

    private void addLinkToSelf() {
        final JsonRepresentation link = LinkBuilder.newBuilder(getResourceContext(), Rel.SELF.getName(), RepresentationType.VERSION, "version").build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final VersionReprRenderer renderer = new VersionReprRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            link.mapPutJsonRepresentation("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinkToUp() {
        final JsonRepresentation link = LinkBuilder.newBuilder(resourceContext, Rel.UP.getName(), RepresentationType.HOME_PAGE, "").build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final HomePageReprRenderer renderer = new HomePageReprRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            link.mapPutJsonRepresentation("value", renderer.render());
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

        optionalCapabilities.mapPutString("blobsClobs", "yes");
        optionalCapabilities.mapPutString("deleteObjects", "yes");
        optionalCapabilities.mapPutString("domainModel", "formal");
        optionalCapabilities.mapPutString("validateOnly", "yes");
        optionalCapabilities.mapPutString("protoPersistentObjects", "yes");

        representation.mapPutJsonRepresentation("optionalCapabilities", optionalCapabilities);
    }

    private void putExtensions() {
        representation.mapPutJsonRepresentation("extensions", JsonRepresentation.newMap());
    }

}
