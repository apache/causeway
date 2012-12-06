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
package org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.viewer.restfulobjects.applib.links.Rel;
import org.apache.isis.viewer.restfulobjects.viewer.representations.LinkBuilder;

public class DomainServiceLinkTo extends DomainObjectLinkTo {
    private String serviceId;

    @Override
    public ObjectAdapterLinkTo with(final ObjectAdapter objectAdapter) {
        serviceId = ServiceUtil.id(objectAdapter.getObject());
        return super.with(objectAdapter);
    }

    @Override
    protected String linkRef() {
        final StringBuilder buf = new StringBuilder("services/");
        buf.append(serviceId);
        return buf.toString();
    }

    @Override
    protected Rel defaultRel() {
        return Rel.SERVICE;
    }

    @Override
    public LinkBuilder builder(final Rel rel) {
        return super.builder(rel).withId(serviceId);
    }

}