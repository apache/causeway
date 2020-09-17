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
package org.apache.isis.viewer.restfulobjects.viewer.resources.serialization;

import java.util.Collection;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import org.apache.isis.commons.internal.resources._Json;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;

public enum SerializationStrategy {
    
    XML {
        @Override public Object entity(final Object jaxbAnnotatedObject) {
            return jaxbAnnotatedObject;
        }
       
    },
    
    JSON {
        @Override public Object entity(final Object jaxbAnnotatedObject) {
            final JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
            final ObjectMapper objectMapper = new ObjectMapper()
                    .registerModule(jaxbAnnotationModule)
                    .disable(SerializationFeature.WRITE_NULL_MAP_VALUES) // doesn't seem to work...
                    .disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
            try {
                return _Json.toString(objectMapper, jaxbAnnotatedObject);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

    },
    
    JSON_INDENTED {
        @Override public Object entity(final Object jaxbAnnotatedObject) {
            final JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
            final ObjectMapper objectMapper = new ObjectMapper()
                    .registerModule(jaxbAnnotationModule)
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .disable(SerializationFeature.WRITE_NULL_MAP_VALUES) // doesn't seem to work...
                    .disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
            try {
                return _Json.toString(objectMapper, jaxbAnnotatedObject);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

    },
    
    ;

    public abstract Object entity(final Object jaxbAnnotatedObject);

    public MediaType type(final RepresentationType representationType) {
        return representationType.getXmlMediaType();
    }

    public static SerializationStrategy determineFrom(final Collection<MediaType> acceptableMediaTypes) {

        for (MediaType acceptableMediaType : acceptableMediaTypes) {
            if(acceptableMediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                return SerializationStrategy.JSON;
            }
            if(acceptableMediaType.isCompatible(MediaType.APPLICATION_XML_TYPE)) {
                return SerializationStrategy.XML;
            }
        }
        return SerializationStrategy.JSON;
    }

}
