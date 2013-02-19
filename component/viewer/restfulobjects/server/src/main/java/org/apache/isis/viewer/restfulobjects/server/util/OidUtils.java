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
    
    public static ObjectAdapter getObjectAdapter(final RendererContext resourceContext, final String domainType, final String instanceId) {

        final String instanceIdUnencoded = UrlDecoderUtils.urlDecode(instanceId);
        
        // REVIEW: it's a bit hokey to join these together just to split them out again.
        final String oidStr = getOidMarshaller().joinAsOid(domainType, instanceIdUnencoded);
        
        return getObjectAdapterForUnencoded(resourceContext, oidStr);
    }

    public static ObjectAdapter getObjectAdapter(final RendererContext resourceContext, final String oidEncodedStr) {
        final String oidStr = UrlDecoderUtils.urlDecode(oidEncodedStr);
        return getObjectAdapterForUnencoded(resourceContext, oidStr);
    }

    private static ObjectAdapter getObjectAdapterForUnencoded(final RendererContext resourceContext, final String oidStr) {
        final RootOid rootOid = RootOidDefault.deStringEncoded(oidStr, getOidMarshaller());
        return resourceContext.getAdapterManager().adapterFor(rootOid);
    }

    private static OidMarshaller getOidMarshaller() {
		return new OidMarshaller();
	}

    
}
