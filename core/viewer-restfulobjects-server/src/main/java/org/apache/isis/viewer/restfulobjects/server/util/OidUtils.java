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
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;

public final class OidUtils {

    private static final OidMarshaller OID_MARSHALLER = OidMarshaller.INSTANCE;

    private OidUtils() {
    }

    /**
     * @return {@code null} if not persistent and not a view model.
     */
    public static ObjectAdapter getObjectAdapterElseNull(
            final RendererContext rendererContext,
            final String domainType, final String instanceIdEncoded) {
        final String instanceIdUnencoded = UrlDecoderUtils.urlDecode(instanceIdEncoded);
        String oidStrUnencoded = OID_MARSHALLER.joinAsOid(domainType, instanceIdUnencoded);
        return getObjectAdapter(rendererContext, oidStrUnencoded);
    }

    /**
     * see {@link #getObjectAdapterElseNull(org.apache.isis.viewer.restfulobjects.rendering.RendererContext, String, String)}
     */
    public static ObjectAdapter getObjectAdapterElseNull(
            final RendererContext rendererContext,
            final String oidStrEncoded) {
        String oidStrUnencoded = UrlDecoderUtils.urlDecode(oidStrEncoded);
        return getObjectAdapter(rendererContext, oidStrUnencoded);
    }

    private static ObjectAdapter getObjectAdapter(
            final RendererContext rendererContext,
            final String oidStrUnencoded) {
        RootOid rootOid = RootOid.deString(oidStrUnencoded);

        final PersistenceSession persistenceSession = rendererContext.getPersistenceSession();

        return persistenceSession.adapterForAny(rootOid);
    }

}
