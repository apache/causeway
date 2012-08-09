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
package org.apache.isis.viewer.restfulobjects.viewer.util;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.viewer.restfulobjects.viewer.ResourceContext;

public final class OidUtils {

    private OidUtils() {
    }

    public static ObjectAdapter getObjectAdapter(final ResourceContext resourceContext, final String oidEncodedStr) {
        final String oidStr = UrlDecoderUtils.urlDecode(oidEncodedStr);
        final RootOid rootOid = resourceContext.getOidStringifier().deString(oidStr);
        final ObjectAdapter adapterFor = resourceContext.getObjectAdapterLookup().getAdapterFor(rootOid);
        if(adapterFor != null) {
            return adapterFor;
        }
        return resourceContext.getPersistenceSession().getAdapterManager().recreatePersistentAdapter(rootOid);
    }

    public static String getOidStr(final ResourceContext resourceContext, final ObjectAdapter objectAdapter) {
        final Oid oid = objectAdapter.getOid();
        if(!(oid instanceof RootOid)) {
            throw new IllegalArgumentException("objectAdapter must be a root adapter");
        }
        RootOid rootOid = (RootOid) oid;
        return oid != null ? resourceContext.getOidStringifier().enString(rootOid) : null;
    }

}
