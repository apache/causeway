/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.json.viewer.resources.domainservices;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.DomainObjectRepBuilder;

public class DomainServiceRepBuilder extends DomainObjectRepBuilder {

    public static DomainServiceRepBuilder newBuilder(ResourceContext representationContext) {
        return new DomainServiceRepBuilder(representationContext);
    }

    public static LinkBuilder newLinkToBuilder(ResourceContext resourceContext, String string, ObjectAdapter adapter) {
        String serviceId = ServiceUtil.id(adapter.getObject());
        return LinkBuilder.newBuilder(resourceContext, "service", "services/%s", serviceId);
    }

    public DomainServiceRepBuilder(ResourceContext resourceContext) {
        super(resourceContext);
    }

    @Override
    protected JsonRepresentation linkTo(ObjectAdapter objectAdapter) {
        return newLinkToBuilder(resourceContext, "service", objectAdapter).build();
    }


}