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
package org.apache.isis.viewer.restfulobjects.applib;

import javax.ws.rs.core.MediaType;

import org.apache.isis.applib.util.Enums;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.ActionDescriptionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.ActionParameterDescriptionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.CollectionDescriptionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.DomainTypeRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.PropertyDescriptionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.TypeActionResultRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.TypeListRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.errors.ErrorRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.user.UserRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.util.Parser;
import org.apache.isis.viewer.restfulobjects.applib.version.VersionRepresentation;
import org.apache.isis.viewer.restfulobjects.domainobjects.ActionResultRepresentation;
import org.apache.isis.viewer.restfulobjects.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.domainobjects.ListRepresentation;
import org.apache.isis.viewer.restfulobjects.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.restfulobjects.domainobjects.ObjectCollectionRepresentation;
import org.apache.isis.viewer.restfulobjects.domainobjects.ObjectPropertyRepresentation;
import org.apache.isis.viewer.restfulobjects.domainobjects.ScalarValueRepresentation;
import org.apache.isis.viewer.restfulobjects.domainobjects.TransientDomainObjectRepresentation;

public enum RepresentationType {

    HOME_PAGE(RestfulMediaType.APPLICATION_JSON_HOME_PAGE, HomePageRepresentation.class), USER(RestfulMediaType.APPLICATION_JSON_USER, UserRepresentation.class), VERSION(RestfulMediaType.APPLICATION_JSON_VERSION, VersionRepresentation.class), LIST(RestfulMediaType.APPLICATION_JSON_LIST,
            ListRepresentation.class), SCALAR_VALUE(RestfulMediaType.APPLICATION_JSON_SCALAR_VALUE, ScalarValueRepresentation.class), DOMAIN_OBJECT(RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT, DomainObjectRepresentation.class), TRANSIENT_DOMAIN_OBJECT(
            RestfulMediaType.APPLICATION_JSON_TRANSIENT_DOMAIN_OBJECT, TransientDomainObjectRepresentation.class), OBJECT_PROPERTY(RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, ObjectPropertyRepresentation.class), OBJECT_COLLECTION(RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION,
            ObjectCollectionRepresentation.class), OBJECT_ACTION(RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION, ObjectActionRepresentation.class), ACTION_RESULT(RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, ActionResultRepresentation.class), TYPE_LIST(
            RestfulMediaType.APPLICATION_JSON_TYPE_LIST, TypeListRepresentation.class), DOMAIN_TYPE(RestfulMediaType.APPLICATION_JSON_DOMAIN_TYPE, DomainTypeRepresentation.class), PROPERTY_DESCRIPTION(RestfulMediaType.APPLICATION_JSON_PROPERTY_DESCRIPTION, PropertyDescriptionRepresentation.class), COLLECTION_DESCRIPTION(
            RestfulMediaType.APPLICATION_JSON_COLLECTION_DESCRIPTION, CollectionDescriptionRepresentation.class), ACTION_DESCRIPTION(RestfulMediaType.APPLICATION_JSON_ACTION_DESCRIPTION, ActionDescriptionRepresentation.class), ACTION_PARAMETER_DESCRIPTION(
            RestfulMediaType.APPLICATION_JSON_ACTION_PARAMETER_DESCRIPTION, ActionParameterDescriptionRepresentation.class), TYPE_ACTION_RESULT(RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_RESULT, TypeActionResultRepresentation.class), ERROR(RestfulMediaType.APPLICATION_JSON_ERROR,
            ErrorRepresentation.class), GENERIC(MediaType.APPLICATION_JSON, JsonRepresentation.class);

    private final String name;
    private final MediaType mediaType;
    private final Class<? extends JsonRepresentation> representationClass;

    private RepresentationType(final String mediaTypeStr, final Class<? extends JsonRepresentation> representationClass) {
        this.representationClass = representationClass;
        this.name = Enums.enumToCamelCase(this);
        this.mediaType = MediaType.valueOf(mediaTypeStr);
    }

    public String getName() {
        return name;
    }

    public final MediaType getMediaType() {
        return mediaType;
    }

    public String getMediaTypeWithProfile() {
        return getMediaType().getParameters().get("profile");
    }

    public Class<? extends JsonRepresentation> getRepresentationClass() {
        return representationClass;
    }

    public static RepresentationType lookup(final String name) {
        for (final RepresentationType representationType : values()) {
            if (representationType.getName().equals(name)) {
                return representationType;
            }
        }
        return RepresentationType.GENERIC;
    }

    public static RepresentationType lookup(final MediaType mediaType) {
        for (final RepresentationType representationType : values()) {
            if (representationType.getMediaType().equals(mediaType)) {
                return representationType;
            }
        }
        return RepresentationType.GENERIC;
    }

    public static Parser<RepresentationType> parser() {
        return new Parser<RepresentationType>() {
            @Override
            public RepresentationType valueOf(final String str) {
                return RepresentationType.lookup(str);
            }

            @Override
            public String asString(final RepresentationType t) {
                return t.getName();
            }
        };
    }

}
