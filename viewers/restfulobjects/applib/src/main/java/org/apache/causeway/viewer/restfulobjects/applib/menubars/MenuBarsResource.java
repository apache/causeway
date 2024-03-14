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
package org.apache.causeway.viewer.restfulobjects.applib.menubars;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.causeway.viewer.restfulobjects.applib.RestfulMediaType;

/**
 * Not part of the Restful Objects spec.
 *
 * @since 1.x {@index}
 */
@Path("/menuBars")
public interface MenuBarsResource {


    /**
     * Not part of the Restful Objects spec.
     */
    @GET
    @Produces({
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_JSON_LAYOUT_MENUBARS,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_LAYOUT_MENUBARS
    })
    public Response menuBars();

    /**
     * Not part of the Restful Objects spec.
     */
    @DELETE
    public Response deleteMenuBarsNotAllowed();

    /**
     * Not part of the Restful Objects spec.
     */
    @PUT
    public Response putMenuBarsNotAllowed();

    /**
     * Not part of the Restful Objects spec.
     */
    @POST
    public Response postMenuBarsNotAllowed();


}
