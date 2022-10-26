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
package org.apache.causeway.viewer.restfulobjects.viewer.resources.serialization;

import java.util.Collection;

import javax.ws.rs.core.MediaType;

import org.apache.causeway.commons.internal.resources._Json;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;

public enum SerializationStrategy {

    XML {
        @Override public Object entity(final Object jaxbAnnotatedObject) {
            return jaxbAnnotatedObject;
        }

    },

    JSON {
        @Override public Object entity(final Object jaxbAnnotatedObject) {
            return _Json.toString(
                    jaxbAnnotatedObject,
                    _Json::jaxbAnnotationSupport);
        }

    },

    JSON_INDENTED {
        @Override public Object entity(final Object jaxbAnnotatedObject) {
            return _Json.toString(
                    jaxbAnnotatedObject,
                    _Json::jaxbAnnotationSupport,
                    _Json::indentedOutput);
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
