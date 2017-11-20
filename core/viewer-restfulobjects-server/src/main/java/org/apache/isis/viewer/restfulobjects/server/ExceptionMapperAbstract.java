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
package org.apache.isis.viewer.restfulobjects.server;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;

public abstract class ExceptionMapperAbstract<T extends Throwable> implements ExceptionMapper<T> {

    @Context
    protected HttpHeaders httpHeaders;

    protected void setContentTypeOn(final ResponseBuilder builder) {
        final boolean xml = isXmlButNotHtml();
        if(!xml) {
            builder.type(RestfulMediaType.APPLICATION_JSON_ERROR);
        } else {
            builder.type(RestfulMediaType.APPLICATION_XML_ERROR);
        }
    }

    protected boolean isHtml() {
        for (final MediaType acceptableMediaType : httpHeaders.getAcceptableMediaTypes()) {
            if(acceptableMediaType.getType().equals("text") && acceptableMediaType.getSubtype().equals("html")) {
                return true;
            }
        }
        return false;
    }

    protected boolean isXmlButNotHtml() {
        if (isHtml()) {
            return false;
        }
        for (final MediaType acceptableMediaType : httpHeaders.getAcceptableMediaTypes()) {
            if(acceptableMediaType.getSubtype().equals("xml")) {
                return true;
            }
        }
        return false;
    }


}
