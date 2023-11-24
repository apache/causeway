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
package org.apache.isis.viewer.restfulobjects.applib.menubars;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;

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
