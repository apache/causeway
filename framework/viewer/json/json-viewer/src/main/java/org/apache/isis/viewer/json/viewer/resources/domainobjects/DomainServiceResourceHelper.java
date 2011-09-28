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
package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.ReprBuilder;
import org.apache.isis.viewer.json.viewer.resources.AbstractResourceHelper;

public class DomainServiceResourceHelper extends AbstractResourceHelper {

    public DomainServiceResourceHelper(ResourceContext resourceContext) {
        this(resourceContext, null);
    }

    public DomainServiceResourceHelper(ResourceContext resourceContext, String selfRef) {
        super(resourceContext, selfRef);
    }

    public ReprBuilder services() {

        final List<ObjectAdapter> serviceAdapters = getResourceContext().getPersistenceSession().getServices();

        DomainObjectListReprBuilder builder = 
                DomainObjectListReprBuilder.newBuilder(getResourceContext())
                    .usingLinkToBuilder(new DomainServiceLinkToBuilder())
                    .withSelf(getSelfRef())
                    .withAdapters(serviceAdapters);
        
        return builder;
    }


}
