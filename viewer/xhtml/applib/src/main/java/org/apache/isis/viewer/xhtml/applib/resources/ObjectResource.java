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
package org.apache.isis.viewer.xhtml.applib.resources;

import java.io.InputStream;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

public interface ObjectResource {

    @GET
    @Path("/{oid}")
    @Produces({ "application/xhtml+xml", "text/html" })
    public String object(@PathParam("oid") final String oidStr);

    @PUT
    @Path("/{oid}/property/{propertyId}")
    @Produces({ "application/xhtml+xml", "text/html" })
    public String modifyProperty(@PathParam("oid") final String oidStr,
        @PathParam("propertyId") final String propertyId, @QueryParam("proposedValue") final String proposedValue);

    @DELETE
    @Path("/{oid}/property/{propertyId}")
    @Produces({ "application/xhtml+xml", "text/html" })
    public String clearProperty(@PathParam("oid") final String oidStr, @PathParam("propertyId") final String propertyId);

    @GET
    @Path("/{oid}/collection/{collectionId}")
    @Produces({ "application/xhtml+xml", "text/html" })
    public String accessCollection(@PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId);

    @PUT
    @Path("/{oid}/collection/{collectionId}")
    @Produces({ "application/xhtml+xml", "text/html" })
    public String addToCollection(@PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        @QueryParam("proposedValue") final String proposedValueOidStr);

    @DELETE
    @Path("/{oid}/collection/{collectionId}")
    @Produces({ "application/xhtml+xml", "text/html" })
    public String removeFromCollection(@PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        @QueryParam("proposedValue") final String proposedValueOidStr);

    @POST
    @Path("/{oid}/action/{actionId}")
    @Produces({ "application/xhtml+xml", "text/html" })
    public String invokeAction(@PathParam("oid") final String oidStr, @PathParam("actionId") final String actionId,
        final InputStream body);

}