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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.isis.applib.util.Enums;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ListRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectCollectionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectPropertyRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.ActionDescriptionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.ActionParameterDescriptionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.CollectionDescriptionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.DomainTypeRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.PropertyDescriptionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.TypeActionResultRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.TypeListRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.errors.ErrorRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.health.HealthRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.user.UserRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.util.MediaTypes;
import org.apache.isis.viewer.restfulobjects.applib.util.Parser;
import org.apache.isis.viewer.restfulobjects.applib.version.VersionRepresentation;

/**
 * @since 1.x {@index}
 */
public enum RepresentationType {

    HOME_PAGE(
            RestfulMediaType.APPLICATION_JSON_HOME_PAGE,
            null,
            HomePageRepresentation.class),
    USER(
            RestfulMediaType.APPLICATION_JSON_USER,
            null,
            UserRepresentation.class),
    VERSION(
            RestfulMediaType.APPLICATION_JSON_VERSION,
            null,
            VersionRepresentation.class),
    HEALTH(
            RestfulMediaType.APPLICATION_JSON_HEALTH,
            null,
            HealthRepresentation.class),
    LIST(
            RestfulMediaType.APPLICATION_JSON_LIST,
            null,
            ListRepresentation.class),
    DOMAIN_OBJECT(
            RestfulMediaType.APPLICATION_JSON_OBJECT,
            RestfulMediaType.APPLICATION_XML_OBJECT,
            DomainObjectRepresentation.class),
    OBJECT_PROPERTY(
            RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY,
            RestfulMediaType.APPLICATION_XML_OBJECT_PROPERTY,
            ObjectPropertyRepresentation.class),
    OBJECT_COLLECTION(
            RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION,
            RestfulMediaType.APPLICATION_XML_OBJECT_COLLECTION,
            ObjectCollectionRepresentation.class),
    OBJECT_ACTION(
            RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION,
            RestfulMediaType.APPLICATION_XML_OBJECT_ACTION,
            ObjectActionRepresentation.class),
    ACTION_RESULT(
            RestfulMediaType.APPLICATION_JSON_ACTION_RESULT,
            RestfulMediaType.APPLICATION_XML_ACTION_RESULT,
            ActionResultRepresentation.class),
    TYPE_LIST(
            RestfulMediaType.APPLICATION_JSON_TYPE_LIST,
            null,
            TypeListRepresentation.class),
    DOMAIN_TYPE(
            RestfulMediaType.APPLICATION_JSON_DOMAIN_TYPE,
            null,
            DomainTypeRepresentation.class),
    LAYOUT(
            RestfulMediaType.APPLICATION_JSON_LAYOUT_BS3,
            RestfulMediaType.APPLICATION_XML_LAYOUT_BS3,
            null),
    OBJECT_LAYOUT(
            RestfulMediaType.APPLICATION_JSON_OBJECT_LAYOUT_BS3,
            RestfulMediaType.APPLICATION_XML_OBJECT_LAYOUT_BS3,
            null),
    OBJECT_ICON(
            "image/*",
            "image/*",
            null),
    MENUBARS(
            RestfulMediaType.APPLICATION_JSON_LAYOUT_MENUBARS,
            RestfulMediaType.APPLICATION_XML_LAYOUT_MENUBARS,
            null),
    PROPERTY_DESCRIPTION(
            RestfulMediaType.APPLICATION_JSON_PROPERTY_DESCRIPTION,
            null,
            PropertyDescriptionRepresentation.class),
    COLLECTION_DESCRIPTION(
            RestfulMediaType.APPLICATION_JSON_COLLECTION_DESCRIPTION,
            null,
            CollectionDescriptionRepresentation.class),
    ACTION_DESCRIPTION(
            RestfulMediaType.APPLICATION_JSON_ACTION_DESCRIPTION,
            null,
            ActionDescriptionRepresentation.class),
    ACTION_PARAMETER_DESCRIPTION(
            RestfulMediaType.APPLICATION_JSON_ACTION_PARAMETER_DESCRIPTION,
            null,
            ActionParameterDescriptionRepresentation.class),
    TYPE_ACTION_RESULT(
            RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_RESULT,
            null,
            TypeActionResultRepresentation.class),
    ERROR(
            RestfulMediaType.APPLICATION_JSON_ERROR,
            RestfulMediaType.APPLICATION_XML_ERROR,
            ErrorRepresentation.class),
    GENERIC(
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            JsonRepresentation.class);

    private final String name;
    private final MediaType jsonMediaType;
    private MediaType xmlMediaType;
    private final Class<? extends JsonRepresentation> representationClass;

    private RepresentationType(
            final String jsonMediaTypeStr,
            final String xmlMediaTypeStr,
            final Class<? extends JsonRepresentation> representationClass) {

        this(	jsonMediaTypeStr != null ? MediaTypes.parse(jsonMediaTypeStr) : null,
                xmlMediaTypeStr != null ? MediaTypes.parse(xmlMediaTypeStr) : null,
                        representationClass
                );
    }

    private RepresentationType(
            final MediaType jsonMediaType,
            final MediaType xmlMediaType,
            final Class<? extends JsonRepresentation> representationClass) {

        this.xmlMediaType = xmlMediaType;
        this.representationClass = representationClass;
        this.name = Enums.enumToCamelCase(this);
        this.jsonMediaType = jsonMediaType;
    }

    public String getName() {
        return name;
    }

    public final MediaType getJsonMediaType() {
        return jsonMediaType;
    }

    public MediaType getXmlMediaType() {
        return xmlMediaType;
    }

    public MediaType getJsonElseXmlMediaType() {
        return jsonMediaType != null ? jsonMediaType : xmlMediaType;
    }

    /**
     * Clones the (immutable) {@link #getJsonMediaType() media type}, adding in one additional
     * parameter value.
     */
    public MediaType getMediaType(final String parameter, final String paramValue) {
        return getJsonMediaType(Collections.singletonMap(parameter, paramValue));
    }

    public MediaType getJsonMediaType(final Map<String, String> mediaTypeParams) {
        Map<String, String> parameters = new HashMap<>(jsonMediaType.getParameters());
        parameters.putAll(mediaTypeParams);
        return new MediaType(jsonMediaType.getType(), jsonMediaType.getSubtype(), parameters);
    }
    public MediaType getXmlMediaType(final Map<String, String> mediaTypeParams) {
        if(xmlMediaType == null) {
            return null;
        }
        Map<String, String> parameters = new HashMap<>(xmlMediaType.getParameters());
        parameters.putAll(mediaTypeParams);
        return new MediaType(xmlMediaType.getType(), xmlMediaType.getSubtype(), parameters);
    }

    public String getMediaTypeProfile() {
        // same for both JSON and XML
        return getJsonMediaType().getParameters().get("profile");
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
        if(mediaType != null) {
            for (final RepresentationType representationType : values()) {
                if(representationType.matches(mediaType)) {
                    return representationType;
                }
            }
        }
        return RepresentationType.GENERIC;
    }

    public boolean matches(final MediaType mediaType) {
        return matchesXmlProfile(mediaType) || matchesJsonProfile(mediaType);
    }

    public boolean matchesXmlProfile(final MediaType mediaType) {
        final MediaType xmlCandidate = this.getXmlMediaType();
        if (xmlCandidate == null) {
            return false;
        }
        return matchesProfile(mediaType, xmlCandidate);
    }

    public boolean matchesJsonProfile(final MediaType mediaType) {
        final MediaType jsonCandidate = this.getJsonMediaType();
        return matchesProfile(mediaType, jsonCandidate);

    }

    private boolean matchesProfile(final MediaType mediaType, final MediaType candidate) {
        if(!candidate.getType().equals(mediaType.getType())) {
            return false;
        }
        if(!candidate.getSubtype().equals(mediaType.getSubtype())) {
            return false;
        }
        String candidateProfile = candidate.getParameters().get("profile");
        String mediaTypeProfile = mediaType.getParameters().get("profile");
        return candidateProfile == null || candidateProfile.equals(mediaTypeProfile);
    }

    /**
     * whether any of the {@link RepresentationType}s matches any (accept header) XML {@link MediaType}
     * with specified parameter.
     */
    public MediaType matchesXmlProfileWithParameter(
            final List<MediaType> mediaTypes, final String parameterKey) {
        for (MediaType mediaType : mediaTypes) {
            if(this.matchesXmlProfile(mediaType)) {
                final String paramValue = mediaType.getParameters().get(parameterKey);
                if(paramValue == null) {
                    return null;
                }
                return mediaType;
            }
        }
        return null;
    }

    /**
     * whether any of the {@link RepresentationType}s matches any (accept header) JSON {@link MediaType}
     * with specified parameter.
     */
    public MediaType matchesJsonProfileWithParameter(
            final List<MediaType> mediaTypes, final String parameterKey) {

        for (MediaType mediaType : mediaTypes) {
            if(this.matchesJsonProfile(mediaType)) {
                final String paramValue = mediaType.getParameters().get(parameterKey);
                if(paramValue == null) {
                    return null;
                }
                return mediaType;
            }
        }
        return null;
    }

    private static final Parser<RepresentationType> PARSER = new Parser<RepresentationType>() {
        @Override
        public RepresentationType valueOf(final String str) {
            return RepresentationType.lookup(str);
        }

        @Override
        public String asString(final RepresentationType t) {
            return t.getName();
        }
    };

    public static Parser<RepresentationType> parser() {
        return PARSER;
    }



}
