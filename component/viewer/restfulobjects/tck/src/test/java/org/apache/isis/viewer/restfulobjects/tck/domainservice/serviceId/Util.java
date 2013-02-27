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
package org.apache.isis.viewer.restfulobjects.tck.domainservice.serviceId;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ListRepresentation;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

public class Util {
    
    private Util(){}

    public static String givenLinkToService(RestfulClient restfulClient, String serviceId) throws JsonParseException, JsonMappingException, IOException {
        
        final DomainServiceResource resource = restfulClient.getDomainServiceResource();
        final Response response = resource.services();
        final ListRepresentation services = RestfulResponse.<ListRepresentation> ofT(response).getEntity();

        final String href = services.getRepresentation("value[rel=" + Rel.SERVICE.getName() + ";serviceId=\"" +
        		serviceId +
        		"\"]").asLink().getHref();
        return href;
    }

}
