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
package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkToBuilder;

public class DomainObjectLinkToBuilder implements ObjectAdapterLinkToBuilder {

    private ObjectAdapter objectAdapter;
    private ResourceContext resourceContext;

    @Override
    public ObjectAdapterLinkToBuilder with(ObjectAdapter objectAdapter) {
        this.objectAdapter = objectAdapter;
        return this;
    }

    @Override
    public DomainObjectLinkToBuilder usingResourceContext(ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
        return this;
    }

    @Override
    public JsonRepresentation build() {
        String oidStr = getOidStringifier().enString(objectAdapter.getOid());
        String url = "objects/" + oidStr;
        LinkToBuilder newLinkToBuilder = LinkToBuilder.newBuilder(resourceContext, "object", url);
        return newLinkToBuilder.build();
    }
    
    protected OidStringifier getOidStringifier() {
        return getOidGenerator().getOidStringifier();
    }

    protected OidGenerator getOidGenerator() {
        return getPersistenceSession().getOidGenerator();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }


}