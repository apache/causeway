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
package org.apache.isis.viewer.restfulobjects.viewer.representations;

import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.google.common.collect.Maps;

import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects.ActionResultReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects.DomainObjectReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects.ListReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects.ObjectActionReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects.ObjectCollectionReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects.ObjectPropertyReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects.ScalarValueReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domaintypes.ActionDescriptionReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domaintypes.ActionParameterDescriptionReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domaintypes.CollectionDescriptionReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domaintypes.DomainTypeReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domaintypes.PropertyDescriptionReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domaintypes.TypeActionResultReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domaintypes.TypeListReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.home.HomePageReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.user.UserReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.resources.version.VersionReprRenderer;

public class RendererFactoryRegistry {

    // TODO: get rid of this horrible singleton
    public final static RendererFactoryRegistry instance = new RendererFactoryRegistry();

    private final Map<MediaType, RendererFactory> factoryByReprType = Maps.newHashMap();

    RendererFactoryRegistry() {
        registerDefaults();
    }

    private void registerDefaults() {
        register(new HomePageReprRenderer.Factory());
        register(new UserReprRenderer.Factory());
        register(new VersionReprRenderer.Factory());
        register(new DomainObjectReprRenderer.Factory());
        register(new ObjectPropertyReprRenderer.Factory());
        register(new ObjectCollectionReprRenderer.Factory());
        register(new ObjectActionReprRenderer.Factory());
        register(new ActionResultReprRenderer.Factory());
        register(new ListReprRenderer.Factory());
        register(new ScalarValueReprRenderer.Factory());
        register(new TypeListReprRenderer.Factory());
        register(new DomainTypeReprRenderer.Factory());
        register(new PropertyDescriptionReprRenderer.Factory());
        register(new CollectionDescriptionReprRenderer.Factory());
        register(new ActionDescriptionReprRenderer.Factory());
        register(new ActionParameterDescriptionReprRenderer.Factory());
        register(new TypeActionResultReprRenderer.Factory());
    }

    public void register(final RendererFactory factory) {
        final RepresentationType representationType = factory.getRepresentationType();
        factoryByReprType.put(representationType.getMediaType(), factory);
    }

    public RendererFactory find(final MediaType mediaType) {
        return factoryByReprType.get(mediaType);
    }

    public RendererFactory find(final RepresentationType representationType) {
        return find(representationType.getMediaType());
    }

}
