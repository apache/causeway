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
package org.apache.isis.viewer.restful.viewer2.resources.services;

import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.restful.applib2.resources.ServicesResource;
import org.apache.isis.viewer.restful.viewer2.RepContext;
import org.apache.isis.viewer.restful.viewer2.representations.Representation;
import org.apache.isis.viewer.restful.viewer2.resources.ResourceAbstract;
import org.apache.isis.viewer.restful.viewer2.resources.objects.DomainObjectRepBuilder;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * Implementation note: it seems to be necessary to annotate the implementation with {@link Path} rather than the
 * interface (at least under RestEasy 1.0.2 and 1.1-RC2).
 */
@Path("/services")
public class ServicesResourceImpl extends ResourceAbstract implements ServicesResource {

    @Override
    @Produces({ "application/json" })
    public String services() {
        init();

        return asJsonList(serviceSelfRepresentations());
    }

    protected List<Representation> serviceSelfRepresentations() {
        final List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();
        RepContext representationContext = getResourceContext().repContext();
        
        Function<ObjectAdapter, Representation> objectSelfRepresentation = 
            Functions.compose(
                DomainObjectRepBuilder.selfOf(), 
                DomainObjectRepBuilder.fromAdapter(representationContext));
        return Lists.newArrayList(
            Collections2.transform(serviceAdapters, objectSelfRepresentation));
    }

}
