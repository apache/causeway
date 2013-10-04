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
package org.apache.isis.viewer.restfulobjects.server.util;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;

public final class OidUtils {

    private OidUtils() {
    }

    public static String getDomainType(final RendererContext renderContext, final ObjectAdapter objectAdapter) {
        return org.apache.isis.viewer.restfulobjects.rendering.util.OidUtils.getDomainType(objectAdapter);
    }

    public static String getInstanceId(final RendererContext renderContext, final ObjectAdapter objectAdapter) {
        return org.apache.isis.viewer.restfulobjects.rendering.util.OidUtils.getInstanceId(renderContext, objectAdapter);
    }

    // REVIEW: it's a bit hokey to join these together just to split them out again.
    public static String joinAsOid(final String domainType, final String instanceIdEncoded) {
        final String instanceIdUnencoded = UrlDecoderUtils.urlDecode(instanceIdEncoded);
        return getOidMarshaller().joinAsOid(domainType, instanceIdUnencoded);
    }

    /**
     * 
     * @return {@code null} if not found.
     */
    public static ObjectAdapter getObjectAdapterElseNull(final RendererContext resourceContext, final String domainType, final String instanceId) throws ObjectNotFoundException {
        try {
            return getObjectAdapterElseThrowNotFound(resourceContext, domainType, instanceId);
        } catch(ObjectNotFoundException ex) {
            return null;
        }
    }

    /**
     * 
     * @throws {@link ObjectNotFoundException} if not found
     */
    public static ObjectAdapter getObjectAdapterElseThrowNotFound(final RendererContext resourceContext, final String domainType, final String instanceId) throws ObjectNotFoundException {
        String oidStr = joinAsOid(domainType, instanceId);
        
        // REVIEW: this is all rather disgusting...
        final ObjectSpecId specId = ObjectSpecId.of(domainType);
        final ObjectSpecification spec = IsisContext.getSpecificationLoader().lookupBySpecId(specId);
        if(spec.containsFacet(ViewModelFacet.class)) {
            // TODO: use the static in OidMarshaller
            oidStr = "*" + oidStr;
        }

        return getObjectAdapterForUnencodedElseThrowNotFound(resourceContext, oidStr);
    }

    /**
     * 
     * @return {@code null} if not found.
     */
    public static ObjectAdapter getObjectAdapterElseNull(final RendererContext resourceContext, final String oidEncodedStr) {
        try {
            return getObjectAdapterElseThrowNotFound(resourceContext, oidEncodedStr);
        } catch(ObjectNotFoundException ex) {
            return null;
        }
    }

    /**
     * 
     * @throws {@link ObjectNotFoundException} if not found
     */
    public static ObjectAdapter getObjectAdapterElseThrowNotFound(final RendererContext resourceContext, final String oidEncodedStr) {
        final String oidStr = UrlDecoderUtils.urlDecode(oidEncodedStr);
        return getObjectAdapterForUnencodedElseThrowNotFound(resourceContext, oidStr);
    }

    private static ObjectAdapter getObjectAdapterForUnencodedElseThrowNotFound(final RendererContext resourceContext, final String oidStr) {
        final RootOid rootOid = RootOidDefault.deStringEncoded(oidStr, getOidMarshaller());
        //return resourceContext.getPersistenceSession().loadObject(rootOid);
        return resourceContext.getPersistenceSession().getAdapterManager().adapterFor(rootOid);
    }

    private static OidMarshaller getOidMarshaller() {
		return new OidMarshaller();
	}

    
}
